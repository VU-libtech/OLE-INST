/*
 * Copyright 2006 The Kuali Foundation
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

import static org.kuali.ole.fp.document.validation.impl.TransferOfFundsDocumentRuleConstants.YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE_CODE;

import org.kuali.ole.fp.document.service.YearEndPendingEntryService;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.service.UniversityDateService;


/**
 * Year End version of the <code>BudgetAdjustmentDocument</code>
 */
public class YearEndBudgetAdjustmentDocument extends BudgetAdjustmentDocument implements YearEndDocument {

    /**
     * Constructs a YearEndBudgetAdjustmentDocument.
     */
    public YearEndBudgetAdjustmentDocument() {
        super();
    }

    /**
     * set posting year to previous fiscal year
     */
    public void initiateDocument() {
        Integer previousYearParam = new Integer(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().intValue() - 1);
        setPostingYear(previousYearParam);
    }
    
    /**
     * This method calls the super class's overridden method to perform the general customization actions, then calls the 
     * YearEndDocumentUtil matching method to perform year end specific customization activities.
     * 
     * @param accountingDocument The accounting document containing the general ledger pending entries being customized.
     * @param accountingLine The accounting line the explicit general ledger pending entry was generated from.
     * @param explicitEntry The explicit general ledger pending entry to be customized.
     * 
     * @see org.kuali.ole.fp.document.validation.impl.BudgetAdjustmentDocumentRule#customizeExplicitGeneralLedgerPendingEntry(org.kuali.rice.krad.document.AccountingDocument,
     *      org.kuali.rice.krad.bo.AccountingLine, org.kuali.module.gl.bo.GeneralLedgerPendingEntry)
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
    
    /**
     * This method retrieves the year end transfer of funds document type code, which is defined as a constant in 
     * TransferOfFundsDocumentRuleConstants.
     * 
     * @return The value defined in the constants class for year end transfer of funds document type code.
     * 
     * @see org.kuali.ole.fp.document.validation.impl.BudgetAdjustmentDocumentRule#getTransferDocumentType()
     * @see org.kuali.ole.fp.document.validation.impl.TransferOfFundsDocumentRuleConstants#YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE_CODE
     */
    @Override
    protected String getTransferDocumentType() {
        return YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE_CODE;
    }
    
    @Override
    public Class<? extends AccountingDocument> getDocumentClassForAccountingLineValueAllowedValidation() {
        return BudgetAdjustmentDocument.class;
    }
}
