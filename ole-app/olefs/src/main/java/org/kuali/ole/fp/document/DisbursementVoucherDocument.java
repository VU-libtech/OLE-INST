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

import static org.kuali.ole.sys.OLEConstants.GL_CREDIT_CODE;
import static org.kuali.ole.sys.OLEConstants.GL_DEBIT_CODE;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.coa.businessobject.ObjectCode;
import org.kuali.ole.coa.service.ObjectTypeService;
import org.kuali.ole.fp.batch.service.DisbursementVoucherExtractService;
import org.kuali.ole.fp.businessobject.DisbursementVoucherDocumentationLocation;
import org.kuali.ole.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.ole.fp.businessobject.DisbursementVoucherNonResidentAlienTax;
import org.kuali.ole.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.ole.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.ole.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.ole.fp.businessobject.DisbursementVoucherWireTransfer;
import org.kuali.ole.fp.businessobject.WireCharge;
import org.kuali.ole.fp.businessobject.options.DisbursementVoucherDocumentationLocationValuesFinder;
import org.kuali.ole.fp.businessobject.options.PaymentMethodValuesFinder;
import org.kuali.ole.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.ole.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.ole.select.businessobject.OlePaymentMethod;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.OLEConstants.AdHocPaymentIndicator;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.businessobject.Bank;
import org.kuali.ole.sys.businessobject.ChartOrgHolder;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocumentBase;
import org.kuali.ole.sys.document.AmountTotaling;
import org.kuali.ole.sys.document.service.AccountingDocumentRuleHelperService;
import org.kuali.ole.sys.document.service.DebitDeterminerService;
import org.kuali.ole.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.ole.sys.service.BankService;
import org.kuali.ole.sys.service.FlexibleOffsetAccountService;
import org.kuali.ole.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.ole.sys.service.HomeOriginationService;
import org.kuali.ole.sys.service.OptionsService;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.ole.sys.service.impl.OleParameterConstants;
import org.kuali.ole.vnd.VendorConstants;
import org.kuali.ole.vnd.businessobject.VendorAddress;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.ole.vnd.document.service.VendorService;
import org.kuali.ole.vnd.service.PhoneNumberService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.document.node.RouteNodeInstance;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.rice.kim.api.identity.entity.Entity;
import org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo;
import org.kuali.rice.kim.api.services.IdentityManagementService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.document.Copyable;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * This is the business object that represents the DisbursementVoucher document in Kuali.
 */
public class DisbursementVoucherDocument extends AccountingDocumentBase implements Copyable, AmountTotaling {
    protected static Logger LOG = Logger.getLogger(DisbursementVoucherDocument.class);

    protected static final String PAYEE_IS_PURCHASE_ORDER_VENDOR_SPLIT = "PayeeIsPurchaseOrderVendor";
    protected static final String PURCHASE_ORDER_VENDOR_TYPE = "PO";
    protected static final String DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT = "RequiresTaxReview";
    protected static final String DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT = "RequiresTravelReview";
    protected static final String DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES = "RequiresSeparationOfDutiesReview";
    protected static final String DISBURSEMENT_VOUCHER_TYPE = "DisbursementVoucher";

    protected static final String PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME = "PAYMENT_REASONS_REQUIRING_TAX_REVIEW";
    protected static final String USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME = "USE_DEFAULT_EMPLOYEE_ADDRESS_IND";
    protected static final String DEFAULT_EMPLOYEE_ADDRESS_TYPE_PARAMETER_NAME = "DEFAULT_EMPLOYEE_ADDRESS_TYPE";
    protected static final String SEPARATION_OF_DUTIES_PARAMETER_NAME = "ENABLE_SEPARATION_OF_DUTIES_IND";

    protected static final String TAX_CONTROL_BACKUP_HOLDING = "B";
    protected static final String TAX_CONTROL_HOLD_PAYMENTS = "H";

    protected static transient PersonService personService;
    protected static transient ParameterService parameterService;
    protected static transient VendorService vendorService;
    protected static transient BusinessObjectService businessObjectService;
    protected static transient DateTimeService dateTimeService;
    protected static transient DisbursementVoucherPaymentReasonService dvPymentReasonService;
    protected static transient IdentityManagementService identityManagementService;
    protected static transient DisbursementVoucherExtractService disbursementVoucherExtractService;

    protected Integer finDocNextRegistrantLineNbr;
    protected String disbVchrContactPersonName;
    protected String disbVchrContactPhoneNumber;
    protected String disbVchrContactEmailId;
    protected Date disbursementVoucherDueDate;
    protected boolean disbVchrAttachmentCode;
    protected boolean disbVchrSpecialHandlingCode;
    protected KualiDecimal disbVchrCheckTotalAmount;
    protected boolean disbVchrForeignCurrencyInd;
    protected String disbursementVoucherDocumentationLocationCode;
    protected String disbVchrCheckStubText;
    protected boolean dvCheckStubOverflowCode;
    protected String campusCode;
    protected String disbVchrPayeeTaxControlCode;
    protected boolean disbVchrPayeeChangedInd;
    protected String disbursementVoucherCheckNbr;
    protected Timestamp disbursementVoucherCheckDate;
    protected boolean disbVchrPayeeW9CompleteCode;
    protected String disbVchrPaymentMethodCode;
    protected boolean exceptionIndicator;
    protected boolean disbExcptAttachedIndicator;
    protected Date extractDate;
    protected Date paidDate;
    protected Date cancelDate;
    protected String disbVchrBankCode;
    protected String disbVchrPdpBankCode;

    protected boolean payeeAssigned = false;
    protected boolean editW9W8BENbox = false;
    protected boolean immediatePaymentIndicator = false;

    protected DocumentHeader financialDocument;
    protected DisbursementVoucherDocumentationLocation disbVchrDocumentationLoc;
    protected DisbursementVoucherNonEmployeeTravel dvNonEmployeeTravel;
    protected DisbursementVoucherNonResidentAlienTax dvNonResidentAlienTax;
    protected DisbursementVoucherPayeeDetail dvPayeeDetail;
    protected DisbursementVoucherPreConferenceDetail dvPreConferenceDetail;
    protected DisbursementVoucherWireTransfer dvWireTransfer;

    protected Bank bank;
    private OlePaymentMethod paymentMethod;
    protected Integer paymentMethodId;
    /**
     * Default no-arg constructor.
     */
    public DisbursementVoucherDocument() {
        super();
        exceptionIndicator = false;
        finDocNextRegistrantLineNbr = new Integer(1);
        dvNonEmployeeTravel = new DisbursementVoucherNonEmployeeTravel();
        dvNonResidentAlienTax = new DisbursementVoucherNonResidentAlienTax();
        dvPayeeDetail = new DisbursementVoucherPayeeDetail();
        dvPreConferenceDetail = new DisbursementVoucherPreConferenceDetail();
        dvWireTransfer = new DisbursementVoucherWireTransfer();
        disbVchrCheckTotalAmount = KualiDecimal.ZERO;
        bank = new Bank();
    }


    /**
     * @see org.kuali.ole.sys.document.AccountingDocumentBase#getPendingLedgerEntriesForSufficientFundsChecking()
     */
    @Override
    public List<GeneralLedgerPendingEntry> getPendingLedgerEntriesForSufficientFundsChecking() {
        List<GeneralLedgerPendingEntry> ples = new ArrayList<GeneralLedgerPendingEntry>();

        ConfigurationService kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        FlexibleOffsetAccountService flexibleOffsetAccountService = SpringContext.getBean(FlexibleOffsetAccountService.class);

        ObjectTypeService objectTypeService = SpringContext.getBean(ObjectTypeService.class);

        for (GeneralLedgerPendingEntry ple : this.getGeneralLedgerPendingEntries()) {
            List<String> expenseObjectTypes = objectTypeService.getExpenseObjectTypes(ple.getUniversityFiscalYear());
            if (expenseObjectTypes.contains(ple.getFinancialObjectTypeCode())) {
                // is an expense object type, keep checking
                ple.refreshNonUpdateableReferences();
                if (ple.getAccount().isPendingAcctSufficientFundsIndicator() && ple.getAccount().getAccountSufficientFundsCode().equals(OLEConstants.SF_TYPE_CASH_AT_ACCOUNT)) {
                    // is a cash account
                    if (flexibleOffsetAccountService.getByPrimaryIdIfEnabled(ple.getChartOfAccountsCode(), ple.getAccountNumber(), ple.getChart().getFinancialCashObjectCode()) == null && flexibleOffsetAccountService.getByPrimaryIdIfEnabled(ple.getChartOfAccountsCode(), ple.getAccountNumber(), ple.getChart().getFinAccountsPayableObjectCode()) == null) {
                        // does not have a flexible offset for cash or liability, set the object code to cash and add to list of
                        // PLEs to check for SF

                        ple = new GeneralLedgerPendingEntry(ple);
                        ple.setFinancialObjectCode(ple.getChart().getFinancialCashObjectCode());
                        ple.setTransactionDebitCreditCode(ple.getTransactionDebitCreditCode().equals(OLEConstants.GL_DEBIT_CODE) ? OLEConstants.GL_CREDIT_CODE : OLEConstants.GL_DEBIT_CODE);
                        ples.add(ple);
                    }

                }
                else {
                    // is not a cash account, process as normal
                    ples.add(ple);
                }
            }
        }

        return ples;
    }


