/*
 * Copyright 2009 The Kuali Foundation
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

import org.kuali.ole.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.ole.module.purap.document.PaymentRequestDocument;
import org.kuali.ole.sys.document.validation.BranchingValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;

public class PaymentRequestCalculationBranchingValidation extends BranchingValidation {

    @Override
    protected String determineBranch(AttributedDocumentEvent event) {
        PaymentRequestDocument preq = (PaymentRequestDocument) event.getDocument();
        if (PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW.equals(preq.getApplicationDocumentStatus())) {
            return "awaitingTaxReview";
        } else {
            return "notAwaitingTaxReview";
        }
    }

}
