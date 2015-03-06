/*
 * Copyright 2008-2009 The Kuali Foundation
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

import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;

/**
 * Fixes the error path in GlobalVariables before other accounting line validation occurs
 */
public class PurchasingAccountsPayableFixErrorPathValidation extends GenericValidation {
    private AccountingLine accountingLineForValidation;
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurchasingAccountsPayableFixErrorPathValidation.class);

    /**
     * @see org.kuali.ole.sys.document.validation.Validation#validate(java.lang.Object[])
     */
    public boolean validate(AttributedDocumentEvent event) {
        PurchasingAccountsPayableErrorPathUtil.fixErrorPath((AccountingDocument) event.getDocument(), accountingLineForValidation);
        //MSU Contribution OLEMI-8434 DTT-3284
        //GlobalVariables.getMessageMap().clearErrorPath(); 
        return true;
    }

    /**
     * Gets the accountingLineForValidation attribute.
     *
     * @return Returns the accountingLineForValidation.
     */
    public AccountingLine getAccountingLineForValidation() {
        return accountingLineForValidation;
    }

    /**
     * Sets the accountingLineForValidation attribute value.
     *
     * @param accountingLineForValidation The accountingLineForValidation to set.
     */
    public void setAccountingLineForValidation(AccountingLine accountingLineForValidation) {
        this.accountingLineForValidation = accountingLineForValidation;
    }


}
