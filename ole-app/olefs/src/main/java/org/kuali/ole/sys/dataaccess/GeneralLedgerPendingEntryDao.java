/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.ole.sys.dataaccess;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.ole.coa.businessobject.Account;
import org.kuali.ole.gl.businessobject.Balance;
import org.kuali.ole.gl.businessobject.Encumbrance;
import org.kuali.ole.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 * This interface defines basic methods that GeneralLedgerPendingEntry Dao's must provide
 */
public interface GeneralLedgerPendingEntryDao {

    /**
     * Get summary of amounts in the pending entry table
     * 
     * @param universityFiscalYear
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param objectCodes
     * @param balanceTypeCodes
     * @param isDebit
     * @return
     */
    public KualiDecimal getTransactionSummary(Collection universityFiscalYears, String chartOfAccountsCode, String accountNumber, Collection objectCodes, Collection balanceTypeCodes, boolean isDebit);

    /**
     * Get summary of amounts in the pending entry table
     * 
     * @param universityFiscalYear
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param objectTypeCodes
     * @param balanceTypeCodes
     * @param acctSufficientFundsFinObjCd
     * @param isDebit
     * @param isYearEnd
     * @return
     */
    public KualiDecimal getTransactionSummary(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, Collection objectTypeCodes, Collection balanceTypeCodes, String acctSufficientFundsFinObjCd, boolean isDebit, boolean isYearEnd);

    /**
     * Get summary of amounts in the pending entry table
     * 
     * @param universityFiscalYear
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param objectTypeCodes
     * @param balanceTypeCodes
     * @param acctSufficientFundsFinObjCd
     * @param isYearEnd
     * @return
     */
    public KualiDecimal getTransactionSummary(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, Collection objectTypeCodes, Collection balanceTypeCodes, String acctSufficientFundsFinObjCd, boolean isYearEnd);

    /**
     * Find Pending Entries
     * 
     * @param fieldValues
     * @param isApproved
     * @param currentFiscalPeriodCode
     * @param currentFiscalYear
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes for the given universityFiscalYear if this is passed in the
     *        fieldValues or the Current Year Encumbrance Balance Types if universityFiscalYear is not present
     * @return
     */
    public Collection findPendingEntries(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFiscalYear, List<String> encumbranceBalanceTypes);

    /**
     * Delete all pending entries for a given document
     * 
     * @param documentHeaderId
     */
    public void delete(String documentHeaderId);

    /**
     * Delete all pending entries based on the document approved code
     * 
     * @param financialDocumentApprovedCode
     */
    public void deleteByFinancialDocumentApprovedCode(String financialDocumentApprovedCode);

    /**
     * This method retrieves all approved pending ledger entries
     * 
     * @return all approved pending ledger entries
     */
    public Iterator findApprovedPendingLedgerEntries();

    /**
     * This method counts all approved pending ledger entries by account
     * 
     * @param account the given account
     * @return count of entries
     */
    public int countPendingLedgerEntries(Account account);

    /**
     * This method retrieves all pending ledger entries for the given encumbrance
     * 
     * @param encumbrance the encumbrance entry in the GL_Encumbrance_T table
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @return all pending ledger entries of the given encumbrance
     */
    public Iterator findPendingLedgerEntries(Encumbrance encumbrance, boolean isApproved);

    /**
     * This method retrieves all pending ledger entries for the given encumbrance
     * 
     * @param balance the balance entry
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param isConsolidated consolidation option is applied or not
     * @return all pending ledger entries of the given balance
     */
    public Iterator findPendingLedgerEntries(Balance balance, boolean isApproved, boolean isConsolidated);

    /**
     * This method retrieves all pending ledger entries matching the given entry criteria
     * 
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param fieldValues the input fields and values
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes
     * @return all pending ledger entries matching the given balance criteria
     */
    public Iterator findPendingLedgerEntriesForEntry(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFY, List<String> encumbranceBalanceTypes);

    /**
     * This method retrieves all pending ledger entries matching the given balance criteria
     * 
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param fieldValues the input fields and values
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes
     * @return all pending ledger entries matching the given balance criteria
     */
    public Iterator findPendingLedgerEntriesForBalance(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFY, List<String> encumbranceBalanceTypes);

    /**
     * This method retrieves all pending ledger entries matching the given cash balance criteria
     * 
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param fieldValues the input fields and values
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes for the given universityFiscalYear if this is passed in the
     *        fieldValues or the Current Year Encumbrance Balance Types if universityFiscalYear is not present
     * @return all pending ledger entries matching the given cash balance criteria
     */
    public Iterator findPendingLedgerEntriesForCashBalance(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFY, List<String> encumbranceBalanceTypes);

    /**
     * This method retrieves all pending ledger entries that may belong to encumbrance table in the future
     * 
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param fieldValues the input fields and values
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param currentYearOptions the current year system options
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes for the given universityFiscalYear if this is passed in the
     *        fieldValues or the Current Year Encumbrance Balance Types if universityFiscalYear is not present
     * @return all pending ledger entries that may belong to encumbrance table
     */
    public Iterator findPendingLedgerEntriesForEncumbrance(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFiscalYear, SystemOptions currentYearOptions, List<String> encumbranceBalanceTypes);

    /**
     * This method retrieves all pending ledger entries that may belong to the given account balance record in the future
     * 
     * @param fieldValues the input fields and values
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes for the given universityFiscalYear if this is passed in the
     *        fieldValues or the Current Year Encumbrance Balance Types if universityFiscalYear is not present
     * @return all pending ledger entries that may belong to encumbrance table
     */
    public Iterator findPendingLedgerEntrySummaryForAccountBalance(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFiscalYear, List<String> encumbranceBalanceTypes);

    /**
     * This method retrieves all pending ledger entries that may belong to the given account balance record in the future
     * 
     * @param fieldValues the input fields and values
     * @param isApproved the flag that indicates whether the pending entries are approved or don't care
     * @param currentFiscalPeriodCode current fiscal year period code
     * @param currentFY current fiscal year
     * @param encumbranceBalanceTypes a list of encumbranceBalanceTypes for the given universityFiscalYear if this is passed in the
     *        fieldValues or the Current Year Encumbrance Balance Types if universityFiscalYear is not present
     * @return all pending ledger entries that may belong to encumbrance table
     */
    public Iterator findPendingLedgerEntriesForAccountBalance(Map fieldValues, boolean isApproved, String currentFiscalPeriodCode, int currentFiscalYear, List<String> encumbranceBalanceTypes);

}
