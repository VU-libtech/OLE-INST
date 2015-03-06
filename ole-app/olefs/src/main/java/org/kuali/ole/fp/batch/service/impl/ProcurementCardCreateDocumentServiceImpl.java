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
package org.kuali.ole.fp.batch.service.impl;

import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.AUTO_APPROVE_DOCUMENTS_IND;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.AUTO_APPROVE_NUMBER_OF_DAYS;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_ACCOUNT_PARM_NM;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_CHART_CODE_PARM_NM;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_OBJECT_CODE_PARM_NM;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.ERROR_TRANS_ACCOUNT_PARM_NM;
import static org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.SINGLE_TRANSACTION_IND_PARM_NM;
import static org.kuali.ole.sys.OLEConstants.GL_CREDIT_CODE;
import static org.kuali.ole.sys.OLEConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.coa.businessobject.ProjectCode;
import org.kuali.ole.coa.businessobject.SubAccount;
import org.kuali.ole.coa.businessobject.SubObjectCode;
import org.kuali.ole.coa.service.AccountService;
import org.kuali.ole.coa.service.ChartService;
import org.kuali.ole.coa.service.ObjectCodeService;
import org.kuali.ole.coa.service.ProjectCodeService;
import org.kuali.ole.coa.service.SubAccountService;
import org.kuali.ole.coa.service.SubObjectCodeService;
import org.kuali.ole.fp.batch.ProcurementCardAutoApproveDocumentsStep;
import org.kuali.ole.fp.batch.ProcurementCardCreateDocumentsStep;
import org.kuali.ole.fp.batch.ProcurementCardLoadStep;
import org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.ole.fp.businessobject.CapitalAssetInformation;
import org.kuali.ole.fp.businessobject.ProcurementCardDefault;
import org.kuali.ole.fp.businessobject.ProcurementCardHolder;
import org.kuali.ole.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.ole.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.ole.fp.businessobject.ProcurementCardTransaction;
import org.kuali.ole.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.ole.fp.businessobject.ProcurementCardVendor;
import org.kuali.ole.fp.document.ProcurementCardDocument;
import org.kuali.ole.fp.document.validation.impl.ProcurementCardDocumentRuleConstants;
import org.kuali.ole.integration.cab.CapitalAssetBuilderModuleService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.service.AccountingLineRuleHelperService;
import org.kuali.ole.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.ole.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;
import org.springframework.transaction.annotation.Transactional;


/**
 * This is the default implementation of the ProcurementCardCreateDocumentService interface.
 *
 * @see org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService
 */
