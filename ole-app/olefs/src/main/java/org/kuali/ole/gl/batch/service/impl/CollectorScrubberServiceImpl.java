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

import java.sql.Date;
import java.util.Collection;

import org.kuali.ole.gl.batch.CollectorBatch;
import org.kuali.ole.gl.batch.CollectorScrubberProcess;
import org.kuali.ole.gl.batch.service.CollectorScrubberService;
import org.kuali.ole.gl.report.CollectorReportData;
import org.kuali.ole.gl.service.ScrubberService;
import org.kuali.ole.gl.service.impl.CollectorScrubberStatus;
import org.kuali.ole.sys.dataaccess.UniversityDateDao;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.service.PersistenceService;
import org.springframework.transaction.annotation.Transactional;

/**
 * An implementation of CollectorScrubberService
 */
@Transactional
public class CollectorScrubberServiceImpl implements CollectorScrubberService {
    private DateTimeService dateTimeService;
    private UniversityDateDao universityDateDao;
    private ConfigurationService kualiConfigurationService;
    private PersistenceService persistenceService;
    private ScrubberService scrubberService;
    private String batchFileDirectoryName;

    /**
     * uns the scrubber on the origin entries in the batch. Any OEs edits/removals result of the scrub and demerger are removed
     * from the batch, and the same changes are reflected in the details in the same batch.
     * 
     * @param batch the data read in by the Collector
     * @param collectorReportData statistics generated by the scrub run on the Collector data
     * @return an object with the collector scrubber status.
     * @see org.kuali.ole.gl.batch.service.CollectorScrubberService#scrub(org.kuali.ole.gl.batch.CollectorBatch,
     *      org.kuali.ole.gl.report.CollectorReportData)
     */
    public CollectorScrubberStatus scrub(CollectorBatch batch, CollectorReportData collectorReportData, String collectorFileDirectoryName) {
        CollectorScrubberProcess collectorScrubberProcess = new CollectorScrubberProcess(batch, kualiConfigurationService, persistenceService, scrubberService, collectorReportData, dateTimeService, batchFileDirectoryName);
        return collectorScrubberProcess.scrub();
        
    }

    /**
     * Removes any temporarily created origin entries and origin entry groups so that they won't be persisted after the transaction
     * is committed.
     * 
     * @param allStatusObjectsFromCollectorExecution a Collection of ScrubberStatus records to help find bad Collector data

     * @see org.kuali.ole.gl.batch.service.CollectorScrubberService#removeTempGroups(java.util.Collection)
     */
    
    //TODO: need to delete files
    public void removeTempGroups(Collection<CollectorScrubberStatus> allStatusObjectsFromCollectorExecution) {
    }

    /**
     * Finds the run date of the current Collector scrubber process
     * @return the date of the process
     */
    protected Date calculateRunDate() {
        return dateTimeService.getCurrentSqlDate();
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
     * Gets the kualiConfigurationService attribute.
     * 
     * @return Returns the kualiConfigurationService.
     */
    public ConfigurationService getConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * 
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Sets the universityDateDao attribute value.
     * 
     * @param universityDateDao The universityDateDao to set.
     */
    public void setUniversityDateDao(UniversityDateDao universityDateDao) {
        this.universityDateDao = universityDateDao;
    }

    /**
     * Gets the persistenceService attribute.
     * 
     * @return Returns the persistenceService.
     */
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    /**
     * Sets the persistenceService attribute value.
     * 
     * @param persistenceService The persistenceService to set.
     */
    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * Gets the universityDateDao attribute.
     * 
     * @return Returns the universityDateDao.
     */
    public UniversityDateDao getUniversityDateDao() {
        return universityDateDao;
    }

    /**
     * Sets the scrubberService attribute value.
     * 
     * @param scrubberService The scrubberService to set.
     */
    public void setScrubberService(ScrubberService scrubberService) {
        this.scrubberService = scrubberService;
    }

    /**
     * Sets the batchFileDirectoryName attribute value.
     * @param batchFileDirectoryName The batchFileDirectoryName to set.
     */
    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }


}
