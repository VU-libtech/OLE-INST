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
package org.kuali.ole.gl.batch;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.kuali.ole.coa.service.ChartService;
import org.kuali.ole.gl.service.CollectorDetailService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.batch.AbstractStep;

/**
 * A step to purge old collector details from the database.
 */
public class PurgeCollectorDetailStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurgeCollectorDetailStep.class);
    private ChartService chartService;
    private CollectorDetailService collectorDetailService;

    /**
     * This step will purge data from the gl_id_bill_t table older than a specified year. It purges the data one chart at a time
     * each within their own transaction so database transaction logs don't get completely filled up when doing this. This step
     * class should NOT be transactional.
     * 
     * @param jobName the name of the job this step is being run as part of
     * @param jobRunDate the time/date the job was started
     * @return true if the job completed successfully, false if otherwise
     * @see org.kuali.ole.sys.batch.Step#execute(String, Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {
        String yearStr = getParameterService().getParameterValueAsString(getClass(), OLEConstants.SystemGroupParameterNames.PURGE_GL_ID_BILL_T_BEFORE_YEAR);
        LOG.info("PurgeCollectorDetailStep was run with year = "+yearStr);
        int year = Integer.parseInt(yearStr);
        List charts = chartService.getAllChartCodes();
        for (Iterator iter = charts.iterator(); iter.hasNext();) {
            String chart = (String) iter.next();
            collectorDetailService.purgeYearByChart(chart, year);
        }
        return true;
    }

    /**
     * Sets the chartService attribute, allowing the injection of an implementation of the service.
     * 
     * @param chartService the chartService implementation to set
     * @see org.kuali.ole.coa.service.ChartService
     */
    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

    /**
     * Sets the collectorDetailService attribute, allowing the injection of an implementation of the service.
     * 
     * @param collectorDetailService the collectorDetailService implementation to set
     * @see org.kuali.ole.gl.service.CollectorDetailService
     */
    public void setCollectorDetailService(CollectorDetailService collectorDetailService) {
        this.collectorDetailService = collectorDetailService;
    }
}
