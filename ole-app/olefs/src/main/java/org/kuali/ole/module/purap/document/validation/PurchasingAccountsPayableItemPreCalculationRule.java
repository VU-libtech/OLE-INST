/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation;

import org.kuali.ole.module.purap.businessobject.PurApItem;

/**
 * Continue Purap Rule Interface
 * Defines a rule which gets invoked immediately before continuing to the next step during creation of a Transactional document.
 */
public interface PurchasingAccountsPayableItemPreCalculationRule {

    /**
     * Checks the rules that says percent must be 100% or
     * item total should be equal to the amount of accounts for that item.
     *
     * @param item the item to check
     * @return true if the business rules pass
     */
    public boolean checkPercentOrTotalAmountsEqual(PurApItem item);

}
