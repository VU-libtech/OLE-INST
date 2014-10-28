/*
 * Copyright 2006 The Kuali Foundation
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

import static org.kuali.ole.fp.document.validation.impl.CreditCardReceiptDocumentRuleConstants.CREDIT_CARD_RECEIPT_PREFIX;
import static org.kuali.ole.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX;

import org.kuali.ole.fp.businessobject.CreditCardDetail;
import org.kuali.ole.fp.document.CreditCardReceiptDocument;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEKeyConstants.CashReceipt;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

/**
 * Common Credit Card Receipt Document rule utilities.
 */
public class CreditCardReceiptDocumentRuleUtil {
    /**
     * This method method will invoke the data dictionary validation for a CreditCardDetail bo instance, in addition to checking
     * existence of the CreditCardType and CreditCardVendor attributes that hang off of it. This method assumes that the document
     * hierarchy for the error map path is managed outside of this call.
     * 
     * @param creditCardReceipt credit card detail
     * @return true if credit card detail amount is non zero and credit card vendor and type references exist
     */
    public static boolean validateCreditCardReceipt(CreditCardDetail creditCardReceipt) {
        MessageMap errorMap = GlobalVariables.getMessageMap();
        int originalErrorCount = errorMap.getErrorCount();

        // call the DD validation which checks basic data integrity
        SpringContext.getBean(DictionaryValidationService.class).validateBusinessObject(creditCardReceipt);
        boolean isValid = (errorMap.getErrorCount() == originalErrorCount);

        // check that dollar amount is not zero before continuing
        if (isValid) {
            isValid = !creditCardReceipt.getCreditCardAdvanceDepositAmount().isZero();
            if (!isValid) {
                String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(CreditCardDetail.class, OLEPropertyConstants.CREDIT_CARD_ADVANCE_DEPOSIT_AMOUNT);
                errorMap.putError(OLEPropertyConstants.CREDIT_CARD_ADVANCE_DEPOSIT_AMOUNT, OLEKeyConstants.ERROR_ZERO_AMOUNT, label);
            }
        }

        if (isValid) {
            isValid = SpringContext.getBean(DictionaryValidationService.class).validateReferenceExists(creditCardReceipt, OLEPropertyConstants.CREDIT_CARD_TYPE);
            if (!isValid) {
                String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(CreditCardDetail.class, OLEPropertyConstants.FINANCIAL_DOCUMENT_CREDIT_CARD_TYPE_CODE);
                errorMap.putError(OLEPropertyConstants.FINANCIAL_DOCUMENT_CREDIT_CARD_TYPE_CODE, OLEKeyConstants.ERROR_EXISTENCE, label);
            }
        }
        if (isValid) {
            isValid = SpringContext.getBean(DictionaryValidationService.class).validateReferenceExists(creditCardReceipt, OLEPropertyConstants.CREDIT_CARD_VENDOR);
            if (!isValid) {
                String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(CreditCardDetail.class, OLEPropertyConstants.FINANCIAL_DOCUMENT_CREDIT_CARD_VENDOR_NUMBER);
                errorMap.putError(OLEPropertyConstants.FINANCIAL_DOCUMENT_CREDIT_CARD_VENDOR_NUMBER, OLEKeyConstants.ERROR_EXISTENCE, label);
            }
        }

        return isValid;
    }


    /**
     * Checks whether the CashReceiptDocument's cash totals are invalid, generating global errors if so.
     * 
     * @param cashReceiptDocument submitted cash receipt document
     * @return true if any of the cash totals on cash credit card receipt document are invalid
     */
    public static boolean areCashTotalsInvalid(CreditCardReceiptDocument ccrDocument) {
        String documentEntryName = ccrDocument.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();

        boolean isInvalid = isTotalInvalid(ccrDocument, ccrDocument.getTotalDollarAmount(), documentEntryName, OLEPropertyConstants.CREDIT_CARD_RECEIPTS_TOTAL);

        return isInvalid;
    }

    /**
     * Returns true if total is invalid and puts an error message in the error map for that property if the amount is negative
     * 
     * @param cashReceiptDocument
     * @param totalAmount
     * @param documentEntryName
     * @param propertyName
     * @return true if the totalAmount is an invalid value
     */
    private static boolean isTotalInvalid(CreditCardReceiptDocument ccrDocument, KualiDecimal totalAmount, String documentEntryName, String propertyName) {
        boolean isInvalid = false;
        String errorProperty = CREDIT_CARD_RECEIPT_PREFIX + propertyName;

        // treating null totalAmount as if it were a zero
        DataDictionaryService dds = SpringContext.getBean(DataDictionaryService.class);
        String errorLabel = dds.getAttributeLabel(documentEntryName, propertyName);
        if ((totalAmount == null) || totalAmount.isZero()) {
            GlobalVariables.getMessageMap().putError(errorProperty, CashReceipt.ERROR_ZERO_TOTAL, errorLabel);

            isInvalid = true;
        }
        else {
            int precount = GlobalVariables.getMessageMap().getNumberOfPropertiesWithErrors();

            DictionaryValidationService dvs = SpringContext.getBean(DictionaryValidationService.class);
            dvs.validateDocumentAttribute(ccrDocument, propertyName, DOCUMENT_ERROR_PREFIX);

            // replace generic error message, if any, with something more readable
            GlobalVariables.getMessageMap().replaceError(errorProperty, OLEKeyConstants.ERROR_MAX_LENGTH, CashReceipt.ERROR_EXCESSIVE_TOTAL, errorLabel);

            int postcount = GlobalVariables.getMessageMap().getNumberOfPropertiesWithErrors();
            isInvalid = (postcount > precount);
        }

        return isInvalid;
    }
}