    /**
     * Gets the finDocNextRegistrantLineNbr attribute.
     *
     * @return Returns the finDocNextRegistrantLineNbr
     */
    public Integer getFinDocNextRegistrantLineNbr() {
        return finDocNextRegistrantLineNbr;
    }


    /**
     * Sets the finDocNextRegistrantLineNbr attribute.
     *
     * @param finDocNextRegistrantLineNbr The finDocNextRegistrantLineNbr to set.
     */
    public void setFinDocNextRegistrantLineNbr(Integer finDocNextRegistrantLineNbr) {
        this.finDocNextRegistrantLineNbr = finDocNextRegistrantLineNbr;
    }

    /**
     * Gets the disbVchrContactPersonName attribute.
     *
     * @return Returns the disbVchrContactPersonName
     */
    public String getDisbVchrContactPersonName() {
        return disbVchrContactPersonName;
    }


    /**
     * Sets the disbVchrContactPersonName attribute.
     *
     * @param disbVchrContactPersonName The disbVchrContactPersonName to set.
     */
    public void setDisbVchrContactPersonName(String disbVchrContactPersonName) {
        this.disbVchrContactPersonName = disbVchrContactPersonName;
    }

    /**
     * Gets the disbVchrContactPhoneNumber attribute.
     *
     * @return Returns the disbVchrContactPhoneNumber
     */
    public String getDisbVchrContactPhoneNumber() {
        return disbVchrContactPhoneNumber;
    }


    /**
     * Sets the disbVchrContactPhoneNumber attribute.
     *
     * @param disbVchrContactPhoneNumber The disbVchrContactPhoneNumber to set.
     */
    public void setDisbVchrContactPhoneNumber(String disbVchrContactPhoneNumber) {
        this.disbVchrContactPhoneNumber = disbVchrContactPhoneNumber;
    }

    /**
     * Gets the disbVchrContactEmailId attribute.
     *
     * @return Returns the disbVchrContactEmailId
     */
    public String getDisbVchrContactEmailId() {
        return disbVchrContactEmailId;
    }


    /**
     * Sets the disbVchrContactEmailId attribute.
     *
     * @param disbVchrContactEmailId The disbVchrContactEmailId to set.
     */
    public void setDisbVchrContactEmailId(String disbVchrContactEmailId) {
        this.disbVchrContactEmailId = disbVchrContactEmailId;
    }

    /**
     * Gets the disbursementVoucherDueDate attribute.
     *
     * @return Returns the disbursementVoucherDueDate
     */
    public Date getDisbursementVoucherDueDate() {
        return disbursementVoucherDueDate;
    }


    /**
     * Sets the disbursementVoucherDueDate attribute.
     *
     * @param disbursementVoucherDueDate The disbursementVoucherDueDate to set.
     */
    public void setDisbursementVoucherDueDate(Date disbursementVoucherDueDate) {
        this.disbursementVoucherDueDate = disbursementVoucherDueDate;
    }

    /**
     * Gets the disbVchrAttachmentCode attribute.
     *
     * @return Returns the disbVchrAttachmentCode
     */
    public boolean isDisbVchrAttachmentCode() {
        return disbVchrAttachmentCode;
    }


    /**
     * Sets the disbVchrAttachmentCode attribute.
     *
     * @param disbVchrAttachmentCode The disbVchrAttachmentCode to set.
     */
    public void setDisbVchrAttachmentCode(boolean disbVchrAttachmentCode) {
        this.disbVchrAttachmentCode = disbVchrAttachmentCode;
    }

    /**
     * Gets the disbVchrSpecialHandlingCode attribute.
     *
     * @return Returns the disbVchrSpecialHandlingCode
     */
    public boolean isDisbVchrSpecialHandlingCode() {
        return disbVchrSpecialHandlingCode;
    }


    /**
     * Sets the disbVchrSpecialHandlingCode attribute.
     *
     * @param disbVchrSpecialHandlingCode The disbVchrSpecialHandlingCode to set.
     */
    public void setDisbVchrSpecialHandlingCode(boolean disbVchrSpecialHandlingCode) {
        this.disbVchrSpecialHandlingCode = disbVchrSpecialHandlingCode;
    }

    /**
     * Gets the disbVchrCheckTotalAmount attribute.
     *
     * @return Returns the disbVchrCheckTotalAmount
     */
    public KualiDecimal getDisbVchrCheckTotalAmount() {
        return disbVchrCheckTotalAmount;
    }


    /**
     * Sets the disbVchrCheckTotalAmount attribute.
     *
     * @param disbVchrCheckTotalAmount The disbVchrCheckTotalAmount to set.
     */
    public void setDisbVchrCheckTotalAmount(KualiDecimal disbVchrCheckTotalAmount) {
        if (disbVchrCheckTotalAmount != null) {
            this.disbVchrCheckTotalAmount = disbVchrCheckTotalAmount;
        }
    }

    /**
     * Gets the disbVchrForeignCurrencyInd attribute.
     *
     * @return Returns the disbVchrForeignCurrencyInd
     */
    public boolean isDisbVchrForeignCurrencyInd() {
        return disbVchrForeignCurrencyInd;
    }


    /**
     * Sets the disbVchrForeignCurrencyInd attribute.
     *
     * @param disbVchrForeignCurrencyInd The disbVchrForeignCurrencyInd to set.
     */
    public void setDisbVchrForeignCurrencyInd(boolean disbVchrForeignCurrencyInd) {
        this.disbVchrForeignCurrencyInd = disbVchrForeignCurrencyInd;
    }

    /**
     * Gets the disbursementVoucherDocumentationLocationCode attribute.
     *
     * @return Returns the disbursementVoucherDocumentationLocationCode
     */
    public String getDisbursementVoucherDocumentationLocationCode() {
        return disbursementVoucherDocumentationLocationCode;
    }


    /**
     * Sets the disbursementVoucherDocumentationLocationCode attribute.
     *
     * @param disbursementVoucherDocumentationLocationCode The disbursementVoucherDocumentationLocationCode to set.
     */
    public void setDisbursementVoucherDocumentationLocationCode(String disbursementVoucherDocumentationLocationCode) {
        this.disbursementVoucherDocumentationLocationCode = disbursementVoucherDocumentationLocationCode;
    }

    /**
     * Gets the disbVchrCheckStubText attribute.
     *
     * @return Returns the disbVchrCheckStubText
     */
    public String getDisbVchrCheckStubText() {
        return disbVchrCheckStubText;
    }


    /**
     * Sets the disbVchrCheckStubText attribute.
     *
     * @param disbVchrCheckStubText The disbVchrCheckStubText to set.
     */
    public void setDisbVchrCheckStubText(String disbVchrCheckStubText) {
        this.disbVchrCheckStubText = disbVchrCheckStubText;
    }

    /**
     * Gets the dvCheckStubOverflowCode attribute.
     *
     * @return Returns the dvCheckStubOverflowCode
     */
    public boolean getDvCheckStubOverflowCode() {
        return dvCheckStubOverflowCode;
    }


    /**
     * Sets the dvCheckStubOverflowCode attribute.
     *
     * @param dvCheckStubOverflowCode The dvCheckStubOverflowCode to set.
     */
    public void setDvCheckStubOverflowCode(boolean dvCheckStubOverflowCode) {
        this.dvCheckStubOverflowCode = dvCheckStubOverflowCode;
    }

    /**
     * Gets the campusCode attribute.
     *
     * @return Returns the campusCode
     */
    public String getCampusCode() {
        return campusCode;
    }


    /**
     * Sets the campusCode attribute.
     *
     * @param campusCode The campusCode to set.
     */
    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    /**
     * Gets the disbVchrPayeeTaxControlCode attribute.
     *
     * @return Returns the disbVchrPayeeTaxControlCode
     */
    public String getDisbVchrPayeeTaxControlCode() {
        return disbVchrPayeeTaxControlCode;
    }


    /**
     * Sets the disbVchrPayeeTaxControlCode attribute.
     *
     * @param disbVchrPayeeTaxControlCode The disbVchrPayeeTaxControlCode to set.
     */
    public void setDisbVchrPayeeTaxControlCode(String disbVchrPayeeTaxControlCode) {
        this.disbVchrPayeeTaxControlCode = disbVchrPayeeTaxControlCode;
    }