public class ProcurementCardCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardCreateDocumentServiceImpl.class);

    protected static final String WORKFLOW_SEARCH_RESULT_KEY = "routeHeaderId";

    protected ParameterService parameterService;
    protected BusinessObjectService businessObjectService;
    protected DocumentService documentService;
    protected WorkflowDocumentService workflowDocumentService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected AccountingLineRuleHelperService accountingLineRuleUtil;
    protected CapitalAssetBuilderModuleService capitalAssetBuilderModuleService;


    /**
     * This method retrieves a collection of credit card transactions and traverses through this list, creating
     * ProcurementCardDocuments for each card.
     *
     * @return True if the procurement card documents were created successfully.  If any problem occur while creating the
     * documents, a runtime exception will be thrown.
     *
     * @see org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService#createProcurementCardDocuments()
     */
    @Override
    @SuppressWarnings("rawtypes")
    @Transactional
    public boolean createProcurementCardDocuments() {
        List documents = new ArrayList();
        List cardTransactions = retrieveTransactions();

        // iterate through card transaction list and create documents
        for (Iterator iter = cardTransactions.iterator(); iter.hasNext();) {
            documents.add(createProcurementCardDocument((List) iter.next()));
        }

        // now store all the documents
        for (Iterator iter = documents.iterator(); iter.hasNext();) {
            ProcurementCardDocument pcardDocument = (ProcurementCardDocument) iter.next();
            try {
                documentService.saveDocument(pcardDocument, DocumentSystemSaveEvent.class);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Saved Procurement Card document: "+pcardDocument.getDocumentNumber());
                }
            }
            catch (Exception e) {
                LOG.error("Error persisting document # " + pcardDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                throw new RuntimeException("Error persisting document # " + pcardDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
            }
        }

        return true;
    }

    /**
     * This method retrieves all the procurement card documents with a status of 'I' and routes them to the next step in the
     * routing path.
     *
     * @return True if the routing was performed successfully.  A runtime exception will be thrown if any errors occur while routing.
     *
     * @see org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService#routeProcurementCardDocuments(java.util.List)
     */
    @Override
    public boolean routeProcurementCardDocuments() {

        List<String> documentIdList = retrieveProcurementCardDocumentsToRoute(KewApiConstants.ROUTE_HEADER_SAVED_CD);

        //Collections.reverse(documentIdList);
        if ( LOG.isInfoEnabled() ) {
            LOG.info("PCards to Route: "+documentIdList);
        }

        for (String pcardDocumentId: documentIdList) {
            try {
                ProcurementCardDocument pcardDocument = (ProcurementCardDocument)documentService.getByDocumentHeaderId(pcardDocumentId);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Routing PCDO document # " + pcardDocumentId + ".");
                }
                documentService.prepareWorkflowDocument(pcardDocument);

                //** NOTE
                //
                //     Calling workflow service to BYPASS business rule checks
                //
                //** NOTE
                workflowDocumentService.route( pcardDocument.getDocumentHeader().getWorkflowDocument(), "", null);
            }
            catch (WorkflowException e) {
                LOG.error("Error routing document # " + pcardDocumentId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(),e);
            }
        }

        return true;
    }

    /**
     * Returns a list of all initiated but not yet routed procurement card documents, using the KualiWorkflowInfo service.
     * @return a list of procurement card documents to route
     */
    protected List<String> retrieveProcurementCardDocumentsToRoute(String statusCode){
        List<String> documentIds = new ArrayList<String>();

        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
        criteria.setDocumentTypeName(OLEConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD);
        criteria.setDocumentStatuses(Collections.singletonList(DocumentStatus.fromCode(statusCode)));
        DocumentSearchResults results = KewApiServiceLocator.getWorkflowDocumentService().documentSearch(
                GlobalVariables.getUserSession().getPrincipalId(), criteria.build());

        for (DocumentSearchResult resultRow: results.getSearchResults()) {
            documentIds.add(resultRow.getDocument().getDocumentId());
    }

        return documentIds;
    }

    /**
     * This method determines if procurement card documents can be auto approved.  A document can be auto approved if
     * the grace period for allowing auto approval of a procurement card document has passed.  The grace period is defined
     * by a parameter in the parameters table.  The create date of the document is then compared against the current date and
     * if the difference is larger than the grace period defined, then the document is auto approved.
     *
     * @return This method always returns true.
     *
     * @see org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService#autoApproveProcurementCardDocuments()
     */
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        // check if auto approve is turned on
        boolean autoApproveOn = parameterService.getParameterValueAsBoolean(ProcurementCardAutoApproveDocumentsStep.class, AUTO_APPROVE_DOCUMENTS_IND);

        if (!autoApproveOn) { // no auto approve?  then skip out of here...
            return true;
        }

        List<String> documentIdList = retrieveProcurementCardDocumentsToRoute(KewApiConstants.ROUTE_HEADER_ENROUTE_CD);

        // get number of days and type for auto approve
        int autoApproveNumberDays = Integer.parseInt(parameterService.getParameterValueAsString(ProcurementCardAutoApproveDocumentsStep.class, AUTO_APPROVE_NUMBER_OF_DAYS));

        Timestamp currentDate = dateTimeService.getCurrentTimestamp();
        for (String pcardDocumentId: documentIdList) {
            try {
                ProcurementCardDocument pcardDocument = (ProcurementCardDocument)documentService.getByDocumentHeaderId(pcardDocumentId);

                // prevent PCard documents from auto approving if they have capital asset info to collect
                if(capitalAssetBuilderModuleService.hasCapitalAssetObjectSubType(pcardDocument)) {
                    continue;
                }

                // if number of days in route is passed the allowed number, call doc service for super user approve
                Timestamp docCreateDate = new Timestamp( pcardDocument.getDocumentHeader().getWorkflowDocument().getDateCreated().getMillis() );
                if (KfsDateUtils.getDifferenceInDays(docCreateDate, currentDate) > autoApproveNumberDays) {
                    // update document description to reflect the auto approval
                    pcardDocument.getDocumentHeader().setDocumentDescription("Auto Approved On " + dateTimeService.toDateTimeString(currentDate) + ".");

                    if ( LOG.isInfoEnabled() ) {
                        LOG.info("Auto approving document # " + pcardDocument.getDocumentHeader().getDocumentNumber());
                    }
                    documentService.superUserApproveDocument(pcardDocument, "");
                }
            } catch (WorkflowException e) {
                LOG.error("Error auto approving document # " + pcardDocumentId + " " + e.getMessage(),e);
                throw new RuntimeException(e.getMessage(),e);
            }
        }

        return true;
    }


    /**
     * This method retrieves a list of transactions from a temporary table, and groups them into document lists, based on
     * single transaction indicator or a grouping by card.
     *
     * @return List containing transactions for document.
     */
    @SuppressWarnings("rawtypes")
    protected List retrieveTransactions() {
        List groupedTransactions = new ArrayList();

        // retrieve records from transaction table order by card number
        List transactions = (List) businessObjectService.findMatchingOrderBy(ProcurementCardTransaction.class, new HashMap(), OLEPropertyConstants.TRANSACTION_CREDIT_CARD_NUMBER, true);

        // check apc for single transaction documents or multiple by card
        boolean singleTransaction = parameterService.getParameterValueAsBoolean(ProcurementCardCreateDocumentsStep.class, SINGLE_TRANSACTION_IND_PARM_NM);

        List documentTransactions = new ArrayList();
        if (singleTransaction) {
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                documentTransactions.add(iter.next());
                groupedTransactions.add(documentTransactions);
                documentTransactions = new ArrayList();
            }
        }
        else {
            Map cardTransactionsMap = new HashMap();
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                ProcurementCardTransaction transaction = (ProcurementCardTransaction) iter.next();
                if (!cardTransactionsMap.containsKey(transaction.getTransactionCreditCardNumber())) {
                    cardTransactionsMap.put(transaction.getTransactionCreditCardNumber(), new ArrayList());
                }
                ((List) cardTransactionsMap.get(transaction.getTransactionCreditCardNumber())).add(transaction);
            }

            for (Iterator iter = cardTransactionsMap.values().iterator(); iter.hasNext();) {
                groupedTransactions.add(iter.next());

            }
        }

        return groupedTransactions;
    }


    /**
     * Creates a ProcurementCardDocument from the List of transactions given.
     *
     * @param transactions List of ProcurementCardTransaction objects to be used for creating the document.
     * @return A ProcurementCardDocument populated with the transactions provided.
     */
    protected ProcurementCardDocument createProcurementCardDocument(List transactions) {
        ProcurementCardDocument pcardDocument = null;

        try {
            // get new document from doc service
            pcardDocument = (ProcurementCardDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PROCUREMENT_CARD);

            List<CapitalAssetInformation> capitalAssets = pcardDocument.getCapitalAssetInformation();
            for (CapitalAssetInformation capitalAsset : capitalAssets) {
                if (ObjectUtils.isNotNull(capitalAsset) && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetInformationDetails())) {
                    capitalAsset.setDocumentNumber(pcardDocument.getDocumentNumber());
                }
            }

            ProcurementCardTransaction trans = (ProcurementCardTransaction) transactions.get(0);
            String errors = validateTransaction(trans);
            createCardHolderRecord(pcardDocument, trans);

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            String errorText = "";
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                ProcurementCardTransaction transaction = (ProcurementCardTransaction) iter.next();

                // create transaction detail record with accounting lines
                errorText += createTransactionDetailRecord(pcardDocument, transaction, transactionLineNumber);

                // update document total
                documentTotalAmount = documentTotalAmount.add(transaction.getFinancialDocumentTotalAmount());

                transactionLineNumber++;
            }

            pcardDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(documentTotalAmount);
            pcardDocument.getDocumentHeader().setDocumentDescription("SYSTEM Generated");

            // Remove duplicate messages from errorText
            String messages[] = StringUtils.split(errorText, ".");
            for (int i = 0; i < messages.length; i++) {
                int countMatches = StringUtils.countMatches(errorText, messages[i]) - 1;
                errorText = StringUtils.replace(errorText, messages[i] + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), OLEPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            pcardDocument.getDocumentHeader().setExplanation(errorText);
        }
        catch (WorkflowException e) {
            LOG.error("Error creating pcdo documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating pcdo documents: " + e.getMessage(),e);
        }

        return pcardDocument;
    }

    /**
     * Creates card holder record and sets that record to the document given.
     *
     * @param pcardDocument Procurement card document to place the record in.
     * @param transaction The transaction to set the card holder record fields from.
     */
    protected void createCardHolderRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction) {
        ProcurementCardHolder cardHolder = new ProcurementCardHolder();

        cardHolder.setDocumentNumber(pcardDocument.getDocumentNumber());
        cardHolder.setTransactionCreditCardNumber(transaction.getTransactionCreditCardNumber());
        cardHolder.setChartOfAccountsCode(transaction.getChartOfAccountsCode());
        cardHolder.setAccountNumber(transaction.getAccountNumber());
        cardHolder.setSubAccountNumber(transaction.getSubAccountNumber());

        if (getParameterService().getParameterValueAsBoolean(ProcurementCardCreateDocumentsStep.class, ProcurementCardCreateDocumentsStep.USE_CARD_HOLDER_DEFAULT_PARAMETER_NAME)) {
            final ProcurementCardDefault procurementCardDefault = retrieveProcurementCardDefault(transaction.getTransactionCreditCardNumber());
            if (procurementCardDefault != null) {
                cardHolder.setCardCycleAmountLimit(procurementCardDefault.getCardCycleAmountLimit());
                cardHolder.setCardCycleVolumeLimit(procurementCardDefault.getCardCycleVolumeLimit());
                cardHolder.setCardHolderAlternateName(procurementCardDefault.getCardHolderAlternateName());
                cardHolder.setCardHolderCityName(procurementCardDefault.getCardHolderCityName());
                cardHolder.setCardHolderLine1Address(procurementCardDefault.getCardHolderLine1Address());
                cardHolder.setCardHolderLine2Address(procurementCardDefault.getCardHolderLine2Address());
                cardHolder.setCardHolderName(procurementCardDefault.getCardHolderName());
                cardHolder.setCardHolderStateCode(procurementCardDefault.getCardHolderStateCode());
                cardHolder.setCardHolderWorkPhoneNumber(procurementCardDefault.getCardHolderWorkPhoneNumber());
                cardHolder.setCardHolderZipCode(procurementCardDefault.getCardHolderZipCode());
                cardHolder.setCardLimit(procurementCardDefault.getCardLimit());
                cardHolder.setCardNoteText(procurementCardDefault.getCardNoteText());
                cardHolder.setCardStatusCode(procurementCardDefault.getCardStatusCode());

                if (getParameterService().getParameterValueAsBoolean(ProcurementCardCreateDocumentsStep.class, ProcurementCardCreateDocumentsStep.USE_ACCOUNTING_DEFAULT_PARAMETER_NAME)) {
                    cardHolder.setChartOfAccountsCode(procurementCardDefault.getChartOfAccountsCode());
                    cardHolder.setAccountNumber(procurementCardDefault.getAccountNumber());
                    cardHolder.setSubAccountNumber(procurementCardDefault.getSubAccountNumber());
                }
            }
        }
        if (StringUtils.isEmpty(cardHolder.getCardHolderName())) {
        cardHolder.setCardCycleAmountLimit(transaction.getCardCycleAmountLimit());
        cardHolder.setCardCycleVolumeLimit(transaction.getCardCycleVolumeLimit());
        cardHolder.setCardHolderAlternateName(transaction.getCardHolderAlternateName());
        cardHolder.setCardHolderCityName(transaction.getCardHolderCityName());
        cardHolder.setCardHolderLine1Address(transaction.getCardHolderLine1Address());
        cardHolder.setCardHolderLine2Address(transaction.getCardHolderLine2Address());
        cardHolder.setCardHolderName(transaction.getCardHolderName());
        cardHolder.setCardHolderStateCode(transaction.getCardHolderStateCode());
        cardHolder.setCardHolderWorkPhoneNumber(transaction.getCardHolderWorkPhoneNumber());
        cardHolder.setCardHolderZipCode(transaction.getCardHolderZipCode());
        cardHolder.setCardLimit(transaction.getCardLimit());
        cardHolder.setCardNoteText(transaction.getCardNoteText());
        cardHolder.setCardStatusCode(transaction.getCardStatusCode());
        }

        pcardDocument.setProcurementCardHolder(cardHolder);
    }

    /**
     * Creates a transaction detail record and adds that record to the document provided.
     *
     * @param pcardDocument Document to place record in.
     * @param transaction Transaction to set fields from.
     * @param transactionLineNumber Line number of the new transaction detail record within the procurement card document.
     * @return The error text that was generated from the creation of the detail records.  If the text is empty, no errors were encountered.
     */
    protected String createTransactionDetailRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber) {
        ProcurementCardTransactionDetail transactionDetail = new ProcurementCardTransactionDetail();

        // set the document transaction detail fields from the loaded transaction record
        transactionDetail.setDocumentNumber(pcardDocument.getDocumentNumber());
        transactionDetail.setFinancialDocumentTransactionLineNumber(transactionLineNumber);
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionReferenceNumber(transaction.getTransactionReferenceNumber());
        transactionDetail.setTransactionBillingCurrencyCode(transaction.getTransactionBillingCurrencyCode());
        transactionDetail.setTransactionCurrencyExchangeRate(transaction.getTransactionCurrencyExchangeRate());
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionOriginalCurrencyAmount(transaction.getTransactionOriginalCurrencyAmount());
        transactionDetail.setTransactionOriginalCurrencyCode(transaction.getTransactionOriginalCurrencyCode());
        transactionDetail.setTransactionPointOfSaleCode(transaction.getTransactionPointOfSaleCode());
        transactionDetail.setTransactionPostingDate(transaction.getTransactionPostingDate());
        transactionDetail.setTransactionPurchaseIdentifierDescription(transaction.getTransactionPurchaseIdentifierDescription());
        transactionDetail.setTransactionPurchaseIdentifierIndicator(transaction.getTransactionPurchaseIdentifierIndicator());
        transactionDetail.setTransactionSalesTaxAmount(transaction.getTransactionSalesTaxAmount());
        transactionDetail.setTransactionSettlementAmount(transaction.getTransactionSettlementAmount());
        transactionDetail.setTransactionTaxExemptIndicator(transaction.getTransactionTaxExemptIndicator());
        transactionDetail.setTransactionTravelAuthorizationCode(transaction.getTransactionTravelAuthorizationCode());
        transactionDetail.setTransactionUnitContactName(transaction.getTransactionUnitContactName());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount());
        }

        // create transaction vendor record
        createTransactionVendorRecord(pcardDocument, transaction, transactionDetail);

        // add transaction detail to document
        pcardDocument.getTransactionEntries().add(transactionDetail);

        // now create the initial source and target lines for this transaction
        return createAndValidateAccountingLines(pcardDocument, transaction, transactionDetail);
    }


    /**
     * Creates a transaction vendor detail record and adds it to the transaction detail.
     *
     * @param pcardDocument The procurement card document to retrieve values from.
     * @param transaction Transaction to set fields from.
     * @param transactionDetail The transaction detail to set the vendor record on.
     */
    protected void createTransactionVendorRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail transactionDetail) {
        ProcurementCardVendor transactionVendor = new ProcurementCardVendor();

        transactionVendor.setDocumentNumber(pcardDocument.getDocumentNumber());
        transactionVendor.setFinancialDocumentTransactionLineNumber(transactionDetail.getFinancialDocumentTransactionLineNumber());
        transactionVendor.setTransactionMerchantCategoryCode(transaction.getTransactionMerchantCategoryCode());
        transactionVendor.setVendorCityName(transaction.getVendorCityName());
        transactionVendor.setVendorLine1Address(transaction.getVendorLine1Address());
        transactionVendor.setVendorLine2Address(transaction.getVendorLine2Address());
        transactionVendor.setVendorName(transaction.getVendorName());
        transactionVendor.setVendorOrderNumber(transaction.getVendorOrderNumber());
        transactionVendor.setVendorStateCode(transaction.getVendorStateCode());
        transactionVendor.setVendorZipCode(transaction.getVendorZipCode());
        transactionVendor.setVisaVendorIdentifier(transaction.getVisaVendorIdentifier());

        transactionDetail.setProcurementCardVendor(transactionVendor);
    }

    /**
     * From the transaction accounting attributes, creates source and target accounting lines. Attributes are validated first, and
     * replaced with default and error values if needed. There will be 1 source and 1 target line generated.
     *
     * @param pcardDocument The procurement card document to add the new accounting lines to.
     * @param transaction The transaction to process into account lines.
     * @param docTransactionDetail The transaction detail to create source and target accounting lines from.
     * @return String containing any error messages.
     */
    protected String createAndValidateAccountingLines(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        // build source lines
        ProcurementCardSourceAccountingLine sourceLine = createSourceAccountingLine(transaction, docTransactionDetail);
        sourceLine.setPostingYear(pcardDocument.getPostingYear());

        // add line to transaction through document since document contains the next sequence number fields
        pcardDocument.addSourceAccountingLine(sourceLine);

        // build target lines
        ProcurementCardTargetAccountingLine targetLine = createTargetAccountingLine(transaction, docTransactionDetail);
        targetLine.setPostingYear(pcardDocument.getPostingYear());

        // add line to transaction through document since document contains the next sequence number fields
        pcardDocument.addTargetAccountingLine(targetLine);

        return validateTargetAccountingLine(targetLine);
    }

    /**
     * Creates the to record for the transaction. The chart of account attributes from the transaction are used to create
     * the accounting line.
     *
     * @param transaction The transaction to pull information from to create the accounting line.
     * @param docTransactionDetail The transaction detail to pull information from to populate the accounting line.
     * @return The target accounting line fully populated with values from the parameters passed in.
     */
    protected ProcurementCardTargetAccountingLine createTargetAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        ProcurementCardTargetAccountingLine targetLine = new ProcurementCardTargetAccountingLine();
        targetLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        targetLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        targetLine.setChartOfAccountsCode(transaction.getChartOfAccountsCode());
        targetLine.setAccountNumber(transaction.getAccountNumber());
        targetLine.setFinancialObjectCode(transaction.getFinancialObjectCode());
        targetLine.setSubAccountNumber(transaction.getSubAccountNumber());
        targetLine.setFinancialSubObjectCode(transaction.getFinancialSubObjectCode());
        targetLine.setProjectCode(transaction.getProjectCode());

        if (getParameterService().getParameterValueAsBoolean(ProcurementCardCreateDocumentsStep.class, ProcurementCardCreateDocumentsStep.USE_ACCOUNTING_DEFAULT_PARAMETER_NAME)) {
            final ProcurementCardDefault procurementCardDefault = retrieveProcurementCardDefault(transaction.getTransactionCreditCardNumber());
            if (procurementCardDefault != null) {
                    targetLine.setChartOfAccountsCode(procurementCardDefault.getChartOfAccountsCode());
                    targetLine.setAccountNumber(procurementCardDefault.getAccountNumber());
                    targetLine.setFinancialObjectCode(procurementCardDefault.getFinancialObjectCode());
                    targetLine.setSubAccountNumber(procurementCardDefault.getSubAccountNumber());
                    targetLine.setFinancialSubObjectCode(procurementCardDefault.getFinancialSubObjectCode());
                    targetLine.setProjectCode(procurementCardDefault.getProjectCode());
            }
        }

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return targetLine;
    }

    /**
     * Creates the from record for the transaction. The clearing chart, account, and object code is used for creating the line.
     *
     * @param transaction The transaction to pull information from to create the accounting line.
     * @param docTransactionDetail The transaction detail to pull information from to populate the accounting line.
     * @return The source accounting line fully populated with values from the parameters passed in.
     */
    protected ProcurementCardSourceAccountingLine createSourceAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        ProcurementCardSourceAccountingLine sourceLine = new ProcurementCardSourceAccountingLine();

        sourceLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        sourceLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        sourceLine.setChartOfAccountsCode(getDefaultChartCode());
        sourceLine.setAccountNumber(getDefaultAccountNumber());
        sourceLine.setFinancialObjectCode(getDefaultObjectCode());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return sourceLine;
    }

    /**
     * Validates the chart of account attributes for existence and active indicator. Will substitute for defined
     * default parameters or set fields to empty that if they have errors.
     *
     * @param targetLine The target accounting line to be validated.
     * @return String with error messages discovered during validation.  An empty string indicates no validation errors were found.
     */
    protected String validateTargetAccountingLine(ProcurementCardTargetAccountingLine targetLine) {
        String errorText = "";

        targetLine.refresh();

        if (!accountingLineRuleUtil.isValidChart("", targetLine.getChart(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " is invalid; using error Chart Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setChartOfAccountsCode(getErrorChartCode());
            targetLine.refresh();
        }

        if (!accountingLineRuleUtil.isValidAccount("", targetLine.getAccount(), dataDictionaryService.getDataDictionary()) || targetLine.getAccount().isExpired()) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " is invalid; using error account.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setChartOfAccountsCode(getErrorChartCode());
            targetLine.setAccountNumber(getErrorAccountNumber());
            targetLine.refresh();
        }

        if (!accountingLineRuleUtil.isValidObjectCode("", targetLine.getObjectCode(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Object Code " + targetLine.getFinancialObjectCode() + " is invalid; using default Object Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setFinancialObjectCode(getDefaultObjectCode());
            targetLine.refresh();
        }

        if (StringUtils.isNotBlank(targetLine.getSubAccountNumber()) && !accountingLineRuleUtil.isValidSubAccount("", targetLine.getSubAccount(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " Sub Account " + targetLine.getSubAccountNumber() + " is invalid; Setting Sub Account to blank.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setSubAccountNumber("");
        }

        if (StringUtils.isNotBlank(targetLine.getFinancialSubObjectCode()) && !accountingLineRuleUtil.isValidSubObjectCode("", targetLine.getSubObjectCode(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " Object Code " + targetLine.getFinancialObjectCode() + " Sub Object Code " + targetLine.getFinancialSubObjectCode() + " is invalid; setting Sub Object to blank.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setFinancialSubObjectCode("");
        }

        if (StringUtils.isNotBlank(targetLine.getProjectCode()) && !accountingLineRuleUtil.isValidProjectCode("", targetLine.getProject(), dataDictionaryService.getDataDictionary())) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info("Project Code " + targetLine.getProjectCode() + " is invalid; setting to blank.");
            }
            errorText += " Project Code " + targetLine.getProjectCode() + " is invalid; setting to blank.";

            targetLine.setProjectCode("");
        }

        // clear out GlobalVariable message map, since we have taken care of the errors
        GlobalVariables.setMessageMap(new MessageMap());

        return errorText;
    }


    /**
     * Validates the chart of account attributes for existence and active indicator. Will substitute for defined
     * default parameters or set fields to empty that if they have errors.
     *
     * @param transaction The transaction to be validated.
     * @return String with error messages discovered during validation.  An empty string indicates no validation errors were found.
     */
    protected String validateTransaction(ProcurementCardTransaction transaction) {
        String errorText = "";

        if (getParameterService().getParameterValueAsBoolean(ProcurementCardCreateDocumentsStep.class, ProcurementCardCreateDocumentsStep.USE_ACCOUNTING_DEFAULT_PARAMETER_NAME)) {
            final ProcurementCardDefault procurementCardDefault = retrieveProcurementCardDefault(transaction.getTransactionCreditCardNumber());
            if (ObjectUtils.isNull(procurementCardDefault)) {
                final String tempErrorText = "Procurement Card Accounting Line Defaults are turned on but no Procurement Card Default record could be retrieved for transaction: "+transaction.getTransactionReferenceNumber() + " by card number.";
                if ( LOG.isInfoEnabled() ) {
                    LOG.info(tempErrorText);
                }
                errorText += " " + tempErrorText;
            }
        }
        else {
            transaction.refresh();

            final ChartService chartService = SpringContext.getBean(ChartService.class);
            if (transaction.getChartOfAccountsCode() == null || ObjectUtils.isNull(chartService.getByPrimaryId(transaction.getChartOfAccountsCode()))) {
                String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " is invalid; using error Chart Code.";
                if ( LOG.isInfoEnabled() ) {
                    LOG.info(tempErrorText);
                }
                errorText += " " + tempErrorText;
                transaction.setChartOfAccountsCode(getErrorChartCode());
                transaction.refresh();
            }

            final AccountService accountService = SpringContext.getBean(AccountService.class);
            if (transaction.getAccountNumber() == null || ObjectUtils.isNull(accountService.getByPrimaryIdWithCaching(transaction.getChartOfAccountsCode(), transaction.getAccountNumber())) || accountService.getByPrimaryIdWithCaching(transaction.getChartOfAccountsCode(), transaction.getAccountNumber()).isExpired()) {
                String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " is invalid; using error account.";
                if ( LOG.isInfoEnabled() ) {
                    LOG.info(tempErrorText);
                }
                errorText += " " + tempErrorText;
                transaction.setChartOfAccountsCode(getErrorChartCode());
                transaction.setAccountNumber(getErrorAccountNumber());
                transaction.refresh();
            }

            final UniversityDateService uds = SpringContext.getBean(UniversityDateService.class);
            final ObjectCodeService ocs = SpringContext.getBean(ObjectCodeService.class);
            if (transaction.getFinancialObjectCode() == null || ObjectUtils.isNull(ocs.getByPrimaryIdWithCaching(uds.getCurrentFiscalYear(), transaction.getChartOfAccountsCode(), transaction.getFinancialObjectCode()))) {
                String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Object Code " + transaction.getFinancialObjectCode() + " is invalid; using default Object Code.";
                if ( LOG.isInfoEnabled() ) {
                    LOG.info(tempErrorText);
                }
                errorText += " " + tempErrorText;

                transaction.setFinancialObjectCode(getDefaultObjectCode());
                transaction.refresh();
            }

            if (StringUtils.isNotBlank(transaction.getSubAccountNumber())) {
                SubAccountService sas = SpringContext.getBean(SubAccountService.class);
                SubAccount subAccount = sas.getByPrimaryIdWithCaching(transaction.getChartOfAccountsCode(), transaction.getAccountNumber(), transaction.getSubAccountNumber());

                if (ObjectUtils.isNull(subAccount)) {
                    String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " Sub Account " + transaction.getSubAccountNumber() + " is invalid; Setting Sub Account to blank.";
                    if ( LOG.isInfoEnabled() ) {
                        LOG.info(tempErrorText);
                    }
                    errorText += " " + tempErrorText;

                    transaction.setSubAccountNumber("");
                }
            }

            if (StringUtils.isNotBlank(transaction.getFinancialSubObjectCode())) {

                SubObjectCodeService socs = SpringContext.getBean(SubObjectCodeService.class);
                SubObjectCode soc = socs.getByPrimaryIdForCurrentYear(transaction.getChartOfAccountsCode(), transaction.getAccountNumber(), transaction.getFinancialObjectCode(), transaction.getFinancialSubObjectCode());

                if (ObjectUtils.isNull(soc)) {
                    String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " Object Code " + transaction.getFinancialObjectCode() + " Sub Object Code " + transaction.getFinancialSubObjectCode() + " is invalid; setting Sub Object to blank.";
                    if ( LOG.isInfoEnabled() ) {
                        LOG.info(tempErrorText);
                    }
                    errorText += " " + tempErrorText;

                    transaction.setFinancialSubObjectCode("");
                }
            }

            if (StringUtils.isNotBlank(transaction.getProjectCode())) {

                ProjectCodeService pcs = SpringContext.getBean(ProjectCodeService.class);
                ProjectCode pc = pcs.getByPrimaryId(transaction.getProjectCode());

                if (ObjectUtils.isNull(pc)) {
                    if ( LOG.isInfoEnabled() ) {
                        LOG.info("Project Code " + transaction.getProjectCode() + " is invalid; setting to blank.");
                    }
                    errorText += " Project Code " + transaction.getProjectCode() + " is invalid; setting to blank.";

                    transaction.setProjectCode("");
                }
            }
        }

        // clear out GlobalVariable message map, since we have taken care of the errors
        GlobalVariables.setMessageMap(new MessageMap());

        return errorText;
    }

    /**
     * Retrieves the error chart code from the parameter table.
     * @return The error chart code defined in the parameter table.
     */
    protected String getErrorChartCode() {
        return parameterService.getParameterValueAsString(ProcurementCardCreateDocumentsStep.class, ProcurementCardDocumentRuleConstants.ERROR_TRANS_CHART_CODE_PARM_NM);
    }

    /**
     * Retrieves the error account number from the parameter table.
     * @return The error account number defined in the parameter table.
     */
    protected String getErrorAccountNumber() {
        return parameterService.getParameterValueAsString(ProcurementCardCreateDocumentsStep.class, ERROR_TRANS_ACCOUNT_PARM_NM);
    }

    /**
     * Gets the default Chart Code, Account from the custom Procurement Cardholder table.
     *
     */
    protected ProcurementCardDefault retrieveProcurementCardDefault(String creditCardNumber) {
        Map<String, String> pkMap = new HashMap<String, String>();
        pkMap.put(OLEPropertyConstants.CREDIT_CARD_NUMBER, creditCardNumber);
        ProcurementCardDefault procurementCardDefault = businessObjectService.findByPrimaryKey(ProcurementCardDefault.class, pkMap);

        return procurementCardDefault;
    }

    /**
     * Retrieves the default chard code from the parameter table.
     * @return The default chart code defined in the parameter table.
     */
    protected String getDefaultChartCode() {
        return parameterService.getParameterValueAsString(ProcurementCardLoadStep.class, DEFAULT_TRANS_CHART_CODE_PARM_NM);
    }

    /**
     * Retrieves the default account number from the parameter table.
     * @return The default account number defined in the parameter table.
     */
    protected String getDefaultAccountNumber() {
        return parameterService.getParameterValueAsString(ProcurementCardLoadStep.class, DEFAULT_TRANS_ACCOUNT_PARM_NM);
    }

    /**
     * Retrieves the default object code from the parameter table.
     * @return The default object code defined in the parameter table.
     */
    protected String getDefaultObjectCode() {
        return parameterService.getParameterValueAsString(ProcurementCardLoadStep.class, DEFAULT_TRANS_OBJECT_CODE_PARM_NM);
    }

    /**
     * Calls businessObjectService to remove all the procurement card transaction rows from the transaction load table.
     */
    protected void cleanTransactionsTable() {
        businessObjectService.deleteMatching(ProcurementCardTransaction.class, new HashMap());
    }

    /**
     * Loads all the parsed XML transactions into the temp transaction table.
     *
     * @param transactions List of ProcurementCardTransactions to load.
     */
    protected void loadTransactions(List transactions) {
        businessObjectService.save(transactions);
    }

    /**
     * @return retrieves the presumably injected implementation of ParameterService to use
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Sets the parameterService attribute.
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the businessObjectService attribute.
     * @return Returns the businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute.
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Gets the documentService attribute.
     * @return Returns the documentService.
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the documentService attribute.
     * @param documentService The documentService to set.
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public WorkflowDocumentService getWorkflowDocumentService() {
        return workflowDocumentService;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    /**
     * Gets the dataDictionaryService attribute.
     * @return Returns the dataDictionaryService.
     */
    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    /**
     * Sets the dataDictionaryService attribute.
     * @param dataDictionaryService dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }


    /**
     * Gets the dateTimeService attribute.
     * @return Returns the dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute.
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the accountingLineRuleUtil attribute value.
     * @param accountingLineRuleUtil The accountingLineRuleUtil to set.
     */
    public void setAccountingLineRuleUtil(AccountingLineRuleHelperService accountingLineRuleUtil) {
        this.accountingLineRuleUtil = accountingLineRuleUtil;
    }

    /**
     * Sets the capitalAssetBuilderModuleService attribute value.
     * @param capitalAssetBuilderModuleService The capitalAssetBuilderModuleService to set.
     */
    public void setCapitalAssetBuilderModuleService(CapitalAssetBuilderModuleService capitalAssetBuilderModuleService) {
        this.capitalAssetBuilderModuleService = capitalAssetBuilderModuleService;
    }

}
