/*
 * Copyright 2012 The Kuali Foundation.
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
package org.kuali.ole.vnd.businessobject.inquiry;

import java.util.List;
import java.util.Map;

import org.kuali.ole.sys.businessobject.inquiry.KfsInquirableImpl;
import org.kuali.ole.vnd.businessobject.DebarredVendorMatch;
import org.kuali.ole.vnd.businessobject.VendorAlias;
import org.kuali.rice.krad.bo.BusinessObject;

public class VendorExclusionInquirable extends KfsInquirableImpl {

    /**
     * @see org.kuali.rice.kns.inquiry.KualiInquirableImpl#getBusinessObject(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public BusinessObject getBusinessObject(Map fieldValues) {
        DebarredVendorMatch match = (DebarredVendorMatch)super.getBusinessObject(fieldValues);
        List<VendorAlias> vendorAliases = match.getVendorDetail().getVendorAliases();
        StringBuffer concatenatedAliases = new StringBuffer();
        for(VendorAlias alias : vendorAliases) {
            concatenatedAliases.append(alias.getVendorAliasName());
            concatenatedAliases.append("/n");
        }
        match.setConcatenatedAliases(concatenatedAliases.toString());
        return match;
    }
}
