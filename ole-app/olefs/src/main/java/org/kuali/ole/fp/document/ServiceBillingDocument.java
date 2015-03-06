/*
 * Copyright 2005-2006 The Kuali Foundation
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

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.service.DebitDeterminerService;

/**
 * This is the business object that represents the ServiceBillingDocument in Kuali. See
 * {@link org.kuali.ole.fp.document.validation.impl.ServiceBillingDocumentRule} for details on how it differs from
 * {@link InternalBillingDocument}.
 */
public class ServiceBillingDocument extends InternalBillingDocument implements CapitalAssetEditable {

    /**
     * This method further restricts the valid accounting line types exclusively to those with income or expense object type codes
     * only. This is done by calling isIncome() and isExpense() passing the accounting line.
     * 
     * @param financialDocument The document used to determine if the accounting line is a debit line.
     * @param accountingLine The accounting line to be analyzed.
     * @return True if the accounting line passed in is an expense or income accounting line and meets the rules defined by
     *         super.isDebit() method.
     * @see org.kuali.ole.fp.document.validation.impl.InternalBillingDocumentRule#isDebit(org.kuali.rice.krad.document.FinancialDocument,
     *      org.kuali.rice.krad.bo.AccountingLine)
     */
    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLine accountingLine = (AccountingLine) postable;
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (!isDebitUtils.isIncome(accountingLine) && !isDebitUtils.isExpense(accountingLine)) {
            throw new IllegalStateException(isDebitUtils.getDebitCalculationIllegalStateExceptionMessage());
        }

        return super.isDebit(postable);
    }

    /**
     * This method sets extra accounting line fields in explicit general ledger pending entries. Internal billing transactions don't
     * have this field.
     * 
     * @param financialDocument The accounting document containing the general ledger pending entries being customized.
     * @param accountingLine The accounting line the explicit general ledger pending entry was generated from.
     * @param explicitEntry The explicit general ledger pending entry to be customized.
     * @see FinancialDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(FinancialDocument, AccountingLine,
     *      GeneralLedgerPendingEntry)
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        String description = postable.getFinancialDocumentLineDescription();
        if (StringUtils.isNotBlank(description)) {
            explicitEntry.setTransactionLedgerEntryDescription(description);
        }
    }
}
