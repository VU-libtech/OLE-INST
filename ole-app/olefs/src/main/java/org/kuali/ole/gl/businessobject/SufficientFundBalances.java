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

package org.kuali.ole.gl.businessobject;

import java.sql.Date;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import org.kuali.ole.coa.businessobject.Account;
import org.kuali.ole.coa.businessobject.Chart;
import org.kuali.ole.coa.businessobject.ObjectCode;
import org.kuali.ole.gl.GeneralLedgerConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * This class represents sufficient fund balances
 */
public class SufficientFundBalances extends PersistableBusinessObjectBase {

    private Integer universityFiscalYear;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String accountSufficientFundsCode;
    private KualiDecimal currentBudgetBalanceAmount;
    private KualiDecimal accountActualExpenditureAmt;
    private KualiDecimal accountEncumbranceAmount;
    private Date transactionDateTimeStamp;
    private ObjectCode objectCode;
    private Chart chart;
    private Account account;

    public static final String BLANKS = "                 ";
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd";

    /**
     * Default constructor.
     */
    public SufficientFundBalances() {

    }

    /**
     * Constructs a SufficientFundBalances.java.
     * @param line
     */
    public SufficientFundBalances(String line) {
        setFromTextFile(line);
    }

    /**
     * This method sets this object's attributes from the passed in line
     * 
     * @param line with sufficient fund balance related attributes
     */
    public void setFromTextFile(String line) {

        // Just in case
        line = line + "                   ";

        if (!GeneralLedgerConstants.getSpaceUniversityFiscalYear().equals(line.substring(0, 4))) {
            setUniversityFiscalYear(new Integer(line.substring(0, 4)));
        }
        else {
            setUniversityFiscalYear(null);
        }
        setChartOfAccountsCode(line.substring(4, 6).trim());
        setAccountNumber(line.substring(6, 13).trim());
        setFinancialObjectCode(line.substring(13, 17).trim());
        setAccountSufficientFundsCode(line.substring(17, 24).trim());
        setCurrentBudgetBalanceAmount(new KualiDecimal(line.substring(24, 41).trim()));
        setAccountActualExpenditureAmt(new KualiDecimal(line.substring(41, 58).trim()));
        setAccountEncumbranceAmount(new KualiDecimal(line.substring(58, 75).trim()));
        setTransactionDateTimeStamp(parseDate(line.substring(75, 85), true));
    }

    /**
     * This method returns a string representing this sufficient fund balance object and its attributes
     * 
     * @return String representing this sufficient fund balance object and its attributes
     */
    public String getLine() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        nf.setMinimumIntegerDigits(1);
        nf.setGroupingUsed(false);

