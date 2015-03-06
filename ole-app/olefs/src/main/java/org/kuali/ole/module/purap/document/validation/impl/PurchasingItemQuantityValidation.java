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
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.module.purap.businessobject.PurchasingItemBase;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class PurchasingItemQuantityValidation extends GenericValidation {

    private PurApItem itemForValidation;
    private DataDictionaryService dataDictionaryService;

    /**
     * Validates that if the item type is quantity based, the item quantity is required and if the item type is amount based, the
     * quantity is not allowed.
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) itemForValidation;
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator() && (ObjectUtils.isNull(purItem.getItemQuantity()))) {
            valid = false;
            String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(purItem.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + purItem.getItemIdentifierString());
        } else if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator() && ObjectUtils.isNotNull(purItem.getItemQuantity())) {
            valid = false;
            String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(purItem.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, PurapKeyConstants.ERROR_ITEM_QUANTITY_NOT_ALLOWED, attributeLabel + " in " + purItem.getItemIdentifierString());
        }

        return valid;
    }

    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
