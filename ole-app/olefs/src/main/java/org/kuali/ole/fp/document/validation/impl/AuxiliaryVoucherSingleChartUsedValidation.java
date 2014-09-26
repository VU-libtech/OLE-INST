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

import static org.kuali.ole.sys.OLEConstants.ACCOUNTING_LINE_ERRORS;
import static org.kuali.ole.sys.OLEKeyConstants.AuxiliaryVoucher.ERROR_DIFFERENT_CHARTS;

import java.util.List;

import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Validates that all accounting lines on the document use only one chart among them all.
 */
public class AuxiliaryVoucherSingleChartUsedValidation extends GenericValidation {
    private AccountingDocument accountingDocumentForValidation;

    /**
     * Iterates <code>{@link AccountingLine}</code> instances in a given <code>{@link FinancialDocument}</code> instance and
     * compares them to see if they are all in the same Chart.
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;

        String baseChartCode = null;
        int index = 0;

        List<AccountingLine> lines = accountingDocumentForValidation.getSourceAccountingLines();
        for (AccountingLine line : lines) {
            if (index == 0) {
                baseChartCode = line.getChartOfAccountsCode();
            }
            else {
                String currentChartCode = line.getChartOfAccountsCode();
                if (!currentChartCode.equals(baseChartCode)) {
                    GlobalVariables.getMessageMap().putError(ACCOUNTING_LINE_ERRORS, ERROR_DIFFERENT_CHARTS, new String[] {});
                    return false;
                }
            }
            index++;
        }
        return true;
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
