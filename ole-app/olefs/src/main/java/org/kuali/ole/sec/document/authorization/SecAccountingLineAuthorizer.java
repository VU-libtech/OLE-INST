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
package org.kuali.ole.sec.document.authorization;

import java.util.List;
import java.util.Set;

import org.kuali.ole.sec.service.AccessSecurityService;
import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.AccountingDocument;
import org.kuali.ole.sys.document.authorization.AccountingLineAuthorizer;
import org.kuali.ole.sys.document.web.AccountingLineRenderingContext;
import org.kuali.ole.sys.document.web.AccountingLineViewAction;
import org.kuali.rice.kim.api.identity.Person;


/**
 * AccountingLineAuthorizer that wraps access security checks around another AccountingLineAuthorizer configured for the document type
 */
public class SecAccountingLineAuthorizer implements AccountingLineAuthorizer {
    protected AccountingLineAuthorizer lineAuthorizer;

    public List<AccountingLineViewAction> getActions(AccountingDocument accountingDocument, AccountingLineRenderingContext accountingLineRenderingContext, String accountingLinePropertyName, Integer lineIndex, Person currentUser, String groupTitle) {
        return lineAuthorizer.getActions(accountingDocument, accountingLineRenderingContext, accountingLinePropertyName, lineIndex, currentUser, groupTitle);
    }

    public Set<String> getUnviewableBlocks(AccountingDocument accountingDocument, AccountingLine accountingLine, boolean newLine, Person currentUser) {
        return lineAuthorizer.getUnviewableBlocks(accountingDocument, accountingLine, newLine, currentUser);
    }

    /**
     * Makes call to check edit access security on accounting line
     * 
     * @see org.kuali.ole.sys.document.authorization.AccountingLineAuthorizer#hasEditPermissionOnAccountingLine
     */
    public boolean hasEditPermissionOnAccountingLine(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, Person currentUser, boolean pageIsEditable) {
        boolean hasEditPermission = lineAuthorizer.hasEditPermissionOnAccountingLine(accountingDocument, accountingLine, accountingLineCollectionProperty, currentUser, pageIsEditable);
        
        if (hasEditPermission) {
            hasEditPermission = SpringContext.getBean(AccessSecurityService.class).canEditDocumentAccountingLine(accountingDocument, accountingLine, currentUser);
        }
            
        return hasEditPermission;
    }

    /**
     * If access was granted to line and line authorizer allows field modify then allow field modify
     * 
     * @see org.kuali.ole.sys.document.authorization.AccountingLineAuthorizer#hasEditPermissionOnField
     */
    public boolean hasEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, String fieldName, boolean editableLine, boolean editablePage, Person currentUser) {
        boolean hasEditPermission = lineAuthorizer.hasEditPermissionOnField(accountingDocument, accountingLine, accountingLineCollectionProperty, fieldName, editableLine, editablePage, currentUser);
        
        return hasEditPermission && editableLine;
    }

    public boolean isGroupEditable(AccountingDocument accountingDocument, List<? extends AccountingLineRenderingContext> accountingLineRenderingContexts, Person currentUser) {
        return lineAuthorizer.isGroupEditable(accountingDocument, accountingLineRenderingContexts, currentUser);
    }

    public boolean renderNewLine(AccountingDocument accountingDocument, String accountingGroupProperty) {
        return lineAuthorizer.renderNewLine(accountingDocument, accountingGroupProperty);
    }

    /**
     * Sets the lineAuthorizer attribute value.
     * 
     * @param lineAuthorizer The lineAuthorizer to set.
     */
    public void setLineAuthorizer(AccountingLineAuthorizer lineAuthorizer) {
        this.lineAuthorizer = lineAuthorizer;
    }


}
