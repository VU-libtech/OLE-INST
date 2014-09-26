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
 * Represents the status of a {@link Proposal}.
 */
public class ProposalStatus extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String proposalStatusCode;
    private String proposalStatusDescription;
    private boolean active;

    /**
     * Default constructor.
     */
    public ProposalStatus() {
    }

    /**
     * Gets the proposalStatusCode attribute.
     * 
     * @return Returns the proposalStatusCode
     */
    public String getProposalStatusCode() {
        return proposalStatusCode;
    }

    /**
     * Sets the proposalStatusCode attribute.
     * 
     * @param proposalStatusCode The proposalStatusCode to set.
     */
    public void setProposalStatusCode(String proposalStatusCode) {
        this.proposalStatusCode = proposalStatusCode;
    }


    /**
     * Gets the proposalStatusDescription attribute.
     * 
     * @return Returns the proposalStatusDescription
     */
    public String getProposalStatusDescription() {
        return proposalStatusDescription;
    }

    /**
     * Sets the proposalStatusDescription attribute.
     * 
     * @param proposalStatusDescription The proposalStatusDescription to set.
     */
    public void setProposalStatusDescription(String proposalStatusDescription) {
        this.proposalStatusDescription = proposalStatusDescription;
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
        m.put("proposalStatusCode", this.proposalStatusCode);
        return m;
    }

}
