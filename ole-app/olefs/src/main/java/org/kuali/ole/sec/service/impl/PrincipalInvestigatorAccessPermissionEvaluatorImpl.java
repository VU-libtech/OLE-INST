/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.sec.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.integration.cg.ContractsAndGrantsModuleService;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;

/**
 * Custom access permission evaluator for principal investigator restrictions
 */
public class PrincipalInvestigatorAccessPermissionEvaluatorImpl extends AccessPermissionEvaluatorImpl {

    /**
     * Matches on accounts for which the person is a principal investigator (project director)
     * 
     * @see org.kuali.ole.sec.service.impl.AccessPermissionEvaluatorImpl#isMatch(java.lang.String, java.lang.String)
     */
    @Override
    protected boolean isMatch(String matchValue, String value) {
        boolean match = false;

        String chartCode = (String) otherKeyFieldValues.get(OLEPropertyConstants.CHART_OF_ACCOUNTS_CODE);

        Person principalInvestigator = SpringContext.getBean(ContractsAndGrantsModuleService.class).getProjectDirectorForAccount(chartCode, value);
        if (StringUtils.equals(person.getPrincipalId(), principalInvestigator.getPrincipalId())) {
            match = true;
        }

        return match;
    }

}
