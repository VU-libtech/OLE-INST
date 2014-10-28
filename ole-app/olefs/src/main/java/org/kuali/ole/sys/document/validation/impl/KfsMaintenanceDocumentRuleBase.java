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
package org.kuali.ole.sys.document.validation.impl;

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.FinancialSystemUserService;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

public class KfsMaintenanceDocumentRuleBase extends MaintenanceDocumentRuleBase {

    protected static PersonService personService;
    protected static FinancialSystemUserService financialSystemUserService;
    
    protected PersonService getKfsUserService() {
        if ( personService == null ) {
            personService = SpringContext.getBean(PersonService.class);
        }
        return personService;
    }

    public FinancialSystemUserService getFinancialSystemUserService() {
        if ( financialSystemUserService == null ) {
            financialSystemUserService = SpringContext.getBean(FinancialSystemUserService.class);
        }
        return financialSystemUserService;
    }
    
    
    
}

