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
package org.kuali.ole.coa.dataaccess;

import java.util.Collection;

import org.kuali.ole.coa.businessobject.BalanceType;

/**
 * This interface defines what methods of data retrieval should be allowed for {@link org.kuali.ole.coa.businessobject.BalanceTyp}
 */
public interface BalanceTypeDao {
    /**
     * Get Encumbrance balance type codes
     * 
     * @return
     */
    public Collection<BalanceType> getEncumbranceBalanceTypes();
}
