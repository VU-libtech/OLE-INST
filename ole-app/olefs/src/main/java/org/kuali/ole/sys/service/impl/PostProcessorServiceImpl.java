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
package org.kuali.ole.sys.service.impl;

import org.kuali.ole.select.document.service.OleSelectDocumentService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PostProcessorServiceImpl extends org.kuali.rice.krad.service.impl.PostProcessorServiceImpl {

    /**
     * Overrides the baseline method so that the KFS document postprocessor uses
     * the "kfs" user instead of the "kr" user.
     * 
     * @see org.kuali.rice.krad.service.impl.PostProcessorServiceImpl#establishPostProcessorUserSession()
     */

    private OleSelectDocumentService oleSelectDocumentService;

    @Override
    protected UserSession establishPostProcessorUserSession() throws WorkflowException {
        if (GlobalVariables.getUserSession() == null) {
            return new UserSession(getOleSelectDocumentService().getSelectParameterValue(OLEConstants.SYSTEM_USER));
        } else {
            return GlobalVariables.getUserSession();
        }
    }

    public OleSelectDocumentService getOleSelectDocumentService() {
        if(oleSelectDocumentService == null){
            oleSelectDocumentService = SpringContext.getBean(OleSelectDocumentService.class);
        }
        return oleSelectDocumentService;
    }

    public void setOleSelectDocumentService(OleSelectDocumentService oleSelectDocumentService) {
        this.oleSelectDocumentService = oleSelectDocumentService;
    }
}
