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
package org.kuali.ole.sys.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.ole.sys.batch.service.SchedulerService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;


public class ScheduledJobStatusValuesFinder extends KeyValuesBase {

    public List getKeyValues() {
        List labels = new ArrayList();

        for (String status : SpringContext.getBean(SchedulerService.class).getJobStatuses()) {
            labels.add(new ConcreteKeyValue(status, status));
        }
        return labels;
    }


}
