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
package org.kuali.ole.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.impl.OleParameterConstants;
import org.kuali.ole.vnd.VendorParameterConstants;
import org.kuali.ole.vnd.businessobject.VendorType;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;

public class ExclusionVendorTypeValuesFinder extends KeyValuesBase{

    private static List<KeyValue> labels = null;

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    @Override
    public List<KeyValue> getKeyValues() {
        if (labels == null) {
            synchronized (this.getClass()) {
                    KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
                    Collection<VendorType> codes = boService.findAll(VendorType.class);
                    Collection<String> exclusionCodes = SpringContext.getBean(ParameterService.class).getParameterValuesAsString( OleParameterConstants.VENDOR_LOOKUP.class, VendorParameterConstants.EXCLUSION_AND_DEBARRED_VENDOR_TYPES);
                    List<KeyValue> tempLabels = new ArrayList<KeyValue>();
                    for (VendorType vt : codes) {
                        if (vt.isActive() && exclusionCodes.contains(vt.getVendorTypeCode())) {
                            tempLabels.add(new ConcreteKeyValue(vt.getVendorTypeCode(), vt.getVendorTypeDescription()));
                        }
                    }
                    labels = tempLabels;
            }
        }
        return labels;
    }
}
