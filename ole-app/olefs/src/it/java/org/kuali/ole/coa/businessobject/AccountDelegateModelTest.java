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
package org.kuali.ole.coa.businessobject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kuali.ole.KualiTestBase;
import org.kuali.ole.coa.businessobject.AccountDelegateGlobalDetail;
import org.kuali.ole.coa.businessobject.AccountDelegateModelDetail;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class AccountDelegateModelTest extends KualiTestBase {

    AccountDelegateModelDetail model;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(OLEConstants.CHART_OF_ACCOUNTS_CODE_PROPERTY_NAME, "BL");
        fieldValues.put(OLEConstants.ORGANIZATION_CODE_PROPERTY_NAME, "CLAS");
        List<AccountDelegateModelDetail> results = (List<AccountDelegateModelDetail>) SpringContext.getBean(BusinessObjectService.class).findMatching(AccountDelegateModelDetail.class, fieldValues);
        assertFalse("no models found", results.isEmpty());

        model = results.get(0);
    }

    @Test
    public void testSaveModel() {
        String name = model.getAccountDelegateModelName();
        AccountDelegateModelDetail routingModel = new AccountDelegateModelDetail();
        routingModel.setAccountDelegateModelName(name);
        routingModel.setChartOfAccountsCode(model.getChartOfAccountsCode());
        routingModel.setOrganizationCode(model.getOrganizationCode());
        routingModel.setAccountDelegateUniversalId(model.getAccountDelegateUniversalId());
        routingModel.setFinancialDocumentTypeCode("GDLM");
        SpringContext.getBean(BusinessObjectService.class).save(routingModel);

        assertTrue(loadModel(name, model.getClass()));
    }

    private boolean loadModel(String name, Class clazz) {

        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("accountDelegateModelName", name);

        Collection<AccountDelegateModelDetail> foundModel = SpringContext.getBean(BusinessObjectService.class).findMatching(clazz, fieldValues);

//        List<AccountDelegateGlobalDetail> delegateGlobals = new ArrayList<AccountDelegateGlobalDetail>();
//
//        for (AccountDelegateModelDetail model : foundModel) {
//            delegateGlobals.add(new AccountDelegateGlobalDetail(model));
//        }

        return (foundModel != null && !foundModel.isEmpty());

    }


}
