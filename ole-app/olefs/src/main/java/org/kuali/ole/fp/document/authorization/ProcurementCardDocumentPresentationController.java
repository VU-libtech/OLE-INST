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
package org.kuali.ole.fp.document.authorization;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.ole.sys.document.authorization.AccountingDocumentPresentationControllerBase;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;

public class ProcurementCardDocumentPresentationController extends AccountingDocumentPresentationControllerBase {

    /**
     * @see org.kuali.rice.krad.document.authorization.DocumentPresentationControllerBase#canCancel(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canCancel(Document document) {
        return false;
    }

    /**
     * @see org.kuali.rice.krad.document.authorization.DocumentPresentationControllerBase#canCopy(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canCopy(Document document) {
        return false;
    }

    /**
     * @see org.kuali.rice.krad.document.authorization.DocumentPresentationControllerBase#canDisapprove(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canDisapprove(Document document) {
        return false;
    }

    /**
     * @see org.kuali.rice.krad.document.authorization.DocumentPresentationControllerBase#canEdit(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canEdit(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        //DocumentType
        boolean canRouteReviewFullEdit = false;
        for (String routeNode : workflowDocument.getCurrentNodeNames() ) {
            if ( StringUtils.equalsIgnoreCase(routeNode, OLEConstants.RouteLevelNames.ACCOUNT_REVIEW_FULL_EDIT ) ) {
                canRouteReviewFullEdit = true;
                break;
            }
        }

        // FULL_ENTRY only if: a) person has an approval request, b) we are at the correct level, c) it's not a correction document,
        // d) it is not an ADHOC request (important so that ADHOC don't get full entry).
        if (canRouteReviewFullEdit
                && (((FinancialSystemDocumentHeader) document.getDocumentHeader()).getFinancialDocumentInErrorNumber() == null)
                && workflowDocument.isApprovalRequested()
                && !workflowDocument.isAcknowledgeRequested()) {
            return true;
        }

        return super.canEdit(document);
    }
}
