/*
 * Copyright 2006-2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.ole.module.purap.service.ElectronicInvoiceHelperService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kns.rules.DocumentRuleBase;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

import java.math.BigDecimal;

/**
 * Business rule(s) applicable to Payment Request documents.
 */
public class ElectronicInvoiceRejectDocumentRule extends DocumentRuleBase {

    protected static KualiDecimal zero = KualiDecimal.ZERO;
    protected static BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /**
     * @see org.kuali.rice.krad.rules.DocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.krad.document.Document)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        return processBusinessRules(document);
    }

    protected boolean processBusinessRules(Document document) {
        boolean isValid = true;

        ElectronicInvoiceRejectDocument eirDocument = (ElectronicInvoiceRejectDocument) document;

        // check to see if the document is being researched
        if (eirDocument.isInvoiceResearchIndicator()) {
            GlobalVariables.getMessageMap().putError(OLEConstants.DOCUMENT_ERRORS, PurapConstants.REJECT_DOCUMENT_RESEARCH_INCOMPETE);
            isValid = false;
        }

        if (!eirDocument.isDocumentCreationInProgress()) {
            isValid = isValid && SpringContext.getBean(ElectronicInvoiceHelperService.class).doMatchingProcess(eirDocument);
            if (isValid) {
                SpringContext.getBean(ElectronicInvoiceHelperService.class).createPaymentRequest(eirDocument);
            }
        }

        return isValid;
    }

}
