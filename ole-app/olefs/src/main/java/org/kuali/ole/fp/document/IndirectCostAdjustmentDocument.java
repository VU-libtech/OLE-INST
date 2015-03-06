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

import java.math.BigDecimal;

import org.kuali.ole.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.ole.fp.document.validation.impl.IndirectCostAdjustmentDocumentRuleConstants;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.ole.sys.businessobject.SourceAccountingLine;
import org.kuali.ole.sys.businessobject.TargetAccountingLine;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocumentBase;
import org.kuali.ole.sys.document.AmountTotaling;
import org.kuali.ole.sys.document.Correctable;
import org.kuali.ole.sys.document.service.DebitDeterminerService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.document.Copyable;

public class IndirectCostAdjustmentDocument extends AccountingDocumentBase implements Copyable, Correctable, AmountTotaling {

    /**
     * Constructs a IndirectCostAdjustmentDocument.java.
     */
    public IndirectCostAdjustmentDocument() {
        super();
    }

    /**
     * @see org.kuali.ole.sys.document.AccountingDocument#getSourceAccountingLinesSectionTitle()
     */
    @Override
    public String getSourceAccountingLinesSectionTitle() {
        return OLEConstants.GRANT;
    }

    /**
     * @see org.kuali.ole.sys.document.AccountingDocument#getTargetAccountingLinesSectionTitle()
     */
    @Override
    public String getTargetAccountingLinesSectionTitle() {
        return OLEConstants.ICR;
    }

    /**
     * per ICA specs, adds a target(receipt) line when an source(grant) line is added using the following logic to populate the
     * target line.
     * <ol>
     * <li>receipt line's chart = chart from grant line
     * <li>receipt line's account = ICR account for the account entered on the grant line
     * <li>receipt line's object code = Financial System Parameter APC for the document global receipt line object code (see APC
     * setion below)
     * <li>receipt line's amount = amount from grant line
     * </ol>
     * 
     * @see org.kuali.ole.sys.document.AccountingDocumentBase#addSourceAccountingLine(SourceAccountingLine)
     */
    @Override
    public void addSourceAccountingLine(SourceAccountingLine line) {
        // add source
        super.addSourceAccountingLine(line);
        
        for (IndirectCostRecoveryAccount icrAccount : line.getAccount().getActiveIndirectCostRecoveryAccounts()){
            
            KualiDecimal percentDecimal = new KualiDecimal(icrAccount.getAccountLinePercent().divide(new BigDecimal(100)));
            
            // create and populate target line
            TargetAccountingLine targetAccountingLine = null;
            try {
                targetAccountingLine = (TargetAccountingLine) getTargetAccountingLineClass().newInstance();
            }
            catch (Exception e) {
                throw new IllegalArgumentException("unable to create a target accounting line", e);
            }
            // get apc object code value
            String objectCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(IndirectCostAdjustmentDocument.class, IndirectCostAdjustmentDocumentRuleConstants.RECEIPT_OBJECT_CODE);
            targetAccountingLine.setFinancialObjectCode(objectCode);
            targetAccountingLine.setAccountNumber(icrAccount.getIndirectCostRecoveryAccountNumber());
            targetAccountingLine.setChartOfAccountsCode(icrAccount.getIndirectCostRecoveryFinCoaCode());
            targetAccountingLine.setDocumentNumber(line.getDocumentNumber());
            targetAccountingLine.setPostingYear(line.getPostingYear());
            targetAccountingLine.setAmount(line.getAmount().multiply(percentDecimal));
            // refresh reference objects
    
            targetAccountingLine.refresh();
            // add target line
            addTargetAccountingLine(targetAccountingLine);
        }
    }
    
    /**
     * Same logic as <code>IsDebitUtils#isDebitConsideringType(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)</code>
     * but has the following accounting line restrictions: 
     * 
     * for grant lines(source):
     * <ol>
     * <li>only allow expense object type codes
     * </ol>
     * for receipt lines(target):
     * <ol>
     * <li>only allow income object type codes
     * </ol>
     * 
     * @param transactionDocument The document associated with the accounting line being reviewed to determine if it's a debit.
     * @param accountingLine The accounting line being reviewed to determine if it's a debit line.
     * @return True if the accounting line is a debit.  See IsDebitUtils.isDebitConsideringType().
     * @throws IllegalStateException Thrown if the accounting line given is a source accounting line representing an expense
     * or is a target accounting line representing an income.
     * 
     * @see IsDebitUtils#isDebitConsideringType(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)
     * @see org.kuali.rice.krad.rule.AccountingLineRule#isDebit(org.kuali.rice.krad.document.FinancialDocument,
     *      org.kuali.rice.krad.bo.AccountingLine)
     */
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) throws IllegalStateException {
        AccountingLine accountingLine = (AccountingLine)postable;
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (!(accountingLine.isSourceAccountingLine() && isDebitUtils.isExpense(accountingLine)) && !(accountingLine.isTargetAccountingLine() && isDebitUtils.isIncome(accountingLine))) {
            throw new IllegalStateException(isDebitUtils.getDebitCalculationIllegalStateExceptionMessage());
        }

        return isDebitUtils.isDebitConsideringType(this, accountingLine);
    }
}
