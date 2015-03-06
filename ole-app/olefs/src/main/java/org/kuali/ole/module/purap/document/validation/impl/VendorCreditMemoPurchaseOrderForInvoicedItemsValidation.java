/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.module.purap.document.service.CreditMemoService;
import org.kuali.ole.module.purap.document.service.PurchaseOrderService;
import org.kuali.ole.select.document.OleVendorCreditMemoDocument;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;

import java.util.List;

public class VendorCreditMemoPurchaseOrderForInvoicedItemsValidation extends GenericValidation {

    private PurchaseOrderService purchaseOrderService;
    private CreditMemoService creditMemoService;

    /**
     * Verifies the purchase order for the credit memo has at least one invoiced item. If no invoiced items are found, a credit memo
     * cannot be processed against the document.
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean hasInvoicedItems = true;
        OleVendorCreditMemoDocument cmDocument = (OleVendorCreditMemoDocument) event.getDocument();
        if (cmDocument.getInvoiceIdentifier() == null) {
            if (cmDocument.isSourceDocumentPurchaseOrder()) {
                GlobalVariables.getMessageMap().clearErrorPath();
                GlobalVariables.getMessageMap().addToErrorPath(OLEPropertyConstants.DOCUMENT);

                PurchaseOrderDocument poDocument = purchaseOrderService.getCurrentPurchaseOrder(cmDocument.getPurchaseOrderIdentifier());
                List<PurchaseOrderItem> invoicedItems = creditMemoService.getPOInvoicedItems(poDocument);

                if (invoicedItems == null || invoicedItems.isEmpty()) {
                    GlobalVariables.getMessageMap().putError(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, PurapKeyConstants.ERROR_CREDIT_MEMO_PURCAHSE_ORDER_NOITEMS);
                    hasInvoicedItems = false;
                }

                GlobalVariables.getMessageMap().clearErrorPath();
            }
        }
        return hasInvoicedItems;
    }

    public PurchaseOrderService getPurchaseOrderService() {
        return purchaseOrderService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public CreditMemoService getCreditMemoService() {
        return creditMemoService;
    }

    public void setCreditMemoService(CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }

}
