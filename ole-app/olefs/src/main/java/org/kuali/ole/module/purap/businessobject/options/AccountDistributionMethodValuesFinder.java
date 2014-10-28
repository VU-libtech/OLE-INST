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
package org.kuali.ole.module.purap.businessobject.options;

import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.util.ArrayList;
import java.util.List;

/**
 * This class returns list of account distribution methods for amounts value pairs.
 */
public class AccountDistributionMethodValuesFinder extends KeyValuesBase {

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    @Override
    public List getKeyValues() {
        List keyValues = new ArrayList();
//        keyValues.add(new ConcreteKeyValue(PurapConstants.AccountDistributionMethodCodes.PROPORTIONAL_CODE, PurapConstants.AccountDistributionMethodCodes.PROPORTIONAL_DESCRIPTION));
//        keyValues.add(new ConcreteKeyValue(PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_CODE, PurapConstants.AccountDistributionMethodCodes.SEQUENTIAL_DESCRIPTION));

        return keyValues;
    }
}
