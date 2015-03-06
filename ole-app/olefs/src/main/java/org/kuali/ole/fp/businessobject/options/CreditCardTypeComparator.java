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
package org.kuali.ole.fp.businessobject.options;

import java.util.Comparator;

import org.kuali.ole.fp.businessobject.CreditCardType;

public class CreditCardTypeComparator implements Comparator {

    public CreditCardTypeComparator() {
    }

    public int compare(Object c1, Object c2) {

        CreditCardType creditCardType1 = (CreditCardType) c1;
        CreditCardType creditCardType2 = (CreditCardType) c2;

        return creditCardType1.getFinancialDocumentCreditCardTypeCode().compareTo(creditCardType2.getFinancialDocumentCreditCardTypeCode());
    }

}
