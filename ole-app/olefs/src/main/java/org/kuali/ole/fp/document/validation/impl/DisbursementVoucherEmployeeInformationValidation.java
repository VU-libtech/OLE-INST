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

import org.kuali.ole.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.ole.fp.document.DisbursementVoucherDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

public class DisbursementVoucherEmployeeInformationValidation extends GenericValidation {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherEmployeeInformationValidation.class);

    private AccountingDocument accountingDocumentForValidation;
    
    public static final String DV_PAYEE_ID_NUMBER_PROPERTY_PATH = OLEPropertyConstants.DV_PAYEE_DETAIL + "." + OLEPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER;

    /**
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");
        boolean isValid = true;
        
        DisbursementVoucherDocument document = (DisbursementVoucherDocument) accountingDocumentForValidation;
        DisbursementVoucherPayeeDetail payeeDetail = document.getDvPayeeDetail();
        
        if(!payeeDetail.isEmployee() || payeeDetail.isVendor()) {
            return true;
        }
        
        String employeeId = payeeDetail.getDisbVchrPayeeIdNumber();
        Person employee = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(employeeId);
        
        MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(OLEPropertyConstants.DOCUMENT);

        // check existence of employee
        if (employee == null) { // If employee is not found, report existence error
            String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(DisbursementVoucherPayeeDetail.class, OLEPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER);
            errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, OLEKeyConstants.ERROR_EXISTENCE, label);
            isValid = false;
        } 
        else if(!OLEConstants.EMPLOYEE_ACTIVE_STATUS.equals(employee.getEmployeeStatusCode())) {
            // If employee is found, then check that employee is active
            String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(DisbursementVoucherPayeeDetail.class, OLEPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER);
            errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, OLEKeyConstants.ERROR_INACTIVE, label);
            isValid = false;
        }
        
        errors.removeFromErrorPath(OLEPropertyConstants.DOCUMENT); 

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
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}

