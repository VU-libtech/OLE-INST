/*
 * Copyright 2008-2009 The Kuali Foundation
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

import org.kuali.ole.module.purap.document.PaymentRequestDocument;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;

public class PaymentRequestGrandTotalOverZeroValidation extends GenericValidation {

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PaymentRequestDocument document = (PaymentRequestDocument) event.getDocument();
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(OLEPropertyConstants.DOCUMENT);

        // The Grand Total Amount must be greater than zero. commented for OLE-2657
       /* if (document.getGrandTotal().compareTo(KualiDecimal.ZERO) <= 0) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.GRAND_TOTAL, PurapKeyConstants.ERROR_PAYMENT_REQUEST_GRAND_TOTAL_NOT_POSITIVE);
            valid &= false;
        }*/

        GlobalVariables.getMessageMap().clearErrorPath();

        //always returns true, as this is a warning, not an error
        return valid;
    }

}
