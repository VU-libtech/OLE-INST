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
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.ole.module.purap.document.service.PurapService;
import org.kuali.ole.sys.document.validation.BranchingValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;

public class PurchasingAccountsPayableFullDocumentEntryCompletedBranchingValidation extends BranchingValidation {

    public static final String FULL_DOCUMENT_ENTRY = "fullDocumentEntry";
    public static final String NOT_FULL_DOCUMENT_ENTRY = "notFullDocumentEntry";
    private PurapService purapService;

    @Override
    protected String determineBranch(AttributedDocumentEvent event) {

        PurchasingAccountsPayableDocument purapDocument = (PurchasingAccountsPayableDocument) event.getDocument();

        if (purapService.isFullDocumentEntryCompleted(purapDocument)) {
            return FULL_DOCUMENT_ENTRY;
        } else {
            return NOT_FULL_DOCUMENT_ENTRY;
        }
    }

    public PurapService getPurapService() {
        return purapService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }
}
