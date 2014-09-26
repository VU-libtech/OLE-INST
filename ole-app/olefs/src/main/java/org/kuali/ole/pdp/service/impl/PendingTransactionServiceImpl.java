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
/*
 * Created on Aug 30, 2004
 *
 */
package org.kuali.ole.pdp.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.coa.businessobject.AccountingPeriod;
import org.kuali.ole.coa.businessobject.OffsetDefinition;
import org.kuali.ole.coa.service.AccountingPeriodService;
import org.kuali.ole.coa.service.OffsetDefinitionService;
import org.kuali.ole.pdp.PdpConstants;
import org.kuali.ole.pdp.businessobject.GlPendingTransaction;
import org.kuali.ole.pdp.businessobject.PaymentAccountDetail;
import org.kuali.ole.pdp.businessobject.PaymentDetail;
import org.kuali.ole.pdp.businessobject.PaymentGroup;
import org.kuali.ole.pdp.dataaccess.PendingTransactionDao;
import org.kuali.ole.pdp.service.PendingTransactionService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.businessobject.Bank;
import org.kuali.ole.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.BankService;
import org.kuali.ole.sys.service.FlexibleOffsetAccountService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see org.kuali.ole.pdp.service.PendingTransactionService
 */
@Transactional
public class PendingTransactionServiceImpl implements PendingTransactionService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PendingTransactionServiceImpl.class);

    protected static String FDOC_TYP_CD_PROCESS_ACH = "ACHD";
    protected static String FDOC_TYP_CD_PROCESS_CHECK = "CHKD";
    protected static String FDOC_TYP_CD_CANCEL_REISSUE_ACH = "ACHR";
    protected static String FDOC_TYP_CD_CANCEL_REISSUE_CHECK = "CHKR";
    protected static String FDOC_TYP_CD_CANCEL_ACH = "ACHC";
    protected static String FDOC_TYP_CD_CANCEL_CHECK = "CHKC";

    private PendingTransactionDao glPendingTransactionDao;
    private AccountingPeriodService accountingPeriodService;
    private DateTimeService dateTimeService;
    private ConfigurationService kualiConfigurationService;
    private BusinessObjectService businessObjectService;
    private BankService bankService;

    public PendingTransactionServiceImpl() {
        super();
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#generatePaymentGeneralLedgerPendingEntry(org.kuali.ole.pdp.businessobject.PaymentGroup)
     */
    public void generatePaymentGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_PROCESS_ACH, FDOC_TYP_CD_PROCESS_CHECK, false);
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#generateCancellationGeneralLedgerPendingEntry(org.kuali.ole.pdp.businessobject.PaymentGroup)
     */
    public void generateCancellationGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_CANCEL_ACH, FDOC_TYP_CD_CANCEL_CHECK, true);
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#generateReissueGeneralLedgerPendingEntry(org.kuali.ole.pdp.businessobject.PaymentGroup)
     */
    public void generateReissueGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_CANCEL_REISSUE_ACH, FDOC_TYP_CD_CANCEL_REISSUE_CHECK, true);
    }

    /**
     * Populates and stores a new GLPE for each account detail in the payment group.
     * 
     * @param paymentGroup payment group to generate entries for
     * @param achFdocTypeCode doc type for ach disbursements
     * @param checkFdocTypeCod doc type for check disbursements
     * @param reversal boolean indicating if this is a reversal
     */
    protected void populatePaymentGeneralLedgerPendingEntry(PaymentGroup paymentGroup, String achFdocTypeCode, String checkFdocTypeCod, boolean reversal) {
        List<PaymentAccountDetail> accountListings = new ArrayList<PaymentAccountDetail>();
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            accountListings.addAll(paymentDetail.getAccountDetail());
        }

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (PaymentAccountDetail paymentAccountDetail : accountListings) {
            GlPendingTransaction glPendingTransaction = new GlPendingTransaction();
            glPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));

            if (StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode()) && StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode())) {
                glPendingTransaction.setFdocRefTypCd(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode());
                glPendingTransaction.setFsRefOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());
            }
            else {
                glPendingTransaction.setFdocRefTypCd(PdpConstants.PDP_FDOC_TYPE_CODE);
                glPendingTransaction.setFsRefOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);
            }

            glPendingTransaction.setFinancialBalanceTypeCode(OLEConstants.BALANCE_TYPE_ACTUAL);

            Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());
            glPendingTransaction.setTransactionDt(transactionTimestamp);
            AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
            glPendingTransaction.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
            glPendingTransaction.setUnivFiscalPrdCd(fiscalPeriod.getUniversityFiscalPeriodCode());

            glPendingTransaction.setAccountNumber(paymentAccountDetail.getAccountNbr());
            glPendingTransaction.setSubAccountNumber(paymentAccountDetail.getSubAccountNbr());
            glPendingTransaction.setChartOfAccountsCode(paymentAccountDetail.getFinChartCode());

            if (paymentGroup.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.ACH)) {
                glPendingTransaction.setFinancialDocumentTypeCode(achFdocTypeCode);
            }
            else if (paymentGroup.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.CHECK)) {
                glPendingTransaction.setFinancialDocumentTypeCode(checkFdocTypeCod);
            }

            glPendingTransaction.setFsOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);
            glPendingTransaction.setFdocNbr(paymentGroup.getDisbursementNbr().toString());

            Boolean relieveLiabilities = paymentGroup.getBatch().getCustomerProfile().getRelieveLiabilities();
            if ((relieveLiabilities != null) && (relieveLiabilities.booleanValue()) && paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode() != null) {
                OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(glPendingTransaction.getUniversityFiscalYear(), glPendingTransaction.getChartOfAccountsCode(), paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode(), glPendingTransaction.getFinancialBalanceTypeCode());
                glPendingTransaction.setFinancialObjectCode(offsetDefinition != null ? offsetDefinition.getFinancialObjectCode() : paymentAccountDetail.getFinObjectCode());
                glPendingTransaction.setFinancialSubObjectCode(OLEConstants.getDashFinancialSubObjectCode());
            }
            else {
                glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
                glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
            }

            glPendingTransaction.setProjectCd(paymentAccountDetail.getProjectCode());
            if (paymentAccountDetail.getPaymentDetail().getNetPaymentAmount().bigDecimalValue().signum() >= 0) {
                 glPendingTransaction.setDebitCrdtCd(reversal ? OLEConstants.GL_CREDIT_CODE : OLEConstants.GL_DEBIT_CODE);
            }
            else {
                 glPendingTransaction.setDebitCrdtCd(reversal ? OLEConstants.GL_DEBIT_CODE : OLEConstants.GL_CREDIT_CODE);
              }
            glPendingTransaction.setAmount(paymentAccountDetail.getAccountNetAmount().abs());

            String trnDesc = "";

            String payeeName = paymentGroup.getPayeeName();
            if (StringUtils.isNotBlank(payeeName)) {
                trnDesc = payeeName.length() > 40 ? payeeName.substring(0, 40) : StringUtils.rightPad(payeeName, 40);
            }

            if (reversal) {
                String poNbr = paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr();
                if (StringUtils.isNotBlank(poNbr)) {
                    trnDesc += " " + (poNbr.length() > 9 ? poNbr.substring(0, 9) : StringUtils.rightPad(poNbr, 9));
                }

                String invoiceNbr = paymentAccountDetail.getPaymentDetail().getInvoiceNbr();
                if (StringUtils.isNotBlank(invoiceNbr)) {
                    trnDesc += " " + (invoiceNbr.length() > 14 ? invoiceNbr.substring(0, 14) : StringUtils.rightPad(invoiceNbr, 14));
                }

                if (trnDesc.length() > 40) {
                    trnDesc = trnDesc.substring(0, 40);
                }
            }

            glPendingTransaction.setDescription(trnDesc);

            glPendingTransaction.setOrgDocNbr(paymentAccountDetail.getPaymentDetail().getOrganizationDocNbr());
            glPendingTransaction.setOrgReferenceId(paymentAccountDetail.getOrgReferenceId());
            glPendingTransaction.setFdocRefNbr(paymentAccountDetail.getPaymentDetail().getCustPaymentDocNbr());

            // update the offset account if necessary
            SpringContext.getBean(FlexibleOffsetAccountService.class).updateOffset(glPendingTransaction);

            this.businessObjectService.save(glPendingTransaction);

            sequenceHelper.increment();

            if (bankService.isBankSpecificationEnabled()) {
                this.populateBankOffsetEntry(paymentGroup, glPendingTransaction, sequenceHelper);
            }
        }
    }
    
    /**
     * Generates the bank offset for an entry (when enabled in the system)
     * 
     * @param paymentGroup PaymentGroup for which entries are being generated, contains the Bank
     * @param glPendingTransaction PDP entry created for payment detail
     * @param sequenceHelper holds current entry sequence value
     */
    protected void populateBankOffsetEntry(PaymentGroup paymentGroup, GlPendingTransaction glPendingTransaction, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        GlPendingTransaction bankPendingTransaction = new GlPendingTransaction();

        bankPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));
        bankPendingTransaction.setFdocRefTypCd(null);
        bankPendingTransaction.setFsRefOriginCd(null);
        bankPendingTransaction.setFinancialBalanceTypeCode(OLEConstants.BALANCE_TYPE_ACTUAL);
        bankPendingTransaction.setTransactionDt(glPendingTransaction.getTransactionDt());
        bankPendingTransaction.setUniversityFiscalYear(glPendingTransaction.getUniversityFiscalYear());
        bankPendingTransaction.setUnivFiscalPrdCd(glPendingTransaction.getUnivFiscalPrdCd());
        bankPendingTransaction.setFinancialDocumentTypeCode(glPendingTransaction.getFinancialDocumentTypeCode());
        bankPendingTransaction.setFsOriginCd(glPendingTransaction.getFsOriginCd());
        bankPendingTransaction.setFdocNbr(glPendingTransaction.getFdocNbr());

        Bank bank = paymentGroup.getBank();
        bankPendingTransaction.setChartOfAccountsCode(bank.getCashOffsetFinancialChartOfAccountCode());
        bankPendingTransaction.setAccountNumber(bank.getCashOffsetAccountNumber());
        if (StringUtils.isBlank(bank.getCashOffsetSubAccountNumber())) {
            bankPendingTransaction.setSubAccountNumber(OLEConstants.getDashSubAccountNumber());
        }
        else {
            bankPendingTransaction.setSubAccountNumber(bank.getCashOffsetSubAccountNumber());
        }

        bankPendingTransaction.setFinancialObjectCode(bank.getCashOffsetObjectCode());
        if (StringUtils.isBlank(bank.getCashOffsetSubObjectCode())) {
            bankPendingTransaction.setFinancialSubObjectCode(OLEConstants.getDashFinancialSubObjectCode());
        }
        else {
            bankPendingTransaction.setFinancialSubObjectCode(bank.getCashOffsetSubObjectCode());
        }
        bankPendingTransaction.setProjectCd(OLEConstants.getDashProjectCode());

        if (OLEConstants.GL_CREDIT_CODE.equals(glPendingTransaction.getDebitCrdtCd())) {
            bankPendingTransaction.setDebitCrdtCd(OLEConstants.GL_DEBIT_CODE);
        }
        else {
            bankPendingTransaction.setDebitCrdtCd(OLEConstants.GL_CREDIT_CODE);
        }
        bankPendingTransaction.setAmount(glPendingTransaction.getAmount());
        
        String description = kualiConfigurationService.getPropertyValueAsString(OLEKeyConstants.Bank.DESCRIPTION_GLPE_BANK_OFFSET);
        bankPendingTransaction.setDescription(description);
        bankPendingTransaction.setOrgDocNbr(glPendingTransaction.getOrgDocNbr());
        bankPendingTransaction.setOrgReferenceId(null);
        bankPendingTransaction.setFdocRefNbr(null);

        this.businessObjectService.save(bankPendingTransaction);

        sequenceHelper.increment();
    }

    /**
     * Gets the bankService attribute.
     * 
     * @return Returns the bankService.
     */
    protected BankService getBankService() {
        return bankService;
    }

    /**
     * Sets the bankService attribute value.
     * 
     * @param bankService The bankService to set.
     */
    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    /**
     * Sets the glPendingTransactionDao attribute value.
     * 
     * @param glPendingTransactionDao The glPendingTransactionDao to set.
     */
    public void setGlPendingTransactionDao(PendingTransactionDao glPendingTransactionDao) {
        this.glPendingTransactionDao = glPendingTransactionDao;
    }

    /**
     * Sets the accountingPeriodService attribute value.
     * 
     * @param accountingPeriodService The accountingPeriodService to set.
     */
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * 
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#save(org.kuali.ole.pdp.businessobject.GlPendingTransaction)
     */
    public void save(GlPendingTransaction tran) {
        LOG.debug("save() started");

        this.businessObjectService.save(tran);
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#getUnextractedTransactions()
     */
    public Iterator<GlPendingTransaction> getUnextractedTransactions() {
        LOG.debug("getUnextractedTransactions() started");

        return glPendingTransactionDao.getUnextractedTransactions();
    }

    /**
     * @see org.kuali.ole.pdp.service.PendingTransactionService#clearExtractedTransactions()
     */
    public void clearExtractedTransactions() {
        glPendingTransactionDao.clearExtractedTransactions();
    }

    /**
     * Sets the business object service
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
