package org.kuali.ole.select.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.EHoldings;
import org.kuali.ole.docstore.common.document.Holdings;
import org.kuali.ole.docstore.common.document.content.instance.OleHoldings;
import org.kuali.ole.docstore.common.document.content.instance.xstream.HoldingOlemlRecordProcessor;
import org.kuali.ole.select.bo.*;
import org.kuali.ole.select.document.*;
import org.kuali.ole.select.form.OLEEResourceRecordForm;
import org.kuali.ole.service.OLEEResourceSearchService;
import org.kuali.ole.service.OleLicenseRequestWebService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.TransactionalDocumentControllerBase;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.kuali.rice.krad.web.form.TransactionalDocumentFormBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Date;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: srinivasane
 * Date: 6/21/13
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/oleERSController")
public class OLEEResourceRecordController extends TransactionalDocumentControllerBase {


    /**
     * @see org.kuali.rice.krad.web.controller.UifControllerBase#createInitialForm(javax.servlet.http.HttpServletRequest)
     */
    private static final Logger LOG = Logger.getLogger(OLEEResourceRecordController.class);

    private OleLicenseRequestWebService oleLicenseRequestWebService = null;
    private KualiRuleService kualiRuleService;
    private BusinessObjectService businessObjectService;
    private OLEEResourceSearchService oleEResourceSearchService = null;
    private DocstoreClientLocator docstoreClientLocator;

    public DocstoreClientLocator getDocstoreClientLocator() {
        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }

    /**
     * This method will return new Instance of OLEEResourceRecordForm.
     *
     * @param request
     * @return OLEEResourceRecordForm.
     */
    @Override
    protected TransactionalDocumentFormBase createInitialForm(HttpServletRequest request) {
        OLEEResourceRecordForm oleeResourceRecordForm = new OLEEResourceRecordForm();
        oleeResourceRecordForm.setStatusDate(new Date(System.currentTimeMillis()).toString());
        oleeResourceRecordForm.setDocumentDescription(OLEConstants.OLEEResourceRecord.NEW_E_RESOURCE_REC);
        return oleeResourceRecordForm;
    }

    /**
     * This method takes the initial request when click on E-Resource Record Screen.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @Override
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Start -- Start Method of OlePatronRecordForm");
        OLEEResourceRecordForm oleeResourceRecordForm = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleeResourceRecordForm.getDocument();
        oleeResourceRecordDocument.setStatusDate(new Date(System.currentTimeMillis()).toString());
        return super.start(oleeResourceRecordForm, result, request, response);
    }

    /**
     * This method populates date of the eventlog object thereby adding to the existing list.
     *
     * @param uifForm
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=addEventLogLine")
    public ModelAndView addEventLogLine(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(uifForm, addLinePath);
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        OLEEResourceEventLog oleERSEventLog = (OLEEResourceEventLog) eventObject;
        oleERSEventLog.setEventType(OLEConstants.OleLicenseRequest.USER);
        oleERSEventLog.setCurrentTimeStamp();
        oleERSEventLog.setOleERSIdentifier(oleeResourceRecordDocument.getDocumentNumber());
        return addLine(uifForm, result, request, response);
    }

    /**
     * This method populates date of the eventlog object thereby adding to the existing list.
     *
     * @param uifForm
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=deleteEventLogLine")
    public ModelAndView deleteEventLogLine(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        OLEEResourceEventLog oleERSEventLog = oleeResourceRecordDocument.getOleERSEventLogs().get(Integer.parseInt(selectedLineIndex));
        //getBusinessObjectService().delete(oleERSEventLog);
        return deleteLine(form, result, request, response);
    }

    /**
     * This method Creates the License Request Document and link to the E-Resource document
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=performCreateLicenseRequest")
    public ModelAndView performCreateLicenseRequest(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                                    HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("performCreateLicenseRequest method starts");
        OLEEResourceRecordForm oleeResourceRecordForm = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleeResourceRecordForm.getDocument();
        OleLicenseRequestBo oleLicenseRequestBo = getOleLicenseRequestService().createLicenseRequest(
                oleeResourceRecordDocument.getDocumentNumber(), null);
        OLEEResourceLicense oleeResourceLicense = new OLEEResourceLicense();
        oleeResourceLicense.setLicenseDocumentNumber(oleLicenseRequestBo.getLicenseDocumentNumber());
        oleeResourceLicense.setDocumentRouteHeaderValue(oleLicenseRequestBo.getDocumentRouteHeaderValue());
       /* Map<String,String> searchCriteria = new HashMap<String,String>();
        searchCriteria.put(OLEConstants.DOC_ID,oleLicenseRequestBo.getLicenseDocumentNumber());*/
        /*DocumentRouteHeaderValue documentHeader= (DocumentRouteHeaderValue)  getBusinessObjectService().findByPrimaryKey
                (DocumentRouteHeaderValue.class,searchCriteria);*/
        /*DocumentRouteHeaderValue documentHeader= KEWServiceLocator.getRouteHeaderService().getRouteHeader(oleLicenseRequestBo.getLicenseDocumentNumber(), true);
        oleLicenseRequestBo.setDocumentRouteHeaderValue(documentHeader);*/