    /**
     * Gets the disbVchrPayeeChangedInd attribute.
     *
     * @return Returns the disbVchrPayeeChangedInd
     */
    public boolean isDisbVchrPayeeChangedInd() {
        return disbVchrPayeeChangedInd;
    }


    /**
     * Sets the disbVchrPayeeChangedInd attribute.
     *
     * @param disbVchrPayeeChangedInd The disbVchrPayeeChangedInd to set.
     */
    public void setDisbVchrPayeeChangedInd(boolean disbVchrPayeeChangedInd) {
        this.disbVchrPayeeChangedInd = disbVchrPayeeChangedInd;
    }

    /**
     * Gets the disbursementVoucherCheckNbr attribute.
     *
     * @return Returns the disbursementVoucherCheckNbr
     */
    public String getDisbursementVoucherCheckNbr() {
        return disbursementVoucherCheckNbr;
    }


    /**
     * Sets the disbursementVoucherCheckNbr attribute.
     *
     * @param disbursementVoucherCheckNbr The disbursementVoucherCheckNbr to set.
     */
    public void setDisbursementVoucherCheckNbr(String disbursementVoucherCheckNbr) {
        this.disbursementVoucherCheckNbr = disbursementVoucherCheckNbr;
    }

    /**
     * Gets the disbursementVoucherCheckDate attribute.
     *
     * @return Returns the disbursementVoucherCheckDate
     */
    public Timestamp getDisbursementVoucherCheckDate() {
        return disbursementVoucherCheckDate;
    }


    /**
     * Sets the disbursementVoucherCheckDate attribute.
     *
     * @param disbursementVoucherCheckDate The disbursementVoucherCheckDate to set.
     */
    public void setDisbursementVoucherCheckDate(Timestamp disbursementVoucherCheckDate) {
        this.disbursementVoucherCheckDate = disbursementVoucherCheckDate;
    }

    /**
     * Gets the disbVchrPayeeW9CompleteCode attribute.
     *
     * @return Returns the disbVchrPayeeW9CompleteCode
     */
    public boolean getDisbVchrPayeeW9CompleteCode() {
        return disbVchrPayeeW9CompleteCode;
    }


    /**
     * Sets the disbVchrPayeeW9CompleteCode attribute.
     *
     * @param disbVchrPayeeW9CompleteCode The disbVchrPayeeW9CompleteCode to set.
     */
    public void setDisbVchrPayeeW9CompleteCode(boolean disbVchrPayeeW9CompleteCode) {
        this.disbVchrPayeeW9CompleteCode = disbVchrPayeeW9CompleteCode;
    }

    /**
     * Gets the disbVchrPaymentMethodCode attribute.
     *
     * @return Returns the disbVchrPaymentMethodCode
     */
    public String getDisbVchrPaymentMethodCode() {
        return disbVchrPaymentMethodCode;
    }


    /**
     * Sets the disbVchrPaymentMethodCode attribute.
     *
     * @param disbVchrPaymentMethodCode The disbVchrPaymentMethodCode to set.
     */
    public void setDisbVchrPaymentMethodCode(String disbVchrPaymentMethodCode) {
        this.disbVchrPaymentMethodCode = disbVchrPaymentMethodCode;
    }

