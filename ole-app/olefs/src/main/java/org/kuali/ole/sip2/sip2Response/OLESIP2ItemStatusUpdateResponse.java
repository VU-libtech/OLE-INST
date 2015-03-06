package org.kuali.ole.sip2.sip2Response;

import org.apache.commons.lang3.StringUtils;
import org.kuali.ole.sip2.constants.OLESIP2Constants;
import org.kuali.ole.sip2.requestParser.OLESIP2ItemStatusUpdateRequestParser;
import org.kuali.ole.sip2.sip2Server.MessageUtil;
import org.kuali.ole.sip2.sip2Server.StringUtil;

/**
 * Created by gayathria on 15/12/14.
 */
public class OLESIP2ItemStatusUpdateResponse extends OLESIP2Response {
    public OLESIP2ItemStatusUpdateResponse() {
        code = OLESIP2Constants.ITEM_STATUS_UPDATE_RESPONSE;
    }

    public String getOLESIP2ItemStatusUpdateResponse(String requestData, OLESIP2ItemStatusUpdateRequestParser olesip2ItemStatusUpdateRequestParser) {

       /* StringBuilder builder = new StringBuilder();
        builder.append(this.code);
        builder.append(StringUtil.bool2Int(false));
        builder.append(MessageUtil.getSipDateTime());
        builder.append("AB");
        builder.append(olesip2ItemStatusUpdateRequestParser.getItemIdentifier());
        builder.append("|AF");
        builder.append("Item Status Update Service - Not supported in OLE");
        if (StringUtils.isNotBlank(olesip2ItemStatusUpdateRequestParser.getSequenceNum())) {
            builder.append("|AY");
            builder.append(olesip2ItemStatusUpdateRequestParser.getSequenceNum());
            builder.append(org.kuali.ole.sip2.common.MessageUtil.computeChecksum(olesip2ItemStatusUpdateRequestParser.toString()));
        }
        return builder.toString() + '\r';*/
        StringBuilder builder = new StringBuilder();
        String[] requestDataArray = requestData.split("\\|");
        builder.append(this.code);
        builder.append(StringUtil.bool2Int(false));
        builder.append(MessageUtil.getSipDateTime());
        builder.append(OLESIP2Constants.ITEM_IDENTIFIER_CODE);
        for (String data : requestDataArray) {
            if (data.startsWith(OLESIP2Constants.ITEM_IDENTIFIER_CODE)) {
                builder.append((data.replaceFirst(OLESIP2Constants.ITEM_IDENTIFIER_CODE, "")).trim());
            }
        }
        builder.append(OLESIP2Constants.SPLIT+
                OLESIP2Constants.SCREEN_MSG_CODE);
        builder.append(OLESIP2Constants.ITEM_SERVICE_NOT_SUPPORTED);
        for (String data : requestDataArray) {
            if (data.startsWith(OLESIP2Constants.SEQUENCE_NUM_CODE)) {
                builder.append(OLESIP2Constants.SPLIT+
                        OLESIP2Constants.SEQUENCE_NUM_CODE);
                builder.append(data.substring(2, 5));
                builder.append(MessageUtil.computeChecksum(builder.toString()));
            }
        }
        System.out.println(builder.toString());


        return builder.toString() + '\r';
    }
}
