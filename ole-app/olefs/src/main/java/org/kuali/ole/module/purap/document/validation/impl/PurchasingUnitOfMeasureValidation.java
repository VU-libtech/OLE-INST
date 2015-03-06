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
import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.module.purap.businessobject.PurchasingItemBase;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.businessobject.UnitOfMeasure;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;

import java.util.HashMap;
import java.util.Map;

public class PurchasingUnitOfMeasureValidation extends GenericValidation {

    private PurApItem itemForValidation;
    private DataDictionaryService dataDictionaryService;
    private BusinessObjectService businessObjectService;

    /**
     * Validates that if the item type is quantity based, the unit of measure is required.
     */
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) itemForValidation;
        GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.ITEM_TAB_ERRORS);

        // Validations for quantity based item type
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
            String uomCode = purItem.getItemUnitOfMeasureCode();
            if (StringUtils.isEmpty(uomCode)) {
                valid = false;
                String attributeLabel = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(purItem.getClass().getName()).
                        getAttributeDefinition(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).
                        getLabel();
                GlobalVariables.getMessageMap().putError(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + purItem.getItemIdentifierString());
            } else {
                //Find out whether the unit of measure code has existed in the database
                Map<String, String> fieldValues = new HashMap<String, String>();
                fieldValues.put(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, purItem.getItemUnitOfMeasureCode());
                if (businessObjectService.countMatching(UnitOfMeasure.class, fieldValues) != 1) {
                    //This is the case where the unit of measure code on the item does not exist in the database.
                    valid = false;
                    GlobalVariables.getMessageMap().putError(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, PurapKeyConstants.PUR_ITEM_UNIT_OF_MEASURE_CODE_INVALID, " in " + purItem.getItemIdentifierString());
                }
            }
        }

        GlobalVariables.getMessageMap().clearErrorPath();

        // MSU Contribution OLEMI-5041 DTT-3371 OLECNTRB-955
        if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator()
                && StringUtils.isNotBlank(purItem.getItemUnitOfMeasureCode())) {
            valid = false;
            if(itemForValidation.getItemLineNumber() != null){
            String errorPrefix = OLEPropertyConstants.DOCUMENT + "." + PurapPropertyConstants.ITEM + "["
                    + (itemForValidation.getItemLineNumber() - 1) + "]."
                    + PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE;
            String attributeLabel = dataDictionaryService.getDataDictionary()
                    .getBusinessObjectEntry(purItem.getClass().getName())
                    .getAttributeDefinition(PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).getLabel();
            GlobalVariables.getMessageMap().putError(errorPrefix, PurapKeyConstants.ERROR_ITEM_UOM_NOT_ALLOWED,
                    attributeLabel + " in " + purItem.getItemIdentifierString());
        }
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

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
