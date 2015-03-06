package org.kuali.ole.describe.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.OLEPropertyConstants;
import org.kuali.ole.deliver.bo.OlePatronDocument;
import org.kuali.ole.describe.bo.*;
import org.kuali.ole.describe.bo.marc.structuralfields.LeaderField;
import org.kuali.ole.describe.bo.marc.structuralfields.controlfield006.ControlField006;
import org.kuali.ole.describe.bo.marc.structuralfields.controlfield008.ControlField008;
import org.kuali.ole.describe.bo.marc.structuralfields.controlfield007.ControlField007;
import org.kuali.ole.describe.form.EditorForm;
import org.kuali.ole.describe.form.WorkBibMarcForm;
import org.kuali.ole.describe.form.WorkEInstanceOlemlForm;
import org.kuali.ole.describe.form.WorkInstanceOlemlForm;
import org.kuali.ole.describe.service.DiscoveryHelperService;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.BibTree;
import org.kuali.ole.docstore.common.document.config.DocFieldConfig;
import org.kuali.ole.docstore.common.document.config.DocFormatConfig;
import org.kuali.ole.docstore.common.document.config.DocTypeConfig;
import org.kuali.ole.docstore.common.document.config.DocumentSearchConfig;
import org.kuali.ole.docstore.common.document.content.instance.*;
import org.kuali.ole.docstore.common.search.*;
import org.kuali.ole.docstore.model.bo.WorkBibDocument;
import org.kuali.ole.docstore.model.bo.WorkInstanceDocument;
import org.kuali.ole.docstore.model.bo.WorkItemDocument;
import org.kuali.ole.docstore.model.enums.DocFormat;
import org.kuali.ole.docstore.model.enums.DocType;
import org.kuali.ole.select.bo.OLEDonor;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: PP7788
 * Date: 12/6/12
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping(value = "/editorcontroller")

public class EditorController extends UifControllerBase {

    private DocumentEditor editor;
    private EditorFormDataHandler editorFormDataHandler;
    private DiscoveryHelperService discoveryHelperService;
    private boolean isFormInitialized = false;
    private DocstoreClientLocator docstoreClientLocator;

    public DocstoreClientLocator getDocstoreClientLocator() {
        if (null == docstoreClientLocator) {
            return SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }

    private static final Logger LOG = Logger.getLogger(EditorController.class);

    private boolean canDeleteItem(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.CAT_NAMESPACE, OLEConstants.INSTANCE_EDITOR_DELETE_ITEM);
    }

