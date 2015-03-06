/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.coa.identity;

import java.util.Map;

import org.kuali.ole.sys.identity.OleKimAttributes;

public class OrganizationOptionalHierarchyRoleTypeServiceImpl extends OrganizationHierarchyAwareRoleTypeServiceBase {
    public static final String DESCEND_HIERARCHY_TRUE_VALUE = "Y";
    public static final String DESCEND_HIERARCHY_FALSE_VALUE = "N";
    private boolean qualificationDeterminesDescendHierarchy;

    /***
     * @see org.kuali.rice.kim.service.support.impl.KimTypeInfoServiceBase#performMatch(org.kuali.rice.kim.bo.types.dto.AttributeSet,
     *      org.kuali.rice.kim.bo.types.dto.AttributeSet)
     */
    @Override
    protected boolean performMatch(Map<String,String> qualification, Map<String,String> roleQualifier) {
        // if no qualification is passed, then we have no basis to reject this
        // (if a null is let through, then we get an NPE below) 
        if ( qualification == null || qualification.isEmpty() || roleQualifier == null || roleQualifier.isEmpty() ) {
            return true;
        }
        String descendHierarchy = null;
        if (qualificationDeterminesDescendHierarchy) {
            descendHierarchy = qualification.get(OleKimAttributes.DESCEND_HIERARCHY);
        }
        else {
            descendHierarchy = roleQualifier.get(OleKimAttributes.DESCEND_HIERARCHY);
        }
        return isParentOrg(qualification.get(OleKimAttributes.CHART_OF_ACCOUNTS_CODE), 
                qualification.get(OleKimAttributes.ORGANIZATION_CODE), 
                roleQualifier.get(OleKimAttributes.CHART_OF_ACCOUNTS_CODE), 
                roleQualifier.get(OleKimAttributes.ORGANIZATION_CODE), DESCEND_HIERARCHY_TRUE_VALUE.equals(descendHierarchy));
    }

    public void setQualificationDeterminesDescendHierarchy(boolean qualificationDeterminesDescendHierarchy) {
        this.qualificationDeterminesDescendHierarchy = qualificationDeterminesDescendHierarchy;
    }


}
