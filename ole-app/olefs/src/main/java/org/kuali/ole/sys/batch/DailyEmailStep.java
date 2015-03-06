/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.sys.batch;

import java.util.Date;

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kew.mail.service.ActionListEmailService;

/**
 * Batch step implementation for the Daily Email
 */
public class DailyEmailStep extends AbstractStep {

    /**
     * @see org.kuali.ole.sys.batch.Step#execute(String, Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        SpringContext.getBean(ActionListEmailService.class).sendDailyReminder();
        return true;
    }

}