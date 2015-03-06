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

package org.kuali.ole.module.purap.businessobject;

import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.document.PaymentRequestDocument;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.module.purap.document.service.AccountsPayableService;
import org.kuali.ole.module.purap.document.service.PurapService;
import org.kuali.ole.module.purap.exception.PurError;
import org.kuali.ole.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.ole.module.purap.util.PurApItemUtils;
import org.kuali.ole.module.purap.util.PurApObjectUtils;
import org.kuali.ole.select.businessobject.OleInvoiceItem;
import org.kuali.ole.select.document.OlePaymentRequestDocument;
import org.kuali.ole.select.document.service.OlePaymentRequestService;
import org.kuali.ole.sys.businessobject.SourceAccountingLine;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Payment Request Item Business Object.
 */
public class PaymentRequestItem extends AccountsPayableItemBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentRequestItem.class);

    protected BigDecimal purchaseOrderItemUnitPrice;
    private KualiDecimal itemOutstandingInvoiceQuantity;
    private KualiDecimal itemOutstandingInvoiceAmount;

    /**
     * Default constructor.
     */
    public PaymentRequestItem() {

    }

    /**
     * preq item constructor - Delegate
     *
     * @param poi  - purchase order item
     * @param preq - payment request document
     */
    public PaymentRequestItem(PurchaseOrderItem poi, PaymentRequestDocument preq) {
        this(poi, preq, new HashMap<String, ExpiredOrClosedAccountEntry>());
    }

    /**
     * Constructs a new payment request item, but also merges expired accounts.
     *
     * @param poi                        - purchase order item
     * @param preq                       - payment request document
     * @param expiredOrClosedAccountList - list of expired or closed accounts to merge
     */
    public PaymentRequestItem(PurchaseOrderItem poi, PaymentRequestDocument preq, HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {

        // copy base attributes w/ extra array of fields not to be copied
        PurApObjectUtils.populateFromBaseClass(PurApItemBase.class, poi, this, PurapConstants.PREQ_ITEM_UNCOPYABLE_FIELDS);

        setItemDescription(poi.getItemDescription());

        //New Source Line should be set for PaymentRequestItem
        resetAccount();

        // set up accounts
        List accounts = new ArrayList();
        for (PurApAccountingLine account : poi.getSourceAccountingLines()) {
            PurchaseOrderAccount poa = (PurchaseOrderAccount) account;

            // check if this account is expired/closed and replace as needed
            SpringContext.getBean(AccountsPayableService.class).processExpiredOrClosedAccount(poa, expiredOrClosedAccountList);

            //KFSMI-4522 copy an accounting line with zero dollar amount if system parameter allows
            if (poa.getAmount().isZero()) {
                if (SpringContext.getBean(AccountsPayableService.class).canCopyAccountingLinesWithZeroAmount()) {
                    accounts.add(new PaymentRequestAccount(this, poa));
                }
            } else {
                accounts.add(new PaymentRequestAccount(this, poa));
            }
        }

        this.setSourceAccountingLines(accounts);
        this.getUseTaxItems().clear();
        //List<PurApItemUseTax> newUseTaxItems = new ArrayList<PurApItemUseTax>();
        /// this.setUseTaxItems(newUseTaxItems);
        //copy use tax items over, and blank out keys (useTaxId and itemIdentifier)
        /*
        this.getUseTaxItems().clear();
        for (PurApItemUseTax useTaxItem : poi.getUseTaxItems()) {
            PaymentRequestItemUseTax newItemUseTax = new PaymentRequestItemUseTax(useTaxItem);
            this.getUseTaxItems().add(newItemUseTax);

        }
        */

        // clear amount and desc on below the line - we probably don't need that null
        // itemType check but it's there just in case remove if it causes problems
        // also do this if of type service
        if ((ObjectUtils.isNotNull(this.getItemType()) && this.getItemType().isAmountBasedGeneralLedgerIndicator())) {
            // setting unit price to be null to be more consistent with other below the line
            this.setItemUnitPrice(null);
        }

        // copy custom
        this.purchaseOrderItemUnitPrice = poi.getItemUnitPrice();
//        this.purchaseOrderCommodityCode = poi.getPurchaseOrderCommodityCd();

        // set doc fields
        this.setPurapDocumentIdentifier(preq.getPurapDocumentIdentifier());
        this.setPurapDocument(preq);
    }

    /**
     * Retrieves a purchase order item by inspecting the item type to see if its above the line or below the line and returns the
     * appropriate type.
     *
     * @return - purchase order item
     */
    @Override
    public PurchaseOrderItem getPurchaseOrderItem() {
        if (ObjectUtils.isNotNull(this.getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(this.getPaymentRequest())) {
                this.refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        // ideally we should do this a different way - maybe move it all into the service or save this info somehow (make sure and
        // update though)
        if (getPaymentRequest() != null) {
            PurchaseOrderDocument po = getPaymentRequest().getPurchaseOrderDocument();
            PurchaseOrderItem poi = null;
            if (this.getItemType().isLineItemIndicator()) {
                List<PurchaseOrderItem> items = po.getItems();
                poi = items.get(this.getItemLineNumber().intValue() - 1);
                // throw error if line numbers don't match
                // MSU Contribution DTT-3014 OLEMI-8483 OLECNTRB-974
                /*
                 * List items = po.getItems(); if (items != null) { for (Object object : items) { PurchaseOrderItem item =
                 * (PurchaseOrderItem) object; if (item != null && item.getItemLineNumber().equals(this.getItemLineNumber())) { poi
                 * = item; break; } } }
                 */
            } else {
                poi = (PurchaseOrderItem) SpringContext.getBean(PurapService.class).getBelowTheLineByType(po, this.getItemType());
            }
            if (poi != null) {
                return poi;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getPurchaseOrderItem() Returning null because PurchaseOrderItem object for line number" + getItemLineNumber() + "or itemType " + getItemTypeCode() + " is null");
                }
                return null;
            }
        } else {

            LOG.error("getPurchaseOrderItem() Returning null because paymentRequest object is null");
            throw new PurError("Payment Request Object in Purchase Order item line number " + getItemLineNumber() + "or itemType " + getItemTypeCode() + " is null");
        }
    }

    public KualiDecimal getPoOutstandingAmount() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(this.getPurchaseOrderItemUnitPrice()) || KualiDecimal.ZERO.equals(this.getPurchaseOrderItemUnitPrice())) {
            return null;
        } else {
            return this.getPoOutstandingAmount(poi);
        }
    }

    private KualiDecimal getPoOutstandingAmount(PurchaseOrderItem poi) {
        if (poi == null) {
            return KualiDecimal.ZERO;
        } else {
            return poi.getItemOutstandingEncumberedAmount();
        }
    }

    public KualiDecimal getPoOriginalAmount() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (poi == null) {
            return null;
        } else {
            return poi.getExtendedPrice();
        }
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param amount - po outstanding amount
     * @deprecated
     */
    @Deprecated
    public void setPoOutstandingAmount(KualiDecimal amount) {
        // do nothing
    }


    public KualiDecimal getPoOutstandingQuantity() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (poi == null) {
            return null;
        } else {
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equals(this.getItemTypeCode())) {
                return null;
            } else {
                return poi.getOutstandingQuantity();
            }
        }
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param qty - po outstanding quantity
     * @deprecated
     */
    @Deprecated
    public void setPoOutstandingQuantity(KualiDecimal qty) {
        // do nothing
    }

    public BigDecimal getPurchaseOrderItemUnitPrice() {
        return purchaseOrderItemUnitPrice;
    }

    public BigDecimal getOriginalAmountfromPO() {
        return purchaseOrderItemUnitPrice;
    }

    public void setOriginalAmountfromPO(BigDecimal purchaseOrderItemUnitPrice) {
        // Do nothing
    }

    public void setPurchaseOrderItemUnitPrice(BigDecimal purchaseOrderItemUnitPrice) {
        this.purchaseOrderItemUnitPrice = purchaseOrderItemUnitPrice;
    }

    public KualiDecimal getItemOutstandingInvoiceAmount() {
        return itemOutstandingInvoiceAmount;
    }

    public void setItemOutstandingInvoiceAmount(KualiDecimal itemOutstandingInvoiceAmount) {
        this.itemOutstandingInvoiceAmount = itemOutstandingInvoiceAmount;
    }

    public KualiDecimal getItemOutstandingInvoiceQuantity() {
        return itemOutstandingInvoiceQuantity;
    }

    public void setItemOutstandingInvoiceQuantity(KualiDecimal itemOutstandingInvoiceQuantity) {
        this.itemOutstandingInvoiceQuantity = itemOutstandingInvoiceQuantity;
    }

    public PaymentRequestDocument getPaymentRequest() {
        if (ObjectUtils.isNotNull(getPurapDocumentIdentifier())) {
            if (ObjectUtils.isNull(getPurapDocument())) {
                this.refreshReferenceObject(PurapPropertyConstants.PURAP_DOC);
            }
        }
        return super.getPurapDocument();
    }

    public void setPaymentRequest(PaymentRequestDocument paymentRequest) {
        this.setPurapDocument(paymentRequest);
    }

    public void generateAccountListFromPoItemAccounts(List<PurApAccountingLine> accounts) {
        for (PurApAccountingLine line : accounts) {
            PurchaseOrderAccount poa = (PurchaseOrderAccount) line;
            if (!line.isEmpty()) {
                getSourceAccountingLines().add(new PaymentRequestAccount(this, poa));
            }
        }
    }

    /**
     * @see org.kuali.ole.module.purap.businessobject.PurApItem#getAccountingLineClass()
     */
    @Override
    public Class getAccountingLineClass() {
        return PaymentRequestAccount.class;
    }

    public boolean isDisplayOnPreq() {
        PurchaseOrderItem poi = getPurchaseOrderItem();
        if (ObjectUtils.isNull(poi)) {
            LOG.debug("poi was null");
            return false;
        }

        // if the po item is not active... skip it
        if (!poi.isItemActiveIndicator()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("poi was not active: " + poi.toString());
            }
            return false;
        }

        ItemType poiType = poi.getItemType();

        if (poiType.isQuantityBasedGeneralLedgerIndicator()) {
            if (poi.getItemQuantity().isGreaterThan(poi.getItemInvoicedTotalQuantity())) {
                return true;
            } else {
                if (ObjectUtils.isNotNull(this.getItemQuantity()) && this.getItemQuantity().isGreaterThan(KualiDecimal.ZERO)) {
                    return true;
                }
            }

            return false;
        } else { // not quantity based
            if (poi.getItemOutstandingEncumberedAmount().isGreaterThan(KualiDecimal.ZERO)) {
                return true;
            } else {
                if (PurApItemUtils.isNonZeroExtended(this)) {
                    return true;
                }
                return false;
            }

        }
    }

    /**
     * sets account line percentage to zero.
     *
     * @see org.kuali.ole.module.purap.businessobject.PurApItem#resetAccount()
     */
    @Override
    public void resetAccount() {
        super.resetAccount();
        this.getNewSourceLine().setAmount(null);
        this.getNewSourceLine().setAccountLinePercent(new BigDecimal(0));
    }

    /**
     * Added for electronic invoice
     */
    public void addToUnitPrice(BigDecimal addThisValue) {
        if (getItemUnitPrice() == null) {
            setItemUnitPrice(BigDecimal.ZERO);
        }
        BigDecimal addedPrice = getItemUnitPrice().add(addThisValue);
        setItemUnitPrice(addedPrice);
    }

    public void addToExtendedPrice(KualiDecimal addThisValue) {
        if (getExtendedPrice() == null) {
            setExtendedPrice(KualiDecimal.ZERO);
        }
        KualiDecimal addedPrice = getExtendedPrice().add(addThisValue);
        setExtendedPrice(addedPrice);
    }

    @Override
    public Class getUseTaxClass() {
        return PaymentRequestItemUseTax.class;
    }

    /**
     * preq item constructor - Delegate
     *
     * @param poi  - purchase order item
     * @param preq - payment request document
     */
    public PaymentRequestItem(OleInvoiceItem poi, OlePaymentRequestDocument preq) {
        this(poi, preq, new HashMap<String, ExpiredOrClosedAccountEntry>());
    }


    /**
     * Constructs a new payment request item, but also merges expired accounts.
     *
     * @param poi                        - purchase order item
     * @param preq                       - payment request document
     * @param expiredOrClosedAccountList - list of expired or closed accounts to merge
     */
    public PaymentRequestItem(OleInvoiceItem poi, OlePaymentRequestDocument preq, HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {

        // copy base attributes w/ extra array of fields not to be copied
        PurApObjectUtils.populateFromBaseClass(PurApItemBase.class, poi, this, PurapConstants.PREQ_ITEM_UNCOPYABLE_FIELDS);

        setItemDescription(poi.getItemDescription());

        //New Source Line should be set for PaymentRequestItem
        resetAccount();

        // set up accounts
        List accounts = new ArrayList();

        for (PurApAccountingLine account : poi.getSourceAccountingLines()) {
            InvoiceAccount poa = (InvoiceAccount) account;

            // check if this account is expired/closed and replace as needed
            SpringContext.getBean(AccountsPayableService.class).processExpiredOrClosedAccount(poa, expiredOrClosedAccountList);

            //KFSMI-4522 copy an accounting line with zero dollar amount if system parameter allows
            if (poa.getAmount().isZero()) {
                if (SpringContext.getBean(AccountsPayableService.class).canCopyAccountingLinesWithZeroAmount()) {
                    accounts.add(new PaymentRequestAccount(this, poa));
                }
            } else {
                accounts.add(new PaymentRequestAccount(this, poa));
            }
        }

        this.setSourceAccountingLines(accounts);
        this.getUseTaxItems().clear();
        //List<PurApItemUseTax> newUseTaxItems = new ArrayList<PurApItemUseTax>();
        /// this.setUseTaxItems(newUseTaxItems);
        //copy use tax items over, and blank out keys (useTaxId and itemIdentifier)
        /*
        this.getUseTaxItems().clear();
        for (PurApItemUseTax useTaxItem : poi.getUseTaxItems()) {
            PaymentRequestItemUseTax newItemUseTax = new PaymentRequestItemUseTax(useTaxItem);
            this.getUseTaxItems().add(newItemUseTax);

        }
        */

        // clear amount and desc on below the line - we probably don't need that null
        // itemType check but it's there just in case remove if it causes problems
        // also do this if of type service
        if ((ObjectUtils.isNotNull(this.getItemType()) && this.getItemType().isAmountBasedGeneralLedgerIndicator())) {
            // setting unit price to be null to be more consistent with other below the line
            // this.setItemUnitPrice(null);
        }

        // copy custom
        /*Modified for the jira -5458*/
        this.purchaseOrderItemUnitPrice = poi.getPurchaseOrderItem()!=null ? poi.getPurchaseOrderItem().getItemUnitPrice() : null;
//        this.purchaseOrderCommodityCode = poi.getPurchaseOrderCommodityCd();

        // set doc fields
        this.setPurapDocumentIdentifier(preq.getPurapDocumentIdentifier());
        this.setPurapDocument(preq);
    }

}
