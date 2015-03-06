package org.kuali.ole.sip2.sip2Response;

import org.apache.commons.lang3.StringUtils;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.ncip.bo.OLECheckOutItem;
import org.kuali.ole.ncip.bo.OLERenewItem;
import org.kuali.ole.sip2.common.MessageUtil;
import org.kuali.ole.sip2.common.OLESIP2Util;
import org.kuali.ole.sip2.constants.OLESIP2Constants;
import org.kuali.ole.sip2.requestParser.OLESIP2CheckOutRequestParser;

/**
 * Created by gayathria on 27/8/14.
 */
public class OLESIP2CheckOutResponse extends OLESIP2Response {


    public OLESIP2CheckOutResponse() {
        this.code = OLESIP2Constants.CHECKOUT_RESPONSE;
    }


    public String getSIP2CheckOutResponse(OLECheckOutItem oleCheckOutItem, OLESIP2CheckOutRequestParser sip2CheckOutRequestParser) {

        StringBuilder checkOutResponseBuilder = new StringBuilder();
        checkOutResponseBuilder.append(code);
        if (oleCheckOutItem.code.equalsIgnoreCase("030") || oleCheckOutItem.getMessage().equalsIgnoreCase(OLESIP2Constants.CHECK_OUT_SUCCESS)) {
            checkOutResponseBuilder.append(OLESIP2Util.bool2Int(true));
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
            checkOutResponseBuilder.append("U");
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(true));
        } else {
            checkOutResponseBuilder.append(OLESIP2Util.bool2Int(false));
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
            checkOutResponseBuilder.append("U");
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
        }
        checkOutResponseBuilder.append(MessageUtil.getSipDateTime());
        checkOutResponseBuilder.append(OLESIP2Constants.INSTITUTION_ID_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(sip2CheckOutRequestParser.getInstitutionId()) ? sip2CheckOutRequestParser.getInstitutionId() : "");
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.PATRON_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleCheckOutItem.getPatronBarcode() != null ? oleCheckOutItem.getPatronBarcode() : sip2CheckOutRequestParser.getPatronIdentifier());
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.ITEM_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleCheckOutItem.getBarcode() != null ? oleCheckOutItem.getBarcode() : sip2CheckOutRequestParser.getItemIdentifier());
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.TITLE_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleCheckOutItem.getTitleIdentifier() != null ? oleCheckOutItem.getTitleIdentifier() : "");
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.DUE_DATE_CODE);
        checkOutResponseBuilder.append(oleCheckOutItem.getDueDate() != null ? oleCheckOutItem.getDueDate() : "");
        if (OLESIP2Util.getDefaultCurrency() != null) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.CURRENCY_TYPE_CODE);
            checkOutResponseBuilder.append(OLESIP2Util.getDefaultCurrency().getCurrencyCode());
        }
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.ITEM_PROPERTIES_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(oleCheckOutItem.getItemProperties()) ? oleCheckOutItem.getItemProperties() : " ");
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.MEDIA_TYPE_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(oleCheckOutItem.getItemType()) ? oleCheckOutItem.getItemType() : "");
        if (oleCheckOutItem.code.equalsIgnoreCase("030") || oleCheckOutItem.getMessage().equalsIgnoreCase(OLESIP2Constants.CHECK_OUT_SUCCESS)) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SCREEN_MSG_CODE+OLESIP2Constants.CHECK_OUT_SUCCESSFULLY);
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.PRINT_LINE_CODE + oleCheckOutItem.getMessage().replaceAll("<br/>", ""));
        } else {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SCREEN_MSG_CODE+OLESIP2Constants.CHECK_OUT_FAILED);
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.PRINT_LINE_CODE + oleCheckOutItem.getMessage().replaceAll("<br/>", ""));
        }
        if (StringUtils.isNotBlank(sip2CheckOutRequestParser.getSequenceNum())) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SEQUENCE_NUM_CODE);
            checkOutResponseBuilder.append(sip2CheckOutRequestParser.getSequenceNum());
            checkOutResponseBuilder.append(MessageUtil.computeChecksum(checkOutResponseBuilder.toString()));
        }
        return checkOutResponseBuilder.toString() + '\r';
    }

    public String getSIP2CheckOutResponse( OLERenewItem oleRenewItem, OLESIP2CheckOutRequestParser sip2CheckOutRequestParser) {

        StringBuilder checkOutResponseBuilder = new StringBuilder();
        checkOutResponseBuilder.append(code);
        if (oleRenewItem.getCode().equalsIgnoreCase("030") || oleRenewItem.getMessage().equalsIgnoreCase(OLESIP2Constants.RENEW_SUCCESS)) {
            checkOutResponseBuilder.append(OLESIP2Util.bool2Int(true));
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
            checkOutResponseBuilder.append("U");
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(true));
        } else {
            checkOutResponseBuilder.append(OLESIP2Util.bool2Int(false));
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
            checkOutResponseBuilder.append("U");
            checkOutResponseBuilder.append(OLESIP2Util.bool2Char(false));
        }
        checkOutResponseBuilder.append(MessageUtil.getSipDateTime());
        checkOutResponseBuilder.append(OLESIP2Constants.INSTITUTION_ID_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(sip2CheckOutRequestParser.getInstitutionId()) ? sip2CheckOutRequestParser.getInstitutionId() : "");
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.PATRON_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleRenewItem.getPatronBarcode() != null ? oleRenewItem.getPatronBarcode() : sip2CheckOutRequestParser.getPatronIdentifier());
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.ITEM_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleRenewItem.getItemBarcode() != null ? oleRenewItem.getItemBarcode() : sip2CheckOutRequestParser.getItemIdentifier());
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.TITLE_IDENTIFIER_CODE);
        checkOutResponseBuilder.append(oleRenewItem.getTitleIdentifier() != null ? oleRenewItem.getTitleIdentifier() : "");
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.DUE_DATE_CODE);
        checkOutResponseBuilder.append(oleRenewItem.getNewDueDate() != null ? oleRenewItem.getNewDueDate() : "");
        if (OLESIP2Util.getDefaultCurrency() != null) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.CURRENCY_TYPE_CODE);
            checkOutResponseBuilder.append(OLESIP2Util.getDefaultCurrency().getCurrencyCode());
        }
        checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.ITEM_PROPERTIES_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(oleRenewItem.getItemProperties()) ? oleRenewItem.getItemProperties() : " ");
       /* checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.MEDIA_TYPE_CODE);
        checkOutResponseBuilder.append(StringUtils.isNotBlank(oleRenewItem.getItemType()) ? oleRenewItem.getItemType() : "");*/
        if (oleRenewItem.getCode().equalsIgnoreCase("030") || oleRenewItem.getMessage().equalsIgnoreCase(OLESIP2Constants.RENEW_SUCCESS)) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SCREEN_MSG_CODE+OLESIP2Constants.CHECK_OUT_SUCCESSFULLY);
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.PRINT_LINE_CODE + oleRenewItem.getMessage().replaceAll("<br/>", ""));
        } else {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SCREEN_MSG_CODE+OLESIP2Constants.CHECK_OUT_FAILED);
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.PRINT_LINE_CODE + oleRenewItem.getMessage().replaceAll("<br/>", ""));
        }
        if (StringUtils.isNotBlank(sip2CheckOutRequestParser.getSequenceNum())) {
            checkOutResponseBuilder.append(OLESIP2Constants.SPLIT+
                    OLESIP2Constants.SEQUENCE_NUM_CODE);
            checkOutResponseBuilder.append(sip2CheckOutRequestParser.getSequenceNum());
            checkOutResponseBuilder.append(MessageUtil.computeChecksum(checkOutResponseBuilder.toString()));
        }
        return checkOutResponseBuilder.toString() + '\r';
    }


}
