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

import org.kuali.ole.module.purap.PurapParameterConstants;
import org.kuali.ole.module.purap.PurapRuleConstants;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.ole.sys.service.impl.OleParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

public class PurchaseOrderNewIndividualItemValidation extends PurchasingNewIndividualItemValidation {

    PurchaseOrderEmptyItemWithAccountsValidation emptyItemsWithAccountsValidation;

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = super.validate(event);
        emptyItemsWithAccountsValidation.setItemForValidation((PurchaseOrderItem) super.getItemForValidation());
        valid &= emptyItemsWithAccountsValidation.validate(event);

        return valid;
    }

    @Override
    protected boolean commodityCodeIsRequired() {
        //if the ENABLE_COMMODITY_CODE_IND parameter is  N then we don't
        //need to check for the ITEMS_REQUIRE_COMMODITY_CODE_IND parameter anymore, just return false. 
        boolean enableCommodityCode = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(OleParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_COMMODITY_CODE_IND);
        if (!enableCommodityCode) {
            return false;
        } else {
            return getParameterService().getParameterValueAsBoolean(PurchaseOrderDocument.class, PurapRuleConstants.ITEMS_REQUIRE_COMMODITY_CODE_IND);
        }
    }

    public PurchaseOrderEmptyItemWithAccountsValidation getEmptyItemsWithAccountsValidation() {
        return emptyItemsWithAccountsValidation;
    }

    public void setEmptyItemsWithAccountsValidation(PurchaseOrderEmptyItemWithAccountsValidation emptyItemsWithAccountsValidation) {
        this.emptyItemsWithAccountsValidation = emptyItemsWithAccountsValidation;
    }

}
