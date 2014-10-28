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

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.document.Document;

public class PurchaseOrderDocumentAuthorizerBase extends PurchasingAccountsPayableTransactionalDocumentAuthorizerBase {

    @Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        // According to the requirement in KFSMI-8056, for PurchaseOrderDocument, the way it should work is :
        // "any person who has edit ability should be able to edit the entire Document Overview tab".
        // So I'm going to return the same value that is returned by canEdit method of the superclass.
        return canEdit(document, user);
    }

}
