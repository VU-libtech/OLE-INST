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
package org.kuali.ole.module.purap.document.authorization;

import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.rice.kim.api.identity.Person;

import java.util.Set;

/**
 * Accounting line authorizer for Requisition document which allows adding accounting lines at specified nodes
 */
public class PaymentRequestAccountingLineAuthorizer extends PurapAccountingLineAuthorizer {
    /**
     * @see org.kuali.ole.sys.document.authorization.AccountingLineAuthorizerBase#getUnviewableBlocks(org.kuali.ole.sys.document.AccountingDocument, org.kuali.ole.sys.businessobject.AccountingLine, boolean, org.kuali.rice.kim.api.identity.Person)
     */
    @Override
    public Set<String> getUnviewableBlocks(AccountingDocument accountingDocument, AccountingLine accountingLine, boolean newLine, Person currentUser) {
        Set<String> unviewableBlocks = super.getUnviewableBlocks(accountingDocument, accountingLine, newLine, currentUser);
        unviewableBlocks.remove(OLEPropertyConstants.PERCENT);
        unviewableBlocks.remove(OLEPropertyConstants.AMOUNT);

        return unviewableBlocks;
    }
}
