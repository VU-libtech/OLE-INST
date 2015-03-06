/*
 * Copyright 2011 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.pdp.document.authorization;

import java.util.Map;

import org.kuali.ole.pdp.businessobject.PayeeACHAccount;
import org.kuali.ole.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.ole.sys.identity.OleKimAttributes;
import org.kuali.rice.krad.bo.BusinessObject;

public class PayeeACHAccountMaintenanceDocumentAuthorizer extends FinancialSystemMaintenanceDocumentAuthorizerBase {

    /**
     * Overridden to add the Payee ACH Account Transaction Type to the qualification
     * @see org.kuali.rice.kns.document.authorization.MaintenanceDocumentAuthorizerBase#addRoleQualification(org.kuali.rice.kns.bo.BusinessObject, java.util.Map)
     */
    @Override
    protected void addRoleQualification(Object businessObject, Map<String, String> qualifications) {
        super.addRoleQualification(businessObject, qualifications);

        if (businessObject instanceof PayeeACHAccount) {
            final PayeeACHAccount payeeACHAccount = (PayeeACHAccount)businessObject;
            qualifications.put(OleKimAttributes.ACH_TRANSACTION_TYPE_CODE, payeeACHAccount.getAchTransactionType());
        }
    }

}