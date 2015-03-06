/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.ole.pdp.batch;

import java.util.Date;

import org.kuali.ole.pdp.batch.service.ProcessPdpCancelPaidService;
import org.kuali.ole.sys.batch.AbstractStep;

public class ProcessPdPCancelsAndPaidStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcessPdPCancelsAndPaidStep.class);

    private ProcessPdpCancelPaidService processPdpCancelPaidService;

    /**
     * @see org.kuali.ole.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.debug("execute() started");

        processPdpCancelPaidService.processPdpCancelsAndPaids();

        return true;
    }

    public void setProcessPdpCancelPaidService(ProcessPdpCancelPaidService processPdpCancelPaidService) {
        this.processPdpCancelPaidService = processPdpCancelPaidService;
    }
}
