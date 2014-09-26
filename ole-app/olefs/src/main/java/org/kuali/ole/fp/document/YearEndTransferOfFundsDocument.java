/*
 * Copyright 2005 The Kuali Foundation
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

package org.kuali.ole.fp.document;

import org.kuali.ole.fp.document.service.YearEndPendingEntryService;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.AmountTotaling;


/**
 * Year End version of the <code>TransferOfFundsDocument</code> functionally the only difference is the accounting period code
 * used on the glpe entries
 */
public class YearEndTransferOfFundsDocument extends TransferOfFundsDocument implements YearEndDocument, AmountTotaling {

    /**
     * Constructs a YearEndTransferOfFundsDocument.java.
     */
    public YearEndTransferOfFundsDocument() {
        super();
    }
    
    /**
     * This method calls the super class's overridden method to perform the general customization actions, then calls the 
     * YearEndDocumentUtil matching method to perform year end specific customization activities.
     * 
     * @param accountingDocument The accounting document containing the general ledger pending entries being customized.
     * @param accountingLine The accounting line the explicit general ledger pending entry was generated from.
     * @param explicitEntry The explicit general ledger pending entry to be customized.
     * 
     * @see org.kuali.ole.fp.document.validation.impl.TransferOfFundsDocumentRule#customizeExplicitGeneralLedgerPendingEntry(org.kuali.ole.sys.document.AccountingDocument,
     *      org.kuali.ole.sys.businessobject.AccountingLine, org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry)
     * @see YearEndDocumentUtil#customizeExplicitGeneralLedgerPendingEntry(TransactionalDocument, AccountingLine,
     *      GeneralLedgerPendingEntry)
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
        AccountingLine accountingLine = (AccountingLine)postable;
        SpringContext.getBean(YearEndPendingEntryService.class).customizeExplicitGeneralLedgerPendingEntry(this, accountingLine, explicitEntry);
    }

    /**
     * Overridden to populate object code from last year's offset definition
     * @see org.kuali.ole.sys.document.AccountingDocumentBase#customizeOffsetGeneralLedgerPendingEntry(org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail, org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry, org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry)
     */
    @Override
    public boolean customizeOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail accountingLine, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        boolean success = super.customizeOffsetGeneralLedgerPendingEntry(accountingLine, explicitEntry, offsetEntry);
        success &= SpringContext.getBean(YearEndPendingEntryService.class).customizeOffsetGeneralLedgerPendingEntry(this, accountingLine, explicitEntry, offsetEntry);
        return success;
    }
    
    @Override
    public Class<? extends AccountingDocument> getDocumentClassForAccountingLineValueAllowedValidation() {
        return TransferOfFundsDocument.class;
    }
}
