/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.ole.coa.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.ole.coa.businessobject.FederalFundedCode;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;

/**
 * This class returns list of {@link FederalFundedCode} key value pairs.
 */
public class FederalFundedCodeValuesFinder extends KeyValuesBase {

    /**
     * Creates a list of {@link FederalFundedCode} using their code as the key and their code "-" name
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {

        Collection<FederalFundedCode> codes = SpringContext.getBean(KeyValuesService.class).findAll(FederalFundedCode.class);
        List<KeyValue> labels = new ArrayList<KeyValue>();
        labels.add(new ConcreteKeyValue("", ""));
        for (FederalFundedCode federalFundedCode : codes) {
            if(federalFundedCode.isActive()) {
                labels.add(new ConcreteKeyValue(federalFundedCode.getCode(), federalFundedCode.getCodeAndDescription()));
            }
        }

        return labels;
    }

}
