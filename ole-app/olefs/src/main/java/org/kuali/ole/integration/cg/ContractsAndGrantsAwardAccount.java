/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.integration.cg;

import org.kuali.rice.krad.bo.ExternalizableBusinessObject;

public interface ContractsAndGrantsAwardAccount extends ExternalizableBusinessObject {

    public long getAwardId();
    
    public String getAwardTitle();
    
    public String getErrorMessage();
    
    public boolean getFederalSponsor();
    
    public String getGrantNumber();
    
    public long getInstitutionalproposalId();
    
    public String getProjectDirector();
    
    public String getProposalFederalPassThroughAgencyNumber();
    
    public String getProposalNumber();
    
    public String getSponsorCode();
    
    public String getSponsorName();

    public String getPrimeSponsorCode();
    
    public String getPrimeSponsorName();

}
