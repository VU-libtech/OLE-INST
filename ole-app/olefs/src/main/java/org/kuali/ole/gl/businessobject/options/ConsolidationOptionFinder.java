/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.ole.gl.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.ole.gl.Constant;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.valuefinder.ValueFinder;

/**
 * An implementation of ValueFinder that allows balance inquiries to choose between consolidated results or detailed results
 */
public class ConsolidationOptionFinder extends KeyValuesBase implements ValueFinder {

    /**
     * Returns a list of key value pairs to allow inquirers to choose between consolidated results or detailed results
     * 
     * @return a List of key value pairs to poplulate radio buttons
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List labels = new ArrayList();
        labels.add(new ConcreteKeyValue(Constant.CONSOLIDATION, Constant.CONSOLIDATION));
        labels.add(new ConcreteKeyValue(Constant.DETAIL, Constant.DETAIL));
        labels.add(new ConcreteKeyValue(Constant.EXCLUDE_SUBACCOUNTS, Constant.EXCLUDE_SUBACCOUNTS));

        return labels;
    }

    /**
     * Gets the default value for this ValueFinder, in this case CONSOLIDATED
     * @return a String with the default value for this ValueFinder
     * @see org.kuali.rice.krad.valuefinder.ValueFinder#getValue()
     */
    public String getValue() {
        return Constant.CONSOLIDATION;
    }
}
