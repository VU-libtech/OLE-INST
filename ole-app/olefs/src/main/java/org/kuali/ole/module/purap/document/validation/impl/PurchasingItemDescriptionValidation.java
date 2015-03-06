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

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.util.GlobalVariables;

public class PurchasingItemDescriptionValidation extends GenericValidation {

    private PurApItem itemForValidation;
    private DataDictionaryService dataDictionaryService;

    /**
     * Checks that a description was entered for the item.
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        if (StringUtils.isEmpty(itemForValidation.getItemDescription())) {
            valid = false;
            String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(itemForValidation.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_DESCRIPTION).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_DESCRIPTION, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + itemForValidation.getItemIdentifierString());
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
