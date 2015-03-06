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

import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;

public class PurchasingAccountsPayableAccountAtleastOneLineHasPercentValidation extends GenericValidation {

    private PurApItem itemForValidation;

    /**
     * Verifies at least one account has percent distribution to indicate how an
     * overage is to be funded.
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;

        // OLE-3405 : disabling the distribution method choice
//        boolean percentExists = false;
//
//        PurchasingAccountsPayableDocumentBase purapDoc = (PurchasingAccountsPayableDocumentBase) event.getDocument();
//
//        if (PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_CODE.equalsIgnoreCase(purapDoc.getAccountDistributionMethod())) {
//            for (PurApAccountingLine account : itemForValidation.getSourceAccountingLines()) {
//                if (ObjectUtils.isNotNull(account.getAccountLinePercent())) {
//                    //there should be atleast one accounting line where percent should be > 0.00
//                    if (account.getAccountLinePercent().compareTo(BigDecimal.ZERO) == 1) {
//                        percentExists = true;
//                }
//            }
//        }
//
//            if (!percentExists) {
//            GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY, PurapKeyConstants.ERROR_ITEM_ACCOUNTING_LINE_ATLEAST_ONE_PERCENT_MISSING);
//                return false;
//            }
//        }

        return valid;
    }

    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
    }
}
