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
package org.kuali.ole.module.purap.fixture;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.module.purap.fixture.PurapTestConstants.PO;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.ole.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 * Fixture class for Purchase Order Document.
 */
public enum PurchaseOrderDocumentFixture {

    // TODO f2f: fix the REQ id

    PO_ONLY_REQUIRED_FIELDS(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.PO_QTY_UNRESTRICTED_ITEM_1 }
    ),
    PO_ONLY_REQUIRED_FIELDS_MULTI_ITEMS(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.PO_QTY_UNRESTRICTED_ITEM_1,
                    PurchaseOrderItemFixture.PO_QTY_UNRESTRICTED_ITEM_2 }
    ),
    PO_WITH_VENDOR_CONTRACT(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_WITH_VENDOR_CONTRACT, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.PO_QTY_UNRESTRICTED_ITEM_1}
    ),
    PO_APPROVAL_REQUIRED(PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS_2, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { PurchaseOrderItemFixture.PO_QTY_APO_ITEM_1 } // purchaseOrderItemMultiFixtures
    ),
    CLOSE_PO_WITH_PREQ(PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS_2, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { PurchaseOrderItemFixture.PO_QTY_APO_ITEM_1 } // purchaseOrderItemMultiFixtures
    ),
    REOPEN_PO_WITH_PREQ(PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS_2, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { PurchaseOrderItemFixture.PO_QTY_APO_ITEM_1 } // purchaseOrderItemMultiFixtures
    ),
    REQUEST_CANCEL_PREQ(PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS_2, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { PurchaseOrderItemFixture.PO_QTY_APO_ITEM_1 } // purchaseOrderItemMultiFixtures
    ),
    REQUEST_HOLD_PREQ(PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS_2, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { PurchaseOrderItemFixture.PO_QTY_APO_ITEM_1 } // purchaseOrderItemMultiFixtures
    ),
    PO_FOR_THRESHOLD_CHECK(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.PO_ITEM_THRESHOLD_CHECK }
    ),
    EINVOICE_PO(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenDate
            null, // purchaseOrderLastTransmitDate
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionDate
            new BigDecimal(1.00), // order  Type
            PurchasingAccountsPayableDocumentFixture.EINVOICE_PO, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.EINVOICE_PO_ITEM }
    ),
    PO_ONLY_REQUIRED_FIELDS_WITH_COPIES(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.PO_QTY_UNRESTRICTED_ITEM }
    ),
    ERES_PO_ONLY_REQUIRED_FIELDS_WITH_COPIES(
            PO.CREATE_DATE, // purchaseOrderCreateDate
            PO.REQ_ID, // requisitionIdentifier
            "LPRC", // purchaseOrderVendorChoiceCode
            null, // recurringPaymentFrequencyCode
            null, // recurringPaymentAmount
            null, // recurringPaymentDate
            null, // initialPaymentAmount
            null, // initialPaymentDate
            null, // finalPaymentAmount
            null, // finalPaymentDate
            null, // purchaseOrderInitialOpenTimestamp
            null, // purchaseOrderLastTransmitTimestamp
            null, // purchaseOrderQuoteDueDate
            null, // purchaseOrderQuoteTypeCode
            null, // purchaseOrderQuoteVendorNoteText
            false, // purchaseOrderConfirmedIndicator
            null, // purchaseOrderCommodityDescription
            null, // purchaseOrderPreviousIdentifier
            null, // alternateVendorHeaderGeneratedIdentifier
            null, // alternateVendorDetailAssignedIdentifier
            null, // newQuoteVendorHeaderGeneratedIdentifier
            null, // newQuoteVendorDetailAssignedIdentifier
            null, // alternateVendorName
            true, // purchaseOrderCurrentIndicator
            false, // pendingActionIndicator
            null, // purchaseOrderFirstTransmissionTimestamp
            new BigDecimal(1.00), // order Type
            PurchasingAccountsPayableDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purapDocumentFixture
            PurchasingDocumentFixture.PO_ONLY_REQUIRED_FIELDS, // purchasingDocumentFixture
            new PurchaseOrderItemFixture[] { // purchaseOrderItemMultiFixtures
                    PurchaseOrderItemFixture.ERES_PO_BASIC_ITEM_QUANTITY }
    ),
    ;
    public final BigDecimal purchaseOrderTypeId;
    public final Timestamp purchaseOrderCreateDate;
    public final Integer requisitionIdentifier;
    public final String purchaseOrderVendorChoiceCode;
    public final String recurringPaymentFrequencyCode;
    public final KualiDecimal recurringPaymentAmount;
    public final Date recurringPaymentDate;
    public final KualiDecimal initialPaymentAmount;
    public final Date initialPaymentDate;
    public final KualiDecimal finalPaymentAmount;
    public final Date finalPaymentDate;
    public final Timestamp purchaseOrderInitialOpenTimestamp;
    public final Timestamp purchaseOrderLastTransmitTimestamp;
    public final Date purchaseOrderQuoteDueDate;
    public final String purchaseOrderQuoteTypeCode;
    public final String purchaseOrderQuoteVendorNoteText;
    public final boolean purchaseOrderConfirmedIndicator;
    public final String purchaseOrderCommodityDescription;
    public final Integer purchaseOrderPreviousIdentifier;
    public final Integer alternateVendorHeaderGeneratedIdentifier;
    public final Integer alternateVendorDetailAssignedIdentifier;
    public final Integer newQuoteVendorHeaderGeneratedIdentifier;
    public final Integer newQuoteVendorDetailAssignedIdentifier;
    public final String alternateVendorName;
    public final boolean purchaseOrderCurrentIndicator;
    public final boolean pendingActionIndicator;
    public final Timestamp purchaseOrderFirstTransmissionTimestamp;
    private PurchasingAccountsPayableDocumentFixture purapDocumentFixture;
    private PurchasingDocumentFixture purchasingDocumentFixture;
    private PurchaseOrderItemFixture[] purchaseOrderItemFixtures;

    /**
     * Private Constructor.
     */
    private PurchaseOrderDocumentFixture(Timestamp purchaseOrderCreateDate, Integer requisitionIdentifier, String purchaseOrderVendorChoiceCode, String recurringPaymentFrequencyCode, KualiDecimal recurringPaymentAmount, Date recurringPaymentDate, KualiDecimal initialPaymentAmount, Date initialPaymentDate, KualiDecimal finalPaymentAmount, Date finalPaymentDate, Timestamp purchaseOrderInitialOpenTimestamp, Timestamp purchaseOrderLastTransmitTimestamp, Date purchaseOrderQuoteDueDate, String purchaseOrderQuoteTypeCode, String purchaseOrderQuoteVendorNoteText, boolean purchaseOrderConfirmedIndicator, String purchaseOrderCommodityDescription, Integer purchaseOrderPreviousIdentifier, Integer alternateVendorHeaderGeneratedIdentifier, Integer alternateVendorDetailAssignedIdentifier, Integer newQuoteVendorHeaderGeneratedIdentifier, Integer newQuoteVendorDetailAssignedIdentifier, String alternateVendorName, boolean purchaseOrderCurrentIndicator, boolean pendingActionIndicator, Timestamp purchaseOrderFirstTransmissionTimestamp,BigDecimal purchaseOrderTypeId,
            PurchasingAccountsPayableDocumentFixture purapDocumentFixture, PurchasingDocumentFixture purchasingDocumentFixture, PurchaseOrderItemFixture[] purchaseOrderItemFixtures) {
        this.purchaseOrderCreateDate = purchaseOrderCreateDate;
        this.requisitionIdentifier = requisitionIdentifier;
        this.purchaseOrderVendorChoiceCode = purchaseOrderVendorChoiceCode;
        this.recurringPaymentFrequencyCode = recurringPaymentFrequencyCode;
        this.recurringPaymentAmount = recurringPaymentAmount;
        this.recurringPaymentDate = recurringPaymentDate;
        this.initialPaymentAmount = initialPaymentAmount;
        this.initialPaymentDate = initialPaymentDate;
        this.finalPaymentAmount = finalPaymentAmount;
        this.finalPaymentDate = finalPaymentDate;
        this.purchaseOrderInitialOpenTimestamp = purchaseOrderInitialOpenTimestamp;
        this.purchaseOrderLastTransmitTimestamp = purchaseOrderLastTransmitTimestamp;
        this.purchaseOrderQuoteDueDate = purchaseOrderQuoteDueDate;
        this.purchaseOrderQuoteTypeCode = purchaseOrderQuoteTypeCode;
        this.purchaseOrderQuoteVendorNoteText = purchaseOrderQuoteVendorNoteText;
        this.purchaseOrderConfirmedIndicator = purchaseOrderConfirmedIndicator;
        this.purchaseOrderCommodityDescription = purchaseOrderCommodityDescription;
        this.purchaseOrderPreviousIdentifier = purchaseOrderPreviousIdentifier;
        this.alternateVendorHeaderGeneratedIdentifier = alternateVendorHeaderGeneratedIdentifier;
        this.alternateVendorDetailAssignedIdentifier = alternateVendorDetailAssignedIdentifier;
        this.newQuoteVendorHeaderGeneratedIdentifier = newQuoteVendorHeaderGeneratedIdentifier;
        this.newQuoteVendorDetailAssignedIdentifier = newQuoteVendorDetailAssignedIdentifier;
        this.alternateVendorName = alternateVendorName;
        this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
        this.pendingActionIndicator = pendingActionIndicator;
        this.purchaseOrderFirstTransmissionTimestamp = purchaseOrderFirstTransmissionTimestamp;
        this.purchaseOrderTypeId = purchaseOrderTypeId;
        this.purapDocumentFixture = purapDocumentFixture;
        this.purchasingDocumentFixture = purchasingDocumentFixture;
        this.purchaseOrderItemFixtures = purchaseOrderItemFixtures;
    }

    /**
     * Creates a Purchase Order Document using this Fixture.
     *
     * @return the created Purchase Order Document.
     */
    public PurchaseOrderDocument createPurchaseOrderDocument() {
        PurchaseOrderDocument doc = purchasingDocumentFixture.createPurchaseOrderDocument(purapDocumentFixture);
//        doc.setAccountDistributionMethod(PurapConstants.AccountDistributionMethodCodes.PROPORTIONAL_CODE); //account distribution method is sequential
        doc.setPurchaseOrderCreateTimestamp(this.purchaseOrderCreateDate);
        doc.setRequisitionIdentifier(this.requisitionIdentifier);
        doc.setPurchaseOrderVendorChoiceCode(this.purchaseOrderVendorChoiceCode);
        doc.setRecurringPaymentFrequencyCode(this.recurringPaymentFrequencyCode);
        doc.setRecurringPaymentAmount(this.recurringPaymentAmount);
        doc.setRecurringPaymentDate(this.recurringPaymentDate);
        doc.setInitialPaymentAmount(this.initialPaymentAmount);
        doc.setInitialPaymentDate(this.initialPaymentDate);
        doc.setFinalPaymentAmount(this.finalPaymentAmount);
        doc.setFinalPaymentDate(this.finalPaymentDate);
        doc.setPurchaseOrderInitialOpenTimestamp(this.purchaseOrderInitialOpenTimestamp);
        doc.setPurchaseOrderLastTransmitTimestamp(this.purchaseOrderLastTransmitTimestamp);
        doc.setPurchaseOrderQuoteDueDate(this.purchaseOrderQuoteDueDate);
        doc.setPurchaseOrderQuoteTypeCode(this.purchaseOrderQuoteTypeCode);
        doc.setPurchaseOrderQuoteVendorNoteText(this.purchaseOrderQuoteVendorNoteText);
        doc.setPurchaseOrderConfirmedIndicator(this.purchaseOrderConfirmedIndicator);
        doc.setPurchaseOrderCommodityDescription(this.purchaseOrderCommodityDescription);
        doc.setPurchaseOrderPreviousIdentifier(this.purchaseOrderPreviousIdentifier);
        doc.setAlternateVendorHeaderGeneratedIdentifier(this.alternateVendorHeaderGeneratedIdentifier);
        doc.setAlternateVendorDetailAssignedIdentifier(this.alternateVendorDetailAssignedIdentifier);
        doc.setNewQuoteVendorHeaderGeneratedIdentifier(this.newQuoteVendorHeaderGeneratedIdentifier);
        doc.setNewQuoteVendorDetailAssignedIdentifier(this.newQuoteVendorDetailAssignedIdentifier);
        doc.setAlternateVendorName(this.alternateVendorName);
        doc.setPurchaseOrderCurrentIndicator(this.purchaseOrderCurrentIndicator);
        doc.setPendingActionIndicator(this.pendingActionIndicator);
        doc.setPurchaseOrderFirstTransmissionTimestamp(this.purchaseOrderFirstTransmissionTimestamp);
        doc.setPurchaseOrderTypeId(this.purchaseOrderTypeId);
        for (PurchaseOrderItemFixture purchaseOrderItemFixture : purchaseOrderItemFixtures) {
            purchaseOrderItemFixture.addTo(doc);
        }

        //If vendor header and vendor detail id are not null, fetch the vendor from
        //vendor service and set it to this PO.
        Integer vendorHeaderGeneratedId = doc.getVendorHeaderGeneratedIdentifier();
        Integer vendorDetailAssignedId = doc.getVendorDetailAssignedIdentifier();

        if (vendorHeaderGeneratedId != null && vendorDetailAssignedId != null) {
            VendorDetail vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(vendorHeaderGeneratedId, vendorDetailAssignedId);
            doc.setVendorDetail(vendorDetail);
        }

        return doc;
    }
}
