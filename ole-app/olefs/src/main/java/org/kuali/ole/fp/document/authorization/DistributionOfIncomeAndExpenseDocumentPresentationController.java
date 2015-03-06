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
package org.kuali.ole.fp.document.authorization;

import java.util.List;
import java.util.Set;

import org.kuali.ole.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.OleAuthorizationConstants;
import org.kuali.ole.sys.businessobject.ElectronicPaymentClaim;
import org.kuali.ole.sys.document.authorization.AccountingDocumentPresentationControllerBase;
import org.kuali.rice.krad.document.Document;

public class DistributionOfIncomeAndExpenseDocumentPresentationController extends AccountingDocumentPresentationControllerBase {

    /**
     * @see org.kuali.ole.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase#getEditModes(org.kuali.rice.krad.document.Document)
     */
    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);

        DistributionOfIncomeAndExpenseDocument distributionOfIncomeAndExpenseDocument = (DistributionOfIncomeAndExpenseDocument) document;
        List<ElectronicPaymentClaim> electronicPaymentClaims = distributionOfIncomeAndExpenseDocument.getElectronicPaymentClaims();

        if (electronicPaymentClaims == null) {
            distributionOfIncomeAndExpenseDocument.refreshReferenceObject(OLEPropertyConstants.ELECTRONIC_PAYMENT_CLAIMS);
            electronicPaymentClaims = distributionOfIncomeAndExpenseDocument.getElectronicPaymentClaims();
        }

        if (electronicPaymentClaims != null && electronicPaymentClaims.size() > 0) {
            editModes.add(OleAuthorizationConstants.DistributionOfIncomeAndExpenseEditMode.SOURCE_LINE_READ_ONLY_MODE);
        }

        return editModes;
    }
}