    /**
     * Gets the financialDocument attribute.
     *
     * @return Returns the financialDocument
     */
    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }


    /**
     * Sets the financialDocument attribute.
     *
     * @param financialDocument The financialDocument to set.
     * @deprecated
     */
    @Deprecated
    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    /**
     * Gets the disbVchrDocumentationLoc attribute.
     *
     * @return Returns the disbVchrDocumentationLoc
     */
    public DisbursementVoucherDocumentationLocation getDisbVchrDocumentationLoc() {
        return disbVchrDocumentationLoc;
    }


    /**
     * Sets the disbVchrDocumentationLoc attribute.
     *
     * @param disbVchrDocumentationLoc The disbVchrDocumentationLoc to set.
     * @deprecated
     */
    @Deprecated
    public void setDisbVchrDocumentationLoc(DisbursementVoucherDocumentationLocation disbVchrDocumentationLoc) {
        this.disbVchrDocumentationLoc = disbVchrDocumentationLoc;
    }


    /**
     * @return Returns the dvNonEmployeeTravel.
     */
    public DisbursementVoucherNonEmployeeTravel getDvNonEmployeeTravel() {
        return dvNonEmployeeTravel;
    }

    /**
     * @param dvNonEmployeeTravel The dvNonEmployeeTravel to set.
     */
    public void setDvNonEmployeeTravel(DisbursementVoucherNonEmployeeTravel dvNonEmployeeTravel) {
        this.dvNonEmployeeTravel = dvNonEmployeeTravel;
    }

    /**
     * @return Returns the dvNonResidentAlienTax.
     */
    public DisbursementVoucherNonResidentAlienTax getDvNonResidentAlienTax() {
        return dvNonResidentAlienTax;
    }

    /**
     * @param dvNonResidentAlienTax The dvNonResidentAlienTax to set.
     */
    public void setDvNonResidentAlienTax(DisbursementVoucherNonResidentAlienTax dvNonResidentAlienTax) {
        this.dvNonResidentAlienTax = dvNonResidentAlienTax;
    }

    /**
     * @return Returns the dvPayeeDetail.
     */
    public DisbursementVoucherPayeeDetail getDvPayeeDetail() {
        return dvPayeeDetail;
    }

    /**
     * @param dvPayeeDetail The dvPayeeDetail to set.
     */
    public void setDvPayeeDetail(DisbursementVoucherPayeeDetail dvPayeeDetail) {
        this.dvPayeeDetail = dvPayeeDetail;
    }

    /**
     * @return Returns the dvPreConferenceDetail.
     */
    public DisbursementVoucherPreConferenceDetail getDvPreConferenceDetail() {
        return dvPreConferenceDetail;
    }

    /**
     * @param dvPreConferenceDetail The dvPreConferenceDetail to set.
     */
    public void setDvPreConferenceDetail(DisbursementVoucherPreConferenceDetail dvPreConferenceDetail) {
        this.dvPreConferenceDetail = dvPreConferenceDetail;
    }

    /**
     * @return Returns the dvWireTransfer.
     */
    public DisbursementVoucherWireTransfer getDvWireTransfer() {
        return dvWireTransfer;
    }

    /**
     * @param dvWireTransfer The dvWireTransfer to set.
     */
    public void setDvWireTransfer(DisbursementVoucherWireTransfer dvWireTransfer) {
        this.dvWireTransfer = dvWireTransfer;
    }

    /**
     * @return Returns the exceptionIndicator.
     */
    public boolean isExceptionIndicator() {
        return exceptionIndicator;
    }

    /**
     * @param exceptionIndicator The exceptionIndicator to set.
     */
    public void setExceptionIndicator(boolean exceptionIndicator) {
        this.exceptionIndicator = exceptionIndicator;
    }

    /**
     * Gets the cancelDate attribute.
     *
     * @return Returns the cancelDate.
     */
    public Date getCancelDate() {
        return cancelDate;
    }

    /**
     * Sets the cancelDate attribute value.
     *
     * @param cancelDate The cancelDate to set.
     */
    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    /**
     * Gets the extractDate attribute.
     *
     * @return Returns the extractDate.
     */
    public Date getExtractDate() {
        return extractDate;
    }

    /**
     * Sets the extractDate attribute value.
     *
     * @param extractDate The extractDate to set.
     */
    public void setExtractDate(Date extractDate) {
        this.extractDate = extractDate;
    }

    /**
     * Gets the paidDate attribute.
     *
     * @return Returns the paidDate.
     */
    public Date getPaidDate() {
        return paidDate;
    }

    /**
     * Sets the paidDate attribute value.
     *
     * @param paidDate The paidDate to set.
     */
    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    /**
     * Based on which pdp dates are present (extract, paid, canceled), determines a String for the status
     *
     * @return a String representation of the status
     */
    public String getDisbursementVoucherPdpStatus() {
        if (cancelDate != null) {
            return "Canceled";
        }
        else if (paidDate != null) {
            return "Paid";
        }
        else if (extractDate != null) {
            return "Extracted";
        }
        else {
            return "Pre-Extraction";
        }
    }

    /**
     * Pretends to set the PDP status for this document
     *
     * @param status the status to pretend to set
     */
    public void setDisbursementVoucherPdpStatus(String status) {
        // don't do nothing, 'cause this ain't a real field
    }

    /**
     * Adds a dv pre-paid registrant line
     *
     * @param line
     */
    public void addDvPrePaidRegistrantLine(DisbursementVoucherPreConferenceRegistrant line) {
        line.setFinancialDocumentLineNumber(getFinDocNextRegistrantLineNbr());
        this.getDvPreConferenceDetail().getDvPreConferenceRegistrants().add(line);
        this.finDocNextRegistrantLineNbr = new Integer(getFinDocNextRegistrantLineNbr().intValue() + 1);
    }

    /**
     * Returns the name associated with the payment method code
     *
     * @return String
     */
    public String getDisbVchrPaymentMethodName() {
        return new PaymentMethodValuesFinder().getKeyLabel(disbVchrPaymentMethodCode);
    }

    /**
     * This method...
     *
     * @param method
     * @deprecated This method should not be used. There is no protected attribute to store this value. The associated getter
     *             retrieves the value remotely.
     */
    @Deprecated
    public void setDisbVchrPaymentMethodName(String method) {
    }

    /**
     * Returns the name associated with the documentation location name
     *
     * @return String
     */
    public String getDisbursementVoucherDocumentationLocationName() {
        return new DisbursementVoucherDocumentationLocationValuesFinder().getKeyLabel(disbursementVoucherDocumentationLocationCode);
    }

    /**
     * This method...
     *
     * @param name
     * @deprecated This method should not be used. There is no protected attribute to store this value. The associated getter
     *             retrieves the value remotely.
     */
    @Deprecated
    public void setDisbursementVoucherDocumentationLocationName(String name) {
    }


    /**
     * Gets the disbVchrBankCode attribute.
     *
     * @return Returns the disbVchrBankCode.
     */
    public String getDisbVchrBankCode() {
        return disbVchrBankCode;
    }


    /**
     * Sets the disbVchrBankCode attribute value.
     *
     * @param disbVchrBankCode The disbVchrBankCode to set.
     */
    public void setDisbVchrBankCode(String disbVchrBankCode) {
        this.disbVchrBankCode = disbVchrBankCode;
    }

    /**
     * Gets the bank attribute.
     *
     * @return Returns the bank.
     */
    public Bank getBank() {
        return bank;
    }


    /**
     * Sets the bank attribute value.
     *
     * @param bank The bank to set.
     */
    public void setBank(Bank bank) {
        this.bank = bank;
    }


    /**
     * Gets the paymentMethod attribute. 
     * @return Returns the paymentMethod.
     */
    public OlePaymentMethod getPaymentMethod() {
        return paymentMethod;
    }


    /**
     * Sets the paymentMethod attribute value.
     * @param paymentMethod The paymentMethod to set.
     */
    public void setPaymentMethod(OlePaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    /**
     * Gets the paymentMethodId attribute. 
     * @return Returns the paymentMethodId.
     */
    public Integer getPaymentMethodId() {
        return paymentMethodId;
    }


    /**
     * Sets the paymentMethodId attribute value.
     * @param paymentMethodId The paymentMethodId to set.
     */
    public void setPaymentMethodId(Integer paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }


    /**
     * Convenience method to set dv payee detail fields based on a given vendor.
     *
     * @param vendor
     */
    public void templateVendor(VendorDetail vendor, VendorAddress vendorAddress) {
        if (vendor == null) {
            return;
        }
        this.setPaymentMethodId(vendor.getPaymentMethodId());
        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(DisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(vendor.getVendorNumber());
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(vendor.getVendorName());

        this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());


        if (ObjectUtils.isNotNull(vendorAddress) && ObjectUtils.isNotNull(vendorAddress.getVendorAddressGeneratedIdentifier())) {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(vendorAddress.getVendorAddressGeneratedIdentifier().toString());
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(vendorAddress.getVendorLine1Address());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(vendorAddress.getVendorLine2Address());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(vendorAddress.getVendorCityName());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(vendorAddress.getVendorStateCode());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(vendorAddress.getVendorZipCode());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(vendorAddress.getVendorCountryCode());
        }
        else {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(StringUtils.EMPTY);
        }

        this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());
        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(VendorConstants.VendorTypes.SUBJECT_PAYMENT.equals(vendor.getVendorHeader().getVendorTypeCode()));
        this.getDvPayeeDetail().setDisbVchrEmployeePaidOutsidePayrollCode(getVendorService().isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier()));

        this.getDvPayeeDetail().setHasMultipleVendorAddresses(1 < vendor.getVendorAddresses().size());

        boolean w9AndW8Checked = false;
        if ( (ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW9ReceivedIndicator()) && vendor.getVendorHeader().getVendorW9ReceivedIndicator() == true) ||
             (ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW8BenReceivedIndicator()) && vendor.getVendorHeader().getVendorW8BenReceivedIndicator() == true) ) {

            w9AndW8Checked = true;
        }

    //    this.disbVchrPayeeW9CompleteCode = vendor.getVendorHeader().getVendorW8BenReceivedIndicator()  == null ? false : vendor.getVendorHeader().getVendorW8BenReceivedIndicator();
        this.disbVchrPayeeW9CompleteCode = w9AndW8Checked;

        Date vendorFederalWithholdingTaxBeginDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxBeginningDate();
        Date vendorFederalWithholdingTaxEndDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxEndDate();
        java.util.Date today = getDateTimeService().getCurrentDate();
        if ((vendorFederalWithholdingTaxBeginDate != null && vendorFederalWithholdingTaxBeginDate.before(today)) && (vendorFederalWithholdingTaxEndDate == null || vendorFederalWithholdingTaxEndDate.after(today))) {
            this.disbVchrPayeeTaxControlCode = DisbursementVoucherConstants.TAX_CONTROL_CODE_BEGIN_WITHHOLDING;
        }

        // if vendor is foreign, default alien payment code to true
        if (getVendorService().isVendorForeign(vendor.getVendorHeaderGeneratedIdentifier())) {
            getDvPayeeDetail().setDisbVchrAlienPaymentCode(true);
        }
    }

    /**
     * Convenience method to set dv payee detail fields based on a given Employee.
     *
     * @param employee
     */
    public void templateEmployee(Person employee) {
        if (employee == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(DisbursementVoucherConstants.DV_PAYEE_TYPE_EMPLOYEE);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getEmployeeId());
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(employee.getName());

        final ParameterService parameterService = this.getParameterService();
        //Commented for the jira issue OLE-3415
        /*if (parameterService.parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME) && parameterService.getParameterValueAsBoolean(DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME)) {*/
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(employee.getAddressLine1Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(employee.getAddressLine2Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(employee.getAddressCityUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(employee.getAddressStateProvinceCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(employee.getAddressPostalCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(employee.getAddressCountryCodeUnmasked());
            //Commented for the jira issue OLE-3415
        /*} else {
            final EntityAddress address = getNonDefaultAddress(employee);
            if (address != null) {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(address.getLine1Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(address.getLine2Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCityName(address.getCityUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode(address.getStateProvinceCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode(address.getPostalCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(address.getCountryCodeUnmasked());
            }
            else {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeCityName("");
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode("");
            }
        }*/

        //OLEMI-8935: When an employee is inactive, the Payment Type field on DV documents should display the message "Is this payee an employee" = No
        if (employee.isActive()) {
        this.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(true);
        } else {
            this.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(false);
        }

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the employee must be foreign
        for ( String externalIdentifierTypeCode : employee.getExternalIdentifiers().keySet() ) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(false);
            }
        }
        //Commented for the jira issue OLE-3415
        // Determine if employee is a research subject
       /* ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, this.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }*/

        this.disbVchrPayeeTaxControlCode = "";
        this.disbVchrPayeeW9CompleteCode = true;
    }

    /**
     * Finds the address for the given employee, matching the type in the OLE-FP / Disbursement Voucher/ DEFAULT_EMPLOYEE_ADDRESS_TYPE parameter,
     * to use as the address for the employee
     * @param employee the employee to find a non-default address for
     * @return the non-default address, or null if not found
     */
    protected EntityAddress getNonDefaultAddress(Person employee) {
        final String addressType = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherDocument.DEFAULT_EMPLOYEE_ADDRESS_TYPE_PARAMETER_NAME);
        final Entity entity = getIdentityManagementService().getEntityByPrincipalId(employee.getPrincipalId());
        if (entity != null) {
            final EntityTypeContactInfo entityEntityType = getPersonEntityEntityType(entity);
            if (entityEntityType != null) {
                final List<? extends EntityAddress> addresses = entityEntityType.getAddresses();

                return findAddressByType(addresses, addressType);
            }
        }
        return null;
    }

    /**
     * Someday this method will be in Rice.  But...'til it is...lazy loop through the entity entity types in the given
     * KimEntityInfo and return the one who has the type of "PERSON"
     * @param entityInfo the entity info to loop through entity entity types of
     * @return a found entity entity type or null if a PERSON entity entity type is not associated with the given KimEntityInfo record
     */
    protected EntityTypeContactInfo getPersonEntityEntityType(Entity entity) {
        final List<EntityTypeContactInfo> entityEntityTypes = entity.getEntityTypeContactInfos();
        int count = 0;
        EntityTypeContactInfo foundInfo = null;

        while (count < entityEntityTypes.size() && foundInfo == null) {
            if (entityEntityTypes.get(count).getEntityTypeCode().equals(KimConstants.EntityTypes.PERSON)) {
                foundInfo = entityEntityTypes.get(count);
            }
            count += 1;
        }

        return foundInfo;
    }

    /**
     * Given a List of KimEntityAddress and an address type, finds the address in the List with the given type (or null if no matching KimEntityAddress is found)
     * @param addresses the List of KimEntityAddress records to search
     * @param addressType the address type of the address to return
     * @return the found KimEntityAddress, or null if not found
     */
    protected EntityAddress findAddressByType(List<? extends EntityAddress> addresses, String addressType) {
        EntityAddress foundAddress = null;
        int count = 0;

        while (count < addresses.size() && foundAddress == null) {
            final EntityAddress currentAddress = addresses.get(count);
            if (currentAddress.isActive() && currentAddress.getAddressType().getCode().equals(addressType)) {
                foundAddress = currentAddress;
            }
            count += 1;
        }

        return foundAddress;
    }

    /**
     * @see org.kuali.rice.kns.document.Document#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        if (this instanceof AmountTotaling) {
            if (getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode().equals(OLEConstants.DocumentStatusCodes.ENROUTE)) {
                if (getParameterService().parameterExists(OleParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
                        && getParameterService().getParameterValueAsBoolean(OleParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
                    getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
                }
            } else {
                getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
            }
        }

        if (dvWireTransfer != null) {
            dvWireTransfer.setDocumentNumber(this.documentNumber);
        }

        if (dvNonResidentAlienTax != null) {
            dvNonResidentAlienTax.setDocumentNumber(this.documentNumber);
        }

        dvPayeeDetail.setDocumentNumber(this.documentNumber);

        if (dvNonEmployeeTravel != null) {
            dvNonEmployeeTravel.setDocumentNumber(this.documentNumber);
            dvNonEmployeeTravel.setTotalTravelAmount(dvNonEmployeeTravel.getTotalTravelAmount());
        }

        if (dvPreConferenceDetail != null) {
            dvPreConferenceDetail.setDocumentNumber(this.documentNumber);
            dvPreConferenceDetail.setDisbVchrConferenceTotalAmt(dvPreConferenceDetail.getDisbVchrConferenceTotalAmt());
        }

        if (shouldClearSpecialHandling()) {
            clearSpecialHandling();
        }
    }

    /**
     * Determines if the special handling fields should be cleared, based on whether the special handling has been turned off and whether the current node is CAMPUS
     * @return true if special handling should be cleared, false otherwise
     */
    protected boolean shouldClearSpecialHandling() {
        if (!isDisbVchrSpecialHandlingCode()) {
            // are we at the campus route node?
            List<RouteNodeInstance> currentNodes = getDocumentHeader().getWorkflowDocument().getCurrentRouteNodeInstances();
            return (currentNodes.contains(DisbursementVoucherConstants.RouteLevelNames.CAMPUS));
        }
        return false;
    }

    /**
     * Clears all set special handling fields
     */
    protected void clearSpecialHandling() {
        DisbursementVoucherPayeeDetail payeeDetail = getDvPayeeDetail();

        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingPersonName())) {
            payeeDetail.setDisbVchrSpecialHandlingPersonName(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingLine1Addr())) {
            payeeDetail.setDisbVchrSpecialHandlingLine1Addr(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingLine2Addr())) {
            payeeDetail.setDisbVchrSpecialHandlingLine2Addr(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingCityName())) {
            payeeDetail.setDisbVchrSpecialHandlingCityName(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingStateCode())) {
            payeeDetail.setDisbVchrSpecialHandlingStateCode(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingZipCode())) {
            payeeDetail.setDisbVchrSpecialHandlingZipCode(null);
        }
        if (!StringUtils.isBlank(payeeDetail.getDisbVchrSpecialHandlingCountryCode())) {
            payeeDetail.setDisbVchrSpecialHandlingCountryCode(null);
        }
    }

    /**
     * This method is overridden to populate some local variables that are not persisted to the database. These values need to be
     * computed and saved to the DV Payee Detail BO so they can be serialized to XML for routing. Some of the routing rules rely on
     * these variables.
     *
     * @see org.kuali.rice.kns.document.DocumentBase#populateDocumentForRouting()
     */
    @Override
    public void populateDocumentForRouting() {
        DisbursementVoucherPayeeDetail payeeDetail = getDvPayeeDetail();

        if (payeeDetail.isVendor()) {
            payeeDetail.setDisbVchrPayeeEmployeeCode(getVendorService().isVendorInstitutionEmployee(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
            payeeDetail.setDvPayeeSubjectPaymentCode(getVendorService().isSubjectPaymentVendor(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
        }
        else if (payeeDetail.isEmployee()) {
            //Commented for the jira issue OLE-3415
            // Determine if employee is a research subject
            /*ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, payeeDetail.getDisbVchrPaymentReasonCode());
            if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
                if (getParameterService().parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                    String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                    if (StringUtils.isNotBlank(researchPayLimit)) {
                        KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                        if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                            payeeDetail.setDvPayeeSubjectPaymentCode(true);
                        }
                    }
                }
            }*/
        }

        super.populateDocumentForRouting(); // Call last, serializes to XML
    }

    /**
     * Clears information that might have been entered for sub tables, but because of changes to the document is longer needed and
     * should not be persisted.
     */
    protected void cleanDocumentData() {
        // TODO: warren: this method ain't called!!! maybe this should be called by prepare for save above
        if (!DisbursementVoucherConstants.PAYMENT_METHOD_WIRE.equals(this.getDisbVchrPaymentMethodCode()) && !DisbursementVoucherConstants.PAYMENT_METHOD_DRAFT.equals(this.getDisbVchrPaymentMethodCode())) {
            getBusinessObjectService().delete(dvWireTransfer);
            dvWireTransfer = null;
        }

        if (!dvPayeeDetail.isDisbVchrAlienPaymentCode()) {
            getBusinessObjectService().delete(dvNonResidentAlienTax);
            dvNonResidentAlienTax = null;
        }

        DisbursementVoucherPaymentReasonService paymentReasonService = SpringContext.getBean(DisbursementVoucherPaymentReasonService.class);
        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        //Commented for the jira issue OLE-3415
        /*if (!paymentReasonService.isNonEmployeeTravelPaymentReason(paymentReasonCode)) {
            getBusinessObjectService().delete(dvNonEmployeeTravel);
            dvNonEmployeeTravel = null;
        }

        if (!paymentReasonService.isPrepaidTravelPaymentReason(paymentReasonCode)) {
            getBusinessObjectService().delete(dvPreConferenceDetail);
            dvPreConferenceDetail = null;
        }*/
    }

    /**
     * @see org.kuali.ole.sys.document.AccountingDocumentBase#toCopy()
     */
    @Override
    public void toCopy() throws WorkflowException {
        super.toCopy();
        initiateDocument();

        // clear fields
        setDisbVchrContactPhoneNumber(StringUtils.EMPTY);
        setDisbVchrContactEmailId(StringUtils.EMPTY);
        setDisbVchrPayeeTaxControlCode(StringUtils.EMPTY);

        // clear nra
        SpringContext.getBean(DisbursementVoucherTaxService.class).clearNRATaxLines(this);
        setDvNonResidentAlienTax(new DisbursementVoucherNonResidentAlienTax());

        // clear waive wire
        getDvWireTransfer().setDisbursementVoucherWireTransferFeeWaiverIndicator(false);

        // check vendor id number to see if still valid, if not, clear dvPayeeDetail; otherwise, use the current dvPayeeDetail as is
        if (!StringUtils.isBlank(getDvPayeeDetail().getDisbVchrPayeeIdNumber())) {
            VendorDetail vendorDetail = getVendorService().getVendorDetail(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger(), getDvPayeeDetail().getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            if (vendorDetail == null) {
                dvPayeeDetail = new DisbursementVoucherPayeeDetail();;
                getDvPayeeDetail().setDisbVchrPayeeIdNumber(StringUtils.EMPTY);
                KNSGlobalVariables.getMessageList().add(OLEKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED);
            }
        }

        // this copied DV has not been extracted
        this.extractDate = null;
        this.paidDate = null;
        this.cancelDate = null;
        getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(OLEConstants.DocumentStatusCodes.INITIATED);
    }

    /**
     * generic, shared logic used to initiate a dv document
     */
    public void initiateDocument() {
        PhoneNumberService phoneNumberService = SpringContext.getBean(PhoneNumberService.class);
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        setDisbVchrContactPersonName(currentUser.getName());

        if(!phoneNumberService.isDefaultFormatPhoneNumber(currentUser.getPhoneNumber())) {
            setDisbVchrContactPhoneNumber(phoneNumberService.formatNumberIfPossible(currentUser.getPhoneNumber()));
        }

        setDisbVchrContactEmailId(currentUser.getEmailAddress());
        ChartOrgHolder chartOrg = SpringContext.getBean(org.kuali.ole.sys.service.FinancialSystemUserService.class).getPrimaryOrganization(currentUser, OLEConstants.ParameterNamespaces.FINANCIAL);

        // Does a valid campus code exist for this person?  If so, simply grab
        // the campus code via the business object service.
        if (chartOrg != null && chartOrg.getOrganization() != null) {
            setCampusCode(chartOrg.getOrganization().getOrganizationPhysicalCampusCode());
        }
        // A valid campus code was not found; therefore, use the default affiliated
        // campus code.
        else {
            String affiliatedCampusCode = currentUser.getCampusCode();
            setCampusCode(affiliatedCampusCode);
        }

        // due date
        Calendar calendar = getDateTimeService().getCurrentCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        setDisbursementVoucherDueDate(new Date(calendar.getTimeInMillis()));

        // default doc location
        if (StringUtils.isBlank(getDisbursementVoucherDocumentationLocationCode())) {
            setDisbursementVoucherDocumentationLocationCode(getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.DEFAULT_DOC_LOCATION_PARM_NM));
        }

        // default bank code
        Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(this.getClass());
        if (defaultBank != null) {
            this.disbVchrBankCode = defaultBank.getBankCode();
            this.bank = defaultBank;
        }
    }

    /**
     * @see org.kuali.rice.kns.document.DocumentBase#buildListOfDeletionAwareLists()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();

        if (dvNonEmployeeTravel != null) {
            managedLists.add(dvNonEmployeeTravel.getDvNonEmployeeExpenses());
            managedLists.add(dvNonEmployeeTravel.getDvPrePaidEmployeeExpenses());
        }

        if (dvPreConferenceDetail != null) {
            managedLists.add(dvPreConferenceDetail.getDvPreConferenceRegistrants());
        }

        return managedLists;
    }

    /**
     * Returns check total.
     *
     * @see org.kuali.ole.sys.document.AccountingDocumentBase#getTotalDollarAmount()
     * @return KualiDecimal
     */
    @Override
    public KualiDecimal getTotalDollarAmount() {
        return this.getDisbVchrCheckTotalAmount();
    }

    /**
     * Returns true if accounting line debit
     *
     * @param financialDocument submitted accounting document
     * @param accountingLine accounting line in accounting document
     * @return true if document is debit
     * @see IsDebitUtils#isDebitConsideringNothingPositiveOnly(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)
     * @see org.kuali.rice.kns.rule.AccountingLineRule#isDebit(org.kuali.rice.kns.document.FinancialDocument,
     *      org.kuali.rice.kns.bo.AccountingLine)
     */
    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        // disallow error corrections
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        isDebitUtils.disallowErrorCorrectionDocumentCheck(this);

        if (getDvNonResidentAlienTax() != null && getDvNonResidentAlienTax().getFinancialDocumentAccountingLineText() != null && getDvNonResidentAlienTax().getFinancialDocumentAccountingLineText().contains(((AccountingLine) postable).getSequenceNumber().toString())) {
            return postable.getAmount().isPositive();
        }

        return isDebitUtils.isDebitConsideringNothingPositiveOnly(this, postable);
    }

    /**
     * Override to change the doc type based on payment method. This is needed to pick up different offset definitions.
     *
     * @param financialDocument submitted accounting document
     * @param accountingLine accounting line in submitted accounting document
     * @param explicitEntry explicit GLPE
     * @see org.kuali.module.financial.rules.FinancialDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(org.kuali.rice.kns.document.FinancialDocument,
     *      org.kuali.rice.kns.bo.AccountingLine, org.kuali.module.gl.bo.GeneralLedgerPendingEntry)
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail accountingLine, GeneralLedgerPendingEntry explicitEntry) {

        /* change document type based on payment method to pick up different offsets */
        if (DisbursementVoucherConstants.PAYMENT_METHOD_CHECK.equals(getDisbVchrPaymentMethodCode())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("changing doc type on pending entry " + explicitEntry.getTransactionLedgerEntrySequenceNumber() + " to " + DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
            }
            explicitEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("changing doc type on pending entry " + explicitEntry.getTransactionLedgerEntrySequenceNumber() + " to " + DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
            }
            explicitEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
        }
    }

    /**
     * Return true if GLPE's are generated successfully (i.e. there are either 0 GLPE's or 1 GLPE in disbursement voucher document)
     *
     * @param financialDocument submitted financial document
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @return true if GLPE's are generated successfully
     * @see org.kuali.rice.kns.rule.GenerateGeneralLedgerDocumentPendingEntriesRule#processGenerateDocumentGeneralLedgerPendingEntries(org.kuali.rice.kns.document.FinancialDocument,org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        if (getGeneralLedgerPendingEntries() == null || getGeneralLedgerPendingEntries().size() < 2) {
            LOG.warn("No gl entries for accounting lines.");
            return true;
        }

        /*
         * only generate additional charge entries for payment method wire charge, and if the fee has not been waived
         */
        if (DisbursementVoucherConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) && !getDvWireTransfer().isDisbursementVoucherWireTransferFeeWaiverIndicator()) {
            LOG.debug("generating wire charge gl pending entries.");

            // retrieve wire charge
            WireCharge wireCharge = retrieveWireCharge();

            // generate debits
            GeneralLedgerPendingEntry chargeEntry = processWireChargeDebitEntries(sequenceHelper, wireCharge);

            // generate credits
            processWireChargeCreditEntries(sequenceHelper, wireCharge, chargeEntry);
        }

        // for wire or drafts generate bank offset entry (if enabled), for ACH and checks offset will be generated by PDP
        if (DisbursementVoucherConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) || DisbursementVoucherConstants.PAYMENT_METHOD_DRAFT.equals(getDisbVchrPaymentMethodCode())) {
            generateDocumentBankOffsetEntries(sequenceHelper);
        }

        return true;
    }

    /**
     * Builds an explicit and offset for the wire charge debit. The account associated with the first accounting is used for the
     * debit. The explicit and offset entries for the first accounting line and copied and customized for the wire charge.
     *
     * @param dvDocument submitted disbursement voucher document
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @param wireCharge wireCharge object from current fiscal year
     * @return GeneralLedgerPendingEntry generated wire charge debit
     */
    protected GeneralLedgerPendingEntry processWireChargeDebitEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, WireCharge wireCharge) {
        LOG.debug("processWireChargeDebitEntries started");

        // grab the explicit entry for the first accounting line and adjust for wire charge entry
        GeneralLedgerPendingEntry explicitEntry = new GeneralLedgerPendingEntry(getGeneralLedgerPendingEntry(0));
        explicitEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceHelper.getSequenceCounter()));
        explicitEntry.setFinancialObjectCode(wireCharge.getExpenseFinancialObjectCode());
        explicitEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
        explicitEntry.setTransactionDebitCreditCode(GL_DEBIT_CODE);

        String objectTypeCode = SpringContext.getBean(OptionsService.class).getCurrentYearOptions().getFinObjTypeExpenditureexpCd();
        explicitEntry.setFinancialObjectTypeCode(objectTypeCode);

        String originationCode = SpringContext.getBean(HomeOriginationService.class).getHomeOrigination().getFinSystemHomeOriginationCode();
        explicitEntry.setFinancialSystemOriginationCode(originationCode);

        if (OLEConstants.COUNTRY_CODE_UNITED_STATES.equals(getDvWireTransfer().getDisbVchrBankCountryCode())) {
            explicitEntry.setTransactionLedgerEntryAmount(wireCharge.getDomesticChargeAmt());
        }
        else {
            explicitEntry.setTransactionLedgerEntryAmount(wireCharge.getForeignChargeAmt());
        }

        explicitEntry.setTransactionLedgerEntryDescription("Automatic debit for wire transfer fee");

        addPendingEntry(explicitEntry);
        sequenceHelper.increment();

        // handle the offset entry
        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitEntry);
        GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), explicitEntry, sequenceHelper, offsetEntry);

        addPendingEntry(offsetEntry);
        sequenceHelper.increment();

        return explicitEntry;
    }

    /**
     * Builds an explicit and offset for the wire charge credit. The account and income object code found in the wire charge table
     * is used for the entry.
     *
     * @param dvDocument submitted disbursement voucher document
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @param chargeEntry GLPE charge
     * @param wireCharge wireCharge object from current fiscal year
     */
    protected void processWireChargeCreditEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, WireCharge wireCharge, GeneralLedgerPendingEntry chargeEntry) {
        LOG.debug("processWireChargeCreditEntries started");

        // copy the charge entry and adjust for credit
        GeneralLedgerPendingEntry explicitEntry = new GeneralLedgerPendingEntry(chargeEntry);
        explicitEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceHelper.getSequenceCounter()));
        explicitEntry.setChartOfAccountsCode(wireCharge.getChartOfAccountsCode());
        explicitEntry.setAccountNumber(wireCharge.getAccountNumber());
        explicitEntry.setFinancialObjectCode(wireCharge.getIncomeFinancialObjectCode());

        // retrieve object type
        ObjectCode objectCode = wireCharge.getIncomeFinancialObject();
        explicitEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());

        explicitEntry.setTransactionDebitCreditCode(GL_CREDIT_CODE);

        explicitEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
        explicitEntry.setSubAccountNumber(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber());
        explicitEntry.setProjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode());

        explicitEntry.setTransactionLedgerEntryDescription("Automatic credit for wire transfer fee");

        addPendingEntry(explicitEntry);
        sequenceHelper.increment();

        // handle the offset entry
        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitEntry);
        GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), explicitEntry, sequenceHelper, offsetEntry);

        addPendingEntry(offsetEntry);
        sequenceHelper.increment();
    }

    /**
     * If bank specification is enabled generates bank offsetting entries for the document amount
     *
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @param paymentMethodCode the payment method of this DV
     */
    public boolean generateDocumentBankOffsetEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        boolean success = true;

        if (!SpringContext.getBean(BankService.class).isBankSpecificationEnabled()) {
            return success;
        }

        this.refreshReferenceObject(OLEPropertyConstants.BANK);

        GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);

        final KualiDecimal bankOffsetAmount = glpeService.getOffsetToCashAmount(this).negated();
        GeneralLedgerPendingEntry bankOffsetEntry = new GeneralLedgerPendingEntry();
        success &= glpeService.populateBankOffsetGeneralLedgerPendingEntry(getBank(), bankOffsetAmount, this, getPostingYear(), sequenceHelper, bankOffsetEntry, KRADConstants.DOCUMENT_PROPERTY_NAME + "." + OLEPropertyConstants.DISB_VCHR_BANK_CODE);

        if (success) {
            AccountingDocumentRuleHelperService accountingDocumentRuleUtil = SpringContext.getBean(AccountingDocumentRuleHelperService.class);
            bankOffsetEntry.setTransactionLedgerEntryDescription(accountingDocumentRuleUtil.formatProperty(OLEKeyConstants.Bank.DESCRIPTION_GLPE_BANK_OFFSET));
            bankOffsetEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
            addPendingEntry(bankOffsetEntry);
            sequenceHelper.increment();

            GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(bankOffsetEntry);
            success &= glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), bankOffsetEntry, sequenceHelper, offsetEntry);
            bankOffsetEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
            addPendingEntry(offsetEntry);
            sequenceHelper.increment();
        }

        return success;
    }

    /**
     * Retrieves the wire transfer information for the current fiscal year.
     *
     * @return <code>WireCharge</code>
     */
    protected WireCharge retrieveWireCharge() {
        WireCharge wireCharge = new WireCharge();
        wireCharge.setUniversityFiscalYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        wireCharge = (WireCharge) SpringContext.getBean(BusinessObjectService.class).retrieve(wireCharge);
        if (wireCharge == null) {
            LOG.error("Wire charge information not found for current fiscal year.");
            throw new RuntimeException("Wire charge information not found for current fiscal year.");
        }

        return wireCharge;
    }


    /**
     * Gets the payeeAssigned attribute. This method returns a flag that is used to indicate if the payee type and value has been
     * set on the DV. This value is used to determine the correct page that should be loaded by the DV flow.
     *
     * @return Returns the payeeAssigned.
     */
    public boolean isPayeeAssigned() {
        // If value is false, check state of document. We should assume payee is assigned if document has been saved.
        // Otherwise, value will be set during creation process.
        if (!payeeAssigned) {
            payeeAssigned = !this.getDocumentHeader().getWorkflowDocument().isInitiated();
        }
        return payeeAssigned;
    }


    /**
     * Sets the payeeAssigned attribute value.
     *
     * @param payeeAssigned The payeeAssigned to set.
     */
    public void setPayeeAssigned(boolean payeeAssigned) {
        this.payeeAssigned = payeeAssigned;
    }

    /**
     * Gets the editW9W8BENbox attribute. This method returns a flag that is used to indicate if the W9/W8BEN check box can be edited
     * by the initiator on the DV.
     *
     * @return Returns the editW9W8BENbox.
     */
    public boolean isEditW9W8BENbox() {
        String initiatorPrincipalID = this.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        if (GlobalVariables.getUserSession().getPrincipalId().equals(initiatorPrincipalID)) {
            editW9W8BENbox = true;
        }
        return editW9W8BENbox;
    }

    /**
     * Sets the editW9W8BENbox attribute value.
     *
     * @param editW9W8BENbox The editW9W8BENbox to set.
     */
    public void setEditW9W8BENbox(boolean editW9W8BENbox) {
        this.editW9W8BENbox = editW9W8BENbox;
    }

    /**
     * Gets the disbVchrPdpBankCode attribute.
     *
     * @return Returns the disbVchrPdpBankCode.
     */
    public String getDisbVchrPdpBankCode() {
        return disbVchrPdpBankCode;
    }

    /**
     * Sets the disbVchrPdpBankCode attribute value.
     *
     * @param disbVchrPdpBankCode The disbVchrPDPBankCode to set.
     */
    public void setDisbVchrPdpBankCode(String disbVchrPdpBankCode) {
        this.disbVchrPdpBankCode = disbVchrPdpBankCode;
    }

    /**
     * @return whether this document should be paid out immediately from PDP
     */
    public boolean isImmediatePaymentIndicator() {
        return immediatePaymentIndicator;
    }

    /**
     * Sets whether this document should be paid out as quickly as possible from PDP
     * @param immediatePaymentIndicator true if this should be immediately disbursed; false otherwise
     */
    public void setImmediatePaymentIndicator(boolean immediatePaymentIndicator) {
        this.immediatePaymentIndicator = immediatePaymentIndicator;
    }

    /**
     * @see org.kuali.rice.kns.document.DocumentBase#getDocumentTitle()
     */
    @Override
    public String getDocumentTitle() {
        String documentTitle = super.getDocumentTitle();
        return this.buildDocumentTitle(documentTitle);
    }

    /**
     * build document title based on the properties of current document
     *
     * @param the default document title
     * @return the combine information of the given title and additional payment indicators
     */
    protected String buildDocumentTitle(String title) {
        DisbursementVoucherPayeeDetail payee = getDvPayeeDetail();
        if(payee == null) {
            return title;
        }

        Object[] indicators = new String[2];
        indicators[0] = payee.isEmployee() ? AdHocPaymentIndicator.EMPLOYEE_PAYEE : AdHocPaymentIndicator.OTHER;
        indicators[1] = payee.isDisbVchrAlienPaymentCode() ? AdHocPaymentIndicator.ALIEN_PAYEE : AdHocPaymentIndicator.OTHER;

//        DisbursementVoucherPaymentReasonService paymentReasonService = SpringContext.getBean(DisbursementVoucherPaymentReasonService.class);
        //Commented for the jira OLE-3415
        /*boolean isTaxReviewRequired = paymentReasonService.isTaxReviewRequired(payee.getDisbVchrPaymentReasonCode());
        indicators[2] = isTaxReviewRequired ? AdHocPaymentIndicator.PAYMENT_REASON_REQUIRING_TAX_REVIEW : AdHocPaymentIndicator.OTHER;*/

        for(Object indicator : indicators) {
            if(!AdHocPaymentIndicator.OTHER.equals(indicator)) {
                String titlePattern = title + " [{0}:{1}]";
                return MessageFormat.format(titlePattern, indicators);
            }
        }

        return title;
    }


    /**
     * Provides answers to the following splits: PayeeIsPurchaseOrderVendor RequiresTaxReview RequiresTravelReview
     *
     * @see org.kuali.ole.sys.document.FinancialSystemTransactionalDocumentBase#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(DisbursementVoucherDocument.PAYEE_IS_PURCHASE_ORDER_VENDOR_SPLIT)) {
            return isPayeePurchaseOrderVendor();
        }
        if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT)) {
            return isTaxReviewRequired();
        }
        if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT)) {
            return true;
        }
        if (nodeName.equals(DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES)) {
            return isSeparationOfDutiesReviewRequired();
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
    }


    protected boolean isSeparationOfDutiesReviewRequired() {
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        boolean sepOfDutiesRequired = parameterService.getParameterValueAsBoolean(OLEConstants.CoreModuleNamespaces.FINANCIAL, DISBURSEMENT_VOUCHER_TYPE, SEPARATION_OF_DUTIES_PARAMETER_NAME);

        if (sepOfDutiesRequired) {
            try {
                Set<Person> priorApprovers = getAllPriorApprovers();
                // The payee cannot be the only approver
                String payeeEmployeeId = this.getDvPayeeDetail().getDisbVchrPayeeIdNumber();
                Person payee = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeEmployeeId);
                // If there is only one approver, and that approver is also the payee, then Separation of Duties is required.
                boolean priorApproverIsPayee = priorApprovers.contains(payee);
                boolean onlyOneApprover = (priorApprovers.size() == 1);
                if ( priorApproverIsPayee && onlyOneApprover) {
                    return true;
                }

                // if there are more than 0 prior approvers which means there had been at least another approver than the current approver
                // then no need for separation of duties
                if (priorApprovers.size() > 0) {
                    return false;
                }
            }catch (WorkflowException we) {
                LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
            }
        }
            return false;
    }

    public Set<Person> getAllPriorApprovers() throws WorkflowException {
        PersonService personService = KimApiServiceLocator.getPersonService();
         List<ActionTaken> actionsTaken = getDocumentHeader().getWorkflowDocument().getActionsTaken();
        Set<String> principalIds = new HashSet<String>();
        Set<Person> persons = new HashSet<Person>();

        for (ActionTaken actionTaken : actionsTaken) {
            if (KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(actionTaken.getActionTaken())) {
                String principalId = actionTaken.getPrincipalId();
                if (!principalIds.contains(principalId)) {
                    principalIds.add(principalId);
                    persons.add(personService.getPerson(principalId));
                }
            }
        }
        return persons;
    }

    /**
     * @return true if the payee is a purchase order vendor and therefore should receive vendor review, false otherwise
     */
    protected boolean isPayeePurchaseOrderVendor() {
       /* if (!this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode().equals(DisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR)) {
            return false;
        }*/

        VendorDetail vendor = getVendorService().getByVendorNumber(this.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
        if (vendor == null) {
            return false;
        }

        vendor.refreshReferenceObject("vendorHeader");
        return vendor.getVendorHeader().getVendorTypeCode().equals(DisbursementVoucherDocument.PURCHASE_ORDER_VENDOR_TYPE);
    }

    /**
     * Tax review is required under the following circumstances: the payee was an employee the payee was a non-resident alien vendor
     * the tax control code = "B" or "H" the payment reason code was "D" the payment reason code was "M" and the campus was listed
     * in the CAMPUSES_TAXED_FOR_MOVING_REIMBURSEMENTS_PARAMETER_NAME parameter
     *
     * @return true if any of the above conditions exist and this document should receive tax review, false otherwise
     */
    protected boolean isTaxReviewRequired() {
        if (isPayeePurchaseOrderVendorHasWithholding()) {
            return true;
        }

        //String payeeTypeCode = this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode();
       /* if (payeeTypeCode.equals(DisbursementVoucherConstants.DV_PAYEE_TYPE_EMPLOYEE)) {
            return false;
        } else if (payeeTypeCode.equals(DisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR)) {
            if(vendorService.isVendorInstitutionEmployee(this.getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())){
            return true;
        }
        }

        if (payeeTypeCode.equals(DisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR) && this.getVendorService().isVendorForeign(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())) {
            return true;
        }*/

        String taxControlCode = this.getDisbVchrPayeeTaxControlCode();
        if (StringUtils.equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_BACKUP_HOLDING) || StringUtils.equals(taxControlCode,DisbursementVoucherDocument.TAX_CONTROL_HOLD_PAYMENTS)) {
            return true;
        }

        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        //Commented for the jira issue OLE-3415
        /*if (this.getDvPymentReasonService().isDecedentCompensationPaymentReason(paymentReasonCode)) {
            return true;
        }

        if (this.getDvPymentReasonService().isMovingPaymentReason(paymentReasonCode)) {
                && taxedCampusForMovingReimbursements()) {
            return true;
        }
        if (SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(this.getClass(), DisbursementVoucherDocument.PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME, paymentReasonCode).evaluationSucceeds()) {
            return true;
        }*/

        return false;
    }

    /**
     * @return true if the payee is a vendor and has withholding dates therefore should receive tax review, false otherwise
     */
    protected boolean isPayeePurchaseOrderVendorHasWithholding() {
        /*if (!this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode().equals(DisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR)) {
            return false;
        }
*/
        VendorDetail vendor = getVendorService().getByVendorNumber(this.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
        if (vendor == null) {
            return false;
        }

        vendor.refreshReferenceObject("vendorHeader");
        return (vendor.getVendorHeader().getVendorFederalWithholdingTaxBeginningDate()!= null || vendor.getVendorHeader().getVendorFederalWithholdingTaxEndDate()!= null);
    }


    /**
     * Determines if the campus this DV is related to is taxed (and should get tax review routing) for moving reimbursements
     *
     * @return true if the campus is taxed for moving reimbursements, false otherwise
     */
    //Commented for the jira issue OLE-3415
   /* protected boolean taxedCampusForMovingReimbursements() {
        return SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(this.getClass(), DisbursementVoucherConstants.CAMPUSES_TAXED_FOR_MOVING_REIMBURSEMENTS_PARM_NM, this.getCampusCode()).evaluationSucceeds();
    }
    */
    /**
     * Travel review is required under the following circumstances: payment reason code is "P" or "N"
     *
     * @return
     */
    public boolean isTravelReviewRequired() {
        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();

        return this.getDvPymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || this.getDvPymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode);
        }

    /**
     * Overridden to immediately extract DV, if it has been marked for immediate extract
     * @see org.kuali.ole.sys.document.GeneralLedgerPostingDocumentBase#doRouteStatusChange(org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            if (isImmediatePaymentIndicator()) {
                getDisbursementVoucherExtractService().extractImmediatePayment(this);
            }
        }
    }


    protected PersonService getPersonService() {
        if ( personService == null ) {
            personService = SpringContext.getBean(PersonService.class);
        }
        return personService;
    }


    @Override
    protected ParameterService getParameterService() {
        if ( parameterService == null ) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }


    protected VendorService getVendorService() {
        if ( vendorService == null ) {
            vendorService = SpringContext.getBean(VendorService.class);
        }
        return vendorService;
    }

    /**
     * Gets the dvPymentReasonService attribute.
     *
     * @return Returns the dvPymentReasonService.
     */
    public DisbursementVoucherPaymentReasonService getDvPymentReasonService() {
        if (dvPymentReasonService == null) {
            dvPymentReasonService = SpringContext.getBean(DisbursementVoucherPaymentReasonService.class);
        }
        return dvPymentReasonService;
    }


    /**
     * Gets the identityManagementService attribute.
     * @return Returns the identityManagementService.
     */
    public static IdentityManagementService getIdentityManagementService() {
        if (identityManagementService == null) {
            identityManagementService = SpringContext.getBean(IdentityManagementService.class);
        }
        return identityManagementService;
    }

    /**
     * Sets the identityManagementService attribute value.
     * @param identityManagementService The identityManagementService to set.
     */
    public static void setIdentityManagementService(IdentityManagementService identityManagementService) {
        DisbursementVoucherDocument.identityManagementService = identityManagementService;
    }

    /**
     * @return the default implementation of the DisbursementVoucherExtractService
     */
    public static DisbursementVoucherExtractService getDisbursementVoucherExtractService() {
        if (disbursementVoucherExtractService == null) {
            disbursementVoucherExtractService = SpringContext.getBean(DisbursementVoucherExtractService.class);
        }
        return disbursementVoucherExtractService;
    }

    /**
     * @return Returns the disbExcptAttachedIndicator.
     */
    public boolean isDisbExcptAttachedIndicator() {
        return disbExcptAttachedIndicator;
    }

    /**
     * @param disbExcptAttachedIndicator The disbExcptAttachedIndicator to set.
     */
    public void setDisbExcptAttachedIndicator(boolean disbExcptAttachedIndicator) {
        this.disbExcptAttachedIndicator = disbExcptAttachedIndicator;
    }
    
    //MSU Contribution AER:RQ_AP_0760 OLEMI-8876 OLECNTRB-980
    
    /**
     * RQ_AP_0760: Ability to view disbursement information on the
     * Disbursement Voucher Document.
     * 
     * This method returns the document type of payment detail of the
     * Disbursement Voucher Document. It is invoked when the user clicks
     * on the disbursement info button on the Pre-Disbursement Processor
     * Status tab on Disbursement Voucher Document.
     * 
     * 
     * @return
     */
    public String getPaymentDetailDocumentType() {
        return DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH;
    }
}
