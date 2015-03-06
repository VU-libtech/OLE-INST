/*
 * Copyright 2012 The Kuali Foundation.
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
package org.kuali.ole.sec.service.impl;

import org.kuali.ole.sec.document.validation.impl.AccessSecurityAccountingDocumentRuleBase;
import org.kuali.ole.sec.service.AccessSecurityService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.rules.rule.BusinessRule;
import org.kuali.rice.krad.service.impl.DocumentDictionaryServiceImpl;

public class SecDocumentDictionaryServiceImpl extends DocumentDictionaryServiceImpl {
    private AccessSecurityService accessSecurityService;

    protected AccessSecurityService getAccessSecurityService() {
        if ( accessSecurityService == null ) {
            accessSecurityService = SpringContext.getBean(AccessSecurityService.class);
        }
        return accessSecurityService;
    }

    /**
     * @see org.kuali.rice.krad.service.DocumentDictionaryService#getBusinessRulesClass
     */
    @Override
    public Class<? extends BusinessRule> getBusinessRulesClass(Document document) {
        String documentType = document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();

        if (getAccessSecurityService().isAccessSecurityControlledDocumentType(documentType)) {
            return AccessSecurityAccountingDocumentRuleBase.class;
        }

        return super.getBusinessRulesClass(document);
    }
}
