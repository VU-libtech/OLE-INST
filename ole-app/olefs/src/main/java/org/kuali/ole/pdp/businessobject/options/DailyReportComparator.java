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
package org.kuali.ole.pdp.businessobject.options;

import java.util.Comparator;

import org.kuali.ole.pdp.businessobject.DailyReport;
import org.kuali.ole.pdp.service.PaymentGroupService;
import org.kuali.ole.sys.context.SpringContext;

public class DailyReportComparator implements Comparator<DailyReport> {
    
    public int compare(DailyReport o1, DailyReport o2) {
        PaymentGroupService paymentGroupService = SpringContext.getBean(PaymentGroupService.class);
        String key1 = paymentGroupService.getSortGroupId(o1.getPaymentGroup()) + o1.getCustomer();
        String key2 = paymentGroupService.getSortGroupId(o2.getPaymentGroup()) + o2.getCustomer();
        
        return key1.compareTo(key2);
    }

}
