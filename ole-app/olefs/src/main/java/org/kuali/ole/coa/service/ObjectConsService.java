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
package org.kuali.ole.coa.service;

import org.kuali.ole.coa.businessobject.ObjectConsolidation;

/**
 * This service interface defines methods necessary for retrieving fully populated ObjectCons business objects from the database
 * that are necessary for transaction processing in the application. 
 */
public interface ObjectConsService {
    /**
     * Retrieves an object level object instance by its composite primary id.
     * 
     * @param chartOfAccountsCode
     * @param ObjectLevelCode
     * @return An ObjLevel object instance.
     */
    public ObjectConsolidation getByPrimaryId(String chartOfAccountsCode, String ObjectConsCode);
}
