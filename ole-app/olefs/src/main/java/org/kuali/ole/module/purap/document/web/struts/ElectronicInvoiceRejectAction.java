/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.web.struts;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.ole.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.util.GlobalVariables;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Struts Action for Electronic invoice document.
 */
public class ElectronicInvoiceRejectAction extends FinancialSystemTransactionalDocumentActionBase {

    public ActionForward startResearch(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        ElectronicInvoiceRejectForm electronicInvoiceRejectForm = (ElectronicInvoiceRejectForm) form;
        ElectronicInvoiceRejectDocument eirDocument = (ElectronicInvoiceRejectDocument) electronicInvoiceRejectForm.getDocument();
        eirDocument.setInvoiceResearchIndicator(true);

        Note noteObj = getDocumentService().createNoteFromDocument(eirDocument, "Research started by: " + GlobalVariables.getUserSession().getPerson().getName());
        PersistableBusinessObject noteParent = eirDocument.getNoteTarget();
        List<Note> noteList = getNoteService().getByRemoteObjectId(noteParent.getObjectId());
        noteList.add(noteObj);
        getNoteService().saveNoteList(noteList);
        getNoteService().save(noteObj);
        getDocumentService().saveDocument(eirDocument);

        return mapping.findForward(OLEConstants.MAPPING_BASIC);
    }

    public ActionForward completeResearch(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        ElectronicInvoiceRejectForm electronicInvoiceRejectForm = (ElectronicInvoiceRejectForm) form;
        ElectronicInvoiceRejectDocument eirDocument = (ElectronicInvoiceRejectDocument) electronicInvoiceRejectForm.getDocument();
        eirDocument.setInvoiceResearchIndicator(false);

        Note noteObj = getDocumentService().createNoteFromDocument(eirDocument, "Research completed by: " + GlobalVariables.getUserSession().getPerson().getName());
        PersistableBusinessObject noteParent = eirDocument.getNoteTarget();
        List<Note> noteList = this.getNoteService().getByRemoteObjectId(noteParent.getObjectId());
        noteList.add(noteObj);
        getNoteService().saveNoteList(noteList);

        getNoteService().save(noteObj);
        getDocumentService().saveDocument(eirDocument);

        return mapping.findForward(OLEConstants.MAPPING_BASIC);

    }

}

