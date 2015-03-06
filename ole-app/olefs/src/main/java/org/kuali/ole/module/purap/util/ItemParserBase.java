/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.util;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;
import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.PurapParameterConstants;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.ole.module.purap.businessobject.RequisitionItem;
import org.kuali.ole.module.purap.exception.ItemParserException;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.impl.OleParameterConstants;
import org.kuali.rice.core.web.format.FormatException;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.kuali.ole.module.purap.PurapKeyConstants.*;
import static org.kuali.ole.module.purap.PurapPropertyConstants.*;

public class ItemParserBase implements ItemParser {

    /**
     * The default format defines the expected item property names and their order in the import file.
     * Please update this if the import file format changes (i.e. adding/deleting item properties, changing their order).
     */
    protected static final String[] DEFAULT_FORMAT = {ITEM_QUANTITY, OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, ITEM_CATALOG_NUMBER, ITEM_COMMODITY_CODE, ITEM_DESCRIPTION, ITEM_UNIT_PRICE};
    protected static final String[] COMMODITY_CODE_DISABLED_FORMAT = {ITEM_QUANTITY, OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, ITEM_CATALOG_NUMBER, ITEM_DESCRIPTION, ITEM_UNIT_PRICE};

    private Integer lineNo = 0;

    /**
     * @see org.kuali.ole.module.purap.util.ItemParser#getItemFormat()
     */
    public String[] getItemFormat() {
        //Check the ENABLE_COMMODITY_CODE_IND system parameter. If it's Y then 
        //we should return the DEFAULT_FORMAT, otherwise
        //we should return the COMMODITY_CODE_DISABLED_FORMAT
        boolean enableCommodityCode = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(OleParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_COMMODITY_CODE_IND);
        if (enableCommodityCode) {
            return DEFAULT_FORMAT;
        }
        return COMMODITY_CODE_DISABLED_FORMAT;
    }

