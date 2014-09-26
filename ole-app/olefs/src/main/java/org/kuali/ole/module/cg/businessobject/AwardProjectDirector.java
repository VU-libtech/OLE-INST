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

package org.kuali.ole.module.cg.businessobject;

import java.util.LinkedHashMap;

import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * This class represents an association between an award and a project director. It's like a reference to the project director from
 * the award. This way an award can maintain a collection of these references instead of owning project directors directly.
 */
public class AwardProjectDirector extends PersistableBusinessObjectBase implements Primaryable, CGProjectDirector, MutableInactivatable {

    private String principalId;
    private Long proposalNumber;
    private boolean awardPrimaryProjectDirectorIndicator;
    private String awardProjectDirectorProjectTitle;
    private boolean active = true;

    private Person projectDirector;

    private final String userLookupRoleNamespaceCode = OLEConstants.ParameterNamespaces.OLE;
    private final String userLookupRoleName = OLEConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR;

    /**
     * Default no-args constructor.
     */
    public AwardProjectDirector() {
    }

    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#getPrincipalId()
     */
    @Override
    public String getPrincipalId() {
        return principalId;
    }

    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#setPrincipalId(java.lang.String)
     */
    @Override
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }


    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#getProposalNumber()
     */
    @Override
    public Long getProposalNumber() {
        return proposalNumber;
    }

    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#setProposalNumber(java.lang.Long)
     */
    @Override
    public void setProposalNumber(Long proposalNumber) {
        this.proposalNumber = proposalNumber;
    }


    /**
     * Gets the awardPrimaryProjectDirectorIndicator attribute.
     *
     * @return Returns the awardPrimaryProjectDirectorIndicator
     */
    public boolean isAwardPrimaryProjectDirectorIndicator() {
        return awardPrimaryProjectDirectorIndicator;
    }


    /**
     * Sets the awardPrimaryProjectDirectorIndicator attribute.
     *
     * @param awardPrimaryProjectDirectorIndicator The awardPrimaryProjectDirectorIndicator to set.
     */
    public void setAwardPrimaryProjectDirectorIndicator(boolean awardPrimaryProjectDirectorIndicator) {
        this.awardPrimaryProjectDirectorIndicator = awardPrimaryProjectDirectorIndicator;
    }


    /**
     * Gets the awardProjectDirectorProjectTitle attribute.
     *
     * @return Returns the awardProjectDirectorProjectTitle
     */
    public String getAwardProjectDirectorProjectTitle() {
        return awardProjectDirectorProjectTitle;
    }

    /**
     * Sets the awardProjectDirectorProjectTitle attribute.
     *
     * @param awardProjectDirectorProjectTitle The awardProjectDirectorProjectTitle to set.
     */
    public void setAwardProjectDirectorProjectTitle(String awardProjectDirectorProjectTitle) {
        this.awardProjectDirectorProjectTitle = awardProjectDirectorProjectTitle;
    }

    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#getProjectDirector()
     */
    @Override
    public Person getProjectDirector() {
        projectDirector = SpringContext.getBean(org.kuali.rice.kim.api.identity.PersonService.class).updatePersonIfNecessary(principalId, projectDirector);
        return projectDirector;
    }

    /**
     * @see org.kuali.ole.module.cg.businessobject.CGProjectDirector#setProjectDirector(org.kuali.ole.module.cg.businessobject.ProjectDirector)
     */
    @Override
    public void setProjectDirector(Person projectDirector) {
        this.projectDirector = projectDirector;
    }

    /**
     * @see Primaryable#isPrimary()
     */
    @Override
    public boolean isPrimary() {
        return isAwardPrimaryProjectDirectorIndicator();
    }

    /**
     * @see org.kuali.rice.core.api.mo.common.active.MutableInactivatable#isActive()
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * @see org.kuali.rice.core.api.mo.common.active.MutableInactivatable#setActive(boolean)
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserLookupRoleNamespaceCode() {
        return userLookupRoleNamespaceCode;
    }

    public void setUserLookupRoleNamespaceCode(String userLookupRoleNamespaceCode) {
    }

    public String getUserLookupRoleName() {
        return userLookupRoleName;
    }

    public void setUserLookupRoleName(String userLookupRoleName) {
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")

    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("principalId", this.principalId);
        if (this.proposalNumber != null) {
            m.put("proposalNumber", this.proposalNumber.toString());
        }
        return m;
    }

}

