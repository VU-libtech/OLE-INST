/*
 * Copyright 2005 The Kuali Foundation
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

import org.kuali.ole.coa.businessobject.SubObjectCode;

/**
 * This interface defines methods that a SubObjectCode Service must provide.
 */
public interface SubObjectCodeService {
    /**
     * Specifies what a method that searches based on the Primary Key of an SubObjectCode object must look like.
     * 
     * @param universityFiscalYear
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param financialObjectCode
     * @param financialSubObjectCode
     * @return SubObjectCode
     */
    public SubObjectCode getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode);

    /**
     * This method returns an financial object code for the current fiscal year.
     * 
     * @param chartOfAccountsCode chart of accounts code for object code
     * @param financialObjectCode financial object code
     * @return the object code specified
     */
    public SubObjectCode getByPrimaryIdForCurrentYear(String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode);
}