    /**
     * @see org.kuali.ole.module.purap.util.ItemParser#getExpectedItemFormatAsString(java.lang.Class)
     */
    public String getExpectedItemFormatAsString(Class<? extends PurApItem> itemClass) {
        checkItemClass(itemClass);
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (String attributeName : getItemFormat()) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(getAttributeLabel(itemClass, attributeName));
        }
        return sb.toString();
    }

    /**
     * Retrieves the attribute label for the specified attribute.
     *
     * @param clazz         the class in which the specified attribute is defined
     * @param attributeName the name of the specified attribute
     * @return the attribute label for the specified attribute
     */
    @SuppressWarnings("rawtypes")
    protected String getAttributeLabel(Class clazz, String attributeName) {
        String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(clazz, attributeName);
        if (StringUtils.isBlank(label)) {
            label = attributeName;
        }
        return label;
    }

    /**
     * Checks whether the specified item class is a subclass of PurApItem;
     * throws exceptions if not.
     *
     * @param itemClass the specified item class
     */
    protected void checkItemClass(Class<? extends PurApItem> itemClass) {
        if (!PurApItem.class.isAssignableFrom(itemClass)) {
            throw new IllegalArgumentException("unknown item class: " + itemClass);
        }
    }

    /**
     * Checks whether the specified item import file is not null and of a valid format;
     * throws exceptions if conditions not satisfied.
     *
     * @param itemClass the specified item import file
     */
    protected void checkItemFile(FormFile itemFile) {
        if (itemFile == null) {
            throw new ItemParserException("invalid (null) item import file", OLEKeyConstants.ERROR_UPLOADFILE_NULL);
        }
        String fileName = itemFile.getFileName();
        if (StringUtils.isNotBlank(fileName) && !StringUtils.lowerCase(fileName).endsWith(".csv") && !StringUtils.lowerCase(fileName).endsWith(".xls")) {
            throw new ItemParserException("unsupported item import file format: " + fileName, ERROR_ITEMPARSER_INVALID_FILE_FORMAT, fileName);
        }
    }

    /**
     * Parses a line of item data from a csv file and retrieves the attributes as key-value string pairs into a map.
     *
     * @param itemLine a string read from a line in the item import file
     * @return a map containing item attribute name-value string pairs
     */
    protected Map<String, String> retrieveItemAttributes(String itemLine) {
        String[] attributeNames = getItemFormat();
        String[] attributeValues = StringUtils.splitPreserveAllTokens(itemLine, ',');
        if (attributeNames.length != attributeValues.length) {
            String[] errorParams = {"" + attributeNames.length, "" + attributeValues.length, "" + lineNo};
            GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERRORS, ERROR_ITEMPARSER_WRONG_PROPERTY_NUMBER, errorParams);
            throw new ItemParserException("wrong number of item properties: " + attributeValues.length + " exist, " + attributeNames.length + " expected (line " + lineNo + ")", ERROR_ITEMPARSER_WRONG_PROPERTY_NUMBER, errorParams);
        }

        Map<String, String> itemMap = new HashMap<String, String>();
        for (int i = 0; i < attributeNames.length; i++) {
            itemMap.put(attributeNames[i], attributeValues[i]);
        }
        return itemMap;
    }

    /**
     * Generates an item instance and populates it with the specified attribute map.
     *
     * @param itemMap   the specified attribute map from which attributes are populated
     * @param itemClass the class of which the new item instance shall be created
     * @return the populated item
     */
    protected PurApItem genItemWithRetrievedAttributes(Map<String, String> itemMap, Class<? extends PurApItem> itemClass) {
        PurApItem item;
        try {
            item = itemClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to complete item line population.", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("unable to complete item line population.", e);
        }

        boolean failed = false;
        for (Entry<String, String> entry : itemMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            try {
                /* removing this part as the checking are done in rule class later
                if ((key.equals(ITEM_DESCRIPTION) || key.equals(ITEM_UNIT_PRICE)) && value.equals("")) {
                    String[] errorParams = { key, "" + lineNo };
                    throw new ItemParserException("empty property value for " + key + " (line " + lineNo + ")", ERROR_ITEMPARSER_EMPTY_PROPERTY_VALUE, errorParams);                    
                }
                else */
                if (key.equals(OLEPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE)) {
                    value = value.toUpperCase(); // force UOM code to uppercase
                }
                try {
                    ObjectUtils.setObjectProperty(item, key, value);
                } catch (FormatException e) {
                    String[] errorParams = {value, key, "" + lineNo};
                    throw new ItemParserException("invalid numeric property value: " + key + " = " + value + " (line " + lineNo + ")", ERROR_ITEMPARSER_INVALID_NUMERIC_VALUE, errorParams);
                }
            } catch (ItemParserException e) {
                // continue to parse the rest of the item properties after the current property fails
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERRORS, e.getErrorKey(), e.getErrorParameters());
                failed = true;
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("unable to complete item line population.", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("unable to complete item line population.", e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("unable to complete item line population.", e);
            }
        }

        if (failed) {
            throw new ItemParserException("empty or invalid item properties in line " + lineNo + ")", ERROR_ITEMPARSER_ITEM_PROPERTY, "" + lineNo);
        }
        return item;
    }

    /**
     * Populates extra item attributes not contained in the imported item data to default values.
     *
     * @param item           the item to be populated
     * @param documentNumber the number of the docment that contains the item
     */
    protected void populateExtraAttributes(PurApItem item, String documentNumber) {
        if (item.getItemQuantity() != null) {
            String paramName = PurapParameterConstants.DEFAULT_QUANTITY_ITEM_TYPE;
            String itemTypeCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, "Document", paramName);
            item.setItemTypeCode(itemTypeCode);
        } else {
            String paramName = PurapParameterConstants.DEFAULT_NON_QUANTITY_ITEM_TYPE;
            String itemTypeCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, "Document", paramName);
            item.setItemTypeCode(itemTypeCode);
        }
        if (item instanceof RequisitionItem)
            ((RequisitionItem) item).setItemRestrictedIndicator(false);
        if (item instanceof PurchaseOrderItem)
            ((PurchaseOrderItem) item).setDocumentNumber(documentNumber);
    }

    /**
     * @see org.kuali.ole.module.purap.util.ItemParser#parseItem(java.lang.String, java.lang.Class, java.lang.String)
     */
    public PurApItem parseItem(String itemLine, Class<? extends PurApItem> itemClass, String documentNumber) {
        Map<String, String> itemMap = retrieveItemAttributes(itemLine);
        PurApItem item = genItemWithRetrievedAttributes(itemMap, itemClass);
        populateExtraAttributes(item, documentNumber);
        item.refresh();
        return item;
    }

    /**
     * @see org.kuali.ole.module.purap.util.ItemParser#parseItem(org.apache.struts.upload.FormFile, java.lang.Class, java.lang.String)
     */
    public List<PurApItem> importItems(FormFile itemFile, Class<? extends PurApItem> itemClass, String documentNumber) {
        // check input parameters
        try {
            checkItemClass(itemClass);
            checkItemFile(itemFile);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unable to import items.", e);
        }

        // open input stream
        List<PurApItem> importedItems = new ArrayList<PurApItem>();
        InputStream is;
        BufferedReader br;
        try {
            is = itemFile.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to open import file in ItemParserBase.", e);
        }

        // parse items line by line
        lineNo = 0;
        boolean failed = false;
        String itemLine = null;
        try {
            while ((itemLine = br.readLine()) != null) {
                lineNo++;

                if (StringUtils.isBlank(StringUtils.remove(StringUtils.deleteWhitespace(itemLine), OLEConstants.COMMA))) {
                    continue;
                }

                try {
                    PurApItem item = parseItem(itemLine, itemClass, documentNumber);
                    importedItems.add(item);
                } catch (ItemParserException e) {
                    // continue to parse the rest of the items after the current item fails
                    // error messages are already dealt with inside parseItem, so no need to do anything here
                    failed = true;
                }
            }

            if (failed) {
                throw new ItemParserException("errors in parsing item lines in file " + itemFile.getFileName(), ERROR_ITEMPARSER_ITEM_LINE, itemFile.getFileName());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to read line from BufferReader in ItemParserBase", e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("unable to close BufferReader in ItemParserBase", e);
            }
        }

        return importedItems;
    }

}
