/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.fp.document.validation.impl;

import org.kuali.ole.fp.document.CashReceiptDocument;
import org.kuali.ole.fp.document.CashReceiptFamilyBase;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.document.validation.RouteNodeValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.util.GlobalVariables;

public class CashReceiptCashManagerValidation extends RouteNodeValidation {

    private CashReceiptFamilyBase cashReceiptDocumentForValidation;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
      //check whether cash manager confirmed amount equals to the old amount
        CashReceiptDocument crDoc = (CashReceiptDocument) getCashReceiptDocumentForValidation();
         if ((crDoc.getTotalDollarAmount().compareTo(crDoc.getTotalConfirmedCashAmount().add(crDoc.getTotalConfirmedCheckAmount()).add(crDoc.getTotalConfirmedCoinAmount()))) != 0) {
            GlobalVariables.getMessageMap().putError(OLEConstants.GLOBAL_ERRORS, OLEKeyConstants.CashReceipt.ERROR_CONFIRMED_TOTAL, crDoc.getTotalDollarAmount().toString());
            return false;
        }

        return true;

    }

    public CashReceiptFamilyBase getCashReceiptDocumentForValidation() {
        return cashReceiptDocumentForValidation;
    }

    public void setCashReceiptDocumentForValidation(CashReceiptFamilyBase cashReceiptDocumentForValidation) {
        this.cashReceiptDocumentForValidation = cashReceiptDocumentForValidation;
    }


}
