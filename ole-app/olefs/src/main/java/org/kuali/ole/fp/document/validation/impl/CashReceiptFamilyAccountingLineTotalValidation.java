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

import static org.kuali.ole.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import org.kuali.ole.sys.OLEKeyConstants.CashReceipt;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Validation for the Cash Receipt family of documents which validates the sum amount of all
 * accounting lines on the document.
 */
public class CashReceiptFamilyAccountingLineTotalValidation extends GenericValidation {
    private AccountingDocument accountingDocumentForValidation;

    /**
     * Return true if source total is non-zero
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean isValid = true;

        if (getAccountingDocumentForValidation().getSourceTotal().isZero()) {
            String errorProperty = DOCUMENT_ERROR_PREFIX + OLEPropertyConstants.SOURCE_ACCOUNTING_LINES;

            isValid = false;
            GlobalVariables.getMessageMap().putError(errorProperty, CashReceipt.ERROR_ZERO_TOTAL, "Accounting Line Total");
        }

        return isValid;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}
