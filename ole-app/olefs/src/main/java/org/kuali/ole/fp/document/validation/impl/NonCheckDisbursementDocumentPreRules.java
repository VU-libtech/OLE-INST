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
package org.kuali.ole.fp.document.validation.impl;

import java.text.MessageFormat;

import org.kuali.ole.fp.document.NonCheckDisbursementDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.businessobject.Bank;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.BankService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kns.rules.PromptBeforeValidationBase;
import org.kuali.rice.krad.document.Document;

/**
 * Performs warning checks and prompts for NonCheckDisbursement.
 */
public class NonCheckDisbursementDocumentPreRules extends PromptBeforeValidationBase {

    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = true;
        
        NonCheckDisbursementDocument nonCheckDocument = (NonCheckDisbursementDocument) document;
        
        preRulesOK &= checkBankCodeActive(nonCheckDocument);
        
        return preRulesOK;
    }
    
    /**
     * If bank specification is enabled, prompts user to use the continuation bank code when the
     * given bank code is inactive
     * 
     * @param nonCheckDocument document containing bank code
     * @return true
     */
    protected boolean checkBankCodeActive(NonCheckDisbursementDocument nonCheckDocument) {
        boolean continueRules = true;
        
        // if bank specification is not enabled, no need to validate bank
        if (!SpringContext.getBean(BankService.class).isBankSpecificationEnabled()) {
            return continueRules;
        }

        // refresh bank reference so continuation bank can be checked for active status
        nonCheckDocument.refreshReferenceObject(OLEPropertyConstants.BANK);
        Bank bank = nonCheckDocument.getBank();

        // if bank is inactive and continuation is active, prompt user to use continuation bank
        if (bank != null && !bank.isActive() && bank.getContinuationBank().isActive()) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(OLEKeyConstants.QUESTION_BANK_INACTIVE);
            questionText = MessageFormat.format(questionText, nonCheckDocument.getFinancialDocumentBankCode(), bank.getContinuationBankCode());

            boolean useContinuation = super.askOrAnalyzeYesNoQuestion(OLEConstants.USE_CONTINUATION_BANK_QUESTION, questionText);
            if (useContinuation) {
                nonCheckDocument.setFinancialDocumentBankCode(bank.getContinuationBankCode());
            }
        }

        return continueRules;
    }

}
