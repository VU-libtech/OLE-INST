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
package org.kuali.ole.fp.document.web.struts;

import static org.kuali.ole.sys.OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE;
import static org.kuali.ole.sys.OLEConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE;
import static org.kuali.ole.sys.OLEConstants.AuxiliaryVoucher.RECODE_DOC_TYPE;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.kuali.ole.coa.businessobject.AccountingPeriod;
import org.kuali.ole.coa.service.AccountingPeriodService;
import org.kuali.ole.fp.document.AuxiliaryVoucherDocument;
import org.kuali.ole.fp.document.validation.impl.AuxiliaryVoucherDocumentRuleConstants;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * Struts form so <code>{@link AuxiliaryVoucherDocument}</code> can be accessed and modified through UI.
 */
public class AuxiliaryVoucherForm extends VoucherForm {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AuxiliaryVoucherForm.class);

    protected String originalVoucherType = OLEConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE; // keep this in sync with the default

    // value set in the document business
    // object

    public AuxiliaryVoucherForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "OLE_AV";
    }

    /**
     * Overrides the parent to call super.populate and then to call the two methods that are specific to loading the two select
     * lists on the page. In addition, this also makes sure that the credit and debit amounts are filled in for situations where
     * validation errors occur and the page reposts.
     *
     * @see org.kuali.rice.kns.web.struts.pojo.PojoForm#populate(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public void populate(HttpServletRequest request) {
        // populate the drop downs
        super.populate(request);
        populateReversalDateForRendering();
    }

    /**
     * @return Returns the serviceBillingDocument.
     */
    public AuxiliaryVoucherDocument getAuxiliaryVoucherDocument() {
        return (AuxiliaryVoucherDocument) getDocument();
    }

    /**
     * @param serviceBillingDocument The serviceBillingDocument to set.
     */
    public void setAuxiliaryVoucherDocument(AuxiliaryVoucherDocument auxiliaryVoucherDocument) {
        setDocument(auxiliaryVoucherDocument);
    }

    /**
     * Gets today's date and then sets the day of the month as 15th, irrespective of the current day of the month
     * @return the modified reversal date
     */
    protected Date getAvReversalDate() {
        Date documentReveralDate = getAuxiliaryVoucherDocument().getReversalDate();
        if (ObjectUtils.isNotNull(documentReveralDate)) {
            return documentReveralDate;
        }

        java.sql.Date avReversalDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight();

        Calendar cal = Calendar.getInstance();
        cal.setTime(avReversalDate);

        int thisMonth;

        if (getAuxiliaryVoucherDocument().getAccountingPeriod().getUniversityFiscalPeriodCode().equals(OLEConstants.MONTH13)) {
            thisMonth = cal.JULY;
        }
        else {
            thisMonth = getAuxiliaryVoucherDocument().getAccountingPeriod().getMonth();
        }

        cal.set(Calendar.MONTH, (thisMonth));

        //if today's day > 15 then set the month to next month.
     //   if (cal.get(Calendar.DAY_OF_MONTH) > OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_DAY_OF_MONTH) {
      //      cal.add(Calendar.MONTH, 1);
      //  }

        int reversalDateDefaultDayOfMonth = this.getReversalDateDefaultDayOfMonth();

        cal.set(Calendar.DAY_OF_MONTH, reversalDateDefaultDayOfMonth);

        long timeInMillis = cal.getTimeInMillis();
        avReversalDate.setTime(timeInMillis);

        return avReversalDate;
    }

    /**
     * Handles special case display rules for displaying Reversal Date at UI layer
     */
    public void populateReversalDateForRendering() {
        java.sql.Date today = getAvReversalDate();

        if (getAuxiliaryVoucherDocument().getTypeCode().equals(ACCRUAL_DOC_TYPE)) {
            getAuxiliaryVoucherDocument().setReversalDate(today);
        }
        else if (getAuxiliaryVoucherDocument().getTypeCode().equals(ADJUSTMENT_DOC_TYPE)) {
            getAuxiliaryVoucherDocument().setReversalDate(null);
        }
        else if (getAuxiliaryVoucherDocument().getTypeCode().equals(RECODE_DOC_TYPE)) {
            DateTime ts = new DateTime(getAuxiliaryVoucherDocument().getDocumentHeader().getWorkflowDocument().getDateCreated());
            Date newts = new Date(ts.getMillis());

            getAuxiliaryVoucherDocument().setReversalDate(newts);
        }
    }

    /**
     * This method returns the reversal date in the format MMM d, yyyy.
     *
     * @return String
     */
    @Override
    public String getFormattedReversalDate() {
        return formatReversalDate(getAuxiliaryVoucherDocument().getReversalDate());
    }

    /**
     * @return String
     */
    public String getOriginalVoucherType() {
        return originalVoucherType;
    }

    /**
     * @param originalVoucherType
     */
    public void setOriginalVoucherType(String originalVoucherType) {
        this.originalVoucherType = originalVoucherType;
    }

    /**
     * Returns a formatted auxiliary voucher type: <Voucher Type Name> (<Voucher Type Code>)
     *
     * @return
     */
    public String getFormattedAuxiliaryVoucherType() {
        String voucherTypeCode = getAuxiliaryVoucherDocument().getTypeCode();
        String formattedVoucherType = new String();

        if (OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE.equals(voucherTypeCode)) {
            formattedVoucherType = OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE_NAME;
        }
        else if (OLEConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE.equals(voucherTypeCode)) {
            formattedVoucherType = OLEConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE_NAME;
        }
        else if (OLEConstants.AuxiliaryVoucher.RECODE_DOC_TYPE.equals(voucherTypeCode)) {
            formattedVoucherType = OLEConstants.AuxiliaryVoucher.RECODE_DOC_TYPE_NAME;
        }
        else {
            throw new IllegalStateException("Invalid auxiliary voucher type code: " + voucherTypeCode);
        }

        return formattedVoucherType + " (" + voucherTypeCode + ")";
    }

    /**
     * This method generates a proper list of valid accounting periods that the user can select from.
     *
     * @see org.kuali.ole.fp.document.web.struts.VoucherForm#populateAccountingPeriodListForRendering()
     */
    @Override
    public void populateAccountingPeriodListForRendering() {
        // grab the list of valid accounting periods
        ArrayList accountingPeriods = new ArrayList(SpringContext.getBean(AccountingPeriodService.class).getOpenAccountingPeriods());
        // now, validate further, based on the rules from AuxiliaryVoucherDocumentRule
        ArrayList filteredAccountingPeriods = new ArrayList();
        filteredAccountingPeriods.addAll(CollectionUtils.select(accountingPeriods, new OpenAuxiliaryVoucherPredicate(this.getDocument())));
        // if our auxiliary voucher doc contains an accounting period already, make sure the collection has it too!
        if (this.getDocument() instanceof AuxiliaryVoucherDocument) {
            AuxiliaryVoucherDocument avDoc = (AuxiliaryVoucherDocument) this.getDocument();
            if (avDoc != null && avDoc.getAccountingPeriod() != null && !filteredAccountingPeriods.contains(avDoc.getAccountingPeriod())) {
                // this is most likely going to happen because the approver is trying
                // to approve a document after the grace period of an accounting period
                // or a fiscal year has switched over when the document was first created;
                // as such, it's probably a good bet that the doc's accounting period
                // belongs at the top of the list
                filteredAccountingPeriods.add(0, avDoc.getAccountingPeriod());
            }
        }
        // set into the form for rendering
        setAccountingPeriods(filteredAccountingPeriods);
        // set the chosen accounting period into the form
        populateSelectedVoucherAccountingPeriod();
    }

    protected class OpenAuxiliaryVoucherPredicate implements Predicate {
        protected ParameterService parameterService;
        protected UniversityDateService dateService;
        protected AccountingPeriodService acctPeriodService;
        protected Document auxiliaryVoucherDocument;
        protected AccountingPeriod currPeriod;
        protected java.sql.Date currentDate;
        protected Integer currentFiscalYear;

        public OpenAuxiliaryVoucherPredicate(Document doc) {
            this.parameterService = SpringContext.getBean(ParameterService.class);
            this.dateService = SpringContext.getBean(UniversityDateService.class);
            this.acctPeriodService = SpringContext.getBean(AccountingPeriodService.class);
            this.auxiliaryVoucherDocument = doc;
            this.currPeriod = acctPeriodService.getByDate(new java.sql.Date(new java.util.GregorianCalendar().getTimeInMillis()));
            this.currentDate = new java.sql.Date(new java.util.Date().getTime());
            this.currentFiscalYear = dateService.getCurrentFiscalYear();
        }

        @Override
        public boolean evaluate(Object o) {
            boolean result = false;
            if (o instanceof AccountingPeriod) {
                AccountingPeriod period = (AccountingPeriod) o;
                result = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(AuxiliaryVoucherDocument.class, AuxiliaryVoucherDocumentRuleConstants.RESTRICTED_PERIOD_CODES, period.getUniversityFiscalPeriodCode()).evaluationSucceeds();
                if (result) {
                    result = (period.getUniversityFiscalYear().equals( currentFiscalYear ));
                    if (result) {
                        // did this accounting period end before now?
                        result = acctPeriodService.compareAccountingPeriodsByDate(period, currPeriod) >= 0;
                        if (!result) {
                            // if yes, are we still in the grace period?
                            result = getAuxiliaryVoucherDocument().calculateIfWithinGracePeriod(currentDate, period);
                        }
                    }
                    else {
                        // are we in current in the grace period of an ending accounting period of the previous fiscal year?
                        result = getAuxiliaryVoucherDocument().calculateIfWithinGracePeriod(currentDate, period) && getAuxiliaryVoucherDocument().isEndOfPreviousFiscalYear(period);
                    }
                }
            }
            return result;
        }
    }

    public List<String> getAccountingPeriodCompositeValueList() {
        List<String> accountingPeriodCompositeValueList = new ArrayList<String>();
        for (int i = 0; i < this.getAccountingPeriods().size(); i++) {
            AccountingPeriod temp = (AccountingPeriod) this.getAccountingPeriods().get(i);
            accountingPeriodCompositeValueList.add(temp.getUniversityFiscalPeriodCode() + temp.getUniversityFiscalYear());
        }

        return accountingPeriodCompositeValueList;
    }

    public List<String> getAccountingPeriodLabelList() {
        List<String> accountingPeriodLabelList = new ArrayList<String>();
        for (int i = 0; i < this.getAccountingPeriods().size(); i++) {
            AccountingPeriod temp = (AccountingPeriod) this.getAccountingPeriods().get(i);
            accountingPeriodLabelList.add(temp.getUniversityFiscalPeriodName());
        }

        return accountingPeriodLabelList;
    }

    public static final String REVERSAL_DATE_DEFAULT_DAY_OF_THE_MONTH_PARM_NAME = "REVERSAL_DATE_DEFAULT_DAY_OF_THE_MONTH";

    /**
     * get the reversal date default day of month defined as an application parameter
     */
    protected int getReversalDateDefaultDayOfMonth() {
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        String defaultDayOfMonth = parameterService.getParameterValueAsString(AuxiliaryVoucherDocument.class, REVERSAL_DATE_DEFAULT_DAY_OF_THE_MONTH_PARM_NAME);

        try {
            Integer reversalDateDefaultDayOfMonth = Integer.parseInt(defaultDayOfMonth);

            return reversalDateDefaultDayOfMonth;
        }
        catch(Exception e){
            LOG.info("Invalid value was assigned to the paremeter: " + REVERSAL_DATE_DEFAULT_DAY_OF_THE_MONTH_PARM_NAME + ". The default value " + OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_DAY_OF_MONTH + " is applied.");
        }

        return OLEConstants.AuxiliaryVoucher.ACCRUAL_DOC_DAY_OF_MONTH;
    }
}
