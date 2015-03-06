/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.select.businessobject;

import org.kuali.ole.module.purap.businessobject.PurApAccountingLine;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.math.BigDecimal;

public class OlePurchaseOrderAccount extends PurchaseOrderAccount {

    public OlePurchaseOrderAccount() {
        super();
    }
    private KualiDecimal existingAmount;

    public KualiDecimal getExistingAmount() {
        return existingAmount;
    }

    public void setExistingAmount(KualiDecimal existingAmount) {
        this.existingAmount = existingAmount;
    }

    public OlePurchaseOrderAccount(PurApAccountingLine purApAccountingLine) {
        super(purApAccountingLine);
        if (((OleRequisitionAccount) purApAccountingLine).getAmount() != null) {
            setAmount(((OleRequisitionAccount) purApAccountingLine).getAmount());
        } else {
            setAmount(KualiDecimal.ZERO);
        }
    }

}
