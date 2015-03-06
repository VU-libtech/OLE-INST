/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.sys.service.impl;

import org.kuali.ole.sys.batch.service.impl.SchedulerServiceImpl;
import org.kuali.ole.sys.service.BatchModuleService;
import org.kuali.rice.krad.service.impl.ModuleServiceBase;

/**
 *
 * This class is the OLE implementation of a module service. It also implements the batch related methods
 *
 */
public class OleModuleServiceImpl extends ModuleServiceBase implements BatchModuleService {

    /***
     * @see org.kuali.ole.sys.service.BatchModuleService#hasJobStatus(java.lang.String)
     */
    @Override
    public boolean isExternalJob(String jobName){
        return false;
    }

    /***
     * @see org.kuali.ole.sys.service.BatchModuleService#getJobStatus(java.lang.String)
     */
    @Override
    public String getExternalJobStatus(String jobName) {
        if(isExternalJob(jobName)) {
            return SchedulerServiceImpl.SUCCEEDED_JOB_STATUS_CODE;
        }
        return null;
    }

}