        List<OLEEResourceInstance> listOfERInstances = oleeResourceRecordDocument.getOleERSInstances();
        List<OleLicenseRequestItemTitle> oleLicenseRequestItemTitles = new ArrayList<>();
        for (OLEEResourceInstance oleeResourceInstance : listOfERInstances) {
            OleLicenseRequestItemTitle oleLicenseRequestItemTitle = new OleLicenseRequestItemTitle();
            oleLicenseRequestItemTitle.setItemUUID(oleeResourceInstance.getBibId());
            oleLicenseRequestItemTitle.setOleLicenseRequestId(oleLicenseRequestBo.getOleLicenseRequestId());
            oleLicenseRequestItemTitle.setOleLicenseRequestBo(oleLicenseRequestBo);
            oleLicenseRequestItemTitles.add(oleLicenseRequestItemTitle);
        }
        oleLicenseRequestBo.setOleLicenseRequestItemTitles(oleLicenseRequestItemTitles);
        oleeResourceLicense.setOleLicenseRequestBo(oleLicenseRequestBo);
        oleeResourceRecordDocument.getOleERSLicenseRequests().add(oleeResourceLicense);

        //oleeResourceRecordDocument.
        return getUIFModelAndView(oleeResourceRecordForm);
    }

    /**
     * Saves the document instance contained on the form
     *
     * @param form - document form base containing the document instance that will be saved
     * @return ModelAndView
     */
    @Override
    @RequestMapping(params = "methodToCall=save")
    public ModelAndView save(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                             HttpServletRequest request, HttpServletResponse response) throws Exception {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        oleeResourceRecordDocument.setStatusDate(oleERSform.getStatusDate().toString());
        if (oleeResourceRecordDocument.getTitle() != null) {
            if (oleeResourceRecordDocument.getTitle().length() < 40) {
                oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(oleeResourceRecordDocument.getTitle());
            } else {
                String documentDescription = oleeResourceRecordDocument.getTitle().substring(0, 39);
                oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(documentDescription);
            }
        }
        List<OLEMaterialTypeList> oleMaterialTypeList = oleeResourceRecordDocument.getOleMaterialTypes();
        List<OLEFormatTypeList> oleFormatTypeLists = oleeResourceRecordDocument.getOleFormatTypes();
        List<OLEContentTypes> oleContentTypeList = oleeResourceRecordDocument.getOleContentTypes();
        List<String> instanceId = new ArrayList<String>();
        /*boolean oleERSFlag = false;
        oleERSFlag &= getKualiRuleService().applyRules(new OLEMaterialTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleMaterialTypes()));
        oleERSFlag &= getKualiRuleService().applyRules(new OLEContentTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleContentTypes()));
        if (oleERSFlag) {
        return getUIFModelAndView(oleERSform);
        }*/
        boolean flag = false;
        boolean datesFlag = true;
        flag = getOleEResourceSearchService().validateEResourceDocument(oleeResourceRecordDocument);
        datesFlag &= getOleEResourceSearchService().validateCoverageStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validateCoverageEndDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessEndDates(oleeResourceRecordDocument,oleERSform);
        if (flag) {
            return getUIFModelAndView(oleERSform);
        }
        if (!datesFlag) {
            return getUIFModelAndView(oleERSform);
        }
        String fileName = oleeResourceRecordDocument.getDocumentNumber();
        if (oleERSform.isCreateInstance()) {
            getOleEResourceSearchService().getNewInstance(oleeResourceRecordDocument, fileName);
            oleERSform.setCreateInstance(false);
        }
        oleERSform.setBibId(null);
        oleERSform.setInstanceId(null);
        oleERSform.setLinkInstance(false);
        if (oleeResourceRecordDocument.getOleERSIdentifier() != null && !oleeResourceRecordDocument.getOleERSIdentifier().isEmpty()) {
            oleeResourceRecordDocument = getOleEResourceSearchService().getNewOleERSDoc(oleeResourceRecordDocument);
            Map<String, String> tempId = new HashMap<String, String>();
            tempId.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleeResourceRecordDocument.getOleERSIdentifier());
            OLEEResourceRecordDocument tempDocument = (OLEEResourceRecordDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, tempId);
            int instancesSize = tempDocument.getOleERSInstances().size();
            int instanceSize = oleeResourceRecordDocument.getOleERSInstances().size();
            if (!oleERSform.isDefaultDatesFlag() && oleERSform.getPageId() != null && oleERSform.getPageId().equalsIgnoreCase("OLEEResourceRecordView-E-ResourceInstanceTab")) {
                if (tempDocument.iseInstanceFlag() && instancesSize >= instanceSize && !oleERSform.isRemoveInstanceFlag()) {
                    try {
                        super.reload(oleERSform, result, request, response);
                    } catch (Exception e) {
                        LOG.error("exception while reloading the e-resource document" + e.getMessage());
                        throw new RuntimeException("exception while reloading the e-resource document", e);
                    }
                }
            }
            oleERSform.setRemoveInstanceFlag(false);
            oleERSform.setDefaultDatesFlag(false);
        }
        if (StringUtils.isNotBlank(oleeResourceRecordDocument.getFundCode())){
            Map fundCodeMap=new HashMap();
            fundCodeMap.put(OLEConstants.OLEBatchProcess.VENDOR_REF_NUMBER,oleeResourceRecordDocument.getFundCode());
            OleVendorAccountInfo oleVendorAccountInfo = getBusinessObjectService().findByPrimaryKey(OleVendorAccountInfo.class, fundCodeMap);
            if (oleVendorAccountInfo==null){
                GlobalVariables.getMessageMap().putError(OLEConstants.OLEEResourceRecord.FUND_CODE,OLEConstants.OLEEResourceRecord.ERROR_FUND_CODE);
            }
        }
        getOleEResourceSearchService().getPOInvoiceForERS(oleeResourceRecordDocument);
        getOleEResourceSearchService().getPOAndInvoiceItemsWithoutDuplicate(oleeResourceRecordDocument);
        return super.save(oleERSform, result, request, response);
    }

    /**
     * Routes the document instance contained on the form
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=route")
    public ModelAndView route(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        oleeResourceRecordDocument.setStatusDate(oleERSform.getStatusDate().toString());
        if (oleeResourceRecordDocument.getTitle().length() < 40) {
            oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(oleeResourceRecordDocument.getTitle());
        } else {
            String documentDescription = oleeResourceRecordDocument.getTitle().substring(0, 39);
            oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(documentDescription);
        }
        List<OLEMaterialTypeList> oleMaterialTypeList = oleeResourceRecordDocument.getOleMaterialTypes();
        List<OLEFormatTypeList> oleFormatTypeLists = oleeResourceRecordDocument.getOleFormatTypes();
        List<OLEContentTypes> oleContentTypeList = oleeResourceRecordDocument.getOleContentTypes();
        List<String> instanceId = new ArrayList<String>();
        /*boolean oleERSFlag = false;
        oleERSFlag &= getKualiRuleService().applyRules(new OLEMaterialTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleMaterialTypes()));
        oleERSFlag &= getKualiRuleService().applyRules(new OLEContentTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleContentTypes()));
        if (oleERSFlag) {
        return getUIFModelAndView(oleERSform);
        }*/
        boolean flag = false;
        boolean datesFlag = true;
        flag=getOleEResourceSearchService().validateEResourceDocument(oleeResourceRecordDocument);
        datesFlag &= getOleEResourceSearchService().validateCoverageStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validateCoverageEndDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessEndDates(oleeResourceRecordDocument,oleERSform);
        if (flag) {
            return getUIFModelAndView(oleERSform);
        }
        if (!datesFlag) {
            return getUIFModelAndView(oleERSform);
        }
        String fileName = oleeResourceRecordDocument.getDocumentNumber();
        if (oleERSform.isCreateInstance()) {
            try {
                getOleEResourceSearchService().getNewInstance(oleeResourceRecordDocument, fileName);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            oleERSform.setCreateInstance(false);
        }
        oleERSform.setBibId(null);
        oleERSform.setInstanceId(null);
        oleERSform.setLinkInstance(false);
        if (oleeResourceRecordDocument.getOleERSIdentifier() != null && !oleeResourceRecordDocument.getOleERSIdentifier().isEmpty()) {
            oleeResourceRecordDocument = getOleEResourceSearchService().getNewOleERSDoc(oleeResourceRecordDocument);
            Map<String, String> tempId = new HashMap<String, String>();
            tempId.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleeResourceRecordDocument.getOleERSIdentifier());
            OLEEResourceRecordDocument tempDocument = (OLEEResourceRecordDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, tempId);
            int instancesSize = tempDocument.getOleERSInstances().size();
            int instanceSize = oleeResourceRecordDocument.getOleERSInstances().size();
            if (tempDocument.iseInstanceFlag() && instancesSize > instanceSize && !oleERSform.isRemoveInstanceFlag()) {
                try {
                    super.reload(oleERSform, result, request, response);
                } catch (Exception e) {
                    LOG.error("Exception while reloading the e-resource document"+e.getMessage());
                }
            }
            oleERSform.setRemoveInstanceFlag(false);
        }
        if (StringUtils.isNotBlank(oleeResourceRecordDocument.getFundCode())){
            Map fundCodeMap=new HashMap();
            fundCodeMap.put(OLEConstants.OLEBatchProcess.VENDOR_REF_NUMBER,oleeResourceRecordDocument.getFundCode());
            OleVendorAccountInfo oleVendorAccountInfo = getBusinessObjectService().findByPrimaryKey(OleVendorAccountInfo.class, fundCodeMap);
            if (oleVendorAccountInfo==null){
                GlobalVariables.getMessageMap().putError(OLEConstants.OLEEResourceRecord.FUND_CODE,OLEConstants.OLEEResourceRecord.ERROR_FUND_CODE);
            }
        }
        return super.route(oleERSform, result, request, response);
    }

    /**
     * Performs the approve workflow action on the form document instance
     *
     * @param form - document form base containing the document instance that will be approved
     * @return ModelAndView
     */
    @Override
    @RequestMapping(params = "methodToCall=approve")
    public ModelAndView approve(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        oleeResourceRecordDocument.setStatusDate(oleERSform.getStatusDate().toString());
        if (oleeResourceRecordDocument.getTitle().length() < 40) {
            oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(oleeResourceRecordDocument.getTitle());
        } else {
            String documentDescription = oleeResourceRecordDocument.getTitle().substring(0, 39);
            oleeResourceRecordDocument.getDocumentHeader().setDocumentDescription(documentDescription);
        }
        List<OLEMaterialTypeList> oleMaterialTypeList = oleeResourceRecordDocument.getOleMaterialTypes();
        List<OLEFormatTypeList> oleFormatTypeLists = oleeResourceRecordDocument.getOleFormatTypes();
        List<OLEContentTypes> oleContentTypeList = oleeResourceRecordDocument.getOleContentTypes();
        List<String> instanceId = new ArrayList<String>();
        /*boolean oleERSFlag = false;
        oleERSFlag &= getKualiRuleService().applyRules(new OLEMaterialTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleMaterialTypes()));
        oleERSFlag &= getKualiRuleService().applyRules(new OLEContentTypeEvent(oleeResourceRecordDocument,oleeResourceRecordDocument.getOleContentTypes()));
        if (oleERSFlag) {
        return getUIFModelAndView(oleERSform);
        }*/
        boolean flag = false;
        boolean datesFlag = true;
        flag=getOleEResourceSearchService().validateEResourceDocument(oleeResourceRecordDocument);
        datesFlag &= getOleEResourceSearchService().validateCoverageStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validateCoverageEndDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessStartDates(oleeResourceRecordDocument,oleERSform);
        datesFlag &= getOleEResourceSearchService().validatePerpetualAccessEndDates(oleeResourceRecordDocument,oleERSform);
        if (flag) {
            return getUIFModelAndView(oleERSform);
        }
        if (!datesFlag) {
            return getUIFModelAndView(oleERSform);
        }
        String fileName = oleeResourceRecordDocument.getDocumentNumber();
        if (oleERSform.isCreateInstance()) {
            getOleEResourceSearchService().getNewInstance(oleeResourceRecordDocument, fileName);
            oleERSform.setCreateInstance(false);
        }
        oleERSform.setBibId(null);
        oleERSform.setInstanceId(null);
        oleERSform.setLinkInstance(false);
        if (oleeResourceRecordDocument.getOleERSIdentifier() != null && !oleeResourceRecordDocument.getOleERSIdentifier().isEmpty()) {
            oleeResourceRecordDocument = getOleEResourceSearchService().getNewOleERSDoc(oleeResourceRecordDocument);
            Map<String, String> tempId = new HashMap<String, String>();
            tempId.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleeResourceRecordDocument.getOleERSIdentifier());
            OLEEResourceRecordDocument tempDocument = (OLEEResourceRecordDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, tempId);
            int instancesSize = tempDocument.getOleERSInstances().size();
            int instanceSize = oleeResourceRecordDocument.getOleERSInstances().size();
            if (tempDocument.iseInstanceFlag() && instancesSize > instanceSize && !oleERSform.isRemoveInstanceFlag()) {
                try {
                    super.reload(oleERSform, result, request, response);
                } catch (Exception e) {
                    LOG.error("Exception while reloading the e-resource document"+e.getMessage());
                }
            }
            oleERSform.setRemoveInstanceFlag(false);
        }
        if (StringUtils.isNotBlank(oleeResourceRecordDocument.getFundCode())){
            Map fundCodeMap=new HashMap();
            fundCodeMap.put(OLEConstants.OLEBatchProcess.VENDOR_REF_NUMBER,oleeResourceRecordDocument.getFundCode());
            OleVendorAccountInfo oleVendorAccountInfo = getBusinessObjectService().findByPrimaryKey(OleVendorAccountInfo.class, fundCodeMap);
            if (oleVendorAccountInfo==null){
                GlobalVariables.getMessageMap().putError(OLEConstants.OLEEResourceRecord.FUND_CODE,OLEConstants.OLEEResourceRecord.ERROR_FUND_CODE);
            }
        }
        return super.approve(oleERSform, result, request, response);
    }

    /*@Override
    @RequestMapping(params = "methodToCall=cancel")
    public ModelAndView cancel(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                               HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        if (oleeResourceRecordDocument != null) {
            Map<String, String> criteriaStatusIdMap = new HashMap<String, String>();
            criteriaStatusIdMap.put(OLEConstants.OLEEResourceRecord.STATUS_NAME, OLEConstants.OLEEResourceRecord.CANCELED);
            List<OLEEResourceStatus> oleERSStatusList = (List<OLEEResourceStatus>) getBusinessObjectService().findMatching(OLEEResourceStatus.class, criteriaStatusIdMap);
            if (oleERSStatusList.size() > 0) {
                OLEEResourceStatus oleERSStatus = oleERSStatusList.get(0);
                if (oleERSStatus != null) {
                    oleeResourceRecordDocument.setStatusId(oleERSStatus.getOleEResourceStatusId());
                    oleeResourceRecordDocument.setStatusName(oleERSStatus.getOleEResourceStatusName());
                }
            }
        }
        try {
            save(oleERSform, result, request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return super.cancel(oleERSform, result, request, response);
    }
*/

    /**
     * Create E-Instance document instance contained on the form
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=createInstance")
    public ModelAndView createInstance(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        oleERSform.setSelectFlag(true);
        oleERSform.setLinkInstance(false);
        oleERSform.setCreateInstance(true);
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    /**
     * close the popup in instance tab
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=closePopup")
    public ModelAndView closePopup(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                   HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    /**
     * This method takes List of UUids as parameter and creates a LinkedHashMap with instance as key and id as value. and calls
     * Docstore's QueryServiceImpl class getWorkBibRecords method and return workBibDocument for passed instance Id.
     *
     * @param instanceIdsList
     * @return List<WorkBibDocument>
     */
