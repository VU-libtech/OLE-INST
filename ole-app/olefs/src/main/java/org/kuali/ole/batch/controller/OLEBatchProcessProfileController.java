package org.kuali.ole.batch.controller;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.batch.bo.*;
import org.kuali.ole.select.OleSelectConstant;
import org.kuali.ole.select.bo.OleGloballyProtectedField;
import org.kuali.ole.select.document.service.OleInvoiceService;
import org.kuali.ole.select.document.service.OleSelectDocumentService;
import org.kuali.ole.service.OleOrderRecordService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.businessobject.OleExchangeRate;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.LookupService;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.controller.MaintenanceDocumentController;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.kuali.rice.krad.web.form.MaintenanceDocumentForm;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: adityas
 * Date: 7/10/13
 * Time: 4:30 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping(value = "/oleBatchProcessProfile")
public class OLEBatchProcessProfileController extends MaintenanceDocumentController {


    BusinessObjectService businessObject = SpringContext.getBean(BusinessObjectService.class);


    private OleSelectDocumentService oleSelectDocumentService;
    private OleOrderRecordService oleOrderRecordService;
    private OleInvoiceService oleInvoiceService;

    public OleSelectDocumentService getOleSelectDocumentService() {
        if (oleSelectDocumentService == null) {
            oleSelectDocumentService = SpringContext.getBean(OleSelectDocumentService.class);
        }
        return oleSelectDocumentService;
    }

    public OleOrderRecordService getOleOrderRecordService() {
        if (oleOrderRecordService == null) {
            oleOrderRecordService = SpringContext.getBean(OleOrderRecordService.class);
        }
        return oleOrderRecordService;
    }

    public OleInvoiceService getOleInvoiceService() {
        if (oleInvoiceService == null) {
            oleInvoiceService = SpringContext.getBean(OleInvoiceService.class);
        }
        return oleInvoiceService;
    }

    private LookupService getLookupService() {
        return KRADServiceLocatorWeb.getLookupService();
    }

    @Override
    protected MaintenanceDocumentForm createInitialForm(HttpServletRequest request) {
        return new MaintenanceDocumentForm();
    }

