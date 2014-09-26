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

import org.kuali.ole.module.purap.util.PurApObjectUtils;
import org.kuali.ole.sys.businessobject.AccountingLineBase;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 * Payment Request Account Business Object.
 */
public class PaymentRequestAccount extends PurApAccountingLineBase {

    private KualiDecimal disencumberedAmount = KualiDecimal.ZERO;
    private KualiDecimal existingAmount;
    /**
     * Default constructor.
     */
    public PaymentRequestAccount() {
        this.setAmount(null);
        this.setAccountLinePercent(null);
        this.setSequenceNumber(0);
    }

    /**
     * Constructor.
     *
     * @param item - payment request item
     * @param poa  - purchase order account
     */
    public PaymentRequestAccount(PaymentRequestItem item, PurchaseOrderAccount poa) {
        this();
        // copy base attributes
        PurApObjectUtils.populateFromBaseClass(AccountingLineBase.class, poa, this);
        // copy percent
        this.setSequenceNumber(poa.getSequenceNumber());
        this.setAccountLinePercent(poa.getAccountLinePercent());
        setItemIdentifier(item.getItemIdentifier());
        setPaymentRequestItem(item);
    }

    /**
     * Constructor.
     *
     * @param item - payment request item
     * @param poa  - purchase order account
     */
    public PaymentRequestAccount(PaymentRequestItem item, InvoiceAccount poa) {
        this();
        // copy base attributes
        PurApObjectUtils.populateFromBaseClass(AccountingLineBase.class, poa, this);
        // copy percent
        this.setSequenceNumber(poa.getSequenceNumber());
        this.setAccountLinePercent(poa.getAccountLinePercent());
        setItemIdentifier(item.getItemIdentifier());
        setPaymentRequestItem(item);
    }

    public KualiDecimal getExistingAmount() {
        return existingAmount;
    }

    public void setExistingAmount(KualiDecimal existingAmount) {
        this.existingAmount = existingAmount;
    }
    public KualiDecimal getDisencumberedAmount() {
        return disencumberedAmount;
    }

    public void setDisencumberedAmount(KualiDecimal disencumberedAmount) {
        this.disencumberedAmount = disencumberedAmount;
    }

    public PaymentRequestItem getPaymentRequestItem() {
        return super.getPurapItem();
    }

    public void setPaymentRequestItem(PaymentRequestItem paymentRequestItem) {
        super.setPurapItem(paymentRequestItem);
    }

    /**
     * Caller of this method should take care of creating PaymentRequestItems
     */
    public void copyFrom(PaymentRequestAccount other) {
        super.copyFrom(other);
        setDisencumberedAmount(other.getDisencumberedAmount());
    }

}
