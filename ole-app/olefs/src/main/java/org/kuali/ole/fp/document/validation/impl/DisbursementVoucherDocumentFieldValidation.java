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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.fp.document.DisbursementVoucherConstants;
import org.kuali.ole.fp.document.DisbursementVoucherDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

public class DisbursementVoucherDocumentFieldValidation extends GenericValidation {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherDocumentFieldValidation.class);

    private AccountingDocument accountingDocumentForValidation;

    /**
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");
        boolean isValid = true;

        DisbursementVoucherDocument document = (DisbursementVoucherDocument) accountingDocumentForValidation;

        MessageMap errors = GlobalVariables.getMessageMap();

        // validate document required fields
        SpringContext.getBean(DictionaryValidationService.class).validateDocument(document);

        // validate payee fields
        errors.addToErrorPath(OLEPropertyConstants.DOCUMENT);
        errors.addToErrorPath(OLEPropertyConstants.DV_PAYEE_DETAIL);
        SpringContext.getBean(DictionaryValidationService.class).validateBusinessObject(document.getDvPayeeDetail());
        errors.removeFromErrorPath(OLEPropertyConstants.DV_PAYEE_DETAIL);
        errors.removeFromErrorPath(OLEPropertyConstants.DOCUMENT);

        //hasErrors() returns true if it not empty else false.  
        if (errors.hasErrors()) {
            return false;
        }

        /* special handling name & address required if special handling is indicated */
        if (document.isDisbVchrSpecialHandlingCode()) {
            if (StringUtils.isBlank(document.getDvPayeeDetail().getDisbVchrSpecialHandlingPersonName()) || StringUtils.isBlank(document.getDvPayeeDetail().getDisbVchrSpecialHandlingLine1Addr())) {
                errors.putErrorWithoutFullErrorPath(OLEConstants.GENERAL_SPECHAND_TAB_ERRORS, OLEKeyConstants.ERROR_DV_SPECIAL_HANDLING);
                isValid = false;
            }
        }

        boolean hasNoNotes = this.hasNoNotes(document);

        /* if no documentation is selected, must be a note explaining why */
        if (DisbursementVoucherConstants.NO_DOCUMENTATION_LOCATION.equals(document.getDisbursementVoucherDocumentationLocationCode()) && hasNoNotes) {
            errors.putError(OLEPropertyConstants.DISBURSEMENT_VOUCHER_DOCUMENTATION_LOCATION_CODE, OLEKeyConstants.ERROR_DV_NO_DOCUMENTATION_NOTE_MISSING);
            isValid = false;
        }

        /* if special handling indicated, must be a note explaining why */
        if (document.isDisbVchrSpecialHandlingCode() && hasNoNotes) {
            errors.putErrorWithoutFullErrorPath(OLEConstants.GENERAL_PAYMENT_TAB_ERRORS, OLEKeyConstants.ERROR_DV_SPECIAL_HANDLING_NOTE_MISSING);
            isValid = false;
        }

        /* if exception attached indicated, must be a note explaining why */
        if (document.isExceptionIndicator() && hasNoNotes) {
            errors.putErrorWithoutFullErrorPath(OLEConstants.GENERAL_PAYMENT_TAB_ERRORS, OLEKeyConstants.ERROR_DV_EXCEPTION_ATTACHED_NOTE_MISSING);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Return true if disbursement voucher does not have any notes
     * 
     * @param document submitted disbursement voucher document
     * @return whether the given document has no notes
     */
    protected boolean hasNoNotes(DisbursementVoucherDocument document) {
        List<Note> notes = document.getNotes();

        return (notes == null || notes.isEmpty());
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Gets the accountingDocumentForValidation attribute.
     * 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }
}