        StringBuffer sb = new StringBuffer();
        if (universityFiscalYear == null) {
            sb.append(GeneralLedgerConstants.getSpaceUniversityFiscalYear());
        }
        else {
            sb.append(universityFiscalYear);
        }
        sb.append(getField(2, chartOfAccountsCode));
        sb.append(getField(7, accountNumber));
        sb.append(getField(4, financialObjectCode));
        sb.append(getField(1, accountSufficientFundsCode));
        if (currentBudgetBalanceAmount == null) {
            sb.append(BLANKS);
        }
        else {
            String a = nf.format(currentBudgetBalanceAmount.doubleValue());
            sb.append(BLANKS.substring(0, 17 - a.length()));
            sb.append(a);
        }
        if (accountActualExpenditureAmt == null) {
            sb.append(BLANKS);
        }
        else {
            String a = nf.format(accountActualExpenditureAmt.doubleValue());
            sb.append(BLANKS.substring(0, 17 - a.length()));
            sb.append(a);
        }
        if (accountEncumbranceAmount == null) {
            sb.append(BLANKS);
        }
        else {
            String a = nf.format(accountEncumbranceAmount.doubleValue());
            sb.append(BLANKS.substring(0, 17 - a.length()));
            sb.append(a);
        }
        return sb.toString();
    }

    private static String SPACES = "          ";

    /**
     * Returns value passed in with additional spaces if need be 
     * 
     * @param size
     * @param value
     * @return
     */
    private String getField(int size, String value) {
        if (value == null) {
            return SPACES.substring(0, size);
        }
        else {
            if (value.length() < size) {
                return value + SPACES.substring(0, size - value.length());
            }
            else {
                return value;
            }
        }
    }

    /**
     * This method parses a date as yyyy-MM-dd
     * 
     * @param sdate
     * @param beLenientWithDates
     * @return
     */
    private java.sql.Date parseDate(String sdate, boolean beLenientWithDates) {
        if ((sdate == null) || (sdate.trim().length() == 0)) {
            return null;
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
            sdf.setLenient(beLenientWithDates);

            try {
                java.util.Date d = sdf.parse(sdate);
                return new Date(d.getTime());
            }
            catch (ParseException e) {
                return null;
            }
        }
    }

    /**
     * This method returns a string representation of date as yyyy-MM-dd 
     * 
     * @param date
     * @return
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "          ";
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
            return sdf.format(date);
        }
    }

    /**
     * Gets the universityFiscalYear attribute.
     * 
     * @return Returns the universityFiscalYear
     */
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    /**
     * Sets the universityFiscalYear attribute.
     * 
     * @param universityFiscalYear The universityFiscalYear to set.
     */
    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }


    /**
     * Gets the chartOfAccountsCode attribute.
     * 
     * @return Returns the chartOfAccountsCode
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode attribute.
     * 
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }


    /**
     * Gets the accountNumber attribute.
     * 
     * @return Returns the accountNumber
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the accountNumber attribute.
     * 
     * @param accountNumber The accountNumber to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    /**
     * Gets the financialObjectCode attribute.
     * 
     * @return Returns the financialObjectCode
     */
    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    /**
     * Sets the financialObjectCode attribute.
     * 
     * @param financialObjectCode The financialObjectCode to set.
     */
    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }


    /**
     * Gets the accountSufficientFundsCode attribute.
     * 
     * @return Returns the accountSufficientFundsCode
     */
    public String getAccountSufficientFundsCode() {
        return accountSufficientFundsCode;
    }

    /**
     * Sets the accountSufficientFundsCode attribute.
     * 
     * @param accountSufficientFundsCode The accountSufficientFundsCode to set.
     */
    public void setAccountSufficientFundsCode(String accountSufficientFundsCode) {
        this.accountSufficientFundsCode = accountSufficientFundsCode;
    }


    /**
     * Gets the currentBudgetBalanceAmount attribute.
     * 
     * @return Returns the currentBudgetBalanceAmount
     */
    public KualiDecimal getCurrentBudgetBalanceAmount() {
        return currentBudgetBalanceAmount;
    }

    /**
     * Sets the currentBudgetBalanceAmount attribute.
     * 
     * @param currentBudgetBalanceAmount The currentBudgetBalanceAmount to set.
     */
    public void setCurrentBudgetBalanceAmount(KualiDecimal currentBudgetBalanceAmount) {
        this.currentBudgetBalanceAmount = currentBudgetBalanceAmount;
    }


    /**
     * Gets the accountActualExpenditureAmt attribute.
     * 
     * @return Returns the accountActualExpenditureAmt
     */
    public KualiDecimal getAccountActualExpenditureAmt() {
        return accountActualExpenditureAmt;
    }

    /**
     * Sets the accountActualExpenditureAmt attribute.
     * 
     * @param accountActualExpenditureAmt The accountActualExpenditureAmt to set.
     */
    public void setAccountActualExpenditureAmt(KualiDecimal accountActualExpenditureAmt) {
        this.accountActualExpenditureAmt = accountActualExpenditureAmt;
    }


    /**
     * Gets the accountEncumbranceAmount attribute.
     * 
     * @return Returns the accountEncumbranceAmount
     */
    public KualiDecimal getAccountEncumbranceAmount() {
        return accountEncumbranceAmount;
    }

    /**
     * Sets the accountEncumbranceAmount attribute.
     * 
     * @param accountEncumbranceAmount The accountEncumbranceAmount to set.
     */
    public void setAccountEncumbranceAmount(KualiDecimal accountEncumbranceAmount) {
        this.accountEncumbranceAmount = accountEncumbranceAmount;
    }


    /**
     * Gets the transactionDateTimeStamp attribute.
     * 
     * @return Returns the transactionDateTimeStamp
     */
    public Date getTransactionDateTimeStamp() {
        return transactionDateTimeStamp;
    }

    /**
     * Sets the transactionDateTimeStamp attribute.
     * 
     * @param transactionDateTimeStamp The transactionDateTimeStamp to set.
     */
    public void setTransactionDateTimeStamp(Date transactionDateTimeStamp) {
        this.transactionDateTimeStamp = transactionDateTimeStamp;
    }


    /**
     * Gets the objectCode attribute.
     * 
     * @return Returns the objectCode
     */
    public ObjectCode getObjectCode() {
        return objectCode;
    }

    /**
     * Sets the objectCode attribute.
     * 
     * @param objectCode The objectCode to set.
     * @deprecated
     */
    public void setObjectCode(ObjectCode objectCode) {
        this.objectCode = objectCode;
    }

    /**
     * Gets the chart attribute.
     * 
     * @return Returns the chart
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Sets the chart attribute.
     * 
     * @param chart The chart to set.
     * @deprecated
     */
    public void setChart(Chart chart) {
        this.chart = chart;
    }

    /**
     * Gets the account attribute.
     * 
     * @return Returns the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the account attribute.
     * 
     * @param account The account to set.
     * @deprecated
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, this.universityFiscalYear.toString());
        m.put(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE, this.chartOfAccountsCode);
        m.put(OLEPropertyConstants.ACCOUNT_NUMBER, this.accountNumber);
        m.put(OLEPropertyConstants.FINANCIAL_OBJECT_CODE, this.financialObjectCode);
        return m;
    }
}
