/*
 * Copyright 2005-2006 The Kuali Foundation
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
package org.kuali.ole.coa.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.ole.coa.businessobject.SubObjectCode;
import org.kuali.ole.coa.service.SubObjectCodeService;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.NonTransactional;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This class is the service implementation for the SubObjectCode structure. This is the default implementation that gets delivered
 * with Kuali.
 */

@NonTransactional
public class SubObjectCodeServiceImpl implements SubObjectCodeService {
    private UniversityDateService universityDateService;

    /**
     * @see org.kuali.ole.coa.service.SubObjectCodeService#getByPrimaryId(java.lang.Integer, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public SubObjectCode getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode) {
        
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        keys.put(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        keys.put(OLEPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        keys.put(OLEPropertyConstants.FINANCIAL_OBJECT_CODE, financialObjectCode);
        keys.put(OLEPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, financialSubObjectCode);
        return (SubObjectCode)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(SubObjectCode.class, keys);
    }

    /**
     * @see org.kuali.ole.coa.service.SubObjectCodeService#getByPrimaryIdForCurrentYear(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public SubObjectCode getByPrimaryIdForCurrentYear(String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode) {
        return this.getByPrimaryId(universityDateService.getCurrentFiscalYear(), chartOfAccountsCode, accountNumber, financialObjectCode, financialSubObjectCode);
    }

    /**
     * 
     * This method injects UniversityDateService
     * @param universityDateService
     */
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }
}
