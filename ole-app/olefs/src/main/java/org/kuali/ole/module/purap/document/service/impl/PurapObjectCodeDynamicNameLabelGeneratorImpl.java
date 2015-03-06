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
package org.kuali.ole.module.purap.document.service.impl;

import org.kuali.ole.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.ole.module.purap.businessobject.PurApItemBase;
import org.kuali.ole.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.document.service.impl.ObjectCodeDynamicNameLabelGeneratorImpl;

public class PurapObjectCodeDynamicNameLabelGeneratorImpl extends ObjectCodeDynamicNameLabelGeneratorImpl {

    /**
     * Overrides the method in ObjectCodeDynamicNameLabelGeneratorImpl so that we could control whether
     * to display or hide the dynamic name label in certain conditions in purap documents.
     *
     * @see org.kuali.ole.sys.document.service.DynamicNameLabelGenerator#getDynamicNameLabelFieldName(org.kuali.ole.sys.businessobject.AccountingLine, java.lang.String)
     */
    @Override
    public String getDynamicNameLabelValue(AccountingLine line, String accountingLineProperty) {
        PurApAccountingLineBase purapLine = (PurApAccountingLineBase) line;
        PurApItemBase purapItem = purapLine.getPurapItem();
        PurchasingAccountsPayableDocumentBase purapDocument = null;
        if (purapItem != null) {
            purapDocument = purapItem.getPurapDocument();
        }
        if (purapItem == null || purapDocument == null || purapDocument.isInquiryRendered()) {
            return super.getDynamicNameLabelValue(line, accountingLineProperty);
        } else {
            return null;
        }
    }


}
