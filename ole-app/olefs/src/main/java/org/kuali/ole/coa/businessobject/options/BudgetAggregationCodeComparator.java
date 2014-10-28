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

import java.util.Comparator;

import org.kuali.ole.coa.businessobject.BudgetAggregationCode;

/**
 * This class allows us to compare two {@link BudgetAggregationCode} objects using their codes for comparison
 */
public class BudgetAggregationCodeComparator implements Comparator {

    public BudgetAggregationCodeComparator() {
    }

    /**
     * compares two {@link BudgetAggregationCode}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {

        BudgetAggregationCode obj1 = (BudgetAggregationCode) o1;
        BudgetAggregationCode obj2 = (BudgetAggregationCode) o2;

        return obj1.getCode().compareTo(obj2.getCode());
    }

}
