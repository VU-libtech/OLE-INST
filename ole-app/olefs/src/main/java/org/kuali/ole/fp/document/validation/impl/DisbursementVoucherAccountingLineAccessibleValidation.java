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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.OleAuthorizationConstants;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.authorization.AccountingLineAuthorizer;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.ole.sys.document.validation.impl.AccountingLineAccessibleValidation;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.rice.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.krad.util.GlobalVariables;

public class DisbursementVoucherAccountingLineAccessibleValidation extends AccountingLineAccessibleValidation {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherAccountingLineAccessibleValidation.class);
    protected AccountingLine oldAccountingLineForValidation;

    /**
     * Validates that the given accounting line is accessible for editing by the current user. <strong>This method expects a
     * document as the first parameter and an accounting line as the second</strong>
     * 
     * @see org.kuali.ole.sys.document.validation.impl.AccountingLineAccessibleValidation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");

        Person financialSystemUser = GlobalVariables.getUserSession().getPerson();
        AccountingDocument accountingDocument = this.getAccountingDocumentForValidation();
        AccountingLine accountingLineForValidation = this.getAccountingLineForValidation();

        final AccountingLineAuthorizer accountingLineAuthorizer = lookupAccountingLineAuthorizer();
        final boolean lineIsAccessible = accountingLineAuthorizer.hasEditPermissionOnAccountingLine(accountingDocument, accountingLineForValidation, getAccountingLineCollectionProperty(), financialSystemUser, true);
        boolean isAccessible = accountingLineAuthorizer.hasEditPermissionOnField(accountingDocument, accountingLineForValidation, getAccountingLineCollectionProperty(), OLEPropertyConstants.ACCOUNT_NUMBER, lineIsAccessible, true, financialSystemUser);

        // get the authorizer class to check for special conditions routing and if the user is part of a particular workgroup
        // but only if the document is enroute
        WorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();
        if (!isAccessible && workflowDocument.isEnroute()) {
        	
        	if (oldAccountingLineForValidation == null || accountUnchanged(accountingLineForValidation, oldAccountingLineForValidation)) {
        		isAccessible = true;
        	} else {
                // if approval is requested and the user has required edit permission, then the line is accessible
                List<String> candidateEditModes = this.getCandidateEditModes();
                if (workflowDocument.isApprovalRequested() && this.hasRequiredEditMode(accountingDocument, financialSystemUser, candidateEditModes)) {
                    isAccessible = true;
                }
        	}
        }

        // report errors if the current user can have no access to the account
        if (!isAccessible) {
            String accountNumber = accountingLineForValidation.getAccountNumber();
            String principalName = GlobalVariables.getUserSession().getPerson().getPrincipalName();
            String errorKey = this.convertEventToMessage(event);

            GlobalVariables.getMessageMap().putError(OLEPropertyConstants.ACCOUNT_NUMBER, errorKey, accountNumber, principalName);
        }

        return isAccessible;
    }

    /**
     * determine whether the give user has permission to any edit mode defined in the given candidate edit modes
     * 
     * @param accountingDocument the given accounting document
     * @param financialSystemUser the given user
     * @param candidateEditEditModes the given candidate edit modes
     * @return true if the give user has permission to any edit mode defined in the given candidate edit modes; otherwise, false
     */
    protected boolean hasRequiredEditMode(AccountingDocument accountingDocument, Person financialSystemUser, List<String> candidateEditModes) {
        DocumentHelperService documentHelperService = SpringContext.getBean(DocumentHelperService.class);
        TransactionalDocumentAuthorizer documentAuthorizer = (TransactionalDocumentAuthorizer) documentHelperService.getDocumentAuthorizer(accountingDocument);
        TransactionalDocumentPresentationController presentationController = (TransactionalDocumentPresentationController) documentHelperService.getDocumentPresentationController(accountingDocument);

        Set<String> presentationControllerEditModes = presentationController.getEditModes(accountingDocument);
        Set<String> editModes = documentAuthorizer.getEditModes(accountingDocument, financialSystemUser, presentationControllerEditModes);

        for (String editMode : candidateEditModes) {
            if (editModes.contains(editMode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * define the possibly desired edit modes
     * 
     * @return the possibly desired edit modes
     */
    protected List<String> getCandidateEditModes() {
        List<String> candidateEdiModes = new ArrayList<String>();
        candidateEdiModes.add(OleAuthorizationConstants.DisbursementVoucherEditMode.TAX_ENTRY);
        candidateEdiModes.add(OleAuthorizationConstants.DisbursementVoucherEditMode.FRN_ENTRY);
        candidateEdiModes.add(OleAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
        candidateEdiModes.add(OleAuthorizationConstants.DisbursementVoucherEditMode.WIRE_ENTRY);

        return candidateEdiModes;
    }
    
    /**
     * Determines if the two given accounting lines have not have their account changed
     * @param pollux the first accounting line to check
     * @param castor the second accounting line to check
     * @return true if the account is the same for the two, false otherwise
     */
    protected boolean accountUnchanged(AccountingLine pollux, AccountingLine castor) {
    	return ((pollux.getChartOfAccountsCode() == null && castor.getChartOfAccountsCode() == null) || (pollux.getChartOfAccountsCode() != null && pollux.getChartOfAccountsCode().equals(castor.getChartOfAccountsCode()))) && ((pollux.getAccountNumber() == null && castor.getAccountNumber() == null) || (pollux.getAccountNumber() != null && pollux.getAccountNumber().equals(castor.getAccountNumber()))); 
    }

    /**
     * Sets the oldAccountingLineForValidation attribute value.
     * @param oldAccountingLineForValidation The oldAccountingLineForValidation to set.
     */
    public void setOldAccountingLineForValidation(AccountingLine oldAccountingLineForValidation) {
        this.oldAccountingLineForValidation = oldAccountingLineForValidation;
    }
}
