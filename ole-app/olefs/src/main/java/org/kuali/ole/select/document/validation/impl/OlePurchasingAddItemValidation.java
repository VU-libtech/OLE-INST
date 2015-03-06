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
package org.kuali.ole.select.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.PurapConstants.ItemFields;
import org.kuali.ole.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.module.purap.businessobject.PurchasingItemBase;
import org.kuali.ole.module.purap.document.validation.impl.PurchasingAccountsPayableAddItemValidation;
import org.kuali.ole.select.bo.OLEDonor;
import org.kuali.ole.select.businessobject.OlePurchaseOrderItem;
import org.kuali.ole.select.businessobject.OleRequisitionItem;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.businessobject.UnitOfMeasure;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.ole.vnd.businessobject.CommodityCode;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OlePurchasingAddItemValidation extends PurchasingAccountsPayableAddItemValidation {

    private BusinessObjectService businessObjectService;
    private DataDictionaryService dataDictionaryService;
    private DocstoreClientLocator docstoreClientLocator;
    private static final Logger LOG = Logger.getLogger(OlePurchasingAddItemValidation.class);


    public DocstoreClientLocator getDocstoreClientLocator() {
        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        GlobalVariables.getMessageMap().addToErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
        //refresh itemType
        PurApItem refreshedItem = getItemForValidation();
        refreshedItem.refreshReferenceObject("itemType");
        super.setItemForValidation(refreshedItem);

        valid &= super.validate(event);
        //valid &= validateItemUnitPrice(getItemForValidation());
        //valid &= validateUnitOfMeasure(getItemForValidation());
        if (getItemForValidation().getItemType().isLineItemIndicator()) {
            valid &= validateItemDescription(getItemForValidation());
            valid &= validateItemLocation(getItemForValidation());
            valid &= validateCopiesAndPartsForEInstance(getItemForValidation());
            valid &= validateCommodityCodes(getItemForValidation(), commodityCodeIsRequired());
        }
        GlobalVariables.getMessageMap().removeFromErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);

        return valid;
    }

    /**
     * Validates whether the commodity code existed on the item, and if existed, whether the
     * commodity code on the item existed in the database, and if so, whether the commodity
     * code is active. Display error if any of these 3 conditions are not met.
     *
     * @param item The PurApItem containing the commodity code to be validated.
     * @return boolean false if the validation fails and true otherwise.
     */
    protected boolean validateCommodityCodes(PurApItem item, boolean commodityCodeRequired) {
        boolean valid = true;
        String identifierString = item.getItemIdentifierString();
        PurchasingItemBase purItem = (PurchasingItemBase) item;

        //This validation is only needed if the commodityCodeRequired system parameter is true
        if (commodityCodeRequired && StringUtils.isBlank(purItem.getPurchasingCommodityCode())) {
            //This is the case where the commodity code is required but the item does not currently contain the commodity code.
            valid = false;
            String attributeLabel = dataDictionaryService.
                    getDataDictionary().getBusinessObjectEntry(CommodityCode.class.getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_COMMODITY_CODE).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + identifierString);
        } else if (StringUtils.isNotBlank(purItem.getPurchasingCommodityCode())) {
            //Find out whether the commodity code has existed in the database
            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(PurapPropertyConstants.ITEM_COMMODITY_CODE, purItem.getPurchasingCommodityCode());
            if (businessObjectService.countMatching(CommodityCode.class, fieldValues) != 1) {
                //This is the case where the commodity code on the item does not exist in the database.
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INVALID, " in " + identifierString);
            } else {
                valid &= validateThatCommodityCodeIsActive(item);
            }
        }

        return valid;
    }

    /**
     * Validates the unit price for all applicable item types. It validates that the unit price field was
     * entered on the item, and that the price is in the right range for the item type.
     *
     * @param purDocument the purchasing document to be validated
     * @return boolean false if there is any validation that fails.
     */
    public boolean validateItemUnitPrice(PurApItem item) {
        boolean valid = true;
        if (item.getItemType().isLineItemIndicator()) {
            if (ObjectUtils.isNull(item.getItemUnitPrice())) {
                valid = false;
                String attributeLabel = dataDictionaryService.
                        getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                        getAttributeDefinition(PurapPropertyConstants.ITEM_UNIT_PRICE).getLabel();
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
            }
        }

        if (ObjectUtils.isNotNull(item.getItemUnitPrice())) {
            if ((BigDecimal.ZERO.compareTo(item.getItemUnitPrice()) > 0) && ((!item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) && (!item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)))) {
                // If the item type is not full order discount or trade in items, don't allow negative unit price.
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_ITEM_AMOUNT_BELOW_ZERO, ItemFields.UNIT_COST, item.getItemIdentifierString());
                valid = false;
            } else if ((BigDecimal.ZERO.compareTo(item.getItemUnitPrice()) < 0) && ((item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) || (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)))) {
                // If the item type is full order discount or trade in items, its unit price must be negative.
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_ITEM_AMOUNT_NOT_BELOW_ZERO, ItemFields.UNIT_COST, item.getItemIdentifierString());
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Validates that if the item type is quantity based, the unit of measure is required.
     *
     * @param item the item to be validated
     * @return boolean false if the item type is quantity based and the unit of measure is empty.
     */
    public boolean validateUnitOfMeasure(PurApItem item) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) item;
        // Validations for quantity based item type
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
            String uomCode = purItem.getItemUnitOfMeasureCode();
            if (StringUtils.isEmpty(uomCode)) {
                valid = false;
                String attributeLabel = dataDictionaryService.
                        getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                        getAttributeDefinition(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).
                        getLabel();
                GlobalVariables.getMessageMap().putError(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
            } else {
                //Find out whether the unit of measure code has existed in the database
                Map<String, String> fieldValues = new HashMap<String, String>();
                fieldValues.put(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, purItem.getItemUnitOfMeasureCode());
                if (businessObjectService.countMatching(UnitOfMeasure.class, fieldValues) != 1) {
                    //This is the case where the unit of measure code on the item does not exist in the database.
                    valid = false;
                    GlobalVariables.getMessageMap().putError(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, PurapKeyConstants.PUR_ITEM_UNIT_OF_MEASURE_CODE_INVALID, " in " + item.getItemIdentifierString());
                }
            }
        }

        return valid;
    }

    /**
     * Checks that a description was entered for the item.
     *
     * @param item
     * @return
     */
    public boolean validateItemDescription(PurApItem item) {
        boolean valid = true;
        if (StringUtils.isEmpty(item.getItemDescription())) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_DESCRIPTION).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_DESCRIPTION, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
        }
        return valid;
    }


    /**
     * Checks that a location was entered for the item for single copy.
     *
     * @param item
     * @return
     */
    public boolean validateItemLocation(PurApItem item) {
        boolean valid = true;
        if (item instanceof OleRequisitionItem) {
            OleRequisitionItem oleRequisitionItem = (OleRequisitionItem) item;
            valid = isValidLocation(oleRequisitionItem.getItemQuantity(), oleRequisitionItem.getItemNoOfParts(), oleRequisitionItem.getItemLocation());
        } else if (item instanceof OlePurchaseOrderItem) {
            OlePurchaseOrderItem olePurchaseOrderItem = (OlePurchaseOrderItem) item;
            valid = isValidLocation(olePurchaseOrderItem.getItemQuantity(), olePurchaseOrderItem.getItemNoOfParts(), olePurchaseOrderItem.getItemLocation());
        }
        return valid;
    }

    private boolean isValidLocation(KualiDecimal noOfCopiesOrdered, KualiInteger noOfPartsOrdered, String itemLocation) {
        boolean valid = true;
        if (noOfCopiesOrdered != null && noOfPartsOrdered != null && (noOfCopiesOrdered.equals(new KualiDecimal(1))
                && noOfPartsOrdered.equals(new KualiInteger(1)))) {
            if (itemLocation == null || itemLocation.isEmpty()) {
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                        OLEConstants.ITEM_LOCATION_REQUIRED, new String[]{});
                valid = false;
            }
        }
        return valid;
    }

    public boolean validateCopiesAndPartsForEInstance(PurApItem item) {
        boolean valid = true;
        if (item instanceof OleRequisitionItem) {
            OleRequisitionItem oleRequisitionItem = (OleRequisitionItem) item;
            valid = isValidCopiesAndPartsForEInstance(oleRequisitionItem.getItemQuantity(), oleRequisitionItem.getItemNoOfParts(), oleRequisitionItem.getLinkToOrderOption());
        } else if (item instanceof OlePurchaseOrderItem) {
            OlePurchaseOrderItem olePurchaseOrderItem = (OlePurchaseOrderItem) item;
            valid = isValidCopiesAndPartsForEInstance(olePurchaseOrderItem.getItemQuantity(), olePurchaseOrderItem.getItemNoOfParts(), olePurchaseOrderItem.getLinkToOrderOption());
        }
        return valid;
    }

    private boolean isValidCopiesAndPartsForEInstance(KualiDecimal noOfCopiesOrdered, KualiInteger noOfPartsOrdered, String linkToOrderOption) {
        boolean valid = true;
        if (StringUtils.isNotBlank(linkToOrderOption) && (linkToOrderOption.equals(OLEConstants.NB_ELECTRONIC) || linkToOrderOption.equals(OLEConstants.EB_ELECTRONIC)) && noOfCopiesOrdered != null && noOfPartsOrdered != null && (noOfCopiesOrdered.isGreaterThan(new KualiDecimal(1))
                || noOfPartsOrdered.isGreaterThan(new KualiInteger(1)))) {
            GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                    OLEConstants.ITEM_COPIESANDPARTS_SHOULDNOT_BE_GREATERTHAN_ONE_EINSTANCE, new String[]{});
            valid = false;
        }
        return valid;
    }

    /**
     * Validates that if the item type is quantity based, the item quantity is required and if the item type is amount based, the
     * quantity is not allowed.
     *
     * @param item the item to be validated
     * @return boolean false if there's any validation that fails.
     */
    public boolean validateItemQuantity(PurApItem item) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) item;
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator() && (ObjectUtils.isNull(purItem.getItemQuantity()))) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, OLEKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
        } else if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator() && ObjectUtils.isNotNull(purItem.getItemQuantity())) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, PurapKeyConstants.ERROR_ITEM_QUANTITY_NOT_ALLOWED, attributeLabel + " in " + item.getItemIdentifierString());
        }

        return valid;
    }

    /**
     * Predicate to do a parameter lookup and tell us whether a commodity code is required.
     * Override in child classes.
     *
     * @return True if a commodity code is required.
     */
    protected boolean commodityCodeIsRequired() {
        return false;
    }

    protected boolean validateThatCommodityCodeIsActive(PurApItem item) {
        if (!((PurchasingItemBase) item).getCommodityCode().isActive()) {
            //This is the case where the commodity code on the item is not active.
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INACTIVE, " in " + item.getItemIdentifierString());
            return false;
        }
        return true;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
