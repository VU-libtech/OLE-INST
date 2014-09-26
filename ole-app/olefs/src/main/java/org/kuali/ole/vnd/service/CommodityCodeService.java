/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.ole.vnd.service;

import org.kuali.ole.vnd.businessobject.CommodityCode;

/**
 * This interface defines methods that a Commodity Code Service must provide
 */
public interface CommodityCodeService {
    /**
     * Retrieves a commodity code object by its primary key - the purchasing commodity code.
     * 
     * @param  purchasingCommodityCode
     * @return CommodityCode the commodity code object which has the purchasingCommodityCode
     *         in the input parameter to match its the primary key.
     */
    public CommodityCode getByPrimaryId(String purchasingCommodityCode);

    /**
     * Checks whether the commodity code in wildcard form exists.
     * For example, if the wildCardCommodityCode in the input parameter is
     * 100* and the database has commodity code 10023, then this method 
     * will return true. If no matching found then this method returns false.
     * 
     * @param wildCardCommodityCode The string containing wild card character to be queried to the database.
     * 
     * @return boolean true if the wildcardCommodityCode exists in the database.
     */
    public boolean wildCardCommodityCodeExists(String wildCardCommodityCode);
}
