/*
 * Copyright 2006-2009 The Kuali Foundation
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

package org.kuali.ole.integration.cg.businessobject;

import org.kuali.ole.integration.cg.ContractAndGrantsProposal;
import org.kuali.ole.integration.cg.ContractsAndGrantsAward;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

/**
 * Defines a financial award object.
 */
public class Award implements ContractsAndGrantsAward {
    private static final String AWARD_INQUIRY_TITLE_PROPERTY = "message.inquiry.award.title";

    private Long proposalNumber;

    /**
     * Default no-args constructor.
     */
    public Award() {
    }


    /**
     * Gets the proposalNumber attribute.
     * 
     * @return Returns the proposalNumber
     */
    public Long getProposalNumber() {
        return proposalNumber;
    }

    /**
     * Sets the proposalNumber attribute.
     * 
     * @param proposalNumber The proposalNumber to set.
     */
    public void setProposalNumber(Long proposalNumber) {
        this.proposalNumber = proposalNumber;
    }
    
    /**
     * @return a String to represent this field on the inquiry
     */
    public String getAwardInquiryTitle() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(AWARD_INQUIRY_TITLE_PROPERTY);
    }


    public ContractAndGrantsProposal getProposal() {
        return null;
    }


    public void prepareForWorkflow() {}


    public void refresh() {}
}

