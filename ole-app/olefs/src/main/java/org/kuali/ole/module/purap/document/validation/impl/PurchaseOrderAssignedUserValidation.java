/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.module.purap.PurapConstants;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.PurapPropertyConstants;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;

public class PurchaseOrderAssignedUserValidation extends GenericValidation {
    private PersonService personService;

    /**
     * Validation to check that the assigned user exists in the system.
     *
     * @return boolean false if the assigned user doesn't exist in the system.
     */
    public boolean validate(AttributedDocumentEvent event) {
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument) event.getDocument();
        MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(PurapConstants.DETAIL_TAB_ERRORS);
        boolean valid = true;

        // assigned user is not a required field
        String principalName = poDocument.getAssignedUserPrincipalName();
        if (StringUtils.isEmpty(principalName))
            return true;

        // check to see if the person exists in the database
        //if (ObjectUtils.isNull(personService.getPersonByPrincipalName(principalName))) {
        // the following if is equivalent to the above if, since the name->ID conversion is done when the PO form is submit
        // so if ID is null that means no person is found by the name
        if (ObjectUtils.isNull(poDocument.getAssignedUserPrincipalId())) {
            valid = false;
            errorMap.putError(PurapPropertyConstants.ASSIGNED_USER_PRINCIPAL_NAME, PurapKeyConstants.ERROR_NONEXIST_ASSIGNED_USER);
        }

        errorMap.clearErrorPath();
        return valid;
    }

    /**
     * @return Returns the personService.
     */
    protected PersonService getPersonService() {
        if (personService == null)
            personService = SpringContext.getBean(PersonService.class);
        return personService;
    }

}
