/*
 * Copyright 2007-2008 The Kuali Foundation
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

import org.kuali.ole.sys.OLEConstants;
import org.kuali.rice.krad.bo.Note;

import java.util.List;

/**
 * Requisition View Business Object.
 */
public class CorrectionReceivingView extends AbstractRelatedView {
    private String lineItemReceivingDocumentNumber;

    public String getLineItemReceivingDocumentNumber() {
        return lineItemReceivingDocumentNumber;
    }

    public void setLineItemReceivingDocumentNumber(String lineItemReceivingDocumentNumber) {
        this.lineItemReceivingDocumentNumber = lineItemReceivingDocumentNumber;
    }

    /**
     * The next three methods are overridden but shouldnt be! If they arent overridden, they dont show up in the tag, not sure why
     * at this point! (AAP)
     *
     * @see org.kuali.ole.module.purap.businessobject.AbstractRelatedView#getPurapDocumentIdentifier()
     */
    @Override
    public Integer getPurapDocumentIdentifier() {
        return super.getPurapDocumentIdentifier();
    }

    @Override
    public String getDocumentIdentifierString() {
        return super.getDocumentIdentifierString();
    }

    /**
     * @see org.kuali.ole.module.purap.businessobject.AbstractRelatedView#getNotes()
     */
    @Override
    public List<Note> getNotes() {
        return super.getNotes();
    }

    /**
     * @see org.kuali.ole.module.purap.businessobject.AbstractRelatedView#getUrl()
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    /**
     * @see org.kuali.ole.module.purap.businessobject.AbstractRelatedView#getDocumentTypeName()
     */
    @Override
    public String getDocumentTypeName() {
        return OLEConstants.FinancialDocumentTypeCodes.CORRECTION_RECEIVING;
    }

}
