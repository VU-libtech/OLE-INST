package org.kuali.ole.batch.ingest;

import org.apache.commons.lang3.StringUtils;
import org.kuali.ole.DataCarrierService;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.batch.bo.OLEBatchBibImportDataObjects;
import org.kuali.ole.batch.bo.OLEBatchBibImportStatistics;
import org.kuali.ole.batch.bo.OLEBatchProcessJobDetailsBo;
import org.kuali.ole.batch.bo.OLEBatchProcessProfileBo;
import org.kuali.ole.batch.document.OLEBatchProcessDefinitionDocument;
import org.kuali.ole.batch.helper.BatchBibImportHelper;
import org.kuali.ole.batch.helper.OLEBatchProcessDataHelper;
import org.kuali.ole.batch.impl.AbstractBatchProcess;
import org.kuali.ole.batch.service.BatchProcessBibImportService;
import org.kuali.ole.docstore.common.document.content.bib.marc.BibMarcRecord;
import org.kuali.ole.docstore.common.document.content.bib.marc.BibMarcRecords;
import org.kuali.ole.docstore.common.document.content.bib.marc.OrderBibMarcRecord;
import org.kuali.ole.docstore.common.document.content.bib.marc.xstream.BibMarcRecordProcessor;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: adityas
 * Date: 7/30/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchProcessBibImport extends AbstractBatchProcess {

    public BatchProcessBibImport() {
    }

    public BatchProcessBibImport(OLEBatchProcessDefinitionDocument processDef, OLEBatchProcessJobDetailsBo job) {
        this.processDef = processDef;
        this.job = job;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BatchProcessBibImport.class);
    OLEBatchBibImportStatistics bibImportStatistics = new OLEBatchBibImportStatistics();

    private OLEBatchProcessProfileBo oleBatchProcessProfileBo;

    private String user;

    private BatchProcessBibImportService batchProcessBibImportService;
    //private boolean isMissMatchedRec = false;
    private OLEBatchProcessDataHelper oleBatchProcessDataHelper;
    DataCarrierService dataCarrierService = GlobalResourceLoader.getService(OLEConstants.DATA_CARRIER_SERVICE);


    @Override
    protected void getNextBatch() throws Exception {
        if (processDef.getChunkSize() < bibImportStatistics.getBibMarcRecordList().size()) {
            if (bibImportStatistics.getChunkCount() + processDef.getChunkSize() < bibImportStatistics.getBibMarcRecordList().size()) {
                bibImportStatistics.setBibImportChunkRecordsList(bibImportStatistics.getBibMarcRecordList().subList(bibImportStatistics.getChunkCount(), bibImportStatistics.getChunkCount() + processDef.getChunkSize()));
                processBatch(bibImportStatistics.getBibImportChunkRecordsList());
                bibImportStatistics.addChunkCount(processDef.getChunkSize());
            } else {
                bibImportStatistics.setBibImportChunkRecordsList(bibImportStatistics.getBibMarcRecordList().subList(bibImportStatistics.getChunkCount(), bibImportStatistics.getBibMarcRecordList().size()));
                processBatch(bibImportStatistics.getBibImportChunkRecordsList());
                bibImportStatistics.setChunkCount(bibImportStatistics.getBibMarcRecordList().size());
                deleteBatchFile();
                job.setStatus(OLEConstants.OLEBatchProcess.JOB_STATUS_COMPLETED);
                //job.setStatusDesc(OLEConstants.OLEBatchProcess.BIB_IMPORT_SUCCESS);
            }
            job.setJobstatistics(bibImportStatistics);
        }
    }

    @Override
    protected void processBatch() throws Exception {
        try {
            user = processDef.getUser();
            if (processDef.getChunkSize() > bibImportStatistics.getBibMarcRecordList().size()) {
                processBatch(bibImportStatistics.getBibMarcRecordList());
                job.setJobstatistics(bibImportStatistics);
                job.setStatus(OLEConstants.OLEBatchProcess.JOB_STATUS_COMPLETED);
                //create error text file
                if (StringUtils.isNotEmpty(bibImportStatistics.getErrorBuilder().toString()))
                    createBatchErrorAttachmentFile(bibImportStatistics.getErrorBuilder().toString());
            }
        } catch (Exception e) {
            job.setStatusDesc(OLEConstants.OLEBatchProcess.BIB_IMPORT_FAILURE);
            LOG.error(String.valueOf(e));
            //create error text file
            if (StringUtils.isNotEmpty(bibImportStatistics.getErrorBuilder().toString()))
                createBatchErrorAttachmentFile(bibImportStatistics.getErrorBuilder().toString());
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void prepareForRead() throws Exception {
        String marcFileContent = getBatchProcessFileContent();
        bibImportStatistics.setBibMarcRecordList(getBibImportRecords(marcFileContent));
        job.setIntailJob(bibImportStatistics);
        oleBatchProcessProfileBo = getBatchProcessProfile(processDef.getBatchProcessProfileId());
    }

    @Override
    protected void prepareForWrite() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the bibliographic records by using upload mrc file data
     *
     * @param marcFileContent
     * @return
     * @throws Exception
     */
    private List<BibMarcRecord> getBibImportRecords(String marcFileContent) throws Exception {
        String marcXMLContent = null;
        try {
            marcXMLContent = getBatchProcessBibImportService().preProcessMarc(marcFileContent);
        } catch (Exception ex) {
            ex.getMessage();
            List<String> reasonForFailure = new ArrayList<>();
            reasonForFailure.add("Unable to parse the marc file. Allowed format is UTF-8");
            reasonForFailure.add("======================================================");
            reasonForFailure.add(ex.getMessage());
            dataCarrierService.addData("reasonForBibImportFailure", reasonForFailure);
        }
        BibMarcRecords bibRecords = new BibMarcRecordProcessor().fromXML(marcXMLContent);
        return bibRecords.getRecords();
    }

    /**
     * Get the  batch process profile Bo object by using batch profile id
     *
     * @param batchProcessProfileId
     * @return
     */
    private OLEBatchProcessProfileBo getBatchProcessProfile(String batchProcessProfileId) {
        Map<String, String> profileIdMap = new HashMap<String, String>();
        profileIdMap.put(OLEConstants.OLEBatchProcess.BATCH_PROCESS_PROFILE_ID, batchProcessProfileId);
        return KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEBatchProcessProfileBo.class, profileIdMap);
    }

    /**
     * Performs the  ingest , checkIn operations
     *
     * @param bibMarcRecordList
     * @throws Exception
     */
    public void processBatch(List<BibMarcRecord> bibMarcRecordList) throws Exception {
        bibImportStatistics.setTotalCount(bibMarcRecordList.size());
        BatchBibImportHelper batchBibImportHelper = new BatchBibImportHelper();
        OLEBatchBibImportDataObjects oleBatchBibImportDataObjects = batchBibImportHelper.processBatch(bibMarcRecordList, oleBatchProcessProfileBo, bibImportStatistics,user);
        saveAndBuildResult(bibMarcRecordList, oleBatchBibImportDataObjects);
    }

    /**
     * Save Batch and  gives mismatch record count
     *
     * @param bibMarcRecordList
     * @throws Exception
     */
    private void saveAndBuildResult(List<BibMarcRecord> bibMarcRecordList, OLEBatchBibImportDataObjects oleBatchBibImportDataObjects) throws Exception {

        List<BibMarcRecord> mismatchRecordList = getBatchProcessBibImportService().saveBatch(bibMarcRecordList, oleBatchBibImportDataObjects, bibImportStatistics);

        bibImportStatistics.setInstanceStatistics(mismatchRecordList);

        // create Mismatched Files
        createMismatchedFiles();

        bibImportStatistics.addSuccessRecord(bibImportStatistics.getTotalCount() - bibImportStatistics.getMismatchRecordList().size());
        //successRecord += totalCount - mismatchRecordList.size();
    }

    private void createMismatchedFiles() throws Exception {

        if (bibImportStatistics.getInvalidLeaderField() != null && bibImportStatistics.getInvalidLeaderField().size() > 0) {
            List reasonForFailure = (List) dataCarrierService.getData("reasonForFailure");
            if (reasonForFailure != null) {
                reasonForFailure.addAll(bibImportStatistics.getInvalidLeaderField());
                dataCarrierService.addData("reasonForFailure", reasonForFailure);
            }
        }


        if (bibImportStatistics.getMismatchRecordList().size() > 0) {
            bibImportStatistics.setMisMatchMarcRecords(new StringBuffer(new BibMarcRecordProcessor().generateXML(bibImportStatistics.getMismatchRecordList())));
            createBatchFailureFile(bibImportStatistics.getMisMatchMarcRecords().toString());
        }

        if (bibImportStatistics.getRecordsCreatedWithOutLink().size() > 0) {
            bibImportStatistics.setMisMatchMarcRecords(new StringBuffer(new BibMarcRecordProcessor().generateXML(bibImportStatistics.getRecordsCreatedWithOutLink())));
            createBatchMismatchFile(bibImportStatistics.getMisMatchMarcRecords().toString(), OLEConstants.OLEBatchProcess.RECORDS_CREATED_WITHOUT_LINK);
        }


        if (bibImportStatistics.getRecordsCreatedWithMoreThanOneLink().size() > 0) {
            bibImportStatistics.setMisMatchMarcRecords(new StringBuffer(new BibMarcRecordProcessor().generateXML(bibImportStatistics.getRecordsCreatedWithMoreThanOneLink())));
            createBatchMismatchFile(bibImportStatistics.getMisMatchMarcRecords().toString(), OLEConstants.OLEBatchProcess.RECORDS_CREATED_WITH_MORE_THAN_ONE_LINK);
        }

        if (bibImportStatistics.getMoreThanOneHoldingsMatched().size() > 0) {
            bibImportStatistics.setMisMatchMarcRecords(new StringBuffer(new BibMarcRecordProcessor().generateXML(bibImportStatistics.getMoreThanOneHoldingsMatched())));
            createBatchMismatchFile(bibImportStatistics.getMisMatchMarcRecords().toString(), OLEConstants.OLEBatchProcess.HOLDINGS_MATCHED_MORE_THAN_ONE);
        }

        if (bibImportStatistics.getMoreThanOneItemMatched().size() > 0) {
            bibImportStatistics.setMisMatchMarcRecords(new StringBuffer(new BibMarcRecordProcessor().generateXML(bibImportStatistics.getMoreThanOneItemMatched())));
            createBatchMismatchFile(bibImportStatistics.getMisMatchMarcRecords().toString(), OLEConstants.OLEBatchProcess.ITEMS_MATCHED_MORE_THAN_ONE);
        }
    }


    private BatchProcessBibImportService getBatchProcessBibImportService() {
        if (batchProcessBibImportService == null)
            batchProcessBibImportService = GlobalResourceLoader.getService("batchProcessBibImportServiceImpl");
        return batchProcessBibImportService;
    }

    public OLEBatchProcessDataHelper getOleBatchProcessDataHelper() {
        if (oleBatchProcessDataHelper == null) oleBatchProcessDataHelper = OLEBatchProcessDataHelper.getInstance();
        return oleBatchProcessDataHelper;
    }

    @Override
    protected void createBatchFailureFile(String misMatchMarcRecords) throws Exception {
        getOleBatchProcessDataHelper().createBatchBibImportFailureFile(misMatchMarcRecords, processDef.getBatchProcessType(), job.getJobId() + "_FailureRecord" + "_" + job.getUploadFileName(), job.getJobId());
    }

    protected void createBatchMismatchFile(String misMatchMarcRecords, String recordName) throws Exception {
        getOleBatchProcessDataHelper().createBatchBibImportFailureFile(misMatchMarcRecords, processDef.getBatchProcessType(), job.getJobId() + recordName + job.getUploadFileName(), job.getJobId());
    }

    public OLEBatchProcessProfileBo getOleBatchProcessProfileBo() {
        return oleBatchProcessProfileBo;
    }

    public void setOleBatchProcessProfileBo(OLEBatchProcessProfileBo oleBatchProcessProfileBo) {
        this.oleBatchProcessProfileBo = oleBatchProcessProfileBo;
    }


    public List<OrderBibMarcRecord> processBatchOrder(List<OrderBibMarcRecord> orderBibMarcRecords) throws Exception {
        bibImportStatistics.setTotalCount(orderBibMarcRecords.size());
        BatchBibImportHelper batchBibImportHelper = new BatchBibImportHelper();
        OLEBatchBibImportDataObjects oleBatchBibImportDataObjects = batchBibImportHelper.processOrderBatch(orderBibMarcRecords, oleBatchProcessProfileBo, bibImportStatistics,user);
        List<OrderBibMarcRecord> orderBibMarcRecordList = getBatchProcessBibImportService().saveOderBatch(orderBibMarcRecords, oleBatchBibImportDataObjects, bibImportStatistics);
        createMismatchedFiles();
        bibImportStatistics.addSuccessRecord(bibImportStatistics.getTotalCount() - bibImportStatistics.getMismatchRecordList().size());
        return orderBibMarcRecordList;
    }

}