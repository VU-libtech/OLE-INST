/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.module.purap.document.PaymentRequestDocument;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.GlobalVariables;

import java.util.List;

public class LineItemQuantityNotZero extends GenericValidation {

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;

        PaymentRequestDocument document = (PaymentRequestDocument) event.getDocument();
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(OLEPropertyConstants.DOCUMENT);

        int i = 0;
        for (PurApItem item : (List<PurApItem>) document.getItems()) {
            KualiDecimal itemQuantity = item.getItemQuantity();
            if (itemQuantity != null) {
                if (!itemQuantity.isNonZero()) {
                    GlobalVariables.getMessageMap().putError("item[" + i + "].itemQuantity", PurapKeyConstants.ERROR_PAYMENT_REQUEST_LINE_ITEM_QUANTITY_ZERO);
                    GlobalVariables.getMessageMap().clearErrorPath();

                    valid = false;
                    break;
                }
                i++;
            }
        }

        return valid;
    }

}
