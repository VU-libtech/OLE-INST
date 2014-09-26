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
package org.kuali.ole.coa.businessobject.defaultvalue;

import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.FinancialSystemUserService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.valuefinder.ValueFinder;

/**
 * A value finder that returns the current user's default organization code.
 */
public class CurrentUserOrgValueFinder implements ValueFinder {

    /**
     * returns the current user's default organization code
     * 
     * @see org.kuali.rice.krad.valuefinder.ValueFinder#getValue()
     */
    public String getValue() {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        if (currentUser != null) {
            return SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, OLEConstants.ParameterNamespaces.CHART).getOrganizationCode();
        }
        else {
            return OLEConstants.EMPTY_STRING;
        }
    }

}