//    private List<WorkBibDocument> getWorkBibDocuments(List<String> instanceIdsList, String docType) {
//        List<LinkedHashMap<String, String>> instanceIdMapList = new ArrayList<LinkedHashMap<String, String>>();
//        for (String instanceId : instanceIdsList) {
//            LinkedHashMap<String, String> instanceIdMap = new LinkedHashMap<String, String>();
//            instanceIdMap.put(docType, instanceId);
//            instanceIdMapList.add(instanceIdMap);
//        }
//
//        QueryService queryService = QueryServiceImpl.getInstance();
//        List<WorkBibDocument> workBibDocuments = new ArrayList<WorkBibDocument>();
//        try {
//            workBibDocuments = queryService.getWorkBibRecords(instanceIdMapList);
//        } catch (Exception ex) {
//            // TODO Auto-generated catch block
//            ex.printStackTrace();
//        }
//        return workBibDocuments;
//    }

    /**
     * Called by the delete line action for a model collection. Method
     * determines which collection the action was selected for and the line
     * index that should be removed, then invokes the view helper service to
     * process the action
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=removeInstance")
    public ModelAndView removeInstance(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {

        OLEEResourceRecordForm oleEResourceRecordForm = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleEResourceRecordDocument = (OLEEResourceRecordDocument) oleEResourceRecordForm.getDocument();
        String selectedCollectionPath = oleEResourceRecordForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException(OLEConstants.OLEEResourceRecord.BLANK_SELECTED_INDEX);
        }
        int selectedLineIndex = -1;
        String selectedLine = oleEResourceRecordForm.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        if (StringUtils.isNotBlank(selectedLine)) {
            OLEEResourceInstance oleERSInstance = oleEResourceRecordDocument.getOleERSInstances().get(Integer.parseInt(selectedLine));
            Map removePOMap = new HashMap();
            removePOMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_INSTANCE_ID, oleERSInstance.getInstanceId());
            removePOMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleERSInstance.getOleERSIdentifier());
            List<OLEEResourcePO> oleERSPOs = (List<OLEEResourcePO>) getBusinessObjectService().findMatching(OLEEResourcePO.class, removePOMap);
            removePOMap.remove(OLEConstants.OLEEResourceRecord.ERESOURCE_INSTANCE_ID);
            removePOMap.put(OLEConstants.HOLDINGS_ID, oleERSInstance.getInstanceId());
            List<OLEEResourceInvoices> oleERSInvoices = (List<OLEEResourceInvoices>) getBusinessObjectService().findMatching(OLEEResourceInvoices.class, removePOMap);
            if (oleERSPOs.size() > 0) {
                if (oleERSInvoices.size()>0){
                    for (int i = 0; i < oleERSInvoices.size(); i++) {
                        for (int j = 0; j < oleEResourceRecordDocument.getOleERSInvoices().size(); j++) {
                            if (oleEResourceRecordDocument.getOleERSInvoices().get(j).getOleEResInvoiceId().equals(oleERSInvoices.get(i).getOleEResInvoiceId())) {
                                oleEResourceRecordDocument.getOleERSInvoices().remove(j);
                            }
                        }
                    }
                }
                for (int i = 0; i < oleERSPOs.size(); i++) {
                    for (int j = 0; j < oleEResourceRecordDocument.getOleERSPOItems().size(); j++) {
                        if (oleEResourceRecordDocument.getOleERSPOItems().get(j).getOleEResPOId().equals(oleERSPOs.get(i).getOleEResPOId())) {
                            oleEResourceRecordDocument.getOleERSPOItems().remove(j);
                        }
                    }
                }
            }
            try {
                Holdings holdings = new EHoldings();
                OleHoldings oleHoldings = new OleHoldings();
                holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(oleERSInstance.getInstanceId());
                if (holdings instanceof EHoldings) {
                    oleHoldings = new HoldingOlemlRecordProcessor().fromXML(holdings.getContent());
                    oleHoldings.setEResourceId(null);
                    holdings.setContent(new HoldingOlemlRecordProcessor().toXML(oleHoldings));
                    getDocstoreClientLocator().getDocstoreClient().updateHoldings(holdings);
                }
            } catch (Exception e) {
                LOG.error("Illegal exception while updating instance record" + e.getMessage());
            }
            selectedLineIndex = Integer.parseInt(selectedLine);
            OLEEResourceEventLog oleEResourceEventLog = new OLEEResourceEventLog();
            oleEResourceEventLog.setCurrentTimeStamp();
            oleEResourceEventLog.setEventUser(GlobalVariables.getUserSession().getPrincipalName());
            oleEResourceEventLog.setEventNote(oleERSInstance.getInstanceTitle() + OLEConstants.OLEEResourceRecord.INSTANCE_ID_REMOVE_NOTE + oleERSInstance.getInstanceId() + OLEConstants.OLEEResourceRecord.REMOVE_NOTE);
            oleEResourceEventLog.setEventType(OLEConstants.OLEEResourceRecord.SYSTEM);
            oleEResourceRecordDocument.getOleERSEventLogs().add(oleEResourceEventLog);
        }
        if (selectedLineIndex == -1) {
            throw new RuntimeException(OLEConstants.OLEEResourceRecord.BLANK_SELECTED_INDEX);
        }
        oleEResourceRecordForm.setRemoveInstanceFlag(true);
        View view = oleEResourceRecordForm.getPostedView();
        view.getViewHelperService().processCollectionDeleteLine(view, oleEResourceRecordForm, selectedCollectionPath,
                selectedLineIndex);
        return getUIFModelAndView(oleEResourceRecordForm);
    }


    /**
     * This method returns the object of OleLicesneRequestService
     *
     * @return oleLicenseRequestService
     */
    public OleLicenseRequestWebService getOleLicenseRequestService() {
        if (oleLicenseRequestWebService == null) {
            oleLicenseRequestWebService = GlobalResourceLoader.getService(OLEConstants.OleLicenseRequest.HELPER_SERVICE);
        }
        return oleLicenseRequestWebService;
    }

    public KualiRuleService getKualiRuleService() {
        if (kualiRuleService == null) {
            kualiRuleService = GlobalResourceLoader.getService(OLEConstants.KUALI_RULE_SERVICE);
        }
        return kualiRuleService;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public OLEEResourceSearchService getOleEResourceSearchService() {
        if (oleEResourceSearchService == null) {
            oleEResourceSearchService = GlobalResourceLoader.getService(OLEConstants.OLEEResourceRecord.ERESOURSE_SEARCH_SERVICE);
        }
        return oleEResourceSearchService;
    }


    /**
     * Edit Default Coverage date for E-Instance
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=editDefaultCoverage")
    public ModelAndView editDefaultCoverage(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        oleERSform.setCoverageFlag(true);
        oleERSform.setPerpetualAccessFlag(false);
        oleERSform.setDefaultDatesFlag(true);
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        String defaultCov = oleeResourceRecordDocument.getDummyDefaultCoverage();
        if(defaultCov != null && !defaultCov.isEmpty()) {
            oleeResourceRecordDocument.setCovEdited(true);
            getOleEResourceSearchService().getDefaultCovDatesToPopup(oleeResourceRecordDocument,defaultCov);
        }
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    /**
     * Edit Default Perpetual date for E-Instance
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=editDefaultPerpetualAccess")
    public ModelAndView editDefaultPerpetualAccess(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                                   HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        oleERSform.setCoverageFlag(false);
        oleERSform.setPerpetualAccessFlag(true);
        oleERSform.setDefaultDatesFlag(true);
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        String defaultPerAcc = oleeResourceRecordDocument.getDummyDefaultPerpetualAccess();
        if(defaultPerAcc != null && !defaultPerAcc.isEmpty()) {
            oleeResourceRecordDocument.setPerAccEdited(true);
            getOleEResourceSearchService().getDefaultPerAccDatesToPopup(oleeResourceRecordDocument,defaultPerAcc);
        }
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    /**
     * close the popup in instance tab
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=closeCoverageOrPerpetualAccessDate")
    public ModelAndView closeCoverageOrPerpetualAccessDate(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        oleERSform.setDefaultDatesFlag(false);
        oleERSform.setDefaultCovStartDateErrorMessage(null);
        oleERSform.setDefaultCovEndDateErrorMessage(null);
        oleERSform.setDefaultPerAccStartDateErrorMessage(null);
        oleERSform.setDefaultPerAccEndDateErrorMessage(null);
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    /**
     * refresh the instance tab
     *
     * @param form - document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @RequestMapping(params = "methodToCall=refreshDefaultDate")
    public ModelAndView refreshDefaultDate(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm oleERSform = (OLEEResourceRecordForm) form;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) oleERSform.getDocument();
        if(oleERSform.isCoverageFlag()) {
            getOleEResourceSearchService().getDefaultCovergeDate(oleeResourceRecordDocument);
            oleERSform.setCoverageFlag(false);
        }
        if(oleERSform.isPerpetualAccessFlag()) {
            getOleEResourceSearchService().getDefaultPerpetualAccessDate(oleeResourceRecordDocument);
            oleERSform.setPerpetualAccessFlag(false);
        }
        return getUIFModelAndView(oleERSform, OLEConstants.OLEEResourceRecord.E_RES_INSTANCE_TAB);
    }

    @RequestMapping(params = "methodToCall=addMaterialType")
    public ModelAndView addMaterialType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEMaterialTypeList> oleMaterialTypeLists=oleeResourceRecordDocument.getOleMaterialTypes();
        oleeResourceRecordDocument.getOleMaterialTypes().add(index, new OLEMaterialTypeList());
        oleeResourceRecordDocument.setOleMaterialTypes(oleMaterialTypeLists);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeMaterialType")
    public ModelAndView removeMaterialType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEMaterialTypeList> oleMaterialTypeLists=oleeResourceRecordDocument.getOleMaterialTypes();
        if (oleMaterialTypeLists.size() > 1) {
            oleMaterialTypeLists.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addFormatType")
    public ModelAndView addFormatType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEFormatTypeList> oleFormatTypeLists=oleeResourceRecordDocument.getOleFormatTypes();
        oleeResourceRecordDocument.getOleFormatTypes().add(index, new OLEFormatTypeList());
        oleeResourceRecordDocument.setOleFormatTypes(oleFormatTypeLists);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeFormatType")
    public ModelAndView removeFormatType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEFormatTypeList> oleFormatTypeLists=oleeResourceRecordDocument.getOleFormatTypes();
        if (oleFormatTypeLists.size() > 1) {
            oleFormatTypeLists.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addContentType")
    public ModelAndView addContentType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEContentTypes> oleContentTypes=oleeResourceRecordDocument.getOleContentTypes();
        oleeResourceRecordDocument.getOleContentTypes().add(index, new OLEContentTypes());
        oleeResourceRecordDocument.setOleContentTypes(oleContentTypes);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeContentType")
    public ModelAndView removeContentType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEContentTypes> oleContentTypes=oleeResourceRecordDocument.getOleContentTypes();
        if (oleContentTypes.size() > 1) {
            oleContentTypes.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }


    @RequestMapping(params = "methodToCall=addSelectorType")
    public ModelAndView addSelectorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEEResourceSelector> oleeResourceSelectors=oleeResourceRecordDocument.getSelectors();
        oleeResourceRecordDocument.getSelectors().add(index, new OLEEResourceSelector());
        oleeResourceRecordDocument.setSelectors(oleeResourceSelectors);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeSelectorType")
    public ModelAndView removeSelectorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEEResourceSelector> oleeResourceSelectors=oleeResourceRecordDocument.getSelectors();
        if (oleeResourceSelectors.size() > 1) {
            oleeResourceSelectors.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addRequestorType")
    public ModelAndView addRequestorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEEResourceRequestor> oleeResourceRequestors=oleeResourceRecordDocument.getRequestors();
        oleeResourceRecordDocument.getRequestors().add(index, new OLEEResourceRequestor());
        oleeResourceRecordDocument.setRequestors(oleeResourceRequestors);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeRequestorType")
    public ModelAndView removeRequestorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEEResourceRequestor> oleeResourceRequestors=oleeResourceRecordDocument.getRequestors();
        if (oleeResourceRequestors.size() > 1) {
            oleeResourceRequestors.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addRequestorSelectorType")
    public ModelAndView addRequestorSelectorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEEResourceReqSelComments> oleeResourceReqSelCommentses=oleeResourceRecordDocument.getReqSelComments();
        oleeResourceRecordDocument.getReqSelComments().add(index, new OLEEResourceReqSelComments());
        oleeResourceRecordDocument.setReqSelComments(oleeResourceReqSelCommentses);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeRequestorSelectorType")
    public ModelAndView removeRequestorSelectorType(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEEResourceReqSelComments> oleeResourceReqSelCommentses=oleeResourceRecordDocument.getReqSelComments();
        if (oleeResourceReqSelCommentses.size() > 1) {
            oleeResourceReqSelCommentses.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=addNoteTextSection")
    public ModelAndView addNoteTextSection(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                 HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        index++;
        List<OLEEResourceNotes> oleeResourceNoteses=oleeResourceRecordDocument.getEresNotes();
        oleeResourceRecordDocument.getEresNotes().add(index, new OLEEResourceNotes());
        oleeResourceRecordDocument.setEresNotes(oleeResourceNoteses);
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=removeNoteTextSection")
    public ModelAndView removeNoteTextSection(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                    HttpServletRequest request, HttpServletResponse response) {
        OLEEResourceRecordForm form = (OLEEResourceRecordForm) uifForm;
        OLEEResourceRecordDocument oleeResourceRecordDocument = (OLEEResourceRecordDocument) form.getDocument();
        int index = Integer.parseInt(form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX));
        List<OLEEResourceNotes> oleeResourceNoteses=oleeResourceRecordDocument.getEresNotes();
        if (oleeResourceNoteses.size() > 1) {
            oleeResourceNoteses.remove(index);
        }
        form.setDocument(oleeResourceRecordDocument);
        return super.navigate(form, result, request, response);
    }



}