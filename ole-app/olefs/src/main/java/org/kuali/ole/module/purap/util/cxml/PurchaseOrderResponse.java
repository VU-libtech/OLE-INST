/*
 * Copyright 2006-2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.util.cxml;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderResponse extends B2BShoppingCartBase {

    private List errors = new ArrayList();

    public void addPOResponseErrorMessage(String errorText) {
        if (StringUtils.isNotEmpty(errorText)) {
            errors.add(errorText);
        }
    }

    public List getPOResponseErrorMessages() {

        if (!isSuccess()) {
            return errors;
        } else {
            return null;
        }
    }

    public String toString() {

        ToStringBuilder toString = new ToStringBuilder(this);
        toString.append("StatusCode", getStatusCode());
        toString.append("StatusText", getStatusText());
        toString.append("isSuccess", isSuccess());
        toString.append("Errors", getPOResponseErrorMessages());

        return toString.toString();
    }
}
