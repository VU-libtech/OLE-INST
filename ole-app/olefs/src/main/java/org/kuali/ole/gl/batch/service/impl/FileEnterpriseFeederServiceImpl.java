/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.gl.batch.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.kuali.ole.gl.GeneralLedgerConstants;
import org.kuali.ole.gl.batch.service.EnterpriseFeederNotificationService;
import org.kuali.ole.gl.batch.service.EnterpriseFeederService;
import org.kuali.ole.gl.batch.service.FileEnterpriseFeederHelperService;
import org.kuali.ole.gl.report.LedgerSummaryReport;
import org.kuali.ole.gl.service.OriginEntryGroupService;
import org.kuali.ole.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.ole.sys.Message;
import org.kuali.ole.sys.batch.InitiateDirectoryBase;
import org.kuali.ole.sys.service.ReportWriterService;
import org.kuali.rice.core.api.datetime.DateTimeService;

/**
 * This class iterates through the files in the enterprise feeder staging directory, which is injected by Spring. Note: this class
 * is NOT annotated as transactional. This allows the helper service, which is defined as transactional, to do a per-file
 * transaction.
 */
public class FileEnterpriseFeederServiceImpl extends InitiateDirectoryBase implements EnterpriseFeederService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FileEnterpriseFeederServiceImpl.class);

    private String directoryName;
    private String glOriginEntryDirectoryName;

    private OriginEntryGroupService originEntryGroupService;
    private DateTimeService dateTimeService;
    private FileEnterpriseFeederHelperService fileEnterpriseFeederHelperService;
    private EnterpriseFeederNotificationService enterpriseFeederNotificationService;
    private String reconciliationTableId;

    private ReportWriterService reportWriterService;
    
    /**
     * Feeds file sets in the directory whose name is returned by the invocation to getDirectoryName()
     * 
     * @see org.kuali.ole.gl.batch.service.EnterpriseFeederService#feed(java.lang.String)
     */
    public void feed(String processName, boolean performNotifications) {
        // ensure that this feeder implementation may not be run concurrently on this JVM

        // to consider: maybe use java NIO classes to perform done file locking?
        synchronized (FileEnterpriseFeederServiceImpl.class) {
            if (StringUtils.isBlank(directoryName)) {
                throw new IllegalArgumentException("directoryName not set for FileEnterpriseFeederServiceImpl.");
            }
            
            //add a step to check for directory paths
            prepareDirectories(getRequiredDirectoryNames());
            
            FileFilter doneFileFilter = new SuffixFileFilter(DONE_FILE_SUFFIX);

            File enterpriseFeedFile = null;
            String enterpriseFeedFileName = GeneralLedgerConstants.BatchFileSystem.ENTERPRISE_FEED + GeneralLedgerConstants.BatchFileSystem.EXTENSION; 
            enterpriseFeedFile = new File(glOriginEntryDirectoryName + File.separator + enterpriseFeedFileName);
            
            PrintStream enterpriseFeedPs = null;
            try {
                enterpriseFeedPs = new PrintStream(enterpriseFeedFile);
            } catch (FileNotFoundException e) {
                LOG.error("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
                throw new RuntimeException("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
            }
            
            LOG.info("New File created for enterprise feeder service run: " + enterpriseFeedFileName);

            File directory = new File(directoryName);
            if (!directory.exists() || !directory.isDirectory()) {
                LOG.error("Directory doesn't exist and or it's not really a directory " + directoryName);
                throw new RuntimeException("Directory doesn't exist and or it's not really a directory " + directoryName);
            }

            File[] doneFiles = directory.listFiles(doneFileFilter);
            reorderDoneFiles(doneFiles);

            LedgerSummaryReport ledgerSummaryReport = new LedgerSummaryReport();

            List<EnterpriseFeederStatusAndErrorMessagesWrapper> statusAndErrorsList = new ArrayList<EnterpriseFeederStatusAndErrorMessagesWrapper>();
            
            for (File doneFile : doneFiles) {
                File dataFile = null;
                File reconFile = null;
                

                EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors = new EnterpriseFeederStatusAndErrorMessagesWrapper();
                statusAndErrors.setErrorMessages(new ArrayList<Message>());

                try {                
                    dataFile = getDataFile(doneFile);
                    reconFile = getReconFile(doneFile);

                    statusAndErrors.setFileNames(dataFile, reconFile, doneFile);

                    if (dataFile == null) {
                        LOG.error("Unable to find data file for done file: " + doneFile.getAbsolutePath());
                        statusAndErrors.getErrorMessages().add(new Message("Unable to find data file for done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
                        statusAndErrors.setStatus(new RequiredFilesMissingStatus());
                    }
                    if (reconFile == null) {
                        LOG.error("Unable to find recon file for done file: " + doneFile.getAbsolutePath());
                        statusAndErrors.getErrorMessages().add(new Message("Unable to find recon file for done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
                        statusAndErrors.setStatus(new RequiredFilesMissingStatus());
                    }

                    if (dataFile != null && reconFile != null) {
                        LOG.info("Data file: " + dataFile.getAbsolutePath());
                        LOG.info("Reconciliation File: " + reconFile.getAbsolutePath());

                        fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFile, reconFile, enterpriseFeedPs, processName, reconciliationTableId, statusAndErrors, ledgerSummaryReport);
                    }
                }
                catch (RuntimeException e) {
                    // we need to be extremely resistant to a file load failing so that it doesn't prevent other files from loading
                    LOG.error("Caught exception when feeding done file: " + doneFile.getAbsolutePath());
                }
                finally {
                    statusAndErrorsList.add(statusAndErrors);
                    boolean doneFileDeleted = doneFile.delete();
                    if (!doneFileDeleted) {
                        statusAndErrors.getErrorMessages().add(new Message("Unable to delete done file: " + doneFile.getAbsolutePath(), Message.TYPE_FATAL));
                    }
                    if (performNotifications) {
                        enterpriseFeederNotificationService.notifyFileFeedStatus(processName, statusAndErrors.getStatus(), doneFile, dataFile, reconFile, statusAndErrors.getErrorMessages());
                    }
                }
            }
            
            enterpriseFeedPs.close();
            generateReport(statusAndErrorsList, ledgerSummaryReport, glOriginEntryDirectoryName + File.separator + enterpriseFeedFileName);
            
            String enterpriseFeedDoneFileName = enterpriseFeedFileName.replace(GeneralLedgerConstants.BatchFileSystem.EXTENSION, GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
            File enterpriseFeedDoneFile = new File (glOriginEntryDirectoryName + File.separator + enterpriseFeedDoneFileName);
            if (!enterpriseFeedDoneFile.exists()){
                try {
                    enterpriseFeedDoneFile.createNewFile();
                } catch (IOException e) {
                    LOG.error("Unable to create done file for enterprise feed output group.", e);
                    throw new RuntimeException("Unable to create done file for enterprise feed output group.", e);
                }
            }
            
        }
    }

    /**
     * Reorders the files in case there's a dependency on the order in which files are fed upon. For this implementation, the
     * purpose is to always order files in a way such that unit testing will be predictable.
     * 
     * @param doneFiles
     */
    protected void reorderDoneFiles(File[] doneFiles) {
        // sort the list so that the unit tests will have more predictable results
        Arrays.sort(doneFiles);
    }

    /**
     * Given the doneFile, this method finds the data file corresponding to the done file
     * 
     * @param doneFile
     * @return a File for the data file, or null if the file doesn't exist or is not readable
     */
    protected File getDataFile(File doneFile) {
        String doneFileAbsPath = doneFile.getAbsolutePath();
        if (!doneFileAbsPath.endsWith(DONE_FILE_SUFFIX)) {
            LOG.error("Done file name must end with " + DONE_FILE_SUFFIX);
            throw new IllegalArgumentException("Done file name must end with " + DONE_FILE_SUFFIX);
        }
        String dataFileAbsPath = StringUtils.removeEnd(doneFileAbsPath, DONE_FILE_SUFFIX) + DATA_FILE_SUFFIX;
        File dataFile = new File(dataFileAbsPath);
        if (!dataFile.exists() || !dataFile.canRead()) {
            LOG.error("Cannot find/read data file " + dataFileAbsPath);
            return null;
        }
        return dataFile;
    }

    /**
     * Given the doneFile, this method finds the reconciliation file corresponding to the data file
     * 
     * @param doneFile
     * @return a file for the reconciliation data, or null if the file doesn't exist or is not readable
     */
    protected File getReconFile(File doneFile) {
        String doneFileAbsPath = doneFile.getAbsolutePath();
        if (!doneFileAbsPath.endsWith(DONE_FILE_SUFFIX)) {
            LOG.error("Done file name must end with " + DONE_FILE_SUFFIX);
            throw new IllegalArgumentException("DOne file name must end with " + DONE_FILE_SUFFIX);
        }
        String reconFileAbsPath = StringUtils.removeEnd(doneFileAbsPath, DONE_FILE_SUFFIX) + RECON_FILE_SUFFIX;
        File reconFile = new File(reconFileAbsPath);
        if (!reconFile.exists() || !reconFile.canRead()) {
            LOG.error("Cannot find/read data file " + reconFileAbsPath);
            return null;
        }
        return reconFile;
    }

    /**
     * Gets the directoryName attribute.
     * 
     * @return Returns the directoryName.
     */
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * Sets the directoryName attribute value.
     * 
     * @param directoryName The directoryName to set.
     */
    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    /**
     * Gets the originEntryGroupService attribute.
     * 
     * @return Returns the originEntryGroupService.
     */
    public OriginEntryGroupService getOriginEntryGroupService() {
        return originEntryGroupService;
    }

    /**
     * Sets the originEntryGroupService attribute value.
     * 
     * @param originEntryGroupService The originEntryGroupService to set.
     */
    public void setOriginEntryGroupService(OriginEntryGroupService originEntryGroupService) {
        this.originEntryGroupService = originEntryGroupService;
    }

    /**
     * Gets the dateTimeService attribute.
     * 
     * @return Returns the dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Gets the fileEnterpriseFeederHelperService attribute.
     * 
     * @return Returns the fileEnterpriseFeederHelperService.
     */
    public FileEnterpriseFeederHelperService getFileEnterpriseFeederHelperService() {
        return fileEnterpriseFeederHelperService;
    }

    /**
     * Sets the fileEnterpriseFeederHelperService attribute value.
     * 
     * @param fileEnterpriseFeederHelperService The fileEnterpriseFeederHelperService to set.
     */
    public void setFileEnterpriseFeederHelperService(FileEnterpriseFeederHelperService fileEnterpriseFeederHelperServiceImpl) {
        this.fileEnterpriseFeederHelperService = fileEnterpriseFeederHelperServiceImpl;
    }

    /**
     * Gets the enterpriseFeederNotificationService attribute.
     * 
     * @return Returns the enterpriseFeederNotificationService.
     */
    public EnterpriseFeederNotificationService getEnterpriseFeederNotificationService() {
        return enterpriseFeederNotificationService;
    }

    /**
     * Sets the enterpriseFeederNotificationService attribute value.
     * 
     * @param enterpriseFeederNotificationService The enterpriseFeederNotificationService to set.
     */
    public void setEnterpriseFeederNotificationService(EnterpriseFeederNotificationService enterpriseFeederNotificationService) {
        this.enterpriseFeederNotificationService = enterpriseFeederNotificationService;
    }

    /**
     * Gets the reconciliationTableId attribute.
     * 
     * @return Returns the reconciliationTableId.
     */
    public String getReconciliationTableId() {
        return reconciliationTableId;
    }

    /**
     * Sets the reconciliationTableId attribute value.
     * 
     * @param reconciliationTableId The reconciliationTableId to set.
     */
    public void setReconciliationTableId(String reconciliationTableId) {
        this.reconciliationTableId = reconciliationTableId;
    }

    public void setGlOriginEntryDirectoryName(String glOriginEntryDirectoryName) {
        this.glOriginEntryDirectoryName = glOriginEntryDirectoryName;
    }
    
    protected void generateReport(List<EnterpriseFeederStatusAndErrorMessagesWrapper> statusAndErrorsList, LedgerSummaryReport report, String outputFileName) {
        reportWriterService.writeFormattedMessageLine("Output File Name:        %s", outputFileName);
        reportWriterService.writeNewLines(1);
        generateFilesLoadedStatusReport(statusAndErrorsList);
        reportWriterService.pageBreak();
        report.writeReport(reportWriterService);
    }
    
    protected void generateFilesLoadedStatusReport(List<EnterpriseFeederStatusAndErrorMessagesWrapper> statusAndErrorsList) {
        boolean successfulFileLoaded = false;
        reportWriterService.writeSubTitle("Files Successfully Loaded");
        for (EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors : statusAndErrorsList) {
            if (!statusAndErrors.getStatus().isErrorEvent()) {
                reportWriterService.writeFormattedMessageLine("Data file:               %s", statusAndErrors.getDataFileName());
                reportWriterService.writeFormattedMessageLine("Reconciliation file:     %s", statusAndErrors.getReconFileName());
                reportWriterService.writeFormattedMessageLine("Status:                  %s", statusAndErrors.getStatus().getStatusDescription());
                reportWriterService.writeNewLines(1);
                
                successfulFileLoaded = true;
            }
        }
        if (!successfulFileLoaded) {
            reportWriterService.writeFormattedMessageLine("No files were successfully loaded");
        }
        
        reportWriterService.writeNewLines(2);
        
        boolean unsuccessfulFileLoaded = false;
        reportWriterService.writeSubTitle("Files NOT Successfully Loaded");
        for (EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors : statusAndErrorsList) {
            if (statusAndErrors.getStatus().isErrorEvent()) {
                reportWriterService.writeFormattedMessageLine("Data file:               %s", statusAndErrors.getDataFileName() == null ? "" : statusAndErrors.getDataFileName());
                reportWriterService.writeFormattedMessageLine("Reconciliation file:     %s", statusAndErrors.getReconFileName() == null ? "" : statusAndErrors.getReconFileName());
                reportWriterService.writeFormattedMessageLine("Status:                  %s", statusAndErrors.getStatus().getStatusDescription());
                reportWriterService.writeNewLines(1);
                
                unsuccessfulFileLoaded = true;
            }
        }
        if (!unsuccessfulFileLoaded) {
            reportWriterService.writeFormattedMessageLine("All files were successfully loaded");
        }
        
    }

    /**
     * Sets the reportWriterService attribute value.
     * @param reportWriterService The reportWriterService to set.
     */
    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    /**
     * @see org.kuali.ole.sys.batch.service.impl.InitiateDirectoryImpl#getRequiredDirectoryNames()
     */
    @Override
    public List<String> getRequiredDirectoryNames() {
        return new ArrayList<String>() {{add(getDirectoryName()); }};
    }
}
