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
package org.kuali.ole.fp.batch.service.impl;


/**
 * An implementation of ElectronicPaymentClaimingHelper for YearEndDisbursementOfIncomeAndExpense documents. Most of the behaviors
 * have been inherited from DistributionOfIncomeAndExpenseElectronicPaymentClaimingHelperStrategyImpl.
 */
public class YearEndDistributionOfIncomeAndExpenseElectronicPaymentClaimingHelperStrategyImpl extends DistributionOfIncomeAndExpenseElectronicPaymentClaimingHelperStrategyImpl {
    private static final String URL_DOC_TYPE = "YearEndDistributionOfIncomeAndExpense";

    /**
     * @see org.kuali.ole.sys.service.ElectronicPaymentClaimingDocumentGenerationStrategy#getClaimingDocumentWorkflowDocumentType()
     * @return the name YearEndDistributionOfIncomeAndExpenseDocument workflow document type
     */
    public String getClaimingDocumentWorkflowDocumentType() {
        return "YEDI";
    }

    /**
     * @see org.kuali.ole.fp.batch.service.impl.DistributionOfIncomeAndExpenseElectronicPaymentClaimingHelperStrategyImpl#getClaimingDocumentClass()
     */
    @Override
    protected String getUrlDocType() {
        return YearEndDistributionOfIncomeAndExpenseElectronicPaymentClaimingHelperStrategyImpl.URL_DOC_TYPE;
    }
}