    private boolean canDeleteInstance(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.CAT_NAMESPACE, OLEConstants.INSTANCE_EDITOR_DELETE_INSTANCE);
    }

    private boolean canAddItem(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.CAT_NAMESPACE, OLEConstants.INSTANCE_EDITOR_ADD_ITEM);
    }

    private boolean canDeleteEInstance(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.CAT_NAMESPACE, OLEConstants.INSTANCE_EDITOR_DELETE_EINSTANCE);
    }

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest httpServletRequest) {
        UifFormBase uifFormBase = null;
        uifFormBase = new EditorForm();
        return uifFormBase;
    }

    @RequestMapping(params = "methodToCall=copy")
    public ModelAndView copy(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                             HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        ModelAndView modelAndView = null;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);
        editorForm = documentEditor.copy((EditorForm) form);
        modelAndView = getUIFModelAndView(editorForm, "WorkEInstanceViewPage");
        return modelAndView;
    }

    @Override
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {

        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        editorForm.setMainSerialReceivingHistoryList(null);
        editorForm.setSupplementSerialReceivingHistoryList(null);
        editorForm.setIndexSerialReceivingHistoryList(null);
        editorForm.setBibFlag(false);
        editorForm.setHoldingFlag(false);
        editorForm.setItemFlag(false);
        editorForm.seteHoldingsFlag(false);

        ModelAndView modelAndView = null;
        // get the document details from request and set them in the form.
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        String docId = request.getParameter("docId");
        String bibId = request.getParameter("bibId");
        String instanceId = request.getParameter("instanceId");
        String editable = request.getParameter("editable");
        String callNumberFlag = request.getParameter("isCallNumberFlag");


        if (null == editable || editable.length() > 0) {
            editable = "true";
        }

        if (docFormat == null || docFormat.length() == 0) {
            docFormat = ((EditorForm) form).getDocFormat();
        }
        if (docId == null || docId.length() == 0) {
            docId = ((EditorForm) form).getDocId();
        }

        if (DocType.ITEM.getCode().equalsIgnoreCase(docType)) {
            ((EditorForm) form).setItemLocalIdentifier(docId);
        }

        /* if (docType.equalsIgnoreCase("holdings")) {
            ((EditorForm) form).setHoldingLocalIdentifier(docId);
        }*/

        // modified code for Global Edit - start

        if (Boolean.parseBoolean(editorForm.getGlobalEditFlag())) {
            prepareGlobalEditFields(editorForm, docType);
        }

        // modified code for Global Edit - end

        ((EditorForm) form).setEditable(editable);
        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);

        if (DocFormat.MARC.getCode().equals(docFormat)) {
            editorForm.setDocumentForm(new WorkBibMarcForm());
            modelAndView = getUIFModelAndView(form, "WorkBibEditorViewPage");
        } else if (DocType.INSTANCE.getCode().equals(docType) || DocType.HOLDINGS.getCode().equals(docType)) {
            editorForm.setDocumentForm(new WorkInstanceOlemlForm());
            modelAndView = getUIFModelAndView(form, "WorkHoldingsViewPage");
        } else if (DocType.EINSTANCE.getCode().equals(docType) || DocType.EHOLDINGS.getCode().equals(docType)) {
            editorForm.setDocumentForm(new WorkEInstanceOlemlForm());
            if (Boolean.parseBoolean(editorForm.getGlobalEditFlag())) {
                OleHoldings eHoldings = new OleHoldings();
                eHoldings.setStatusDate(new java.sql.Date(System.currentTimeMillis()).toString());
                eHoldings.setEResourceId(editorForm.geteResourceId());
                //getOleEResourceSearchService().getAccessLocationFromEInstance(eHoldings, workEInstanceOlemlForm);
                if (eHoldings.getExtentOfOwnership() != null && eHoldings.getExtentOfOwnership().size() > 0
                        && eHoldings.getExtentOfOwnership().get(0).getCoverages() != null
                        && eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().size() == 0) {
                    eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().add(new Coverage());
                }
                else if (eHoldings.getExtentOfOwnership() == null || eHoldings.getExtentOfOwnership().size() == 0) {
                    eHoldings.getExtentOfOwnership().add(new ExtentOfOwnership());
                    eHoldings.getExtentOfOwnership().get(0).setCoverages(new Coverages());
                    eHoldings.getExtentOfOwnership().get(0).setPerpetualAccesses(new PerpetualAccesses());
                }
                if (eHoldings.getExtentOfOwnership() != null && eHoldings.getExtentOfOwnership().size() > 0
                        && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses() != null
                        && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().size() == 0) {
                    eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().add(new Coverage());
                    eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().add(new PerpetualAccess());
                }
                if (eHoldings.getNote() != null && eHoldings.getNote().size() == 0) {
                    eHoldings.getNote().add(new Note());
                }
                ((WorkEInstanceOlemlForm)editorForm.getDocumentForm()).setSelectedEHoldings(eHoldings);
            }
            modelAndView = getUIFModelAndView(form, "WorkEInstanceViewPage");
        } else if (DocType.ITEM.getCode().equals(docType)) {
            editorForm.setDocumentForm(new WorkInstanceOlemlForm());
            modelAndView = getUIFModelAndView(form, "WorkItemViewPage");
        }
        return modelAndView;
    }


    /**
     * Load a (new or existing) document in the editor.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=load")
    public ModelAndView load(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                             HttpServletRequest request, HttpServletResponse response) {

        String tokenId = request.getParameter("tokenId");
        EditorForm editorForm = (EditorForm) form;
        if (tokenId != null) {
            editorForm.setTokenId(tokenId);
        }
        if (editorForm.getBibId() == null) {
            request.getSession().removeAttribute("treeDocumentList");
        }
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        editorForm.setMainSerialReceivingHistoryList(null);
        editorForm.setSupplementSerialReceivingHistoryList(null);
        editorForm.setIndexSerialReceivingHistoryList(null);
		editorForm.setShowTime(true);
        // get the document details from request and set them in the form.
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        String docId = request.getParameter("docId");
        String bibId = request.getParameter("bibId");
        String instanceId = request.getParameter("instanceId");
        String editable = request.getParameter("editable");
        String callNumberFlag = request.getParameter("isCallNumberFlag");
        String eResourceId = request.getParameter("eResourceId");
        String holdingsId = request.getParameter("holdingsId");

        //Verifying editable at form object
        if ((null == editable) || (editable.length() == 0)) {
            editable = ((EditorForm) form).getEditable();
        }

        //Default value for editable field if it is null
        if (null == editable || editable.length() == 0) {
            editable = "true";
        }

        /*if (docType.equalsIgnoreCase("item")) {
            ((EditorForm) form).setItemLocalIdentifier(docId);
        }*/

        if (docType.equalsIgnoreCase("holdings")) {

            ((EditorForm) form).setInstanceId(docId);
        }
        if (docType.equalsIgnoreCase("instance")) {
            ((EditorForm) form).setInstanceId(docId);
        }
        ((EditorForm) form).setEditable(editable);
        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        ((EditorForm) form).setDocId(docId);
        ((EditorForm) form).setBibId(bibId);
        ((EditorForm) form).setCallNumberFlag(callNumberFlag);
        ((EditorForm) form).setHideFooter(true);
        boolean canDelete = canDeleteItem(GlobalVariables.getUserSession().getPrincipalId()) && canDeleteInstance(GlobalVariables.getUserSession().getPrincipalId());
        ((EditorForm) form).setCanDelete(canDelete);
        boolean canAdd = canAddItem(GlobalVariables.getUserSession().getPrincipalId());
        ((EditorForm) form).setCanAdd(canAdd);
        //if the user doesn't have permission to add or edit item, editable will be set as false
        if (!canAdd) {
            ((EditorForm) form).setEditable(String.valueOf(canAdd));
        }
        boolean canDeleteEInstance = canDeleteEInstance(GlobalVariables.getUserSession().getPrincipalId());
        ((EditorForm) form).setCanDeleteEInstance(canDeleteEInstance);

        if (eResourceId != null) {
            ((EditorForm) form).seteResourceId(eResourceId);
        }
        if (holdingsId != null) {
            ((EditorForm) form).setHoldingsId(holdingsId);
        }
        // Identify the document editor to be used for the requested document.
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        // TODO: Need to save the editorForm in the session.
        // Get documentList from session.
        /*List<WorkBibDocument> workBibDocumentList = (List) request.getSession().getAttribute("treeDocumentList");
        if (null != workBibDocumentList) {
            ((EditorForm) form).setWorkBibDocumentList(workBibDocumentList);
        }*/

        List<BibTree> bibTreeList = (List) request.getSession().getAttribute("treeDocumentList");
        if (null != bibTreeList) {
            ((EditorForm) form).setBibTreeList(bibTreeList);
        }
        if ("bibliographic".equals(editorForm.getDocType())) {
            editorForm.setBibFlag(false);
        }
        if ("holdings".equals(editorForm.getDocType())) {
            editorForm.setHoldingFlag(false);
        }
        if ("item".equals(editorForm.getDocType())) {
            editorForm.setItemFlag(false);
        }
        if ("eHoldings".equals(editorForm.getDocType())) {
            editorForm.seteHoldingsFlag(false);
        }

        EditorForm documentForm = null;
        //get request object fromm session and build new work bib marc record.
        if (request.getSession() != null && request.getSession().getAttribute("bibTree") != null &&
                "true".equalsIgnoreCase(request.getParameter("loadFromSession"))) {
            BibTree bibTree = (BibTree) request.getSession().getAttribute("bibTree");
            if (docId != null && docId.length() > 0 && !docId.equalsIgnoreCase("null")) {
                ((EditorForm) form).setShowLeftPane(false);
            } else {
                ((EditorForm) form).setShowLeftPane(false);
                ((EditorForm) form).setShowEditorFooter(false);
            }
            documentForm = documentEditor.createNewRecord((EditorForm) form, bibTree);
        } else {
            // Send the input through one (request)form and get the output through another (response) form.
            documentForm = documentEditor.loadDocument((EditorForm) form);
            if (documentForm instanceof WorkInstanceOlemlForm) {
                Item item = ((WorkInstanceOlemlForm) documentForm).getSelectedItem();
                if (item.getCurrentBorrower() != null && !item.getCurrentBorrower().isEmpty()) {
                    OlePatronDocument olePatronDocument = KRADServiceLocator.getBusinessObjectService().findBySinglePrimaryKey(OlePatronDocument.class, item.getCurrentBorrower());
                    ((WorkInstanceOlemlForm) documentForm).setCurrentBarcode(olePatronDocument.getBarcode());
                }
                if (item.getProxyBorrower() != null && !item.getProxyBorrower().isEmpty()) {
                    OlePatronDocument olePatronDocument = KRADServiceLocator.getBusinessObjectService().findBySinglePrimaryKey(OlePatronDocument.class, item.getProxyBorrower());
                    ((WorkInstanceOlemlForm) documentForm).setProxyBarcode(olePatronDocument.getBarcode());
                }
            }
        }

        // Set the output (response) form containing document info into the current form.
        ((EditorForm) form).setDocumentForm(documentForm);
        // Set documentlist to session.
        request.getSession().setAttribute("treeDocumentList", ((EditorForm) form).getDocumentForm().getBibTreeList());

        // Build or update left pane data (tree structure of documents)
        getEditorFormDataHandler().buildLeftPaneData((EditorForm) form);

        // Return the next view to be shown to user.
        ModelAndView modelAndView = getUIFModelAndView(form, documentForm.getViewId());
        return modelAndView;
    }

    @RequestMapping(params = "methodToCall=updateLeftPane")
    public ModelAndView updateLeftPane(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        EditorForm editorForm = (EditorForm) form;
        String hiddenVal = editorForm.getHdnUuid();
        int hdnIndex = editorForm.getHdnIndex();
        DiscoveryHelperService discoveryHelperService = getDiscoveryHelperService();
        List responseFromSOLR = discoveryHelperService.getResponseFromSOLR("id", hiddenVal);
        String docType = null;
        for (Iterator iterator = responseFromSOLR.iterator(); iterator.hasNext(); ) {
            Map map = (Map) iterator.next();
            if (map.containsKey(OLEConstants.DOC_TYPE)) {
                String list = (String) map.get(OLEConstants.DOC_TYPE);
                docType = list;
                break;
            }
        }
        if ((docType.toString().equals(OLEConstants.ITEM_DOC_TYPE))) {
            WorkInstanceOlemlForm instanceEditorForm = new WorkInstanceOlemlForm();
            //instanceEditorForm.setInstance(editorForm.getInstance());
            //instanceEditorForm.setUuid(editorForm.getInstance().getInstanceIdentifier());
            //editor = new WorkInstanceOlemlEditor();
            //instanceEditorForm.setSelectedItem(editorForm.getInstance().getItems().getItem().get(hdnIndex));
            //return getUIFModelAndView(editorForm, "WorkItemViewPage");
            //ModelAndView modelAndView = editor.load(form, result, request, response);
            //return modelAndView;
            return null;
        } else {
            //editorForm.setSelectedHolding(editorForm.getInstance().getOleHoldings());
            return getUIFModelAndView(editorForm, "WorkHoldingsViewPage");
        }
    }

    public DiscoveryHelperService getDiscoveryHelperService() {
        if (null == discoveryHelperService) {
            return new DiscoveryHelperService();
        }
        return discoveryHelperService;
    }

    public EditorFormDataHandler getEditorFormDataHandler() {
        if (null == editorFormDataHandler) {
            editorFormDataHandler = new EditorFormDataHandler();
        }
        return editorFormDataHandler;
    }

    @RequestMapping(params = "methodToCall=EditNewInstance")
    public ModelAndView EditNewInstance(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {

        EditorForm editorForm = (EditorForm) form;
        // get request object from session
        BibTree bibTree = (BibTree) request.getSession().getAttribute("bibTree");
        // Get documentList from session.
        /*List<WorkBibDocument> workBibDocumentList = (List) request.getSession().getAttribute("treeDocumentList");
        if (null != workBibDocumentList) {
            ((EditorForm) form).setWorkBibDocumentList(workBibDocumentList);
        }*/
        if (editorForm.getDocumentForm().getViewId().equalsIgnoreCase("WorkHoldingsViewPage")) {
            // call instance editor
            DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                    .getDocumentEditor("work", "item", "oleml");
            // set item record to display on item tab
            editorForm = documentEditor.createNewRecord((EditorForm) form, bibTree);
            editorForm.setNeedToCreateInstance(false);

        } else {
            // call bib editor to store bib data
            DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                    .getDocumentEditor("work", "bibliographic", "marc");
            // update request object with modified marc data.
            EditorForm marcEditorForm = documentEditor.editNewRecord(editorForm, bibTree);

            // validate bib before going to instance
            if (!marcEditorForm.isValidInput()) {
                return getUIFModelAndView(form, marcEditorForm.getViewId());
            }

            // call instance editor and set holding record to display on holding tab
            documentEditor = DocumentEditorFactory.getInstance().getDocumentEditor("work", "holdings", "oleml");
            ((EditorForm) form).setDocCategory("work");
            ((EditorForm) form).setDocType("holdings");
            ((EditorForm) form).setDocFormat("oleml");
            editorForm = documentEditor.createNewRecord((EditorForm) form, bibTree);
        }

        // Set the output (response) form containing document info into the current form.
        ((EditorForm) form).setDocumentForm(editorForm);
        // Set documentlist to session.
        //request.getSession().setAttribute("treeDocumentList", ((EditorForm) form).getWorkBibDocumentList());

        // Build or update left pane data (tree structure of documents)
        getEditorFormDataHandler().buildLeftPaneData((EditorForm) form);

        // Return the next view to be shown to user.
        ModelAndView modelAndView = getUIFModelAndView(form, editorForm.getViewId());
        return modelAndView;

    }

    @RequestMapping(params = "methodToCall=save")
    public ModelAndView save(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                             HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        if ("bibliographic".equals(((EditorForm) form).getDocType())) {
            ((EditorForm) form).setBibFlag(false);
        }
        if ("holdings".equals(((EditorForm) form).getDocType())) {
            ((EditorForm) form).setHoldingFlag(false);
        }
        if ("item".equals(((EditorForm) form).getDocType())) {
            ((EditorForm) form).setItemFlag(false);
        }
        if ("eHoldings".equals(((EditorForm) form).getDocType())) {
            ((EditorForm) form).seteHoldingsFlag(false);
        }
        if (request.getSession() != null && request.getSession().getAttribute("bibTree") != null) {
            DocumentEditor documentEditor = null;
            if (((EditorForm) form).getDocId() == null || "null".equalsIgnoreCase(((EditorForm) form).getDocId())) {
                documentEditor = DocumentEditorFactory.getInstance()
                        .getDocumentEditor("work", "item", "oleml");
            } else {
                documentEditor = DocumentEditorFactory.getInstance()
                        .getDocumentEditor("work", "bibliographic", "marc");
            }

            BibTree bibTree = (BibTree) request.getSession().getAttribute("bibTree");
            EditorForm editorForm = documentEditor.editNewRecord((EditorForm) form, bibTree);

            // validate user entered item data before saving to docstore.
            if (!editorForm.isValidInput()) {
                return getUIFModelAndView(form, editorForm.getViewId());
            }

            String responseFromDocstore = documentEditor.saveDocument(bibTree, (EditorForm) form);
            if (StringUtils.isNotEmpty(responseFromDocstore) && responseFromDocstore.contains("success")) {
                request.getSession().setAttribute("responseBibTree", bibTree);

                String url = ConfigContext.getCurrentContextConfig().getProperty(OLEPropertyConstants.OLE_URL_BASE);
                url = url + "/portal.do?channelTitle=Import Bib&channelUrl=" + url +
                        "/ole-kr-krad/importBibController?viewId=ImportBibView&methodToCall=viewRecordNext";
                return performRedirect(editorForm, url);
            }
        } else {
            if (!isFormInitialized) {
                super.start(form, result, request, response);
                isFormInitialized = true;
            }

            // get the document details from request and set them in the form.
            String docCategory = request.getParameter("docCategory");
            String docType = request.getParameter("docType");
            String docFormat = request.getParameter("docFormat");
            String docId = request.getParameter("docId");
            String bibId = request.getParameter("bibId");
            String instanceId = request.getParameter("instanceId");

            String editable = request.getParameter("editable");

            if (docType != null && docType.equalsIgnoreCase("item")) {
                ((EditorForm) form).setItemLocalIdentifier(docId);
            }

            /* if (docType != null && docType.equalsIgnoreCase("holdings")) {
                ((EditorForm) form).setHoldingLocalIdentifier(docId);
            }*/

            if ((null == editable) || (editable.length() == 0)) {
                editable = "true";
            }
            if (docCategory == null || docCategory.length() == 0) {
                docCategory = ((EditorForm) form).getDocCategory();
            }
            if (docType == null || docType.length() == 0) {
                docType = ((EditorForm) form).getDocType();
            }

            if (docFormat == null || docFormat.length() == 0) {
                docFormat = ((EditorForm) form).getDocFormat();
            }
            if (docId == null || docId.length() == 0) {
                docId = ((EditorForm) form).getDocId();
            }

            ((EditorForm) form).setEditable(editable);
            ((EditorForm) form).setDocCategory(docCategory);
            ((EditorForm) form).setDocType(docType);
            ((EditorForm) form).setDocFormat(docFormat);
            ((EditorForm) form).setDocId(docId);
            ((EditorForm) form).setBibId(bibId);
            ((EditorForm) form).setInstanceId(instanceId);

            // Identify the document editor to be used for the requested document.
            DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                    .getDocumentEditor(docCategory, docType, docFormat);

            // TODO: Need to save the editorForm in the session.
            // Get documentList from session.
            /* List<WorkBibDocument> workBibDocumentList = (List) request.getSession().getAttribute("treeDocumentList");
            if (null != workBibDocumentList) {
                ((EditorForm) form).setWorkBibDocumentList(workBibDocumentList);
            }*/

            List<BibTree> bibTreeList = (List) request.getSession().getAttribute("treeDocumentList");
            if (null != bibTreeList) {
                ((EditorForm) form).getDocumentForm().setBibTreeList(bibTreeList);
                ((EditorForm) form).setBibTreeList(bibTreeList);
            }
            if("overwrite".equals(((EditorForm) form).getCheckOverwriteFlag())) {
                EditorForm documentForm = documentEditor.saveDocument((EditorForm) form);
                ((EditorForm) form).setDocumentForm(documentForm);
            } else {
                if (documentEditor.isValidUpdate((EditorForm) form) && ((EditorForm) form).getAllowUpdate().equals
                        ("true") || documentEditor.isValidUpdate((EditorForm) form) && ((EditorForm) form).getAllowUpdate().equals
                        ("")) {
                    // Send the input through one (request)form and get the output through another (response) form.
                    EditorForm documentForm = documentEditor.saveDocument((EditorForm) form);
                    // Set the output (response) form containing docum ((EditorForm) form).isAllowUpdate()ent info into the current form.
                    ((EditorForm) form).setDocumentForm(documentForm);
                    ((EditorForm) form).setAllowUpdate("true");
                } else {
                    ((EditorForm) form).setAllowUpdate("false");
                    // EditorForm documentForm = documentEditor.loadDocument((EditorForm) form);
                    // ((EditorForm) form).setDocumentForm(documentForm);
                }

            }
            ((EditorForm) form).setCheckOverwriteFlag("");
            // Set documentlist to session.
            request.getSession().setAttribute("treeDocumentList", ((EditorForm) form).getDocumentForm().getBibTreeList());

            // Build or update left pane data (tree structure of documents)
            getEditorFormDataHandler().buildLeftPaneData((EditorForm) form);

        }
        ((EditorForm) form).setDisplayField006("false");
        ((EditorForm) form).setDisplayField007("false");
        ((EditorForm) form).setDisplayField008("false");
        ((EditorForm) form).setBibFlag(false);
        ((EditorForm) form).setHoldingFlag(false);
        ((EditorForm) form).setItemFlag(false);
        ((EditorForm) form).seteHoldingsFlag(false);
        // Return the next view to be shown to user.
        long endTime = System.currentTimeMillis();
        ((EditorForm) form).setTotalTime(String.valueOf((endTime-startTime)/1000));
        ModelAndView modelAndView = getUIFModelAndView(form, ((EditorForm) form).getDocumentForm().getViewId());
        return modelAndView;
    }

    /**
     * This method will add the controlField 006 record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addControlField006")
    public ModelAndView addControlField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the controlField  006 based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeControlField006")
    public ModelAndView removeControlField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }


    /**
     * This method will add the controlField 007 record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addControlField007")
    public ModelAndView addControlField007(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the controlField  007 based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeControlField007")
    public ModelAndView removeControlField007(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                              HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }


    /**
     * This method will add the datField record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addDataField")
    public ModelAndView addDataField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the dataField based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeDataField")
    public ModelAndView removeDataField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

/*
    private UifFormBase buildUifForm (HttpServletRequest httpServletRequest) {
        UifFormBase uifFormBase = null;

        String docType = httpServletRequest.getParameter("docType");
        String docFormat = httpServletRequest.getParameter("docFormat");
        if ("bibliographic".equalsIgnoreCase(docType)) {
            if ("marc".equalsIgnoreCase(docFormat)) {
                LOG.info("Inside EditorController createInitialForm if bibliographic marc");
                editor = new WorkBibMarcEditor();
                uifFormBase = editor.createInitialForm(httpServletRequest);
            } else if ("dublin".equalsIgnoreCase(docFormat)) {
                editor = new WorkBibDublinEditor();
                uifFormBase = editor.createInitialForm(httpServletRequest);
            }
        } else if ("instance".equalsIgnoreCase(docType)) {
            if ("oleml".equalsIgnoreCase(docFormat)) {
                editor = new WorkInstanceOlemlEditor();
                uifFormBase = editor.createInitialForm(httpServletRequest);
            }
        } else if ("holdings".equalsIgnoreCase(docType) || "item".equalsIgnoreCase(docType)) {
            if ("oleml".equalsIgnoreCase(docFormat)) {
                editor = new WorkInstanceOlemlEditor();
                uifFormBase = editor.createInitialForm(httpServletRequest);
            }
        }
        return uifFormBase;

    }
*/

    /**
     * DeleteVerify a  document in the editor.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=deleteVerify")
    public ModelAndView deleteVerify(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }

        // get the document details from request and set them in the form.
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        String docId = request.getParameter("docId");
        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        ((EditorForm) form).setDocId(docId);
        ((EditorForm) form).setMessage("");
//        ((EditorForm) form).setEditable("false");
        ((EditorForm) form).setHideFooter(false);

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance().getDocumentEditor(docCategory, docType, docFormat);
        EditorForm documentForm = documentEditor.deleteVerify((EditorForm) form);
        LOG.info("view id-->" + documentForm.getViewId());
        //List<String> uuidList  = new ArrayList<String>();
        EditorForm editorForm = (EditorForm) form;
        //uuidList.add(docId);

/*        DocumentSelectionTree dst = new DocumentSelectionTree();
        LOG.info("get uud list from form-->"+editorForm.getUuidList());
        Node<DocumentTreeNode, String> rootNode = dst.add(editorForm.getUuidList());

         editorForm.getDocTree().setRootElement(rootNode);*/

        return getUIFModelAndView(editorForm, documentForm.getViewId());
/*
        System.out.println("delete verify method is executed-----1");

        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        ((EditorForm) form).setDocId(docId);

        // Identify the document editor to be used for the requested document.
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance().getDocumentEditor(docCategory, docType, docFormat);
        System.out.println("delete verify method is executed-----2");
        //EditorForm documentForm = null;
        EditorForm documentForm = documentEditor.deleteVerify((EditorForm) form);
        ((EditorForm) form).setDocumentForm(documentForm);

        try {
            documentForm = documentEditor.deleteVerify((EditorForm) form);
            modelAndView =getUIFModelAndView(form,documentForm.getViewId());
            System.out.println("delete verify method is executed-----3");
        }catch (Exception e) {
            modelAndView =getUIFModelAndView(form,"WorkBibEditorViewPage");
            System.out.println("delete verify method is executed-----4");
        }
        System.out.println("delete verify method is executed-----5");
        return modelAndView;
*/
    }

    /**
     * Delete a  document in the editor.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=delete")
    public ModelAndView delete(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = null;
        EditorForm editorForm = (EditorForm) form;
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }

        // get the document details from request and set them in the form.
        String docCategory = ((EditorForm) form).getDocCategory();
        String docType = ((EditorForm) form).getDocType();
        String docFormat = ((EditorForm) form).getDocFormat();
        String docId = ((EditorForm) form).getDocId();


        LOG.info("docCategory-->" + docCategory);
        LOG.info("docType-->" + docType);
        LOG.info("docFormat-->" + docFormat);
        LOG.info("uuid-->" + docId);


/*        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        ((EditorForm) form).setUuid(docId);*/

        // Identify the document editor to be used for the requested document.
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance().getDocumentEditor(docCategory, docType, docFormat);

        EditorForm documentForm = documentEditor.delete((EditorForm) form);

        /*       //boolean hasLinks = delete();
        try {

            modelAndView =getUIFModelAndView(form,"deleteConfirmation");
        } catch (Exception e) {
            modelAndView =getUIFModelAndView(form,"");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        // Send the input through one (request)form and get the output through another (response) form.

        // Build or update left pane data (tree structure of documents)
        getEditorFormDataHandler().buildLeftPaneData((EditorForm) form);

        // Redirect to bib page after deleting eholdings.
        if (documentForm.isCanDeleteEHoldings() && docType.equalsIgnoreCase(DocType.EHOLDINGS.getCode())) {
            String url = ConfigContext.getCurrentContextConfig().getProperty(OLEPropertyConstants.OLE_URL_BASE);
            url = url + "/portal.do?channelTitle=Editor&channelUrl=" + url +
                    "/ole-kr-krad/editorcontroller?viewId=EditorView&methodToCall=load&docCategory=work&docType=bibliographic&docFormat=marc&bibId=&editable=true&docId=" + editorForm.getBibId();
            return performRedirect(editorForm, url);
        }

        return getUIFModelAndView(editorForm, documentForm.getViewId());
    }

    /**
     * This method will add the ExtentOfOwnership record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addExtentOfOwnership")
    public ModelAndView addExtentOfOwnership(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveExtentOfOwnership(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the ExtentOfOwnership based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeExtentOfOwnership")
    public ModelAndView removeExtentOfOwnership(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveExtentOfOwnership(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will add the EOWHoldingNotes record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addEOWHoldingNotes")
    public ModelAndView addEOWHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveExtentOfOwnership(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the EOWHoldingNotes based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeEOWHoldingNotes")
    public ModelAndView removeEOWHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                              HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveExtentOfOwnership(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will add the AccessInformation record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addAccessInformation")
    public ModelAndView addAccessInformation(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveAccessInformationAndHoldingsNotes(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the AccessInformation based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeAccessInformation")
    public ModelAndView removeAccessInformation(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveAccessInformationAndHoldingsNotes(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will add the HoldingNotes record based on the selected Line index and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addHoldingNotes")
    public ModelAndView addHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveAccessInformationAndHoldingsNotes(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * This method will remove the HoldingNotes based on the index position and updates the component.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=removeHoldingNotes")
    public ModelAndView removeHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveAccessInformationAndHoldingsNotes(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addField")
    public ModelAndView addField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeField")
    public ModelAndView removeField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORDeleteFields(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }


    /**
     * Used for Test-case
     *
     * @param result
     * @param request
     * @param response
     * @param editorForm
     * @return ModelAndView
     */
    protected ModelAndView callSuper(BindingResult result, HttpServletRequest request, HttpServletResponse response,
                                     EditorForm editorForm) {
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addItemNote")
    public ModelAndView addItemNote(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveItemNote(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addDonorToItem")
    public ModelAndView addDonorToItem(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        EditorForm editorForm = (EditorForm) form;
        WorkInstanceOlemlForm workInstanceOlemlForm = (WorkInstanceOlemlForm) editorForm.getDocumentForm();
        List<DonorInfo> donorInfoList = workInstanceOlemlForm.getSelectedItem().getDonorInfo();
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        DonorInfo donorInfo = (DonorInfo) eventObject;
        if (donorInfo != null && StringUtils.isNotEmpty(donorInfo.getDonorCode())) {
            Map map = new HashMap();
            map.put("donorCode", donorInfo.getDonorCode());
            OLEDonor oleDonor = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEDonor.class, map);
            if (oleDonor != null) {
                for (DonorInfo donor : donorInfoList) {
                    if (donor.getDonorCode().equals(donorInfo.getDonorCode())) {
                        GlobalVariables.getMessageMap().putErrorForSectionId("OleDonorInformation-ListOfDonors", "error.donor.code.exist");
                        return getUIFModelAndView(form);
                    }
                }
            } else {
                GlobalVariables.getMessageMap().putErrorForSectionId("OleDonorInformation-ListOfDonors", "error.donor.code.doesnt.exist");
                return getUIFModelAndView(form);
            }
        }
        View view = form.getPostedView();
        view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=addDonorToEInstance")
    public ModelAndView addDonorToEInstance(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        EditorForm editorForm = (EditorForm) form;
        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        List<DonorInfo> donorInfoList = workEInstanceOlemlForm.getSelectedEHoldings().getDonorInfo();
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        DonorInfo donorInfo = (DonorInfo) eventObject;
        if (donorInfo != null && StringUtils.isNotEmpty(donorInfo.getDonorCode())) {
            Map map = new HashMap();
            map.put("donorCode", donorInfo.getDonorCode());
            OLEDonor oleDonor = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEDonor.class, map);
            if (oleDonor != null) {
                for (DonorInfo donor : donorInfoList) {
                    if (donor.getDonorCode().equals(donorInfo.getDonorCode())) {
                        GlobalVariables.getMessageMap().putErrorForSectionId("OleEInstanceDonorInformation-ListOfDonors", "error.donor.code.exist");
                        return getUIFModelAndView(form);
                    }
                }
            } else {
                GlobalVariables.getMessageMap().putErrorForSectionId("OleEInstanceDonorInformation-ListOfDonors", "error.donor.code.doesnt.exist");
                return getUIFModelAndView(form);
            }
        }
        View view = form.getPostedView();
        view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=removeItemNote")
    public ModelAndView removeItemNote(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        editorForm = documentEditor.addORRemoveItemNote(editorForm, request);
        return super.navigate(editorForm, result, request, response);
    }

    /**
     * Display the linked Bib details for corresponding Holdings/Items.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=showBibs")
    public ModelAndView showBibs(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        EditorForm documentForm = null;
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        // get the document details from request and set them in the form.
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        String docId = request.getParameter("docId");
        String instanceId = request.getParameter("instanceId");
        String editable = request.getParameter("editable");

        //Verifying editable at form object
        if ((null == editable) || (editable.length() == 0)) {
            editable = ((EditorForm) form).getEditable();
        }

        //Default value for editable field if it is null
        if (null == editable || editable.length() == 0) {
            editable = "true";
        }

        ((EditorForm) form).setEditable(editable);
        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        ((EditorForm) form).setDocId(docId);
        ((EditorForm) form).setInstanceId(instanceId);

        // Identify the document editor to be used for the requested document.
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        documentForm = documentEditor.showBibs((EditorForm) form);
        // Set the output (response) form containing document info into the current form.
        ((EditorForm) form).setDocumentForm(documentForm);

        // Return the next view to be shown to user.
        ModelAndView modelAndView = getUIFModelAndView(form, ((EditorForm) form).getDocumentForm().getViewId());
        return modelAndView;
    }


    @RequestMapping(params = "methodToCall=leaderFieldReset")
    public ModelAndView resetLeaderField(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Inside the clear leader method");
        EditorForm editForm = (EditorForm) form;
        WorkBibMarcForm workBibMarcForm = (WorkBibMarcForm) editForm.getDocumentForm();
        workBibMarcForm.setLeaderField(new LeaderField());
        ((EditorForm) form).setDocumentForm(workBibMarcForm);
        return navigate(form, result, request, response);
    }


    @RequestMapping(params = "methodToCall=createSerialReceiving")
    public ModelAndView createSerialReceiving(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                              HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Inside the createSerialReceiving method");
        ModelAndView modelAndView = getUIFModelAndView(form, ((EditorForm) form).getDocumentForm().getViewId());
        return modelAndView;

    }


    @RequestMapping(params = "methodToCall=loadControlField006")
    public ModelAndView loadControlField006(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        ((EditorForm) form).setDisplayField006("true");
        WorkBibMarcForm workBibMarcForm = (WorkBibMarcForm) ((EditorForm) form).getDocumentForm();
        String controlField006Format = workBibMarcForm.getMarcControlFields().getValue();
        if(workBibMarcForm.getMarcControlFields().getControlField006()==null){
            ControlField006 controlField006 = new ControlField006();
            workBibMarcForm.getMarcControlFields().setControlField006(controlField006);
        }
        String controlField006Format1 = workBibMarcForm.getMarcControlFields().getControlField006().getFormat();
        if(controlField006Format1!=null){
            controlField006Format=controlField006Format1;
        }
        ControlField006 controlFiled006 = new ControlField006();
        controlFiled006.setFormat(workBibMarcForm.getMarcControlFields().getValue());
        if(controlField006Format1==null){
        controlFiled006.setFormat(workBibMarcForm.getMarcControlFields().getValue());
        workBibMarcForm.getMarcControlFields().setControlField006(controlFiled006);
        }else {
            controlFiled006.setFormat(controlField006Format1);
            workBibMarcForm.getMarcControlFields().setControlField006(controlFiled006);

        }
        workBibMarcForm.getMarcControlFields().setMapVisible("false");
        workBibMarcForm.getMarcControlFields().setBooksVisible("false");
        workBibMarcForm.getMarcControlFields().setMusicVisible("false");
        workBibMarcForm.getMarcControlFields().setComputerFilesVisible("false");
        workBibMarcForm.getMarcControlFields().setContinuingResourcesVisible("false");
        workBibMarcForm.getMarcControlFields().setMixedMaterialVisible("false");
        workBibMarcForm.getMarcControlFields().setVisualMaterialsVisible("false");

        if (controlField006Format.equalsIgnoreCase("e") || controlField006Format.equalsIgnoreCase("f")) {
            workBibMarcForm.getMarcControlFields().setMapVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("a") || controlField006Format.equalsIgnoreCase("t")) {
            workBibMarcForm.getMarcControlFields().setBooksVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("c") || controlField006Format.equalsIgnoreCase("d") ||
                controlField006Format.equalsIgnoreCase("i") || controlField006Format.equalsIgnoreCase("j")) {
            workBibMarcForm.getMarcControlFields().setMusicVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("m")) {
            workBibMarcForm.getMarcControlFields().setComputerFilesVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("s")) {
            workBibMarcForm.getMarcControlFields().setContinuingResourcesVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("p")) {
            workBibMarcForm.getMarcControlFields().setMixedMaterialVisible("true");
        } else if (controlField006Format.equalsIgnoreCase("g") || controlField006Format.equalsIgnoreCase("k") ||
                controlField006Format.equalsIgnoreCase("o") || controlField006Format.equalsIgnoreCase("r")) {
            workBibMarcForm.getMarcControlFields().setVisualMaterialsVisible("true");
        }
        ((EditorForm) form).setDocumentForm(workBibMarcForm);
        return navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=loadControlField007")
    public ModelAndView loadControlField007(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        ((EditorForm) form).setDisplayField007("true");
        WorkBibMarcForm workBibMarcForm = (WorkBibMarcForm) ((EditorForm) form).getDocumentForm();
        if(workBibMarcForm.getMarcControlFields().getControlField007()==null){
            ControlField007 controlField007 = new ControlField007();
            workBibMarcForm.getMarcControlFields().setControlField007(controlField007);
        }

        String controlField007Format = workBibMarcForm.getMarcControlFields().getValue007();
        String controlField007Format1 = workBibMarcForm.getMarcControlFields().getControlField007().getFormat();

        ControlField007 controlFiled007 = new ControlField007();

        if(controlField007Format1!=null){
            controlField007Format=controlField007Format1;
        }
        if(controlField007Format1!=null){
            controlFiled007.setFormat(controlField007Format1);
            workBibMarcForm.getMarcControlFields().setControlField007(controlFiled007);
        }   else{
            controlFiled007.setFormat(controlField007Format);
            workBibMarcForm.getMarcControlFields().setControlField007(controlFiled007);
        }

        workBibMarcForm.getMarcControlFields().setMapVisible007("false");
        workBibMarcForm.getMarcControlFields().setElectronicResourcesVisible007("false");
        workBibMarcForm.getMarcControlFields().setGlobeVisible007("false");
        workBibMarcForm.getMarcControlFields().setTactileMaterialVisible007("false");
        workBibMarcForm.getMarcControlFields().setProjectGraphicVisible007("false");
        workBibMarcForm.getMarcControlFields().setMicroFormVisible007("false");
        workBibMarcForm.getMarcControlFields().setNonProjectedGraphicVisible007("false");
        workBibMarcForm.getMarcControlFields().setMotionPictureVisible007("false");
        workBibMarcForm.getMarcControlFields().setKitVisible007("false");
        workBibMarcForm.getMarcControlFields().setNotatedMusicVisible007("false");
        workBibMarcForm.getMarcControlFields().setRemoteSensingImageVisible007("false");
        workBibMarcForm.getMarcControlFields().setSoundRecordingVisible007("false");
        workBibMarcForm.getMarcControlFields().setTextVisible007("false");
        workBibMarcForm.getMarcControlFields().setVideoRecordingVisible007("false");
        workBibMarcForm.getMarcControlFields().setUnspecifiedVisible007("false");

        if (controlField007Format.equalsIgnoreCase("a")) {
            workBibMarcForm.getMarcControlFields().setMapVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("c")) {
            workBibMarcForm.getMarcControlFields().setElectronicResourcesVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("d")) {
            workBibMarcForm.getMarcControlFields().setGlobeVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("f")) {
            workBibMarcForm.getMarcControlFields().setTactileMaterialVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("g")) {
            workBibMarcForm.getMarcControlFields().setProjectGraphicVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("h")) {
            workBibMarcForm.getMarcControlFields().setMicroFormVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("k")) {
            workBibMarcForm.getMarcControlFields().setNonProjectedGraphicVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("m")) {
            workBibMarcForm.getMarcControlFields().setMotionPictureVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("o")) {
            workBibMarcForm.getMarcControlFields().setKitVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("q")) {
            workBibMarcForm.getMarcControlFields().setNotatedMusicVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("r")) {
            workBibMarcForm.getMarcControlFields().setRemoteSensingImageVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("s")) {
            workBibMarcForm.getMarcControlFields().setSoundRecordingVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("t")) {
            workBibMarcForm.getMarcControlFields().setTextVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("v")) {
            workBibMarcForm.getMarcControlFields().setVideoRecordingVisible007("true");
        } else if (controlField007Format.equalsIgnoreCase("z")) {
            workBibMarcForm.getMarcControlFields().setUnspecifiedVisible007("true");
        }
        ((EditorForm) form).setDocumentForm(workBibMarcForm);

        return navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=loadControlField008")
    public ModelAndView loadControlField008(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        ((EditorForm) form).setDisplayField008("true");
        WorkBibMarcForm workBibMarcForm = (WorkBibMarcForm) ((EditorForm) form).getDocumentForm();

        if(workBibMarcForm.getMarcControlFields().getControlField008()==null){
            ControlField008 controlField008 = new ControlField008();
            workBibMarcForm.getMarcControlFields().setControlField008(controlField008);
        }
        String controlField008Format = workBibMarcForm.getMarcControlFields().getValue008();
        workBibMarcForm.getMarcControlFields().setControlField008(new ControlField008());


        workBibMarcForm.getMarcControlFields().setMapVisible008("false");
        workBibMarcForm.getMarcControlFields().setBooksVisible008("false");
        workBibMarcForm.getMarcControlFields().setMusicVisible008("false");
        workBibMarcForm.getMarcControlFields().setComputerFilesVisible008("false");
        workBibMarcForm.getMarcControlFields().setContinuingResourcesVisible008("false");
        workBibMarcForm.getMarcControlFields().setMixedMaterialVisible008("false");
        workBibMarcForm.getMarcControlFields().setVisualMaterialsVisible008("false");

        if (controlField008Format.equalsIgnoreCase("map")) {
            workBibMarcForm.getMarcControlFields().setMapVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("books")) {
            workBibMarcForm.getMarcControlFields().setBooksVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("music")) {
            workBibMarcForm.getMarcControlFields().setMusicVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("computer")) {
            workBibMarcForm.getMarcControlFields().setComputerFilesVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("countRes")) {
            workBibMarcForm.getMarcControlFields().setContinuingResourcesVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("mixMat")) {
            workBibMarcForm.getMarcControlFields().setMixedMaterialVisible008("true");
        } else if (controlField008Format.equalsIgnoreCase("visMat")) {
            workBibMarcForm.getMarcControlFields().setVisualMaterialsVisible008("true");
        }

        ((EditorForm) form).setDocumentForm(workBibMarcForm);
        return navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addCoverageSection")
    public ModelAndView addCoverageSection(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().add(new Coverage());
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeCoverageSection")
    public ModelAndView removeCoverageSection(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                              HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        if (workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().size() > 1) {
            workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().remove(index);
        } else {
            if (workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().size() == 1) {
                workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().remove(index);
                Coverage coverage = new Coverage();
                workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getCoverages().getCoverage().add(coverage);
            }
        }
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addLink")
    public ModelAndView addLink(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        workEInstanceOlemlForm.getSelectedEHoldings().getLink().add(index+1,new Link());
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=deleteLink")
    public ModelAndView deleteLink(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        if (workEInstanceOlemlForm.getSelectedEHoldings().getLink().size() > 1) {
            workEInstanceOlemlForm.getSelectedEHoldings().getLink().remove(index);
        } else {
            if (workEInstanceOlemlForm.getSelectedEHoldings().getLink().size() == 1) {
                workEInstanceOlemlForm.getSelectedEHoldings().getLink().remove(index);
                Link link = new Link();
                workEInstanceOlemlForm.getSelectedEHoldings().getLink().add(link);
            }
        }
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addPerpetualAccessSection")
    public ModelAndView addPerpetualAccessSection(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                  HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        //int index = Integer.parseInt(workEInstanceOlemlForm.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
       /* String selectedPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        int selectedExtentIndex = Integer.parseInt(StringUtils.substring(selectedPath,
                (StringUtils.indexOf(selectedPath, "[") + 1),
                StringUtils.lastIndexOf(selectedPath, "]")));*/
        workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().add(new PerpetualAccess());
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removePerpetualAccessSection")
    public ModelAndView removePerpetualAccessSection(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                     HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        if (workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().size() > 1) {
            workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().remove(index);
        } else {
            if (workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().size() == 1) {
                workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().remove(index);
                PerpetualAccess perpetualAccess = new PerpetualAccess();
                workEInstanceOlemlForm.getSelectedEHoldings().getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().add(perpetualAccess);
            }
        }
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addOleEInstanceHoldingNotes")
    public ModelAndView addOleEInstanceHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                    HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        workEInstanceOlemlForm.getSelectedEHoldings().getNote().add(new Note());
        return super.navigate(editorForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeOleEInstanceHoldingNotes")
    public ModelAndView removeOleEInstanceHoldingNotes(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                       HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        WorkEInstanceOlemlForm workEInstanceOlemlForm = (WorkEInstanceOlemlForm) editorForm.getDocumentForm();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        if (workEInstanceOlemlForm.getSelectedEHoldings().getNote().size() > 1) {
            workEInstanceOlemlForm.getSelectedEHoldings().getNote().remove(index);
        } else {
            if (workEInstanceOlemlForm.getSelectedEHoldings().getNote().size() == 1) {
                workEInstanceOlemlForm.getSelectedEHoldings().getNote().remove(index);
                Note note = new Note();
                workEInstanceOlemlForm.getSelectedEHoldings().getNote().add(note);
            }
        }
        return super.navigate(editorForm, result, request, response);
    }


    @RequestMapping(params = "methodToCall=globalEditSave")
    public ModelAndView globalEditSave(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        if (!isFormInitialized) {
            super.start(form, result, request, response);
            isFormInitialized = true;
        }
        EditorForm editorForm = (EditorForm) form;
        String docCategory = request.getParameter("docCategory");
        String docType = request.getParameter("docType");
        String docFormat = request.getParameter("docFormat");
        List<String> ids = (List<String>) request.getSession().getAttribute("Ids");
        if (docCategory == null) {
            docCategory = editorForm.getDocCategory();
        }
        if (docType == null) {
            docType = editorForm.getDocType();
        }
        if (docFormat == null) {
            docFormat = editorForm.getDocFormat();
        }

        ((EditorForm) form).setDocCategory(docCategory);
        ((EditorForm) form).setDocType(docType);
        ((EditorForm) form).setDocFormat(docFormat);
        DocumentEditor documentEditor = DocumentEditorFactory.getInstance()
                .getDocumentEditor(docCategory, docType, docFormat);

        if (documentEditor != null && documentEditor.isValidUpdate((EditorForm) form) || ((EditorForm) form).getAllowUpdate().equals
                ("true")) {
            // Send the input through one (request)form and get the output through another (response) form.
            EditorForm documentForm = documentEditor.bulkUpdate((EditorForm) form, ids);
            // Set the output (response) form containing docum ((EditorForm) form).isAllowUpdate()ent info into the current form.
            ((EditorForm) form).setDocumentForm(documentForm);
            ((EditorForm) form).setAllowUpdate(" ");
        } else {
            ((EditorForm) form).setAllowUpdate("false");
        }
        return super.navigate(editorForm, result, request, response);
    }


    @RequestMapping(params = "methodToCall=printCallSlip")
    public void printCallSlip(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        String formKey = request.getParameter("formKey");
        EditorForm editorForm = (EditorForm) GlobalVariables.getUifFormManager().getSessionForm(formKey);
        generateCallSlip(editorForm, response);
    }

    private void generateCallSlip(EditorForm editorForm, HttpServletResponse response) {
        LOG.debug("Creating pdf");
        String title = "", author = "", callNumber = "", location = "", copyNumber = "", enumeration = "", chronology = "", barcode = "";
        SearchResponse searchResponse = null;
        SearchParams searchParams = new SearchParams();
        SearchField searchField1 = searchParams.buildSearchField("item", "ItemIdentifier_search", editorForm.getDocId());
        searchParams.getSearchConditions().add(searchParams.buildSearchCondition("OR", searchField1, "AND"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("bibliographic", "Title"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("bibliographic", "Author"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "CallNumber"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "LocationName"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "CopyNumber"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "enumeration"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "chronology"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ItemBarcode"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("holdings", "CallNumber"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("holdings", "LocationName"));
        try {
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(searchParams);
        } catch (Exception e) {
            LOG.error(e, e);
        }
        if (CollectionUtils.isNotEmpty(searchResponse.getSearchResults())) {
            for (SearchResultField searchResultField : searchResponse.getSearchResults().get(0).getSearchResultFields()) {
                if (searchResultField.getDocType().equalsIgnoreCase(DocType.BIB.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("Title")) {
                    if (StringUtils.isBlank(title)) {
                        title = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                    }
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.BIB.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("Author")) {
                    if (StringUtils.isBlank(author)) {
                        author = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                    }
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("CallNumber")) {
                    callNumber = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("LocationName")) {
                    location = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("CopyNumber")) {
                    copyNumber = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("enumeration")) {
                    enumeration = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("chronology")) {
                    chronology = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.ITEM.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("ItemBarcode")) {
                    barcode = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.HOLDINGS.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("CallNumber")) {
                    if (StringUtils.isBlank(callNumber)) {
                        callNumber = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                    }
                } else if (searchResultField.getDocType().equalsIgnoreCase(DocType.HOLDINGS.getCode()) && searchResultField.getFieldName().equalsIgnoreCase("LocationName")) {
                    if (StringUtils.isBlank(location)) {
                        location = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                    }
                }
            }
        }
        String fileName = "Call/Paging Slip" + "_" + (editorForm.getTitle() != null ? editorForm.getTitle() : "") + "_" + new Date(System.currentTimeMillis()) + ".pdf";
        if (LOG.isInfoEnabled()) {
            LOG.info("File Created :" + title + "file name ::" + fileName + "::");
        }
        try {
            Document document = this.getDocument(0, 0, 5, 5);
            OutputStream outputStream = null;
            response.setContentType("application/pdf");
            //response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            outputStream = response.getOutputStream();
            PdfWriter.getInstance(document, outputStream);
            Font boldFont = new Font(Font.TIMES_ROMAN, 15, Font.BOLD);
            document.open();
            document.newPage();
            PdfPTable pdfTable = new PdfPTable(3);
            pdfTable.setWidths(new int[]{20, 2, 30});
            Paragraph paraGraph = new Paragraph();
            paraGraph.setAlignment(Element.ALIGN_CENTER);
            paraGraph.add(new Chunk("Call/Paging Slip", boldFont));
            paraGraph.add(Chunk.NEWLINE);
            paraGraph.add(Chunk.NEWLINE);
            paraGraph.add(Chunk.NEWLINE);
            document.add(paraGraph);

            pdfTable.addCell(getPdfPCellInJustified("Title"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(title));

            pdfTable.addCell(getPdfPCellInJustified("Author"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(author));

            pdfTable.addCell(getPdfPCellInJustified("Call Number"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(callNumber));

            pdfTable.addCell(getPdfPCellInJustified("Location"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(location));

            pdfTable.addCell(getPdfPCellInJustified("Copy Number"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(copyNumber));

            pdfTable.addCell(getPdfPCellInJustified("Enumeration"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(enumeration));

            pdfTable.addCell(getPdfPCellInJustified("Chronology"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(chronology));

            pdfTable.addCell(getPdfPCellInJustified("Barcode"));
            pdfTable.addCell(getPdfPCellInLeft(":"));
            pdfTable.addCell(getPdfPCellInJustified(barcode));

            document.add(pdfTable);
            document.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            LOG.error(e, e);
        }
    }

    private PdfPCell getPdfPCellInJustified(String chunk) {
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(new Chunk(chunk)));
        pdfPCell.setBorder(pdfPCell.NO_BORDER);
        pdfPCell.setHorizontalAlignment(pdfPCell.ALIGN_JUSTIFIED);
        return pdfPCell;
    }

    private PdfPCell getPdfPCellInLeft(String chunk) {
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(new Chunk(chunk)));
        pdfPCell.setBorder(pdfPCell.NO_BORDER);
        pdfPCell.setHorizontalAlignment(pdfPCell.ALIGN_LEFT);
        return pdfPCell;
    }

    public Document getDocument(float f1, float f2, float f3, float f4) {
        Document document = new Document(PageSize.A4);
        document.setMargins(f1, f2, f3, f4);
        return document;
    }


    private void prepareGlobalEditFields(EditorForm editorForm, String docType) {

        if (DocType.INSTANCE.getCode().equals(docType) || DocType.HOLDINGS.getCode().equals(docType)) {
            editorForm.setHeaderText(OLEConstants.GLOBAL_EDIT_HOLDINGS_HEADER_MESSAGE);
        } else if (DocType.EINSTANCE.getCode().equals(docType) || DocType.EHOLDINGS.getCode().equals(docType)) {
            editorForm.setHeaderText(OLEConstants.GLOBAL_EDIT_EHOLDINGS_HEADER_MESSAGE);
        } else if (DocType.ITEM.getCode().equals(docType)) {
            editorForm.setHeaderText(OLEConstants.GLOBAL_EDIT_ITEM_HEADER_MESSAGE);
        }

        DocumentSearchConfig documentSearchConfig = DocumentSearchConfig.getDocumentSearchConfig();
        List<DocTypeConfig> docTypeFields = documentSearchConfig.getDocTypeConfigs();

        for (DocTypeConfig docTypeConfig : docTypeFields) {

            List<DocFormatConfig> docFormatConfigList = docTypeConfig.getDocFormatConfigList();

            if (DocType.HOLDINGS.getCode().equals(docTypeConfig.getName()) && DocType.HOLDINGS.getCode().equals(docType)) {
                setGlobalEditFlagValues(editorForm, docFormatConfigList, docType);
            } else if (DocType.EHOLDINGS.getCode().equals(docTypeConfig.getName()) && DocType.EHOLDINGS.getCode().equals(docType)) {
                setGlobalEditFlagValues(editorForm, docFormatConfigList, docType);
            } else if (DocType.ITEM.getCode().equals(docType) && DocType.ITEM.getCode().equals(docTypeConfig.getName())) {
                setGlobalEditFlagValues(editorForm, docFormatConfigList, docType);
            }

        }

    }

    private void setGlobalEditFlagValues(EditorForm editorForm, List<DocFormatConfig> docFormatConfigList, String docType) {

        for (DocFormatConfig docFormatConfig : docFormatConfigList) {

            List<DocFieldConfig> docFieldConfigList = docFormatConfig.getDocFieldConfigList();

            if (OLEConstants.OLEML_FORMAT.equals(docFormatConfig.getName())) {
                if (DocType.INSTANCE.getCode().equals(docType) || DocType.HOLDINGS.getCode().equals(docType)) {
                    GlobalEditHoldingsFieldsFlagBO globalEditHoldingsFieldsFlagBO = editorForm.getGlobalEditHoldingsFieldsFlagBO();
                    for (DocFieldConfig docFieldConfig : docFieldConfigList) {

                        if (docFieldConfig.isGloballyEditable()) {

                            if (OLEConstants.CALL_NUMBER.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setCallNumberEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_PREFIX.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setCallNumberPrefixEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_TYPE_CODE.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setCallNumberTypeEditFlag(true);
                            } else if (OLEConstants.SHELVING_ORDER.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setShelvingOrderEditFlag(true);
                            } else if (OLEConstants.LOCATION_LEVEL.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setLocationEditFlag(true);
                            } else if (OLEConstants.HOLDINGS_NOTE.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setHoldingsNoteEditFlag(true);
                            } else if (OLEConstants.URI.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setAccessInformationEditFlag(true);
                            } else if (OLEConstants.RECEIPT_STATUS_CODE.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setReceiptStatusEditFlag(true);
                            } else if (OLEConstants.COPY_NUMBER_LABEL.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setCopyNumberEditFlag(true);
                            } else if (OLEConstants.EXTENTOFOWNERSHIP_NOTE_VALUE_DISPLAY.equals(docFieldConfig.getName()) || OLEConstants.EXTENTOFOWNERSHIP_NOTE_TYPE_DISPLAY.equals(docFieldConfig.getName()) || OLEConstants.EXTENTOFOWNERSHIP_Type_display.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setExtentOwnerShipEditFlag(true);
                            } else if (OLEConstants.URI_SEARCH.equals(docFieldConfig.getName()) || OLEConstants.URI_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditHoldingsFieldsFlagBO.setExtendedInfoEditFlag(true);
                            }
                            //TODO - Need to do for other fields in holdings
                        }
                    }
                } else if (DocType.EINSTANCE.getCode().equals(docType) || DocType.EHOLDINGS.getCode().equals(docType)) {
                    GlobalEditEHoldingsFieldsFlagBO globalEditEHoldingsFieldsFlagBO = editorForm.getGlobalEditEHoldingsFieldsFlagBO();
                    for (DocFieldConfig docFieldConfig : docFieldConfigList) {

                            if (docFieldConfig.isGloballyEditable()) {
                            if (OLEConstants.CALL_NUMBER.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setCallNumberEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_PREFIX.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setCallNumberPrefixEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_TYPE_CODE.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setCallNumberTypeEditFlag(true);
                            } else if (OLEConstants.SHELVING_ORDER.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setShelvingOrderEditFlag(true);
                            } else if (OLEConstants.LOCATION_LEVEL.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setLocationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ACCESS_STATUS.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAccessStatusEditFlag(true);
                            } else if (OLEConstants.EHOLDING_PLATFORM_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setPlatformEditFlag(true);
                            } else if (OLEConstants.EHOLDING_IMPRINT.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setImprintEditFlag(true);
                            } else if (OLEConstants.EHOLDING_E_PUBLISHER.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setPublisherEditFlag(true);
                            } else if (OLEConstants.EHOLDING_STATISTICAL_CODE.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setStatisticalCodeEditFlag(true);
                            } /*else if (OLEConstants.EHOLDING_SUBSCRIPTION_STATUS.equals(docFieldConfig.getName())) {
                                // TODO add field to js staffOnlyEditFlag
                                globalEditEHoldingsFieldsFlagBO.setSubscriptionEditFlag(true);
                            }*/ else if (OLEConstants.EHOLDING_SUBSCRIPTION_STATUS.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAcquisitionInformationEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setSubscriptionEditFlag(true);
                            } else if (OLEConstants.EHOLDING_LINK.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setLinkEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setAccessInformationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_SIMULTANEOUS_USER.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setSimultaneousEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setAccessInformationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_PERSIST_LINK.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setPersistentLinkEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setAccessInformationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ACCESS_LOCATION.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAccessInformationEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setAccessLocationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_LINK_TEXT.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setLinkTextEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ADMIN_USER.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAdminUserNameEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ADMIN_PWSD.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAdminPasswordEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ACCESS_USR_NAME.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAccessUserNameEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ACCESS_PSWD.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAccessPasswordEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ADMIN_URL.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAdminUrlEditFlag(true);
                            } else if (OLEConstants.EHOLDING_AUTHENTICATION_TYPE.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setAuthenticationEditFlag(true);
                            } else if (OLEConstants.EHOLDING_PROXIED.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setProxiedEditFlag(true);
                            } else if (OLEConstants.EHOLDING_ILL_IND.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setLicenseDetailsEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setIllEditFlag(true);
                            } else if (OLEConstants.EHOLDING_COVERAGE.equals(docFieldConfig.getName())) {
                                //globalEditEHoldingsFieldsFlagBO.setExtentOfOwnerShipEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setCoverageExtentOfOwnerShipEditFlag(true);
                            } else if (OLEConstants.EHOLDING_PERPETUAL.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setExtentOfOwnerShipEditFlag(true);
                                globalEditEHoldingsFieldsFlagBO.setPerpetualAccessEditFlag(true);
                            } else if (OLEConstants.HOLDINGS_NOTE.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.seteHoldingsNoteEditFlag(true);
                            } else if (OLEConstants.DONOR_CODE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setDonorCodeEditFlag(true);
                            } else if (OLEConstants.EHOLDING_DONOR_PUBLIC_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setDonorPublicDisplayEditFlag(true);
                            } else if (OLEConstants.EHOLDING_DONOR_NOTE.equals(docFieldConfig.getName())) {
                                globalEditEHoldingsFieldsFlagBO.setDonorNoteEditFlag(true);
                            }
                        }
                    }

                    // TODO -  need to do for E-holdings for global edit

                } else if (DocType.ITEM.getCode().equals(docType)) {

                    GlobalEditItemFieldsFlagBO globalEditItemFieldsFlagBO = editorForm.getGlobalEditItemFieldsFlagBO();
                    for (DocFieldConfig docFieldConfig : docFieldConfigList) {
                        if (docFieldConfig.isGloballyEditable()) {

                            if (OLEConstants.CALL_NUMBER.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCallNumberEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_PREFIX.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCallNumberPrefixEditFlag(true);
                            } else if (OLEConstants.CALL_NUMBER_TYPE_CODE.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCallNumberTypeEditFlag(true);
                            } else if (OLEConstants.SHELVING_ORDER.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setShelvingOrderEditFlag(true);
                            } else if (OLEConstants.LOCATION_LEVEL.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setLocationEditFlag(true);
                            } else if (OLEConstants.ITEM_STATUS_CODE_VALUE.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setItemStatusEditFlag(true);
                                globalEditItemFieldsFlagBO.setAccessInfoEditFlag(true);
                            } else if (OLEConstants.PO_LINE_ITEM_IDENTIFIER.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCallNumberTypeEditFlag(true);
                            } else if (OLEConstants.VENDOR_LINE_ITEM_IDENTIFIER.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setVendorLineItemIDEditFlag(true);
                            } else if (OLEConstants.BAR_CODE_ARSL.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setBarcodeARSLEditFlag(true);
                            } else if (OLEConstants.STATISTICAL_SEARCH_CODE.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setStatisticalSearchingCodesEditFlag(true);
                            } else if (OLEConstants.ITEM_TYPE_CODE_VALUE.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setItemTypeEditFlag(true);
                            } else if (OLEConstants.COPY_NUMBER_LABEL.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCopyNumberEditFlag(true);
                            } else if (OLEConstants.VOLUME_NUMBER.equals(docFieldConfig.getName()) || OLEConstants.VOLUME_NUMBER_LABEL.equals(docFieldConfig.getName())) {
                                //globalEditItemFieldsFlagBO.set(true);
                            } else if (OLEConstants.ENUMARATION.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setEnumerationEditFlag(true);
                            } else if (OLEConstants.CHRONOLOGY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setChronologyEditFlag(true);
                            } else if (OLEConstants.DONORPUBLIC_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setDonorPublicDisplayEditFlag(true);
                            } else if (OLEConstants.DONORNOTE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setDonorNoteEditFlag(true);
                            } else if (OLEConstants.ITEMNOTE_TYPE_DISPLAY.equals(docFieldConfig.getName()) ) {
                                globalEditItemFieldsFlagBO.setExtndInfoEditFlag(true);
                            } else if (OLEConstants.ITEMBARCODE_DISPLAY.equals(docFieldConfig.getName()) || OLEConstants.ITEMBARCODE_SEARCH.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setBarcodeEditFlag(true);
                            } else if (OLEConstants.TEMPITEMTYPE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setTempItemTypeEditFlag(true);
                            } else if (OLEConstants.DONORCODE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setDonorCodeEditFlag(true);
                            } else if (OLEConstants.FORMERIDENTIFIER_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setFormerIdentifiersEditFlag(true);
                            } else if (OLEConstants.FASTADD_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setFastAddEditFlag(true);
                            } else if (OLEConstants.PIECES_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setNumberOfPiecesEditFlag(true);
                            } else if (OLEConstants.ITEMSTATUSDATE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setItemStatusDateEditFlag(true);
                            } else if (OLEConstants.CHECKINNOTE_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setCheckinNoteEditFlag(true);
                            } else if (OLEConstants.DUEDATETIME_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setDueDatetimeEditFlag(true);
                            } else if (OLEConstants.CLAIMSRETURNFLAG_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setClaimsReturnEditFlag(true);
                            } else if (OLEConstants.MISSINGPIECEFLAG_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setMissingPieceEditFlag(true);
                            } else if (OLEConstants.ITEMDAMAGEDSTATUS_DISPLAY.equals(docFieldConfig.getName())) {
                                globalEditItemFieldsFlagBO.setItemDamagedStatusEditFlag(true);
                            }

                            // TODO - need to do for other fields in ITEM
                        }
                    }
                }
            }


        }


    }

}