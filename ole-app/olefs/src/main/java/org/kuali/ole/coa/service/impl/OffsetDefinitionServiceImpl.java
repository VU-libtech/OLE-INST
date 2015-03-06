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
package org.kuali.ole.coa.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.ole.coa.businessobject.OffsetDefinition;
import org.kuali.ole.coa.service.OffsetDefinitionService;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.NonTransactional;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This class is the service implementation for the OffsetDefinition structure. This is the default implementation, that is
 * delivered with Kuali.
 */

@NonTransactional
public class OffsetDefinitionServiceImpl implements OffsetDefinitionService {

    /**
     * @see org.kuali.ole.coa.service.OffsetDefinitionService#getByPrimaryId(java.lang.Integer, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public OffsetDefinition getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String financialDocumentTypeCode, String financialBalanceTypeCode) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        keys.put(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        keys.put(OLEPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, financialDocumentTypeCode);
        keys.put(OLEPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, financialBalanceTypeCode);
        return (OffsetDefinition)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(OffsetDefinition.class, keys);
    }

}
