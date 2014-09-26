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

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * The type of {@link Award} associated with a {@link Proposal} instance.
 */
public class ProposalAwardType extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String proposalAwardTypeCode;
    private String proposalAwardTypeDescription;
    private boolean active;

    /**
     * Default constructor.
     */
    public ProposalAwardType() {
    }

    /**
     * Gets the proposalAwardTypeCode attribute.
     * 
     * @return Returns the proposalAwardTypeCode
     */
    public String getProposalAwardTypeCode() {
        return proposalAwardTypeCode;
    }

    /**
     * Sets the proposalAwardTypeCode attribute.
     * 
     * @param proposalAwardTypeCode The proposalAwardTypeCode to set.
     */
    public void setProposalAwardTypeCode(String proposalAwardTypeCode) {
        this.proposalAwardTypeCode = proposalAwardTypeCode;
    }


    /**
     * Gets the proposalAwardTypeDescription attribute.
     * 
     * @return Returns the proposalAwardTypeDescription
     */
    public String getProposalAwardTypeDescription() {
        return proposalAwardTypeDescription;
    }

    /**
     * Sets the proposalAwardTypeDescription attribute.
     * 
     * @param proposalAwardTypeDescription The proposalAwardTypeDescription to set.
     */
    public void setProposalAwardTypeDescription(String proposalAwardTypeDescription) {
        this.proposalAwardTypeDescription = proposalAwardTypeDescription;
    }

    /**
     * Gets the active attribute.
     * 
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute value.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("proposalAwardTypeCode", this.proposalAwardTypeCode);
        return m;
    }
}
