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
package org.kuali.ole.fp.document.validation.impl;

import org.kuali.ole.fp.document.CashReceiptDocument;
import org.kuali.ole.fp.document.service.CashReceiptService;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;

/**
 * Validation that checks all of the various cash totals on a cash receipt document.
 */
public class CashReceiptCashTotalsValidation extends GenericValidation {
    private CashReceiptDocument cashReceiptDocumentForValidation;
    private CashReceiptService cashReceiptService;

    /**
     * Uses the CashReceiptService.areCashTotalsInvalid method to check the cash totals.
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        return !getCashReceiptService().areCashTotalsInvalid(getCashReceiptDocumentForValidation());
    }

    /**
     * Gets the cashReceiptDocumentForValidation attribute. 
     * @return Returns the cashReceiptDocumentForValidation.
     */
    public CashReceiptDocument getCashReceiptDocumentForValidation() {
        return cashReceiptDocumentForValidation;
    }

    /**
     * Sets the cashReceiptDocumentForValidation attribute value.
     * @param cashReceiptDocumentForValidation The cashReceiptDocumentForValidation to set.
     */
    public void setCashReceiptDocumentForValidation(CashReceiptDocument cashReceiptDocumentForValidation) {
        this.cashReceiptDocumentForValidation = cashReceiptDocumentForValidation;
    }

    /**
     * Gets the cashReceiptService attribute. 
     * @return Returns the cashReceiptService.
     */
    public CashReceiptService getCashReceiptService() {
        return cashReceiptService;
    }

    /**
     * Sets the cashReceiptService attribute value.
     * @param cashReceiptService The cashReceiptService to set.
     */
    public void setCashReceiptService(CashReceiptService cashReceiptService) {
        this.cashReceiptService = cashReceiptService;
    }
}