    /**
     *   This method takes the initial request when click on Batch Process Profile Screen.
     * @param form
     * @param result
     * @param request
     * @param response
     * @return  ModelAndView
    */
    @Override
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Start -- Start Method of OlePatronRecordForm");
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        setupMaintenance(maintenanceForm, request, KRADConstants.MAINTENANCE_NEW_ACTION);
        MaintenanceDocument document = (MaintenanceDocument) maintenanceForm.getDocument();
        OLEBatchProcessProfileBo batchProcessProfileBo = (OLEBatchProcessProfileBo) document.getDocumentDataObject();
        List<OleGloballyProtectedField> oleGloballyProtectedFields = (List<OleGloballyProtectedField>) KRADServiceLocator.getBusinessObjectService().findAll(OleGloballyProtectedField.class);
        if(oleGloballyProtectedFields.size()>0){
            List<OLEBatchGloballyProtectedField> batchGloballyProtectedFields = new ArrayList<OLEBatchGloballyProtectedField>();
            for(OleGloballyProtectedField globallyProtectedField : oleGloballyProtectedFields){
                OLEBatchGloballyProtectedField oleBatchGloballyProtectedField = new OLEBatchGloballyProtectedField();
                oleBatchGloballyProtectedField.setGloballyProtectedFieldId(globallyProtectedField.getOleGloballyProtectedFieldId());
                oleBatchGloballyProtectedField.setTag(globallyProtectedField.getTag());
                oleBatchGloballyProtectedField.setFirstIndicator(globallyProtectedField.getFirstIndicator());
                oleBatchGloballyProtectedField.setSecondIndicator(globallyProtectedField.getSecondIndicator());
                oleBatchGloballyProtectedField.setSubField(globallyProtectedField.getSubField());
                batchGloballyProtectedFields.add(oleBatchGloballyProtectedField);
            }
            batchProcessProfileBo.setOleBatchGloballyProtectedFieldList(batchGloballyProtectedFields);
        }
        /*List<OLEBatchGloballyProtectedField> oleGloballyProtectedFields=batchProcessProfileBo.getOleBatchGloballyProtectedFieldList();

        List<OLEBatchProcessProfileProtectedField> oleBatchProcessProfileProtectedFields=batchProcessProfileBo.getOleBatchProcessProfileProtectedFieldList();
        for(OLEBatchGloballyProtectedField globallyProtectedField:oleGloballyProtectedFields){
                   if(globallyProtectedField.isIgnoreValue()){
                       OLEBatchProcessProfileProtectedField oleBatchGloballyProtectedField = new OLEBatchProcessProfileProtectedField();
                       oleBatchGloballyProtectedField.setFirstIndicator(globallyProtectedField.getFirstIndicator());
                       oleBatchGloballyProtectedField.setSecondIndicator(globallyProtectedField.getSecondIndicator());
                       oleBatchGloballyProtectedField.setTag(globallyProtectedField.getTag());
                       oleBatchGloballyProtectedField.setSubField(globallyProtectedField.getSubField());
                       oleBatchProcessProfileProtectedFields.add(oleBatchGloballyProtectedField);
                   }
        }
        batchProcessProfileBo.setOleBatchGloballyProtectedFieldList(oleGloballyProtectedFields);*/
        return super.start(form, result, request, response);
    }

    @Override
    @RequestMapping(params = "methodToCall=maintenanceEdit")
    public ModelAndView maintenanceEdit(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        setupMaintenance(form, request, KRADConstants.MAINTENANCE_EDIT_ACTION);
        MaintenanceDocument document = (MaintenanceDocument) maintenanceForm.getDocument();
        OLEBatchProcessProfileBo oldOleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getOldMaintainableObject().getDataObject();
        OLEBatchProcessProfileBo newOleBatchProcessProfileBo = (OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        //oldOleBatchProcessProfileBo.setMatchingProfileObj(MatchingProfile.buildMatchProfileObj(oldOleBatchProcessProfileBo.getMatchingProfile()));
        Map<String,String> map=new HashMap<String,String>();
        map.put("batchProcessProfileId",oldOleBatchProcessProfileBo.getBatchProcessProfileId());
        List<OLEBatchGloballyProtectedField> existBatchGloballyProtectedFields = (List<OLEBatchGloballyProtectedField>) KRADServiceLocator.getBusinessObjectService().findMatching(OLEBatchGloballyProtectedField.class,map);
        List<OleGloballyProtectedField> oleGloballyProtectedFields = (List<OleGloballyProtectedField>) KRADServiceLocator.getBusinessObjectService().findAll(OleGloballyProtectedField.class);
        if (oleGloballyProtectedFields.size() > 0) {
            List<OLEBatchGloballyProtectedField> batchGloballyProtectedFields = new ArrayList<OLEBatchGloballyProtectedField>();
            for (OleGloballyProtectedField globallyProtectedField : oleGloballyProtectedFields) {
                OLEBatchGloballyProtectedField oleBatchGloballyProtectedField = new OLEBatchGloballyProtectedField();
                oleBatchGloballyProtectedField.setGloballyProtectedFieldId(globallyProtectedField.getOleGloballyProtectedFieldId());
                oleBatchGloballyProtectedField.setTag(globallyProtectedField.getTag());
                oleBatchGloballyProtectedField.setFirstIndicator(globallyProtectedField.getFirstIndicator());
                oleBatchGloballyProtectedField.setSecondIndicator(globallyProtectedField.getSecondIndicator());
                oleBatchGloballyProtectedField.setSubField(globallyProtectedField.getSubField());
                for (OLEBatchGloballyProtectedField existBatchGloballyProtectedField : existBatchGloballyProtectedFields) {
                    if (existBatchGloballyProtectedField.getGloballyProtectedFieldId().equals(globallyProtectedField.getOleGloballyProtectedFieldId())) {
                        oleBatchGloballyProtectedField.setIgnoreValue(existBatchGloballyProtectedField.isIgnoreValue());
                        oleBatchGloballyProtectedField.setId(existBatchGloballyProtectedField.getId());
                    }
                }
                batchGloballyProtectedFields.add(oleBatchGloballyProtectedField);
            }
            oldOleBatchProcessProfileBo.setOleBatchGloballyProtectedFieldList(batchGloballyProtectedFields);
        }

        for (OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint : oldOleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList()) {
           if(oleBatchProcessProfileMatchPoint.getMatchPointType() != null && oleBatchProcessProfileMatchPoint.getMatchPointType().equalsIgnoreCase("bibliographic")) {
               oldOleBatchProcessProfileBo.getOleBatchProcessProfileBibliographicMatchPointList().add(oleBatchProcessProfileMatchPoint);
           } else if(oleBatchProcessProfileMatchPoint.getMatchPointType() != null && oleBatchProcessProfileMatchPoint.getMatchPointType().equalsIgnoreCase("holdings")) {
               oldOleBatchProcessProfileBo.getOleBatchProcessProfileHoldingMatchPointList().add(oleBatchProcessProfileMatchPoint);
           } else if(oleBatchProcessProfileMatchPoint.getMatchPointType() != null && oleBatchProcessProfileMatchPoint.getMatchPointType().equalsIgnoreCase("item")) {
               oldOleBatchProcessProfileBo.getOleBatchProcessProfileItemMatchPointList().add(oleBatchProcessProfileMatchPoint);
           } else if(oleBatchProcessProfileMatchPoint.getMatchPointType() != null && oleBatchProcessProfileMatchPoint.getMatchPointType().equalsIgnoreCase("eHoldings")) {
               oldOleBatchProcessProfileBo.getOleBatchProcessProfileEholdingMatchPointList().add(oleBatchProcessProfileMatchPoint);
           }
        }
        return super.maintenanceEdit(form, result, request, response);
    }

    @Override
    @RequestMapping(params = "methodToCall=route")
    public ModelAndView route(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        //OLEBatchProcessProfileBo oldOleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getOldMaintainableObject().getDataObject();
        //OLEBatchProcessProfileBo oleBatchProcessProfileBo1 = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        OLEBatchProcessProfileBo newOleBatchProcessProfileBo = (OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        newOleBatchProcessProfileBo.setMatchingProfile(newOleBatchProcessProfileBo.getMatchingProfileObj().toString());
        List<OLEBatchProcessProfileFilterCriteriaBo> filterCriteriaBoList=new ArrayList<OLEBatchProcessProfileFilterCriteriaBo>();
        if(newOleBatchProcessProfileBo.getOleBatchProcessProfileFilterCriteriaList().size()>0){
            for(OLEBatchProcessProfileFilterCriteriaBo oleBatchProcessProfileFilterCriteriaBo:newOleBatchProcessProfileBo.getOleBatchProcessProfileFilterCriteriaList()){
                if((oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName().isEmpty() || oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName()==null) ){
                    oleBatchProcessProfileFilterCriteriaBo.setFilterFieldName(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldNameText());
                }
                filterCriteriaBoList.add(oleBatchProcessProfileFilterCriteriaBo);
            }
            ((OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject()).setOleBatchProcessProfileFilterCriteriaList(filterCriteriaBoList);
            ((MaintenanceDocumentForm) form).setDocument(document);
        }
        if (!newOleBatchProcessProfileBo.getRemoveValueFrom001()){
                newOleBatchProcessProfileBo.setValueToRemove("");
        }

        List<OLEBatchProcessProfileBibStatus> oleBatchProcessProfileBibStatuses=newOleBatchProcessProfileBo.getDeleteBatchProcessProfileBibStatusList();
        KRADServiceLocator.getBusinessObjectService().save(oleBatchProcessProfileBibStatuses);
        for(OLEBatchProcessProfileBibStatus oleBatchProcessProfileBibStatus:oleBatchProcessProfileBibStatuses){
            Map<String,String> map=new HashMap<String,String>();
            map.put("batchProcessBibStatusId",oleBatchProcessProfileBibStatus.getBatchProcessBibStatusId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileBibStatus.class,map);
        }

        List<OLEBatchProcessProfileDeleteField> oleBatchProcessProfileDeleteFields=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileDeleteFieldsList();
        KRADServiceLocator.getBusinessObjectService().save(oleBatchProcessProfileDeleteFields);
        for(OLEBatchProcessProfileDeleteField oleBatchProcessProfileDeleteField:oleBatchProcessProfileDeleteFields){
            Map<String,String> map=new HashMap<String,String>();
            map.put("id",oleBatchProcessProfileDeleteField.getId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileDeleteField.class,map);
        }

        List<OLEBatchProcessProfileRenameField> oleBatchProcessProfileRenameFields=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileRenameFieldsList();
        for(OLEBatchProcessProfileRenameField oleBatchProcessProfileRenameField:oleBatchProcessProfileRenameFields){
            Map<String,String> map=new HashMap<String,String>();
            map.put("id",oleBatchProcessProfileRenameField.getId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileRenameField.class,map);
        }
        List<OLEBatchProcessProfileFilterCriteriaBo> oleBatchProcessProfileFilterCriteriaBos=newOleBatchProcessProfileBo.getDeleteBatchProcessProfileFilterCriteriaList();
        for(OLEBatchProcessProfileFilterCriteriaBo oleBatchProcessProfileFilterCriteriaBo:oleBatchProcessProfileFilterCriteriaBos){
            Map<String,String> map=new HashMap<String,String>();
            map.put("filterId",oleBatchProcessProfileFilterCriteriaBo.getFilterId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileFilterCriteriaBo.class,map);
        }
        List<OLEBatchProcessProfileDataMappingOptionsBo> oleBatchProcessProfileDataMappingOptionsBos=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileDataMappingOptionsList();
        for(OLEBatchProcessProfileDataMappingOptionsBo oleBatchProcessProfileDataMappingOptionsBo:oleBatchProcessProfileDataMappingOptionsBos){
            Map<String,String> map=new HashMap<String,String>();
            map.put("oleBatchProcessProfileDataMappingOptionId",oleBatchProcessProfileDataMappingOptionsBo.getOleBatchProcessProfileDataMappingOptionId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileDataMappingOptionsBo.class,map);
        }
        List<OLEBatchProcessProfileMappingOptionsBo> oleBatchProcessProfileMappingOptionsBos=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileMappingOptionsList();
        for(OLEBatchProcessProfileMappingOptionsBo oleBatchProcessProfileMappingOptionsBo:oleBatchProcessProfileMappingOptionsBos){
            Map<String,String> map=new HashMap<String,String>();
            map.put("oleBatchProcessDataMapId",oleBatchProcessProfileMappingOptionsBo.getOleBatchProcessDataMapId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileMappingOptionsBo.class,map);
        }
        List<OLEBatchProcessProfileConstantsBo> oleBatchProcessProfileConstantsBos=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileConstantsList();
        for(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo:oleBatchProcessProfileConstantsBos){
            Map<String,String> map=new HashMap<String,String>();
            map.put("oleBatchProcessProfileConstantsId",oleBatchProcessProfileConstantsBo.getOleBatchProcessProfileConstantsId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileConstantsBo.class,map);
        }
        List<OLEBatchProcessProfileProtectedField> oleBatchProcessProfileProtectedFields=newOleBatchProcessProfileBo.getDeletedBatchProcessProfileProtectedFieldList();
        for(OLEBatchProcessProfileProtectedField oleBatchProcessProfileProtectedField:oleBatchProcessProfileProtectedFields){
            Map<String,String> map=new HashMap<String,String>();
            map.put("oleProfileProtectedFieldId",oleBatchProcessProfileProtectedField.getOleProfileProtectedFieldId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchProcessProfileProtectedField.class,map);
        }
        List<OLEBatchProcessProfileMatchPoint> oleBatchProcessProfileMatchPointList = newOleBatchProcessProfileBo.getDeletedBatchProcessProfileMatchPointList();
        newOleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList().removeAll(oleBatchProcessProfileMatchPointList);
        for (OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint : oleBatchProcessProfileMatchPointList) {
            KRADServiceLocator.getBusinessObjectService().delete(oleBatchProcessProfileMatchPoint);
        }
        List<OLEBatchGloballyProtectedField> batchGloballyProtectedFields = new ArrayList<OLEBatchGloballyProtectedField>();
        batchGloballyProtectedFields.addAll(newOleBatchProcessProfileBo.getOleBatchGloballyProtectedFieldList());

        if (StringUtils.isNotBlank(newOleBatchProcessProfileBo.getBatchProcessProfileId())) {
            for (OLEBatchGloballyProtectedField globallyProtectedField : batchGloballyProtectedFields) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("batchProcessProfileId", newOleBatchProcessProfileBo.getBatchProcessProfileId());
                KRADServiceLocator.getBusinessObjectService().deleteMatching(OLEBatchGloballyProtectedField.class, map);
            }

            List<OLEBatchGloballyProtectedField> batchGloballyProtectedFieldList = new ArrayList<OLEBatchGloballyProtectedField>();
            for (OLEBatchGloballyProtectedField globallyProtectedField : batchGloballyProtectedFields) {
                //if (globallyProtectedField.isIgnoreValue()) {
                    globallyProtectedField.setBatchProcessProfileId(newOleBatchProcessProfileBo.getBatchProcessProfileId());
                    batchGloballyProtectedFieldList.add(globallyProtectedField);
                //}
            }
            newOleBatchProcessProfileBo.setOleBatchGloballyProtectedFieldList(batchGloballyProtectedFieldList);
            KRADServiceLocator.getBusinessObjectService().linkAndSave(batchGloballyProtectedFieldList);
        } else {

            List<OLEBatchGloballyProtectedField> batchGloballyProtectedFieldList = new ArrayList<OLEBatchGloballyProtectedField>();
            for (OLEBatchGloballyProtectedField globallyProtectedField : batchGloballyProtectedFields) {
                //if (globallyProtectedField.isIgnoreValue()) {
                    //globallyProtectedField.setBatchProcessProfileId(newOleBatchProcessProfileBo.getBatchProcessProfileId());
                    batchGloballyProtectedFieldList.add(globallyProtectedField);
                //}
            }
            newOleBatchProcessProfileBo.setOleBatchGloballyProtectedFieldList(batchGloballyProtectedFieldList);
        }
        return super.route(form, result, request, response);
    }

    @Override
    @RequestMapping(params = "methodToCall=blanketApprove")
    public ModelAndView blanketApprove(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        route(form, result, request, response);
        return super.blanketApprove(form, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addBibMatch")
    public ModelAndView addBibMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();

            String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
            CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
            String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
            Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint = (OLEBatchProcessProfileMatchPoint) eventObject;
            if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)){
                if(validateBibMatchPoint(oleBatchProcessProfileMatchPoint.getMatchPoint())==Boolean.FALSE){
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_BIB_MATCH_POINT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_MATCH_POINT_ERR);
                    return getUIFModelAndView(form);
                }
            }
            View view = form.getPostedView();
            view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
            oleBatchProcessProfileBo.getOleBatchProcessProfileBibliographicMatchPointList().remove(oleBatchProcessProfileMatchPoint);
            int lastIndex=oleBatchProcessProfileBo.getOleBatchProcessProfileBibliographicMatchPointList().size();
            oleBatchProcessProfileBo.getOleBatchProcessProfileBibliographicMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);
            oleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);


        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addHoldingMatch")
    public ModelAndView addHoldingMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();

        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint = (OLEBatchProcessProfileMatchPoint) eventObject;
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)){
//            if(validateBibMatchPoint(oleBatchProcessProfileMatchPoint.getMatchPoint())==Boolean.FALSE){
//                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_BIB_MATCH_POINT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_MATCH_POINT_ERR);
//                return getUIFModelAndView(form);
//            }
        }
        View view = form.getPostedView();
        view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
        oleBatchProcessProfileBo.getOleBatchProcessProfileHoldingMatchPointList().remove(oleBatchProcessProfileMatchPoint);
        int lastIndex=oleBatchProcessProfileBo.getOleBatchProcessProfileHoldingMatchPointList().size();
        oleBatchProcessProfileBo.getOleBatchProcessProfileHoldingMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);
        oleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);


        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addItemMatch")
    public ModelAndView addItemMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();

        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint = (OLEBatchProcessProfileMatchPoint) eventObject;
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)){
//            if(validateBibMatchPoint(oleBatchProcessProfileMatchPoint.getMatchPoint())==Boolean.FALSE){
//                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_BIB_MATCH_POINT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_MATCH_POINT_ERR);
//                return getUIFModelAndView(form);
//            }
            View view = form.getPostedView();
            view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
            oleBatchProcessProfileBo.getOleBatchProcessProfileItemMatchPointList().remove(oleBatchProcessProfileMatchPoint);
            int lastIndex=oleBatchProcessProfileBo.getOleBatchProcessProfileItemMatchPointList().size();
            oleBatchProcessProfileBo.getOleBatchProcessProfileItemMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);
            oleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);
        }



        return getUIFModelAndView(form);
    }



    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addEholdingMatch")
    public ModelAndView addEholdingMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();

        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileMatchPoint oleBatchProcessProfileMatchPoint = (OLEBatchProcessProfileMatchPoint) eventObject;
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)){
//            if(validateBibMatchPoint(oleBatchProcessProfileMatchPoint.getMatchPoint())==Boolean.FALSE){
//                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_BIB_MATCH_POINT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_MATCH_POINT_ERR);
//                return getUIFModelAndView(form);
//            }
        }
        View view = form.getPostedView();
        view.getViewHelperService().processCollectionAddLine(view, form, selectedCollectionPath);
        oleBatchProcessProfileBo.getOleBatchProcessProfileEholdingMatchPointList().remove(oleBatchProcessProfileMatchPoint);
        int lastIndex=oleBatchProcessProfileBo.getOleBatchProcessProfileEholdingMatchPointList().size();
        oleBatchProcessProfileBo.getOleBatchProcessProfileEholdingMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);
        oleBatchProcessProfileBo.getOleBatchProcessProfileMatchPointList().add(lastIndex,oleBatchProcessProfileMatchPoint);


        return getUIFModelAndView(form);
    }


    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addFilterCriteriaMapping")
    public ModelAndView addFilterCriteriaMapping(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                 HttpServletRequest request, HttpServletResponse response) {

        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileFilterCriteriaBo oleBatchProcessProfileFilterCriteriaBo = (OLEBatchProcessProfileFilterCriteriaBo) eventObject;
        boolean matchPointFlag1 = true;
        if ((oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_EXPORT)) && oleBatchProcessProfileFilterCriteriaBo != null && oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName()!=null && oleBatchProcessProfileFilterCriteriaBo.getFilterFieldNameText()!=null) {
            if(!StringUtils.isEmpty(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName()) && !StringUtils.isEmpty(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldNameText()) && !StringUtils.isEmpty(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldValue())){
                matchPointFlag1 = validateFilterCriteriaFieldValue(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName());
                if (!matchPointFlag1) {
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_FILTER_CRITERIA_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_FILTER_CRITERIA_FIELD_ERR);
                    return getUIFModelAndView(form);
                }
            } else if(!StringUtils.isEmpty(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldName()) && StringUtils.isEmpty(oleBatchProcessProfileFilterCriteriaBo.getFilterFieldNameText())){
                return super.addLine(uifForm, result, request, response);
            } else {
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_FILTER_CRITERIA_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_FILTER_CRITERIA_FIELD_ERR);
                return getUIFModelAndView(form);
            }
        } else {
            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_FILTER_CRITERIA_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_FILTER_CRITERIA_FIELD_ERR);
            return getUIFModelAndView(form);
        }
        return super.addLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addLineDataMapping")
    public ModelAndView addLineDataMapping(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {

        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileDataMappingOptionsBo oleBatchProcessProfileDataMappingOptionsBo = (OLEBatchProcessProfileDataMappingOptionsBo) eventObject;

        //validation for data mapping fields
        if (!validateRequiredForMapping(oleBatchProcessProfileDataMappingOptionsBo, oleBatchProcessProfileBo, form)) {
            return getUIFModelAndView(form);
        }
        boolean matchPointFlag1 = true;
        boolean matchPointFlag2 = true;
        if ((oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT) || oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_EXPORT) || oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.ORDER_RECORD_IMPORT)) && oleBatchProcessProfileDataMappingOptionsBo != null) {
            if (oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT) || oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_EXPORT)){
                if (oleBatchProcessProfileDataMappingOptionsBo.getDataType().equals(OLEConstants.OLEBatchProcess.BATCH_PROCESS_PROFILE_DATATYPE_BIBMARC)) {
                    matchPointFlag1 = validateDataMappingFieldValue(oleBatchProcessProfileDataMappingOptionsBo.getSourceFieldText());
                }
                if (oleBatchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField().equals(OLEConstants.OLEBatchProcess.BATCH_PROCESS_PROFILE_DATATYPE_BIBMARC)) {
                    matchPointFlag2 = validateDataMappingFieldValue(oleBatchProcessProfileDataMappingOptionsBo.getDestinationFieldText());
                }
                if (!matchPointFlag1 || !matchPointFlag2) {
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_DATA_MAPPING_FIELD_ERR);
                    return getUIFModelAndView(form);
                }
            }

            boolean dupTagFlag = false;
            List<OLEBatchProcessProfileMappingOptionsBo> oleBatchProcessProfileMappingOptionsList = oleBatchProcessProfileBo.getOleBatchProcessProfileMappingOptionsList();
            for (OLEBatchProcessProfileMappingOptionsBo oleBatchProcessProfileMappingOptionsBo : oleBatchProcessProfileMappingOptionsList) {
                List<OLEBatchProcessProfileDataMappingOptionsBo> oleBatchProcessProfileDataMappingOptionsBoList = oleBatchProcessProfileMappingOptionsBo.getOleBatchProcessProfileDataMappingOptionsBoList();
                for (OLEBatchProcessProfileDataMappingOptionsBo batchProcessProfileDataMappingOptionsBo : oleBatchProcessProfileDataMappingOptionsBoList) {
                    if (oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)) {
                        oleBatchProcessProfileDataMappingOptionsBo.setSourceField(oleBatchProcessProfileDataMappingOptionsBo.getSourceFieldText());
                        if (oleBatchProcessProfileDataMappingOptionsBo.getDataType().equals(OLEConstants.OLEBatchProcess.BATCH_PROCESS_PROFILE_DATATYPE_BIBMARC)) {
                            if (batchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField().equals(oleBatchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField())) {
                                if (batchProcessProfileDataMappingOptionsBo.getDestinationField().equals(oleBatchProcessProfileDataMappingOptionsBo.getDestinationField())
                                        && batchProcessProfileDataMappingOptionsBo.getPriority() == oleBatchProcessProfileDataMappingOptionsBo.getPriority()) {
                                    dupTagFlag = true;
                                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_DATA_MAPPING_IMPORT_DESTINATION_FIELD_ERR, oleBatchProcessProfileDataMappingOptionsBo.getDestinationField(), oleBatchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField(), batchProcessProfileDataMappingOptionsBo.getSourceField());
                                    break;
                                }
                            }
                        }
                    } else if (oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_EXPORT)) {
                        oleBatchProcessProfileDataMappingOptionsBo.setDestinationField(oleBatchProcessProfileDataMappingOptionsBo.getDestinationFieldText());
                        if (!convertEnterDataField(batchProcessProfileDataMappingOptionsBo.getDestinationField()).equals(convertEnterDataField(oleBatchProcessProfileDataMappingOptionsBo.getDestinationField()))) {
                            if (oleBatchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField().equals(OLEConstants.OLEBatchProcess.BATCH_PROCESS_PROFILE_DATATYPE_BIBMARC)) {
                                if (batchProcessProfileDataMappingOptionsBo.getSourceField().equals(oleBatchProcessProfileDataMappingOptionsBo.getSourceField())) {
                                    if (batchProcessProfileDataMappingOptionsBo.getDataType().equals(oleBatchProcessProfileDataMappingOptionsBo.getDataType())) {
                                        dupTagFlag = true;
                                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_DATA_MAPPING_EXPORT_SOURCE_FIELD_ERR, oleBatchProcessProfileDataMappingOptionsBo.getSourceField(), oleBatchProcessProfileDataMappingOptionsBo.getDataType(), batchProcessProfileDataMappingOptionsBo.getDestinationField());
                                        break;
                                    }
                                }
                            }
                        } else {
                            dupTagFlag = true;
                            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_BIB_DATA_MAPPING_EXPORT_DESTINATION_FIELD_ERR, oleBatchProcessProfileDataMappingOptionsBo.getDestinationField(), oleBatchProcessProfileDataMappingOptionsBo.getDataTypeDestinationField(), batchProcessProfileDataMappingOptionsBo.getDataType(), batchProcessProfileDataMappingOptionsBo.getSourceField());
                            break;
                        }
                    } else if (oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.ORDER_RECORD_IMPORT)) {
                        if (batchProcessProfileDataMappingOptionsBo.getDestinationField().equals(oleBatchProcessProfileDataMappingOptionsBo.getDestinationField())
                                && batchProcessProfileDataMappingOptionsBo.getPriority() == oleBatchProcessProfileDataMappingOptionsBo.getPriority()) {
                            dupTagFlag = true;
                            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_PRIORITY);
                            break;
                        }
                    }
                }
            }

            if (dupTagFlag) {
                return getUIFModelAndView(form);
            }
        }
        return super.addLine(uifForm, result, request, response);
    }


    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addLineForProfileConstants")
    public ModelAndView addLineForProfileConstants(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {

        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo = (OLEBatchProcessProfileConstantsBo ) eventObject;
        if (oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT)) {
            setAttributeValueForBibImport(oleBatchProcessProfileConstantsBo);
        }
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.ORDER_RECORD_IMPORT)){
            oleBatchProcessProfileConstantsBo.setOldAttributeName(null);
            setAttributeValueForOrderImport(oleBatchProcessProfileConstantsBo);
            boolean isRequired = validateRequiredFieldsForProfileConstants(oleBatchProcessProfileConstantsBo);
            if(!isRequired){
                return getUIFModelAndView(form);
            }
            boolean isDupFound = validateDuplicateValuesForAttributeName(oleBatchProcessProfileBo.getOleBatchProcessProfileConstantsList(),oleBatchProcessProfileConstantsBo);
            if(isDupFound){
                return getUIFModelAndView(form);
            }
            boolean invalidFieldValue = validateFieldValuesForConstantsAndDefaultForOrderImport(oleBatchProcessProfileConstantsBo);
            if(invalidFieldValue){
                return getUIFModelAndView(form);
            }
        }
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.INVOICE_RECORD_IMPORT)){
            oleBatchProcessProfileConstantsBo.setOldAttributeName(null);
            setAttributeValueForInvoiceImport(oleBatchProcessProfileConstantsBo);
            boolean isRequired = validateRequiredFieldsForProfileConstants(oleBatchProcessProfileConstantsBo);
            if(!isRequired){
                return getUIFModelAndView(form);
            }
            boolean isDupFound = validateDuplicateValuesForAttributeName(oleBatchProcessProfileBo.getOleBatchProcessProfileConstantsList(),oleBatchProcessProfileConstantsBo);
            if(isDupFound){
                return getUIFModelAndView(form);
            }
            boolean invalidFieldValue = validateFieldValuesForConstantsAndDefaultForInvoiceImport(oleBatchProcessProfileConstantsBo);
            if(invalidFieldValue){
                return getUIFModelAndView(form);
            }
        }
        return super.addLine(uifForm, result, request, response);
    }

    private boolean validateDuplicateValuesForAttributeName(List<OLEBatchProcessProfileConstantsBo> oleBatchProcessProfileConstantsBoList,OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo){
        for(int constantList = 0;constantList < oleBatchProcessProfileConstantsBoList.size();constantList++){
            if(oleBatchProcessProfileConstantsBoList.get(constantList).getAttributeName().equalsIgnoreCase(oleBatchProcessProfileConstantsBo.getAttributeName())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_DUPLICATE ,"This field name" );
                return true;
            }
        }
        return false;
    }

    private boolean validateRequiredFieldsForProfileConstants(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo) {
        boolean valid = true;
            if (StringUtils.isBlank(oleBatchProcessProfileConstantsBo.getDataType())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(oleBatchProcessProfileConstantsBo.getAttributeName())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Field Name");
                valid = false;
            }
            if(StringUtils.isBlank(oleBatchProcessProfileConstantsBo.getAttributeValue())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Field Value");
                valid = false;
            }
        return  valid;
    }

    private boolean validateFieldValuesForConstantsAndDefaultForOrderImport(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo) {
        String attributeName = oleBatchProcessProfileConstantsBo.getAttributeName();
        if(OLEConstants.OLEBatchProcess.PERCENT.equals(attributeName)){
            String percent = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateForPercentage(percent)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_PERCENT);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.LIST_PRICE.equals(attributeName)){
            String listPrice = oleBatchProcessProfileConstantsBo.getAttributeValueText();
            if(!getOleOrderRecordService().validateDestinationFieldValues(listPrice)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_LIST_PRICE);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.DISCOUNT.equals(attributeName)){
            String discount = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateDestinationFieldValues(discount)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_DISCOUNT);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.QUANTITY.equals(attributeName)){
            String quantity = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateForNumber(quantity)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_QUANTITY);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.ITEM_NO_OF_PARTS.equals(attributeName)){
            String itemNoOfParts = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateForNumber(itemNoOfParts)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_NO_OF_PARTS);
                return true;
            }
        }
        return false;
    }

    private void setAttributeValueForOrderImport(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo){
        String attributeName = oleBatchProcessProfileConstantsBo.getAttributeName();
        if(attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.PERCENT) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.LIST_PRICE) ||
                attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DISCOUNT) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.QUANTITY) || OLEConstants.OLEBatchProcess.ITEM_NO_OF_PARTS.equals(attributeName)){
            oleBatchProcessProfileConstantsBo.setAttributeValue(oleBatchProcessProfileConstantsBo.getAttributeValueText());
        }
    }
    private void setAttributeValueForInvoiceImport(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo){
        String attributeName = oleBatchProcessProfileConstantsBo.getAttributeName();
        if(attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.LIST_PRICE) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.INVOICE_NUMBER) ||
                attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.QUANTITY) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.VENDOR_INVOICE_AMOUNT) ||
                attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.INVOICE_DATE) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.EXCHANGE_RATE) || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.FOREIGN_LIST_PRICE)){
            oleBatchProcessProfileConstantsBo.setAttributeValue(oleBatchProcessProfileConstantsBo.getAttributeValueText());
        }
    }

    private void setAttributeValueForBibImport(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo) {
        String attributeName = oleBatchProcessProfileConstantsBo.getAttributeName();
        if (attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_CALL_NUMBER)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_CALL_NUMBER_TYPE_PREFIX)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COPY_NUMBER)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_ITEM_HOLDINGS_CALL_NUMBER)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_ITEM_HOLDINGS_CALL_NUMBER_PREFIX)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_ITEM_HOLDINGS_COPY_NUMBER)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.SOURCE_FIELD_DATE_CREATED)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_ITEM_ITEM_BARCODE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_LINK_URL)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_PERSISTENTLINK)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_LINK_TEXT)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_PUBLIC_DISPLAY_NOTE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_START_DATE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_START_ISSUE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_START_VOLUME)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_END_DATE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_END_ISSUE)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_COVERAGE_END_VOLUME)
                || attributeName.equalsIgnoreCase(OLEConstants.OLEBatchProcess.DESTINATION_FIELD_PLATFORM)) {
            oleBatchProcessProfileConstantsBo.setAttributeValue(oleBatchProcessProfileConstantsBo.getAttributeValueText());
        }
    }

    private boolean validateRequiredForMapping(OLEBatchProcessProfileDataMappingOptionsBo dataMappingOptionsBo, OLEBatchProcessProfileBo profileBo , MaintenanceDocumentForm form) {
        boolean valid = true;
        if ((profileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_BIB_IMPORT))) {
            if (StringUtils.isBlank(dataMappingOptionsBo.getDataType())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getSourceFieldText())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Source Field");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDataTypeDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Field");
                valid = false;
            }
        }
        if ((profileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.BATCH_EXPORT))) {
            if(StringUtils.isBlank(dataMappingOptionsBo.getDataType())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getSourceField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Source Field");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDataTypeDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Field");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDestinationFieldText())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Field Value");
                valid = false;
            }
        }
        if ((profileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.ORDER_RECORD_IMPORT))) {
            if(StringUtils.isBlank(dataMappingOptionsBo.getSourceField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Source Field");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDataTypeDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Field");
                valid = false;
            }
        }
        if ((profileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.INVOICE_RECORD_IMPORT))) {
            if(StringUtils.isBlank(dataMappingOptionsBo.getSourceField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Source Field");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDataTypeDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Data Type");
                valid = false;
            }
            if(StringUtils.isBlank(dataMappingOptionsBo.getDestinationField())){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_DATA_MAPPING_SECTION_ID, OLEConstants.OLEBatchProcess.OLE_BATCH_REQUIRED , "Destination Field");
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateFieldValuesForConstantsAndDefaultForInvoiceImport(OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo) {

        if(OLEConstants.OLEBatchProcess.QUANTITY.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String quantity = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateForNumber(quantity)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_QUANTITY);
                return true;
            }
        }
        /*else if(OLEConstants.OLEBatchProcess.INVOICE_NUMBER.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String invoiceNumber = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateForNumber(invoiceNumber)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_INVOICE_NUMBER);
                return true;
            }
        }*/
        else if(OLEConstants.OLEBatchProcess.LIST_PRICE.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String listPrice = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateDestinationFieldValues(listPrice)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_INVOICED_PRICE);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.FOREIGN_LIST_PRICE.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String foreignListPrice = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateDestinationFieldValues(foreignListPrice)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_INVOICED_FOREIGN_PRICE);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.VENDOR_INVOICE_AMOUNT.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String vendorInvoiceAmount = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateDestinationFieldValues(vendorInvoiceAmount)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_VENDOR_INVOICE_AMOUNT);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.INVOICE_DATE.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            boolean validInvoiceDate = validateInvoiceDate(oleBatchProcessProfileConstantsBo.getAttributeValue());
            if(!validInvoiceDate){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_INVOICE_DATE);
                return true;
            }
        }
        else if(OLEConstants.OLEBatchProcess.EXCHANGE_RATE.equals(oleBatchProcessProfileConstantsBo.getAttributeName())){
            String exchangeRate = oleBatchProcessProfileConstantsBo.getAttributeValue();
            if(!getOleOrderRecordService().validateDestinationFieldValues(exchangeRate)){
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_EXCHANGE_RATE);
                return true;
            }
            if (exchangeRate.length()>10) {
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEBatchProcess.OLE_BATCH_PROFILE_CONSTANT_SECTION_ID, OLEConstants.OLEBatchProcess.INVALID_EXCHANGE_RATE_NUMBER);
                return true;
            }
        }
        return false;
    }

    private boolean validateInvoiceDate(String invoiceDate){
        SimpleDateFormat dateFromRawFile = new SimpleDateFormat("yyyyMMdd");
        try {
            dateFromRawFile.parse(invoiceDate);
            return true;
        }
        catch (ParseException e) {
            return false;
        }
    }

    private String convertEnterDataField(String tag) {
        if (StringUtils.isNotEmpty(tag)) {
            String[] matchPointArray = tag.split(" ");
            if (matchPointArray.length == 3) {
                tag = matchPointArray[0] + " " + matchPointArray[2];
            }
        }
        return tag;
    }

    /**
     * Validates the match point to match with the pattern 001 ,245 $a, 245 ## $a, 245 04 $a, 245 #4 $a, 245 0# $a
     * @param bibMatchPoint
     * @return
     */
    private boolean validateBibMatchPoint(String bibMatchPoint) {
        boolean valid = true;
        String pattern = "((001)|(^\\d\\d\\d\\s+([$]([a-z]|\\d))$)|(^\\d\\d\\d\\s+[#|\\d][#|\\d]\\s+([$]([a-z]|\\d))$))";
        if (Pattern.matches(pattern, bibMatchPoint) == Boolean.FALSE) {
            valid = false;
        }
        return valid;
    }

    /**
     * Validates the data mapping field value to match with the pattern 001, 245 $a, 245 $a$b, 245 ## $a, 245 04 $a, 245 ## $a$b
     * @param fieldValue
     * @return
     */
    private boolean validateDataMappingFieldValue(String fieldValue) {
        boolean valid = true;
        String pattern = "((001)|(^\\d\\d\\d\\s+([$]([a-z]|\\d))+$)|(^\\d\\d\\d\\s+[#|\\d][#|\\d]\\s+([$]([a-z]|\\d))+$))";
        if (Pattern.matches(pattern, fieldValue) == Boolean.FALSE) {
            valid = false;
        }
        return valid;
    }

    private boolean validateFilterCriteriaFieldValue(String fieldValue) {
        boolean valid = true;

        String pattern = "((^\\d\\d\\d\\s+([$][a-z])+$)|(^\\d\\d\\d\\s+[#|\\d][#|\\d]\\s+([$][a-z])+$))";
        if (Pattern.matches(pattern, fieldValue) == Boolean.FALSE) {
            valid = false;
        }
        return valid;
    }

    @RequestMapping(params = "methodToCall=refreshPageView")
    public ModelAndView refreshPageView(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {

        //OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();

       /* MaintenanceDocumentForm form1 = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form1.getDocument();*/

        return refresh(form,result,request,response);
    }
    @RequestMapping(params = "methodToCall=refreshBeanId")
    public ModelAndView refreshBeanId(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                      HttpServletRequest request, HttpServletResponse response) throws Exception {
        return navigate(form, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteBibStatus")
    public ModelAndView deleteBibStatus(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeleteBatchProcessProfileBibStatusList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileBibStatusList().get(Integer.parseInt(selectedLineIndex)));

        return deleteLine(uifForm, result, request, response);

    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteDeleteField")
    public ModelAndView deleteDeleteField(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileDeleteFieldsList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileDeleteFieldsList().get(Integer.parseInt(selectedLineIndex)));

        return deleteLine(uifForm, result, request, response);

    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteRenameField")
    public ModelAndView deleteRenameField(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileRenameFieldsList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileRenameFieldsList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=filterCriteriaDelete")
    public ModelAndView filterCriteriaDelete(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeleteBatchProcessProfileFilterCriteriaList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileFilterCriteriaList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=dataMappingDelete")
    public ModelAndView dataMappingDelete(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileMappingOptionsList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileMappingOptionsList().get(Integer.parseInt(selectedLineIndex)));
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileDataMappingOptionsList().addAll(oleBatchProcessProfileBo.getOleBatchProcessProfileMappingOptionsList().get(Integer.parseInt(selectedLineIndex)).getOleBatchProcessProfileDataMappingOptionsBoList());
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addLineDataMappingDelete")
    public ModelAndView addLineDataMappingDelete(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                 HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        Map<String,String> actionParameters = form.getActionParameters();
        String subCollectionIndex = actionParameters.get(UifParameters.SELECTED_LINE_INDEX);
        String mainCollectionIndex= StringUtils.substringBefore(StringUtils.substringAfter(actionParameters.get(UifParameters.SELLECTED_COLLECTION_PATH),"["),"]");
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        OLEBatchProcessProfileDataMappingOptionsBo oleBatchProcessProfileDataMappingOptionsBo = oleBatchProcessProfileBo.getOleBatchProcessProfileMappingOptionsList().get(Integer.parseInt(mainCollectionIndex)).getOleBatchProcessProfileDataMappingOptionsBoList().get(Integer.parseInt(subCollectionIndex));
        if(oleBatchProcessProfileBo.getBatchProcessProfileId()!=null){
            //KRADServiceLocator.getBusinessObjectService().delete(oleBatchProcessProfileDataMappingOptionsBo);
            oleBatchProcessProfileBo.getDeletedBatchProcessProfileDataMappingOptionsList().add(oleBatchProcessProfileDataMappingOptionsBo);
            oleBatchProcessProfileBo.getOleBatchProcessProfileMappingOptionsList().get(Integer.parseInt(mainCollectionIndex)).getOleBatchProcessProfileDataMappingOptionsBoList().remove(Integer.parseInt(subCollectionIndex));
            return getUIFModelAndView(uifForm);
        }else{
            oleBatchProcessProfileBo.getDeletedBatchProcessProfileDataMappingOptionsList().add(oleBatchProcessProfileDataMappingOptionsBo);
            return deleteLine(uifForm, result, request, response);
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteProfileConstant")
    public ModelAndView deleteProfileConstant(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                              HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        if(oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.ORDER_RECORD_IMPORT) ||
                oleBatchProcessProfileBo.getBatchProcessProfileType().equalsIgnoreCase(OLEConstants.OLEBatchProcess.INVOICE_RECORD_IMPORT)){
            String selectedCollectionPath = form.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
            CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath(selectedCollectionPath);
            String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
            Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
            OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo = (OLEBatchProcessProfileConstantsBo ) eventObject;
            oleBatchProcessProfileConstantsBo.setOldAttributeName(null);
        }
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileConstantsList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileConstantsList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteProfileProtectedField")
    public ModelAndView deleteProfileProtectedField(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                    HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileProtectedFieldList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileProtectedFieldList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteBibMatch")
    public ModelAndView deleteBibMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized deleteLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileMatchPointList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileBibliographicMatchPointList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteHoldingMatch")
    public ModelAndView deleteHoldingMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized deleteLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileMatchPointList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileHoldingMatchPointList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteItemMatch")
    public ModelAndView deleteItemMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized deleteLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileMatchPointList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileItemMatchPointList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteEholdingMatch")
    public ModelAndView deleteEholdingMatch(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized deleteLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OLEBatchProcessProfileBo oleBatchProcessProfileBo = (OLEBatchProcessProfileBo) document.getNewMaintainableObject().getDataObject();
        oleBatchProcessProfileBo.getDeletedBatchProcessProfileMatchPointList().add(oleBatchProcessProfileBo.getOleBatchProcessProfileEholdingMatchPointList().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(params = "methodToCall=populateExchangeRate")
    public ModelAndView populateExchangeRate(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {

        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        CollectionGroup collectionGroup = form.getPostedView().getViewIndex().getCollectionGroupByPath("document.newMaintainableObject.dataObject.oleBatchProcessProfileConstantsList");
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(form, addLinePath);
        OLEBatchProcessProfileConstantsBo oleBatchProcessProfileConstantsBo = (OLEBatchProcessProfileConstantsBo ) eventObject;
        OLEBatchProcessProfileBo oleBatchProcessProfileBo=(OLEBatchProcessProfileBo)document.getNewMaintainableObject().getDataObject();
        List<OLEBatchProcessProfileConstantsBo> oleBatchProcessProfileConstantsBoList = oleBatchProcessProfileBo.getOleBatchProcessProfileConstantsList();
        for(int recordCount = 0; recordCount < oleBatchProcessProfileConstantsBoList.size();recordCount++) {
            if(oleBatchProcessProfileConstantsBoList.get(recordCount).getAttributeName().equalsIgnoreCase(OLEConstants.OLEBatchProcess.CURRENCY_TYPE)){
                if(!oleBatchProcessProfileConstantsBoList.get(recordCount).getAttributeValue().equalsIgnoreCase(OleSelectConstant.CURRENCY_TYPE_NAME)){
                    String currencyTypeId = getOleInvoiceService().getCurrencyTypeIdFromCurrencyType(oleBatchProcessProfileConstantsBoList.get(recordCount).getAttributeValue());
                    Map exchangeRateMap = new HashMap();
                    exchangeRateMap.put(OLEConstants.OLEBatchProcess.CURRENCY_TYPE_ID,currencyTypeId);
                    List<OleExchangeRate> exchangeRateList = (List) getBusinessObjectService().findMatchingOrderBy(
                            OleExchangeRate.class, exchangeRateMap, OleSelectConstant.EXCHANGE_RATE_DATE, false);
                    oleBatchProcessProfileConstantsBo.setAttributeValueText(exchangeRateList.get(0).getExchangeRate().toString());
                }
                else {
                    oleBatchProcessProfileConstantsBo.setAttributeValueText("0.0");
                }
                break;
            }
            oleBatchProcessProfileConstantsBo.setAttributeValueText("0.0");
        }
        return getUIFModelAndView(form);
    }

}
