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

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.ole.vnd.document.service.VendorService;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.util.LinkedHashMap;

/**
 * Purchase Order Quote List Vendor Business Object.
 */
public class PurchaseOrderQuoteListVendor extends PersistableBusinessObjectBase implements MutableInactivatable {

    protected Integer purchaseOrderQuoteListIdentifier;
    protected Integer vendorHeaderGeneratedIdentifier;
    protected Integer vendorDetailAssignedIdentifier;
    protected boolean active;

    protected PurchaseOrderQuoteList purchaseOrderQuoteList;
    protected VendorDetail vendorDetail;

    /**
     * Default constructor.
     */
    public PurchaseOrderQuoteListVendor() {

    }

    public Integer getPurchaseOrderQuoteListIdentifier() {
        return purchaseOrderQuoteListIdentifier;
    }

    public void setPurchaseOrderQuoteListIdentifier(Integer purchaseOrderQuoteListIdentifier) {
        this.purchaseOrderQuoteListIdentifier = purchaseOrderQuoteListIdentifier;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public PurchaseOrderQuoteList getPurchaseOrderQuoteList() {
        return purchaseOrderQuoteList;
    }

    /**
     * Sets the purchaseOrderQuoteList attribute value.
     *
     * @param purchaseOrderQuoteList The purchaseOrderQuoteList to set.
     * @deprecated
     */
    public void setPurchaseOrderQuoteList(PurchaseOrderQuoteList purchaseOrderQuoteList) {
        this.purchaseOrderQuoteList = purchaseOrderQuoteList;
    }

    public VendorDetail getVendorDetail() {
        if (vendorHeaderGeneratedIdentifier != null && vendorDetailAssignedIdentifier != null && (vendorDetail == null || vendorDetail.getVendorHeaderGeneratedIdentifier() != vendorHeaderGeneratedIdentifier || vendorDetail.getVendorDetailAssignedIdentifier() != vendorDetailAssignedIdentifier)) {
            vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(vendorHeaderGeneratedIdentifier, vendorDetailAssignedIdentifier);
        }
        return vendorDetail;
    }

    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        if (this.purchaseOrderQuoteListIdentifier != null) {
            m.put("purchaseOrderQuoteListIdentifier", this.purchaseOrderQuoteListIdentifier.toString());
        }
        if (this.vendorHeaderGeneratedIdentifier != null) {
            m.put("vendorHeaderGeneratedIdentifier", this.vendorHeaderGeneratedIdentifier.toString());
        }
        if (this.vendorDetailAssignedIdentifier != null) {
            m.put("vendorDetailAssignedIdentifier", this.vendorDetailAssignedIdentifier.toString());
        }
        return m;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
