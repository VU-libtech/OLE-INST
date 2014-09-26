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
package org.kuali.ole.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * 
 */
public class AccountType extends PersistableBusinessObjectBase implements MutableInactivatable {

    /**
     * Default no-arg constructor.
     */
    public AccountType() {

    }

    private String accountTypeCode;
    private String accountTypeName;
    private boolean active;

    /**
     * Gets the accountTypeCode attribute.
     * 
     * @return Returns the accountTypeCode
     */
    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    /**
     * Sets the accountTypeCode attribute.
     * 
     * @param accountTypeCode The accountTypeCode to set.
     */
    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    /**
     * Gets the accountTypeName attribute.
     * 
     * @return Returns the accountTypeName
     */
    public String getAccountTypeName() {
        return accountTypeName;
    }

    /**
     * Sets the accountTypeName attribute.
     * 
     * @param accountTypeName The accountTypeName to set.
     */
    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("accountTypeCode", this.accountTypeCode);
        return m;
    }

    /**
     * Gets the active attribute. 
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute value.
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
