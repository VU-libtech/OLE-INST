package org.kuali.ole.deliver.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.DocumentUniqueIDPrefix;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.bo.OleAddressBo;
import org.kuali.ole.deliver.bo.OleDeliverRequestBo;
import org.kuali.ole.deliver.bo.OleEntityAddressBo;
import org.kuali.ole.deliver.bo.OleItemSearch;
import org.kuali.ole.deliver.bo.OleLoanDocument;
import org.kuali.ole.deliver.bo.OlePatronAffiliation;
import org.kuali.ole.deliver.bo.OlePatronDocument;
import org.kuali.ole.deliver.bo.OlePatronLocalIdentificationBo;
import org.kuali.ole.deliver.bo.OlePatronLostBarcode;
import org.kuali.ole.deliver.bo.OleProxyPatronDocument;
import org.kuali.ole.deliver.bo.OleTemporaryCirculationHistory;
import org.kuali.ole.deliver.form.OlePatronMaintenanceDocumentForm;
import org.kuali.ole.deliver.processor.LoanProcessor;
import org.kuali.ole.deliver.service.OleDeliverRequestDocumentHelperServiceImpl;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.ItemOleml;
import org.kuali.ole.docstore.common.document.content.enums.DocType;
import org.kuali.ole.docstore.common.document.content.instance.Item;
import org.kuali.ole.docstore.common.document.content.instance.xstream.ItemOlemlRecordProcessor;
import org.kuali.ole.docstore.common.document.content.instance.MissingPieceItemRecord;
import org.kuali.ole.service.OlePatronHelperService;
import org.kuali.ole.service.OlePatronHelperServiceImpl;
import org.kuali.ole.service.OlePatronMaintenanceDocumentServiceImpl;
import org.kuali.ole.service.OlePatronService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.impl.identity.address.EntityAddressBo;
import org.kuali.rice.kim.impl.identity.affiliation.EntityAffiliationBo;
import org.kuali.rice.kim.impl.identity.email.EntityEmailBo;
import org.kuali.rice.kim.impl.identity.employment.EntityEmploymentBo;
import org.kuali.rice.kim.impl.identity.name.EntityNameBo;
import org.kuali.rice.kim.impl.identity.phone.EntityPhoneBo;
import org.kuali.rice.kim.impl.identity.type.EntityTypeContactInfoBo;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.maintenance.MaintenanceUtils;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.MaintenanceDocumentService;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.util.ProcessLogger;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.web.controller.MaintenanceDocumentController;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.kuali.rice.krad.web.form.MaintenanceDocumentForm;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * .OlePatronMaintenanceDocumentController invokes MaintenanceDocumentController and returns instance of MaintenanceDocumentService.
 */
@Controller
@RequestMapping(value = "/patronMaintenance")
public class OlePatronMaintenanceDocumentController extends MaintenanceDocumentController {

    private static final Logger LOG = Logger.getLogger(OlePatronMaintenanceDocumentController.class);
    private OlePatronService olePatronService;
    private OlePatronHelperService olePatronHelperService = new OlePatronHelperServiceImpl();
    private byte[] imageInByte;
    private OleDeliverRequestDocumentHelperServiceImpl oleDeliverRequestDocumentHelperService;
    private DateTimeService dateTimeService;

    @Override
	protected MaintenanceDocumentForm createInitialForm(
			HttpServletRequest request) {
		return new OlePatronMaintenanceDocumentForm();
	}

	public DateTimeService getDateTimeService() {
        return (DateTimeService)SpringContext.getService("dateTimeService");
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public OlePatronService getOlePatronService() {
        if (olePatronService != null) {
            olePatronService = GlobalResourceLoader.getService(OLEConstants.OlePatron.OLE_PATRON_SERVICE);
        }
        return olePatronService;
    }

    private LoanProcessor loanProcessor;

    private OleDeliverRequestDocumentHelperServiceImpl getOleDeliverRequestDocumentHelperService() {
        if (oleDeliverRequestDocumentHelperService == null) {
            oleDeliverRequestDocumentHelperService = new OleDeliverRequestDocumentHelperServiceImpl();
        }
        return oleDeliverRequestDocumentHelperService;
    }

    private DocstoreClientLocator docstoreClientLocator;

    public DocstoreClientLocator getDocstoreClientLocator() {

        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);

        }
        return docstoreClientLocator;
    }

    /**
     * This method returns the instance of olePatronMaintenanceDocumentService
     *
     * @return olePatronMaintenanceDocumentService(MaintenanceDocumentService)
     */
    @Override
    protected MaintenanceDocumentService getMaintenanceDocumentService() {
        return GlobalResourceLoader.getService(OLEConstants.OlePatron.OLE_PATRON_MAINTENANCE_DOC_SERVICE);
    }


    @Override
    @RequestMapping(params = "methodToCall=maintenanceEdit")
    public ModelAndView maintenanceEdit(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) throws Exception {

        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        setupMaintenance(form, request, KRADConstants.MAINTENANCE_EDIT_ACTION);
        MaintenanceDocument document = (MaintenanceDocument) maintenanceForm.getDocument();
        super.maintenanceEdit(form, result, request, response);
        OlePatronDocument olePatronDocument = (OlePatronDocument) document.getOldMaintainableObject().getDataObject();
        olePatronDocument.getAddresses().clear();
        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OlePatron.PATRON_MAINTENANCE_DATE_FORMAT);
        if (olePatronDocument.getActivationDate() == null)
            olePatronDocument.setActivationDate(getDateTimeService().getCurrentDate());
        OlePatronDocument patronDocument = (OlePatronDocument)document.getNewMaintainableObject().getDataObject();
        patronDocument.setBarcodeEditable(false);
        if((patronDocument.getExpirationDate() != null && patronDocument.getActivationDate() != null && fmt.format(patronDocument.getActivationDate()).compareTo(fmt.format(patronDocument.getExpirationDate())) >= 0) || (patronDocument.getExpirationDate() != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(patronDocument.getExpirationDate())) > 0)){

            patronDocument.setExpirationFlag(false);
        }
        olePatronDocument.setShowLoanedRecords(false);
        olePatronDocument.setShowRequestedItems(false);
        olePatronDocument.setShowTemporaryCirculationHistoryRecords(false);
        return getUIFModelAndView(form);
    }

    @Override
    @RequestMapping(params = "methodToCall=maintenanceCopy")
    public ModelAndView maintenanceCopy(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        setupMaintenance(form, request, KRADConstants.MAINTENANCE_COPY_ACTION);
        super.maintenanceCopy(form, result, request, response);
        MaintenanceDocument document = (MaintenanceDocument) maintenanceForm.getDocument();
        OlePatronDocument patronDocument=(OlePatronDocument)document.getNewMaintainableObject().getDataObject();
        if(patronDocument.getOlePatronLocalIds()!=null && patronDocument.getOlePatronLocalIds().size()>0){
           for(OlePatronLocalIdentificationBo  olePatronLocalIdentificationBo:patronDocument.getOlePatronLocalIds()){
                olePatronLocalIdentificationBo.setPatronLocalSeqId(null);
                olePatronLocalIdentificationBo.setOlePatronId(null);
                olePatronLocalIdentificationBo.setOlePatronDocument(null);
           }
        }
        patronDocument.setShowLoanedRecords(false);
        patronDocument.setShowRequestedItems(false);
        patronDocument.setShowTemporaryCirculationHistoryRecords(false);
        return getUIFModelAndView(form);
    }

    /**
     * Use to create a link delete in the lookup result action field which will use to delete patron record..
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(params = "methodToCall=" + "maintenanceDelete")
    public ModelAndView maintenanceDelete(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug(" Inside maintenanceDelete ");
        setupMaintenanceForDelete(form, request, OLEConstants.OlePatron.OLE_PATRON_DELETE);
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) maintenanceForm.getDocument();
        OlePatronDocument olePatronDocument = (OlePatronDocument) document.getOldMaintainableObject().getDataObject();
        OlePatronDocument patronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        olePatronDocument.setBarcodeEditable(false);
        patronDocument.setBarcodeEditable(false);
        olePatronDocument.setShowLoanedRecords(false);
        olePatronDocument.setShowRequestedItems(false);
        olePatronDocument.setShowTemporaryCirculationHistoryRecords(false);
        return getUIFModelAndView(form);
    }

    /**
     * To delete the whole patron document.
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return Close the document
     * @throws Exception
     */
    @RequestMapping(params = "methodToCall=" + "deleteDocument")
    public ModelAndView deleteDocument(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOG.debug(" Inside deleteDocument ");
        MaintenanceDocument document = form.getDocument();
        OlePatronDocument olePatronDocument = new OlePatronDocument();
        try {
            if (document.getDocumentDataObject() != null) {
                olePatronDocument = (OlePatronDocument) document.getDocumentDataObject();
                if (olePatronDocument != null && olePatronDocument.getOlePatronId() != null) {
                    boolean deletePatronDetail = olePatronHelperService.deletePatron(olePatronDocument);
                    if (deletePatronDetail) {
                        return back(form, result, request, response);
                    } else {
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_MESSAGES, OLEConstants.OlePatron.ERROR_PATRON_HAS_LOAN);
                        return getUIFModelAndView(form);
                    }
                } else {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_MESSAGES, OLEConstants.OlePatron.ERROR_PATRON_NOT_FOUND);
                    return getUIFModelAndView(form);
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while delete document", ex);
        }
        return back(form, result, request, response);
    }


    /**
     * This method populates confirmation to delete the document.
     *
     * @param form
     * @param request
     * @param maintenanceAction
     */
    protected void setupMaintenanceForDelete(MaintenanceDocumentForm form, HttpServletRequest request, String maintenanceAction) {
        LOG.debug(" Inside setupMaintenanceForDelete ");
        MaintenanceDocument document = form.getDocument();
        if (document == null) {
            document = getMaintenanceDocumentService()
                    .setupNewMaintenanceDocument(form.getDataObjectClassName(), form.getDocTypeName(),
                            maintenanceAction);

            form.setDocument(document);
            form.setDocTypeName(document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
        }

        form.setMaintenanceAction(maintenanceAction);
        OlePatronMaintenanceDocumentServiceImpl olePatronMaintenanceDocumentServicec = (OlePatronMaintenanceDocumentServiceImpl) getMaintenanceDocumentService();
        olePatronMaintenanceDocumentServicec.setupMaintenanceObjectForDelete(document, maintenanceAction, request.getParameterMap());
        MaintenanceUtils.checkForLockingDocument(document, false);
    }


    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=searchAddLine")
    public ModelAndView searchAddLine(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                      HttpServletRequest request, HttpServletResponse response) {
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument) maintenanceForm.getDocument();
        OlePatronDocument olePatronDocument = (OlePatronDocument) maintenanceDocument.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = maintenanceForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = maintenanceForm.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(maintenanceForm, addLinePath);
        OleProxyPatronDocument oleProxyPatronDocument = (OleProxyPatronDocument) eventObject;
        Map<String, String> proxyMap = new HashMap<String, String>();
        proxyMap.put(OLEConstants.OlePatron.BARCODE, oleProxyPatronDocument.getProxyPatronBarcode());
        OlePatronDocument tempDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, proxyMap);
        if (tempDocument != null) {
            oleProxyPatronDocument.setProxyPatronId(tempDocument.getOlePatronId());
            oleProxyPatronDocument.setProxyPatronBarcode(tempDocument.getBarcode());
            oleProxyPatronDocument.setProxyPatronFirstName(tempDocument.getEntity().getNames().get(0).getFirstName());
            oleProxyPatronDocument.setProxyPatronLastName(tempDocument.getEntity().getNames().get(0).getLastName());
        } else {
            GlobalVariables.getMessageMap().putError(OLEConstants.OleDeliverRequest.BORROWER_ID, OLEConstants.OleDeliverRequest.INVALID_PATRON);
        }
        ModelAndView modelAndView = super.addLine(form, result, request, response);
        return modelAndView;
    }




    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addPhoneNumber")
    public ModelAndView addPhoneNumber(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                   HttpServletRequest request, HttpServletResponse response) {
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument) maintenanceForm.getDocument();
        OlePatronDocument olePatronDocument = (OlePatronDocument) maintenanceDocument.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = maintenanceForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = maintenanceForm.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(maintenanceForm, addLinePath);
        EntityPhoneBo entityPhoneBo = (EntityPhoneBo) eventObject;
        boolean isValidPhoneNumber=false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating the Phone Number  Format - ##########, (###)###-#### , ###-###-#### , ### ###-#### , ### ### ####");
        }
        if (entityPhoneBo.getPhoneNumber().matches("\\d{10}")) isValidPhoneNumber=true;
        else if (entityPhoneBo.getPhoneNumber().matches("\\d{3}[-]\\d{3}[-]\\d{4}")) isValidPhoneNumber=true;
        else if (entityPhoneBo.getPhoneNumber().matches("\\d{3}[\\s]\\d{3}[-]\\d{4}")) isValidPhoneNumber=true;
        else if (entityPhoneBo.getPhoneNumber().matches("\\d{3}[\\s]\\d{3}[\\s]\\d{4}")) isValidPhoneNumber=true;
        else if (entityPhoneBo.getPhoneNumber().matches("\\(\\d{3}\\)[\\s]\\d{3}[-]\\d{4}")) isValidPhoneNumber=true;
        else isValidPhoneNumber=false;
        if(!isValidPhoneNumber){
            GlobalVariables.getMessageMap().putErrorForSectionId("OlePatronDocument-Phone", OLEConstants.INVALID_PHONE_NUMBER_FORMAT);
        }
        if (isValidPhoneNumber) {
            ModelAndView modelAndView = super.addLine(form, result, request, response);
            return modelAndView;
        } else {
            return getUIFModelAndView(form);
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addEmailAddress")
    public ModelAndView addEmailAddress(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                   HttpServletRequest request, HttpServletResponse response) {
        MaintenanceDocumentForm maintenanceForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument) maintenanceForm.getDocument();
        OlePatronDocument olePatronDocument = (OlePatronDocument) maintenanceDocument.getNewMaintainableObject().getDataObject();
        String selectedCollectionPath = maintenanceForm.getActionParamaterValue(UifParameters.SELLECTED_COLLECTION_PATH);
        CollectionGroup collectionGroup = maintenanceForm.getPostedView().getViewIndex().getCollectionGroupByPath(
                selectedCollectionPath);
        String addLinePath = collectionGroup.getAddLineBindingInfo().getBindingPath();
        Object eventObject = ObjectPropertyUtils.getPropertyValue(maintenanceForm, addLinePath);
        EntityEmailBo  emailBo = (EntityEmailBo) eventObject;
        return super.addLine(form, result, request, response);
    }

    @Override
    public ModelAndView save(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws Exception {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        LOG.debug(" Inside route method of patron maintenance controller ");
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        String action = mainForm.getMaintenanceAction();
        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OlePatron.PATRON_MAINTENANCE_DATE_FORMAT);
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        List<OleLoanDocument> oleLoanDocumentList = newOlePatronDocument.getOleLoanDocuments();
        for (OleLoanDocument oleLoanDocument : oleLoanDocumentList) {
            if (oleLoanDocument.isMissingPieceFlag()) {
                if (oleLoanDocument.getMissingPiecesCount() == null || oleLoanDocument.getMissingPiecesCount() != null && oleLoanDocument.getMissingPiecesCount().equalsIgnoreCase("")) {
                    oleLoanDocument.setNoOfMissingPiecesEditable(true);
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_INFO);
                    return getUIFModelAndView(form);
                }
                if (oleLoanDocument.getMissingPiecesCount() != null && !oleLoanDocument.getMissingPiecesCount().equalsIgnoreCase("")) {
                    int count = Integer.parseInt(oleLoanDocument.getMissingPiecesCount());
                    if (count < 1) {
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_GREATER, new String[]{"Missing Piece Count", "1"});
                        return getUIFModelAndView(form);
                    }
                }
                if (oleLoanDocument.getItemNumberOfPieces() == null || oleLoanDocument.getItemNumberOfPieces() != null && oleLoanDocument.getItemNumberOfPieces().toString().equalsIgnoreCase("")) {
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_ITEM_INFO);
                    oleLoanDocument.setNoOfPiecesEditable(true);
                    return getUIFModelAndView(form);
                }
                if(oleLoanDocument.getItemNumberOfPieces().intValue()<1){
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_GREATER, new String[]{"no of piece", "1"});
                    return getUIFModelAndView(form);
                }
                int missingPieceCount=Integer.parseInt(oleLoanDocument.getMissingPiecesCount());
                int numberOfPieces=oleLoanDocument.getItemNumberOfPieces();
                if(missingPieceCount>numberOfPieces){
                    GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_LESSER, new String[]{"Missing Piece Count", "no of pieces"});
                    return getUIFModelAndView(form);
                }
            }

        }
        for (OleLoanDocument oleLoanDocument : oleLoanDocumentList) {
            try {
                String itemXmlContent = getLoanProcessor().getItemXML(oleLoanDocument.getItemUuid());
                Item oleItem = getLoanProcessor().getItemPojo(itemXmlContent);
                boolean isMissingPieceFlagEnabled=(oleItem != null && oleItem.isMissingPieceFlag())?true:false;

                oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                if (oleItem.isClaimsReturnedFlag()) {
                    getOleDeliverRequestDocumentHelperService().cancelPendingRequestForClaimsReturnedItem(oleItem.getItemIdentifier());
                    oleItem.setClaimsReturnedNote(oleLoanDocument.getClaimsReturnNote());
                    oleItem.setClaimsReturnedFlagCreateDate(df.format(getDateTimeService().getCurrentDate()));
                } else {
                    oleItem.setClaimsReturnedNote(null);
                    oleItem.setClaimsReturnedFlagCreateDate(null);
                }
                SimpleDateFormat dfs = new SimpleDateFormat("MM/dd/yyyy");
                String parsedDate = dfs.format((new Date()));
                if(oleLoanDocument.isMissingPieceFlag() && !isMissingPieceFlagEnabled){
                    MissingPieceItemRecord missingPieceItemRecord = new MissingPieceItemRecord();
                    missingPieceItemRecord.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
                    missingPieceItemRecord.setMissingPieceCount(oleLoanDocument.getMissingPiecesCount());
                    missingPieceItemRecord.setMissingPieceDate(parsedDate);
                    missingPieceItemRecord.setOperatorId(GlobalVariables.getUserSession().getPrincipalId());
                    missingPieceItemRecord.setPatronBarcode(oleLoanDocument.getPatronBarcode());
                    missingPieceItemRecord.setItemId(oleLoanDocument.getItemUuid());
                    if (CollectionUtils.isNotEmpty(oleItem.getMissingPieceItemRecordList())) {

                        oleItem.getMissingPieceItemRecordList().add(missingPieceItemRecord);
                    } else {
                        List<MissingPieceItemRecord> missingPieceItemRecords = new ArrayList<MissingPieceItemRecord>();
                        missingPieceItemRecords.add(missingPieceItemRecord);
                        oleItem.setMissingPieceItemRecordList(missingPieceItemRecords);
                    }
                }

                if (oleLoanDocument.isClaimsReturnedIndicator()) {
                    getLoanProcessor().updateClaimsReturnedHistory(oleItem,oleLoanDocument,oleLoanDocument.getPatronId());
                    oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                    getOleDeliverRequestDocumentHelperService().cancelPendingRequestForClaimsReturnedItem(oleItem.getItemIdentifier());
                    oleItem.setClaimsReturnedNote(oleLoanDocument.getClaimsReturnNote());
                    oleItem.setClaimsReturnedFlagCreateDate(df.format(getDateTimeService().getCurrentDate()));
                } else {
                    oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                    oleItem.setClaimsReturnedNote(null);
                    oleItem.setClaimsReturnedFlagCreateDate(null);
                }
                oleItem.setMissingPieceFlag(oleLoanDocument.isMissingPieceFlag());
                oleItem.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
                if(oleLoanDocument.isItemDamagedStatus()){
                    getLoanProcessor().updateItemDamagedHistory(oleItem,oleLoanDocument,newOlePatronDocument.getOlePatronId());
                    oleItem.setItemDamagedStatus(oleLoanDocument.isItemDamagedStatus());
                    oleItem.setDamagedItemNote(oleLoanDocument.getItemDamagedNote());
                } else {
                    oleItem.setItemDamagedStatus(oleLoanDocument.isItemDamagedStatus());
                    oleItem.setDamagedItemNote(null);
                }
                oleItem.setMissingPiecesCount(oleLoanDocument.getMissingPiecesCount());
                if(oleLoanDocument.getItemNumberOfPieces()!=null&& !oleLoanDocument.getItemNumberOfPieces().toString().equalsIgnoreCase("")){
                    oleItem.setNumberOfPieces(oleLoanDocument.getItemNumberOfPieces().toString());
                }
                org.kuali.ole.docstore.common.document.Item itemXML = new ItemOleml();
                ItemOlemlRecordProcessor itemOlemlRecordProcessor = new ItemOlemlRecordProcessor();
                itemXML.setContent(itemOlemlRecordProcessor.toXML(oleItem));
                itemXML.setCategory(OLEConstants.WORK_CATEGORY);
                itemXML.setType(DocType.ITEM.getCode());
                itemXML.setFormat(OLEConstants.OLEML_FORMAT);
                itemXML.setId(oleLoanDocument.getItemUuid());
                getDocstoreClientLocator().getDocstoreClient().updateItem(itemXML);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(!olePatronHelperService.validatePatron(newOlePatronDocument)) {
            return getUIFModelAndView(form);
        }
        if (!KRADConstants.MAINTENANCE_EDIT_ACTION.equals(action)) {
            if (newOlePatronDocument.getOlePatronId() == null || newOlePatronDocument.getOlePatronId().isEmpty()) {
                if (newOlePatronDocument.getActivationDate() != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(newOlePatronDocument.getActivationDate())) > 0) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_ACTIVATION_DATE);

                    return getUIFModelAndView(mainForm);
                }
            }
        } else {
            Map<String, String> patronMap = new HashMap<String, String>();
            patronMap.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
            OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            Date todayDate=getDateTimeService().getCurrentDate();
            String format=simpleDateFormat.format(getDateTimeService().getCurrentDate());
            try {
                todayDate=simpleDateFormat.parse(format);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(newOlePatronDocument.getActivationDate()!=null && newOlePatronDocument.getActivationDate().compareTo(todayDate)<0 && patronDocument!=null && patronDocument.getActivationDate()!=null && patronDocument.getActivationDate().compareTo(newOlePatronDocument.getActivationDate())>0){
                if (!newOlePatronDocument.isPopupDialog()) {
                    newOlePatronDocument.setPopupDialog(true);
                    newOlePatronDocument.setUiMessageType("patron-message-info");
                    newOlePatronDocument.setPatronMessage(OLEConstants.OlePatron.ERROR_PATRON_NEW_PAST_DATE+"<br/>"+OLEConstants.PROCEED_MESSAGE);
                    return getUIFModelAndView(mainForm);
                }
            }
        }

        newOlePatronDocument.setPopupDialog(false);
        newOlePatronDocument.setOleLoanDocuments(oleLoanDocumentList);
        List<OleProxyPatronDocument> oleProxyPatronDocumentList = newOlePatronDocument.getOleProxyPatronDocuments();
        List<OleProxyPatronDocument> proxyPatronDocumentList = new ArrayList<OleProxyPatronDocument>();
        if (oleProxyPatronDocumentList.size() > 0) {
            for (OleProxyPatronDocument oleProxyPatronDocument : oleProxyPatronDocumentList) {
                Map<String, String> proxyMap = new HashMap<String, String>();
                proxyMap.put(OLEConstants.OlePatron.PATRON_ID, oleProxyPatronDocument.getProxyPatronId());
                OlePatronDocument tempDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, proxyMap);
                if (tempDocument != null) {
                    oleProxyPatronDocument.setProxyPatronBarcode(tempDocument.getBarcode());
                    oleProxyPatronDocument.setProxyPatronFirstName(tempDocument.getEntity().getNames().get(0).getFirstName());
                    oleProxyPatronDocument.setProxyPatronLastName(tempDocument.getEntity().getNames().get(0).getLastName());
                    proxyPatronDocumentList.add(oleProxyPatronDocument);
                }
            }
            newOlePatronDocument.setOleProxyPatronDocuments(proxyPatronDocumentList);
        }
        // Lost and reinstate barcode section
        if (newOlePatronDocument.getOlePatronId() != null && !newOlePatronDocument.getOlePatronId().equalsIgnoreCase("")) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.OlePatron.PATRON_LOST_BARCODE_FLD, newOlePatronDocument.getBarcode());
            map.put("active", "N");
            List<OlePatronLostBarcode> olePatronLostBarcodes = (List<OlePatronLostBarcode>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronLostBarcode.class, map);
            if (olePatronLostBarcodes.size() > 0 && (!newOlePatronDocument.isSkipBarcodeValidation())) {
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.PATRON_DUPLICATE_BARCODE);
                return getUIFModelAndView(mainForm);
            }
            // LOST Barcode Validation for current lost section
            boolean isBarcodeContainsInLostSection = false;
            if(newOlePatronDocument.getLostBarcodes()!=null&& newOlePatronDocument.getBarcode()!=null){
                // if barcode contains in lost section submitting the document will reinstate the barcode
                for(OlePatronLostBarcode olePatronLostBarcode:olePatronLostBarcodes){
                    if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber()!=null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())){
                        isBarcodeContainsInLostSection=true;
                    }
                }
            }
            // if barcode is editable mode and shouldn't be reinstated
            if (newOlePatronDocument.isBarcodeEditable()&& !newOlePatronDocument.isReinstated()) {
                for (OlePatronLostBarcode olePatronLostBarcode : newOlePatronDocument.getLostBarcodes()) {
                    if (olePatronLostBarcode.getInvalidOrLostBarcodeNumber() != null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())) {
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.PATRON_BARCODE_BLOCK_MANUALLY,new String[]{newOlePatronDocument.getBarcode()});
                        newOlePatronDocument.setBarcode(null);
                        newOlePatronDocument.setBarcodeEditable(true);
                        newOlePatronDocument.setReinstated(false);
                        return getUIFModelAndView(mainForm);
                    }
                }
            }
            if (!isBarcodeContainsInLostSection) {
                //JIRA OLE-5707
                Map<String, String> patronMap = new HashMap<String, String>();
                patronMap.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
                OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                if (patronDocument != null && patronDocument.getBarcode() != null && !(patronDocument.getBarcode().equalsIgnoreCase(""))) {
                    if (newOlePatronDocument.getLostBarcodes() == null) {
                        newOlePatronDocument.setLostBarcodes(new ArrayList<OlePatronLostBarcode>());
                    }
                    if (!patronDocument.getBarcode().equalsIgnoreCase(newOlePatronDocument.getBarcode()) && (!newOlePatronDocument.isBarcodeChanged())) {
                        if (patronDocument != null && patronDocument.getBarcode() != null) {
                            boolean isNeedToUpdateBarcode=false;
                            String originalBarcode=patronDocument.getBarcode();
                            for(OlePatronLostBarcode olePatronLostBarcode:newOlePatronDocument.getLostBarcodes()){
                                if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber()!=null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(originalBarcode)){
                                    isNeedToUpdateBarcode=true;
                                }
                            }
                            if (!isNeedToUpdateBarcode) {
                                OlePatronLostBarcode olePatronLostBarcode = new OlePatronLostBarcode();
                                olePatronLostBarcode.setInvalidOrLostBarcodeNumber(patronDocument.getBarcode());
                                olePatronLostBarcode.setInvalidOrLostBarcodeEffDate(new java.sql.Date(getDateTimeService().getCurrentDate().getTime()));
                                olePatronLostBarcode.setRevertBarcode(true);
                                olePatronLostBarcode.setDescription("");
                                olePatronLostBarcode.setStatus("Lost");
                                newOlePatronDocument.getLostBarcodes().add(olePatronLostBarcode);
                            }
                        }

                    }
                }
            }
        }
        // proxy patron
        List<OleProxyPatronDocument> proxyPatron = newOlePatronDocument.getOleProxyPatronDocuments();
        String patronId = "";
        Date proxyActDate = null;
        Date proxyExpDate = null;
        if (proxyPatron.size() > 0) {
            for (OleProxyPatronDocument proxy : proxyPatron) {
                patronId = proxy.getProxyPatronId();
                proxyActDate = proxy.getProxyPatronActivationDate();
                proxyExpDate = proxy.getProxyPatronExpirationDate();
                Map<String, String> patronMap = new HashMap<String, String>();
                patronMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
                OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                if (patronDocument != null) {
                    Date patronExpDate = patronDocument.getExpirationDate();
                    Date patronActDate = patronDocument.getActivationDate();
                    if (proxyActDate != null) {
                        if (patronActDate != null && fmt.format(patronActDate).compareTo(fmt.format(proxyActDate)) > 0) {
                            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REAL_PATRON_ACTIVATION_DATE);
                            return getUIFModelAndView(mainForm);
                        }
                    }
                    if (proxyExpDate != null) {
                        if (patronExpDate != null) {
                            if (fmt.format(proxyExpDate).compareTo(fmt.format(patronExpDate)) > 0) {
                                if(newOlePatronDocument.isExpirationFlag()){
                                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REAL_PATRON_EXPIRATION_DATE);
                                    return getUIFModelAndView(mainForm);
                                }
                            }
                        }
                    }
                }
                boolean isBorrowerTypeActive = olePatronHelperService.isBorrowerTypeActive(newOlePatronDocument);
                if (!isBorrowerTypeActive) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_BORROWER_TYPE_INACTIVE);
                    return getUIFModelAndView(mainForm);
                }
                if (patronId != null && newOlePatronDocument.getOlePatronId() != null && newOlePatronDocument.getOlePatronId().equals(patronId)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_ID);
                    return getUIFModelAndView(mainForm);
                } else {
                    if (proxyExpDate != null) {
                        if (proxyActDate != null) {
                            if ((fmt.format(proxyActDate).compareTo(fmt.format(proxyExpDate)) >= 0)) {
                                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_EXPIRATION_DATE);
                                return getUIFModelAndView(mainForm);
                            }
                        }
                    }
                }
            }
        }
        return super.save(form, result, request, response);
    }

    /**
     * To submit or route the patron maintenance document
     *
     * @param form document form base containing the document instance that will be routed
     * @return ModelAndView
     */
    @Override
    @RequestMapping(params = "methodToCall=route")
    public ModelAndView route(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        LOG.debug(" Inside route method of patron maintenance controller ");
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        String action = mainForm.getMaintenanceAction();
        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OlePatron.PATRON_MAINTENANCE_DATE_FORMAT);
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        List<OleLoanDocument> oleLoanDocumentList = newOlePatronDocument.getOleLoanDocuments();
        for (OleLoanDocument oleLoanDocument : oleLoanDocumentList) {
            if (oleLoanDocument.isLoanModified()) {
                if (oleLoanDocument.isMissingPieceFlag()) {
                    if (oleLoanDocument.getMissingPiecesCount() == null || oleLoanDocument.getMissingPiecesCount() != null && oleLoanDocument.getMissingPiecesCount().equalsIgnoreCase("")) {
                        oleLoanDocument.setNoOfMissingPiecesEditable(true);
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_INFO);
                        return getUIFModelAndView(form);
                    }
                    if (oleLoanDocument.getMissingPiecesCount() != null && !oleLoanDocument.getMissingPiecesCount().equalsIgnoreCase("")) {
                        int count = Integer.parseInt(oleLoanDocument.getMissingPiecesCount());
                        if (count < 1) {
                            GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_GREATER, new String[]{"Missing Piece Count", "1"});
                            return getUIFModelAndView(form);
                        }
                    }
                    if (oleLoanDocument.getItemNumberOfPieces() == null || oleLoanDocument.getItemNumberOfPieces() != null && oleLoanDocument.getItemNumberOfPieces().toString().equalsIgnoreCase("")) {
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_ITEM_INFO);
                        oleLoanDocument.setNoOfPiecesEditable(true);
                        return getUIFModelAndView(form);
                    }
                    if (oleLoanDocument.getItemNumberOfPieces().intValue() < 1) {
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_GREATER, new String[]{"no of piece", "1"});
                        oleLoanDocument.setNoOfPiecesEditable(true);
                        oleLoanDocument.setNoOfMissingPiecesEditable(true);
                        return getUIFModelAndView(form);
                    }
                    int missingPieceCount = Integer.parseInt(oleLoanDocument.getMissingPiecesCount());
                    int numberOfPieces = oleLoanDocument.getItemNumberOfPieces();
                    if (missingPieceCount > numberOfPieces) {
                        GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OlePatron.PATRON_LOANED_ITEM_SECTION, OLEConstants.OlePatron.ERROR_PATRON_MISSING_PIECE_ITEM_COUNT_LESSER, new String[]{"Missing Piece Count", "no of pieces"});
                        oleLoanDocument.setNoOfPiecesEditable(true);
                        oleLoanDocument.setNoOfMissingPiecesEditable(true);
                        return getUIFModelAndView(form);
                    }
                }
                try {
                    String itemXmlContent = getLoanProcessor().getItemXML(oleLoanDocument.getItemUuid());
                    Item oleItem = getLoanProcessor().getItemPojo(itemXmlContent);
                    boolean isMissingPieceFlagEnabled=(oleItem != null && oleItem.isMissingPieceFlag())?true:false;
                    if (oleLoanDocument.isClaimsReturnedIndicator()) {
                        getLoanProcessor().updateClaimsReturnedHistory(oleItem,oleLoanDocument,newOlePatronDocument.getOlePatronId());
                        oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                        getOleDeliverRequestDocumentHelperService().cancelPendingRequestForClaimsReturnedItem(oleItem.getItemIdentifier());
                        oleItem.setClaimsReturnedNote(oleLoanDocument.getClaimsReturnNote());
                        oleItem.setClaimsReturnedFlagCreateDate(df.format(getDateTimeService().getCurrentDate()));
                    } else {
                        oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                        oleItem.setClaimsReturnedNote(null);
                        oleItem.setClaimsReturnedFlagCreateDate(null);
                    }
                    SimpleDateFormat dfs = new SimpleDateFormat("MM/dd/yyyy");
                    String parsedDate = dfs.format((new Date()));
                    if(oleLoanDocument.isMissingPieceFlag() && !isMissingPieceFlagEnabled){
                        MissingPieceItemRecord missingPieceItemRecord = new MissingPieceItemRecord();
                        missingPieceItemRecord.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
                        missingPieceItemRecord.setMissingPieceCount(oleLoanDocument.getMissingPiecesCount());
                        missingPieceItemRecord.setMissingPieceDate(parsedDate);
                        missingPieceItemRecord.setOperatorId(GlobalVariables.getUserSession().getPrincipalId());
                        missingPieceItemRecord.setPatronBarcode(oleLoanDocument.getPatronBarcode());
                        missingPieceItemRecord.setItemId(oleLoanDocument.getItemUuid());
                        if (CollectionUtils.isNotEmpty(oleItem.getMissingPieceItemRecordList())) {

                            oleItem.getMissingPieceItemRecordList().add(missingPieceItemRecord);
                        } else {
                            List<MissingPieceItemRecord> missingPieceItemRecords = new ArrayList<MissingPieceItemRecord>();
                            missingPieceItemRecords.add(missingPieceItemRecord);
                            oleItem.setMissingPieceItemRecordList(missingPieceItemRecords);
                        }
                    }else{
                        Map<String, String> map = new HashMap<>();
                        map.put("itemId", DocumentUniqueIDPrefix.getDocumentId(oleLoanDocument.getItemUuid()));
                        List<org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.MissingPieceItemRecord> missingPieceItemRecordList1 = (List<org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.MissingPieceItemRecord>) KRADServiceLocator.getBusinessObjectService()
                                .findMatchingOrderBy(org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.MissingPieceItemRecord.class, map, "missingPieceItemId", true);
                        List<MissingPieceItemRecord> missingPieceItemRecords = new ArrayList<>();
                        for (int index = 0; index < missingPieceItemRecordList1.size(); index++) {
                            MissingPieceItemRecord missingPieceItemRecord1 = new MissingPieceItemRecord();
                            if (index == missingPieceItemRecordList1.size() - 1) {
                                /*if (oleLoanForm.getMissi != null) {
                                    claimsReturnedRecord.setClaimsReturnedFlagCreateDate(convertToString(loanObject.getClaimsReturnedDate()));
                                }
                                else{
                                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                    claimsReturnedRecord.setClaimsReturnedFlagCreateDate(df.format(getDateTimeService().getCurrentDate()));
                                }*/
                                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                String missingPieceItemDate = dateFormat.format((new Date()));
                                missingPieceItemRecord1.setMissingPieceDate(missingPieceItemDate);
                                missingPieceItemRecord1.setMissingPieceCount(oleLoanDocument.getMissingPiecesCount());
                                missingPieceItemRecord1.setPatronBarcode(newOlePatronDocument.getBarcode());
                                missingPieceItemRecord1.setOperatorId(GlobalVariables.getUserSession().getPrincipalId());
                                missingPieceItemRecord1.setItemId(DocumentUniqueIDPrefix.getDocumentId(oleLoanDocument.getItemUuid()));
                                missingPieceItemRecord1.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
                                missingPieceItemRecords.add(missingPieceItemRecord1);

                            } else {
                                if (missingPieceItemRecordList1.get(index).getMissingPieceDate() != null && !missingPieceItemRecordList1.get(index).getMissingPieceDate().toString().isEmpty()) {
                                    SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                    Date missingPieceItemDate = null;
                                    try {
                                        missingPieceItemDate = format2.parse(missingPieceItemRecordList1.get(index).getMissingPieceDate().toString());
                                    } catch (org.kuali.ole.sys.exception.ParseException e) {
                                        LOG.error("format string to Date " + e);
                                    }
                                    missingPieceItemRecord1.setMissingPieceDate(format1.format(missingPieceItemDate).toString());
                                }
                                missingPieceItemRecord1.setMissingPieceFlagNote(missingPieceItemRecordList1.get(index).getMissingPieceFlagNote());
                                missingPieceItemRecord1.setMissingPieceCount(missingPieceItemRecordList1.get(index).getMissingPieceCount());
                                missingPieceItemRecord1.setOperatorId(missingPieceItemRecordList1.get(index).getOperatorId());
                                missingPieceItemRecord1.setPatronBarcode(missingPieceItemRecordList1.get(index).getPatronBarcode());
                                missingPieceItemRecord1.setItemId(missingPieceItemRecordList1.get(index).getItemId());
                                missingPieceItemRecords.add(missingPieceItemRecord1);
                            }
                        }
                        oleItem.setMissingPieceItemRecordList(missingPieceItemRecords);

                    }
                    oleItem.setMissingPieceFlag(oleLoanDocument.isMissingPieceFlag());
                    oleItem.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
                    if(oleLoanDocument.isItemDamagedStatus()){
                        getLoanProcessor().updateItemDamagedHistory(oleItem,oleLoanDocument,newOlePatronDocument.getOlePatronId());
                        oleItem.setItemDamagedStatus(oleLoanDocument.isItemDamagedStatus());
                        oleItem.setDamagedItemNote(oleLoanDocument.getItemDamagedNote());
                    } else {
                        oleItem.setItemDamagedStatus(oleLoanDocument.isItemDamagedStatus());
                        oleItem.setDamagedItemNote(null);
                    }
                    oleItem.setMissingPiecesCount(oleLoanDocument.getMissingPiecesCount());
                    if (oleLoanDocument.getItemNumberOfPieces() != null && !oleLoanDocument.getItemNumberOfPieces().toString().equalsIgnoreCase("")) {
                        oleItem.setNumberOfPieces(oleLoanDocument.getItemNumberOfPieces().toString());
                    }
                    org.kuali.ole.docstore.common.document.Item itemXML = new ItemOleml();
                    ItemOlemlRecordProcessor itemOlemlRecordProcessor = new ItemOlemlRecordProcessor();
                    itemXML.setContent(itemOlemlRecordProcessor.toXML(oleItem));
                    itemXML.setCategory(OLEConstants.WORK_CATEGORY);
                    itemXML.setType(DocType.ITEM.getCode());
                    itemXML.setFormat(OLEConstants.OLEML_FORMAT);
                    itemXML.setId(oleLoanDocument.getItemUuid());
                    getDocstoreClientLocator().getDocstoreClient().updateItem(itemXML);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
        if(!olePatronHelperService.validatePatron(newOlePatronDocument)) {
            return getUIFModelAndView(form);
        }
        if (!KRADConstants.MAINTENANCE_EDIT_ACTION.equals(action)) {
            if (newOlePatronDocument.getOlePatronId() == null || newOlePatronDocument.getOlePatronId().isEmpty()) {
                if (newOlePatronDocument.getActivationDate() != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(newOlePatronDocument.getActivationDate())) > 0) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_ACTIVATION_DATE);

                    return getUIFModelAndView(mainForm); // JIRA OLE-5107
                }
            }
        } else {
            Map<String, String> patronMap = new HashMap<String, String>();
            patronMap.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
            OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            Date todayDate=getDateTimeService().getCurrentDate();
            String format=simpleDateFormat.format(getDateTimeService().getCurrentDate());
            try {
                 todayDate=simpleDateFormat.parse(format);
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if(newOlePatronDocument.getActivationDate()!=null && newOlePatronDocument.getActivationDate().compareTo(todayDate)<0 && patronDocument!=null && patronDocument.getActivationDate()!=null && patronDocument.getActivationDate().compareTo(newOlePatronDocument.getActivationDate())>0){
                if (!newOlePatronDocument.isPopupDialog()) {
                    newOlePatronDocument.setPopupDialog(true);
                    newOlePatronDocument.setUiMessageType("patron-message-info");
                    newOlePatronDocument.setPatronMessage(OLEConstants.OlePatron.ERROR_PATRON_NEW_PAST_DATE+"<br/>"+OLEConstants.PROCEED_MESSAGE);
                    return getUIFModelAndView(mainForm);
                }
            }
        }

        newOlePatronDocument.setPopupDialog(false);
        newOlePatronDocument.setOleLoanDocuments(oleLoanDocumentList);
        List<OleProxyPatronDocument> oleProxyPatronDocumentList = newOlePatronDocument.getOleProxyPatronDocuments();
        List<OleProxyPatronDocument> proxyPatronDocumentList = new ArrayList<OleProxyPatronDocument>();
        if (oleProxyPatronDocumentList.size() > 0) {
            for (OleProxyPatronDocument oleProxyPatronDocument : oleProxyPatronDocumentList) {
                Map<String, String> proxyMap = new HashMap<String, String>();
                proxyMap.put(OLEConstants.OlePatron.PATRON_ID, oleProxyPatronDocument.getProxyPatronId());
                OlePatronDocument tempDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, proxyMap);
                if (tempDocument != null) {
                    oleProxyPatronDocument.setProxyPatronBarcode(tempDocument.getBarcode());
                    oleProxyPatronDocument.setProxyPatronFirstName(tempDocument.getEntity().getNames().get(0).getFirstName());
                    oleProxyPatronDocument.setProxyPatronLastName(tempDocument.getEntity().getNames().get(0).getLastName());
                    proxyPatronDocumentList.add(oleProxyPatronDocument);
                }
            }
            newOlePatronDocument.setOleProxyPatronDocuments(proxyPatronDocumentList);
        }
        // Lost and reinstate barcode section
        if (newOlePatronDocument.getOlePatronId() != null && !newOlePatronDocument.getOlePatronId().equalsIgnoreCase("")) {
            // if barcode is editable mode and shouldn't be reinstated
            if (newOlePatronDocument.isBarcodeEditable()&& !newOlePatronDocument.isReinstated()) {
                for (OlePatronLostBarcode olePatronLostBarcode : newOlePatronDocument.getLostBarcodes()) {
                    if (olePatronLostBarcode.getInvalidOrLostBarcodeNumber() != null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())) {
                        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.PATRON_BARCODE_BLOCK_MANUALLY,new String[]{newOlePatronDocument.getBarcode()});
                        newOlePatronDocument.setBarcode(null);
                        newOlePatronDocument.setBarcodeEditable(true);
                        newOlePatronDocument.setReinstated(false);
                        olePatronLostBarcode.setStatus("LOST");
                        olePatronLostBarcode.setDescription("LOST");
                        return getUIFModelAndView(mainForm);
                    }
                }
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.OlePatron.PATRON_LOST_BARCODE_FLD, newOlePatronDocument.getBarcode());
            map.put("active", "N");
            List<OlePatronLostBarcode> olePatronLostBarcodes = (List<OlePatronLostBarcode>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronLostBarcode.class, map);
            if (olePatronLostBarcodes.size() > 0 && (!newOlePatronDocument.isSkipBarcodeValidation())) {
                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.PATRON_DUPLICATE_BARCODE);
                return getUIFModelAndView(mainForm);
            }
            // LOST Barcode Validation for current lost section
            boolean isBarcodeContainsInLostSection = false;
            if(newOlePatronDocument.getLostBarcodes()!=null&& newOlePatronDocument.getBarcode()!=null){
                // if barcode contains in lost section submitting the document will reinstate the barcode
                for(OlePatronLostBarcode olePatronLostBarcode:olePatronLostBarcodes){
                     if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber()!=null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())){
                         isBarcodeContainsInLostSection=true;
                     }
                }
            }
            if (!isBarcodeContainsInLostSection) {
                //JIRA OLE-5707
                Map<String, String> patronMap = new HashMap<String, String>();
                patronMap.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
                OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                if (patronDocument != null && patronDocument.getBarcode() != null && !(patronDocument.getBarcode().equalsIgnoreCase(""))) {
                    if (newOlePatronDocument.getLostBarcodes() == null) {
                        newOlePatronDocument.setLostBarcodes(new ArrayList<OlePatronLostBarcode>());
                    }
                    if (!patronDocument.getBarcode().equalsIgnoreCase(newOlePatronDocument.getBarcode()) && (!newOlePatronDocument.isBarcodeChanged())) {
                        if (patronDocument != null && patronDocument.getBarcode() != null) {
                            boolean isNeedToUpdateBarcode=false;
                            String originalBarcode=patronDocument.getBarcode();
                            for(OlePatronLostBarcode olePatronLostBarcode:newOlePatronDocument.getLostBarcodes()){
                                if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber()!=null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(originalBarcode)){
                                    isNeedToUpdateBarcode=true;
                                }
                            }
                            if (!isNeedToUpdateBarcode) {
                                OlePatronLostBarcode olePatronLostBarcode = new OlePatronLostBarcode();
                                olePatronLostBarcode.setInvalidOrLostBarcodeNumber(patronDocument.getBarcode());
                                olePatronLostBarcode.setInvalidOrLostBarcodeEffDate(new java.sql.Date(getDateTimeService().getCurrentDate().getTime()));
                                olePatronLostBarcode.setRevertBarcode(true);
                                olePatronLostBarcode.setDescription("");
                                olePatronLostBarcode.setStatus("Lost");
                                newOlePatronDocument.getLostBarcodes().add(olePatronLostBarcode);
                            }
                        }

                    }
                }
            }
        }
        // proxy patron
        List<OleProxyPatronDocument> proxyPatron = newOlePatronDocument.getOleProxyPatronDocuments();
        String patronId = "";
        Date proxyActDate = null;
        Date proxyExpDate = null;
        if (proxyPatron.size() > 0) {
            for (OleProxyPatronDocument proxy : proxyPatron) {
                patronId = proxy.getProxyPatronId();
                proxyActDate = proxy.getProxyPatronActivationDate();
                proxyExpDate = proxy.getProxyPatronExpirationDate();
                Map<String, String> patronMap = new HashMap<String, String>();
                patronMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
                OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                if (patronDocument != null) {
                    Date patronExpDate = patronDocument.getExpirationDate();
                    Date patronActDate = patronDocument.getActivationDate();
                    if (proxyActDate != null) {
                        if (patronActDate != null && fmt.format(patronActDate).compareTo(fmt.format(proxyActDate)) > 0) {
                            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REAL_PATRON_ACTIVATION_DATE);
                            return getUIFModelAndView(mainForm);
                        }
                    }
                    if (proxyExpDate != null) {
                        if (patronExpDate != null) {
                            if (fmt.format(proxyExpDate).compareTo(fmt.format(patronExpDate)) > 0) {
                                if(newOlePatronDocument.isExpirationFlag()){
                                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REAL_PATRON_EXPIRATION_DATE);
                                    return getUIFModelAndView(mainForm);
                                }
                            }
                        }
                    }
                }
               /* boolean lostBarcodeCheck = olePatronHelperService.CheckBarcodeAndLostBarcode(newOlePatronDocument);
                if (lostBarcodeCheck) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_BARCODE_INVALID);
                    return getUIFModelAndView(mainForm);
                }*/
                boolean isBorrowerTypeActive = olePatronHelperService.isBorrowerTypeActive(newOlePatronDocument);
                if (!isBorrowerTypeActive) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_BORROWER_TYPE_INACTIVE);
                    return getUIFModelAndView(mainForm);
                }
                if (patronId != null && newOlePatronDocument.getOlePatronId() != null && newOlePatronDocument.getOlePatronId().equals(patronId)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_ID);
                    return getUIFModelAndView(mainForm);
                } /*else if (proxyActDate != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(proxyActDate)) > 0) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_ACTIVATION_DATE);
                    return getUIFModelAndView(mainForm);
                } */else {
                    if (proxyExpDate != null) {
                        if (proxyActDate != null) {
                            if ((fmt.format(proxyActDate).compareTo(fmt.format(proxyExpDate)) >= 0)) {
                                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_EXPIRATION_DATE);
                                return getUIFModelAndView(mainForm);
                            }
                        } /*else if (fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(proxyExpDate)) > 0) {
                            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PROXY_PATRON_EXPIRATION_DATE);
                            return getUIFModelAndView(mainForm);
                        }*/
                    }
                }
            }
        }
        if (newOlePatronDocument.isGeneralBlock() && (newOlePatronDocument.getGeneralBlockNotes() == null || newOlePatronDocument.getGeneralBlockNotes().equals(""))) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_GENERAL_BLOCK_NOTES, OLEConstants.OlePatron.PATRON_GENERAL_BLOCK_NOTES);
            return getUIFModelAndView(mainForm);
        } /*else if (newOlePatronDocument.getActivationDate() != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(newOlePatronDocument.getActivationDate())) > 0) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_ACTIVATION_DATE);
            return getUIFModelAndView(mainForm); // JIRA OLE-5107
        }*/ else if ((newOlePatronDocument.getExpirationDate() != null && newOlePatronDocument.getActivationDate() != null && fmt.format(newOlePatronDocument.getActivationDate()).compareTo(fmt.format(newOlePatronDocument.getExpirationDate())) >= 0) || (newOlePatronDocument.getExpirationDate() != null && fmt.format(new Date(System.currentTimeMillis())).compareTo(fmt.format(newOlePatronDocument.getExpirationDate())) > 0)) {
            if(newOlePatronDocument.isExpirationFlag()){
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_EXPIRATION_DATE);
            return getUIFModelAndView(mainForm);
            }
        } else if (newOlePatronDocument.getOlePatronId() != null) {
            Map<String, String> tempId = new HashMap<String, String>();
            tempId.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
            OlePatronDocument tempDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, tempId);
            if (tempDocument != null) {
                if (tempDocument.getEntity() != null) {
                    if (newOlePatronDocument.getDeletedOleEntityAddressBo().size() > 0) {
                        for(OleEntityAddressBo oleEntityAddressBo:newOlePatronDocument.getDeletedOleEntityAddressBo()){
                            KRADServiceLocator.getBusinessObjectService().delete(oleEntityAddressBo.getOleAddressBo());
                            KRADServiceLocator.getBusinessObjectService().delete(oleEntityAddressBo.getEntityAddressBo());
                        }
                    }
                    Map<String, String> mapEntityId = new HashMap<String, String>();
                    mapEntityId.put("entityId", newOlePatronDocument.getOlePatronId());
                    List<EntityEmailBo> emailBoList = tempDocument.getEntity().getEntityTypeContactInfos().get(0).getEmailAddresses();
                    if(newOlePatronDocument.getDeletedEmails().size()>0){
                        KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedEmails());
                    }
                    List<EntityNameBo> entityNameBos = tempDocument.getEntity().getNames();
                    if (entityNameBos.size() > 0) {
                        KRADServiceLocator.getBusinessObjectService().deleteMatching(EntityNameBo.class,mapEntityId);
                    }
                    if(newOlePatronDocument.getDeletedPhones().size()>0){
                        KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedPhones());
                    }
                    if(newOlePatronDocument.getDeletedNotes().size()>0){
                        KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedNotes());
                    }

                    List<OlePatronLostBarcode> lostBarcodeList = tempDocument.getLostBarcodes();
                    if (lostBarcodeList.size() > 0) {
                        KRADServiceLocator.getBusinessObjectService().delete(lostBarcodeList);
                    }
                    if(newOlePatronDocument.getDeletedEmployments().size()>0){
                        KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedEmployments());
                    }
                    if(newOlePatronDocument.getDeletedPatronAffiliations().size()>0){
                        for(OlePatronAffiliation olePatronAffiliation:newOlePatronDocument.getDeletedPatronAffiliations()){
                           if(olePatronAffiliation.getEmployments()!=null && olePatronAffiliation.getEmployments().size()>0){
                               KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedOleProxyPatronDocuments());
                           }
                           Map<String,String> affiliationsMap=new HashMap<String,String>();
                           affiliationsMap.put("id",olePatronAffiliation.getEntityAffiliationId());
                            KRADServiceLocator.getBusinessObjectService().deleteMatching(EntityAffiliationBo.class,affiliationsMap);

                        }
                    }
                    if(newOlePatronDocument.getDeletedOleProxyPatronDocuments().size()>0){
                       KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedOleProxyPatronDocuments());
                    }
                    if(newOlePatronDocument.getDeletedOlePatronLocalIds().size()>0){
                        KRADServiceLocator.getBusinessObjectService().delete(newOlePatronDocument.getDeletedOlePatronLocalIds());
                    }
                }
            }
        }
        if (newOlePatronDocument.getLostBarcodes() != null) {
            for (OlePatronLostBarcode lostBarcodes : newOlePatronDocument.getLostBarcodes()) {
                if (lostBarcodes.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())) {
                    lostBarcodes.setActive(true);
                }
            }
        }
        if (KRADConstants.MAINTENANCE_EDIT_ACTION.equals(action)) {
            newOlePatronDocument.setEntity(olePatronHelperService.editAndSaveEntityBo(newOlePatronDocument));
            Map<String, Object> criteria = new HashMap<String, Object>();
            criteria.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
            KRADServiceLocator.getBusinessObjectService().deleteMatching(OleAddressBo.class, criteria);
            prepareOleAddressForSave(newOlePatronDocument);
        }
        if (KRADConstants.MAINTENANCE_COPY_ACTION.equals(action) || KRADConstants.MAINTENANCE_NEW_ACTION.equals(action)) {
            newOlePatronDocument.setEntity(olePatronHelperService.copyAndSaveEntityBo(newOlePatronDocument));
            newOlePatronDocument.setOlePatronId(newOlePatronDocument.getEntity().getId());
            for (OleEntityAddressBo oleEntityAddressBo : newOlePatronDocument.getOleEntityAddressBo()) {
               if(oleEntityAddressBo.getOleAddressBo()!=null){
                   oleEntityAddressBo.getOleAddressBo().setOleAddressId(null);
               }
            }
            prepareOleAddressForSave(newOlePatronDocument);
        }
        ModelAndView model = null;
        try {
            model = super.route(mainForm, result, request, response);
        } catch (Exception e) {
            return model;
        }
        return model;
    }


    @RequestMapping(params = "methodToCall=lostBarcode")
    public ModelAndView lostBarcode(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {

        LOG.debug(" Inside route method of patron maintenance controller ");
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        if (StringUtils.isBlank(newOlePatronDocument.getBarcode()) || StringUtils.isEmpty(newOlePatronDocument.getBarcode())) {
            newOlePatronDocument.setBarcodeEditable(true);
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REQUIRED, new String[]{"Barcode"});
            return getUIFModelAndView(mainForm);
        }
        newOlePatronDocument.setReinstated(false);
        boolean isBarcodeExistInOLE = false;
        Map<String, String> patronMap = new HashMap<String, String>();
        patronMap.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
        OlePatronDocument patronDocument = (OlePatronDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
        if (newOlePatronDocument.getBarcode() != null && (!newOlePatronDocument.getBarcode().equalsIgnoreCase(""))) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.OlePatron.PATRON_ID, newOlePatronDocument.getOlePatronId());
            map.put("active", "N");
            List<OlePatronLostBarcode> olePatronLostBarcodes = (List<OlePatronLostBarcode>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronLostBarcode.class, map);
            if (olePatronLostBarcodes.size() > 0 && (!newOlePatronDocument.isSkipBarcodeValidation())) {
                isBarcodeExistInOLE = true;
            }
            if (patronDocument != null && patronDocument.getBarcode() != null && patronDocument.getBarcode().equalsIgnoreCase(newOlePatronDocument.getBarcode())) {
                isBarcodeExistInOLE = true;
            }
        }
        if (LOG.isDebugEnabled()){
            LOG.debug("Allow the patron to add barcode "+isBarcodeExistInOLE);
        }
        if (!isBarcodeExistInOLE) {
            // Added validation for if barcode exist in OLE system
            newOlePatronDocument.setBarcodeEditable(true);
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_BARCODE_EXIST_LOST_SECTION);
            return getUIFModelAndView(mainForm);
        }
        for(OlePatronLostBarcode olePatronLostBarcode:newOlePatronDocument.getLostBarcodes()){
            // Added validation if barcode exist in current section to restrict duplicacy
            if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber()!=null && olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(newOlePatronDocument.getBarcode())){
                if (!(patronDocument != null && patronDocument.getBarcode().equalsIgnoreCase(newOlePatronDocument.getBarcode()))) {
                    GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REQUIRED, new String[]{"Barcode"});
                    return getUIFModelAndView(mainForm);
                }
            }
        }
        if (isBarcodeExistInOLE) {
            if (!newOlePatronDocument.isInvalidateBarcode()) {
                if (StringUtils.isBlank(newOlePatronDocument.getBarcode()) || StringUtils.isEmpty(newOlePatronDocument.getBarcode())) {
                    GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_REQUIRED, new String[]{"Barcode"});
                    return getUIFModelAndView(mainForm);
                }
                newOlePatronDocument.setBarcodeEditable(true);
                newOlePatronDocument.setLostDescription("");
                newOlePatronDocument.setInvalidateBarcode(true);
                return getUIFModelAndView(mainForm);
            }
            boolean isBarcodeExist = false;
            newOlePatronDocument.setBarcodeChanged(true);
            newOlePatronDocument.setInvalidateBarcode(false);
            String lostBarcode = newOlePatronDocument.getBarcode();
            OlePatronLostBarcode olePatronLostBarcode = new OlePatronLostBarcode();
            olePatronLostBarcode.setInvalidOrLostBarcodeNumber(lostBarcode);
            olePatronLostBarcode.setInvalidOrLostBarcodeEffDate(new java.sql.Date(getDateTimeService().getCurrentDate().getTime()));
            olePatronLostBarcode.setRevertBarcode(true);
            olePatronLostBarcode.setDescription(newOlePatronDocument.getLostDescription());
            olePatronLostBarcode.setStatus(newOlePatronDocument.getLostStatus());
            for (OlePatronLostBarcode lostBarcodes : newOlePatronDocument.getLostBarcodes()) {
                if (lostBarcodes.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(lostBarcode)) {
                    lostBarcodes.setDescription(newOlePatronDocument.getLostDescription());
                    lostBarcodes.setStatus(newOlePatronDocument.getLostStatus());
                    isBarcodeExist = true;
                }
            }
            List<OlePatronLostBarcode> lostBarcodes = newOlePatronDocument.getLostBarcodes();
            List<OlePatronLostBarcode> lostBarcodeList = new ArrayList<OlePatronLostBarcode>();
            if (!isBarcodeExist) {
                lostBarcodeList.add(olePatronLostBarcode);
                if (lostBarcodes.size() > 0) {
                    for (OlePatronLostBarcode lostPatronBarcode : lostBarcodes) {
                        lostPatronBarcode.setRevertBarcode(false);
                        lostBarcodeList.add(lostPatronBarcode);
                    }
                }
                newOlePatronDocument.setLostBarcodes(lostBarcodeList);
            }
            newOlePatronDocument.setReinstated(false);
            newOlePatronDocument.setBarcode(null);
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ENTER_PATRON_BARCODE);
        }
        return getUIFModelAndView(mainForm);
    }

    @RequestMapping(params = "methodToCall=reinstateBarcode")
    public ModelAndView reinstateBarcode(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) {

        LOG.debug(" Inside route method of patron maintenance controller ");
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        String oldBarcode=newOlePatronDocument.getBarcode();
        if (!newOlePatronDocument.isReinstateBarcode()) {
            newOlePatronDocument.setReinstateBarcode(true);
            if (oldBarcode != null && !oldBarcode.equalsIgnoreCase("")) {
                newOlePatronDocument.setBarcodeEditable(false);
            } else {
                newOlePatronDocument.setBarcodeEditable(true);
            }
            return getUIFModelAndView(mainForm);
        }
        //adding or updating  older barcode to lost barcode table

        boolean isBarcodeExistInOLE=false;

        if (oldBarcode != null && !oldBarcode.equalsIgnoreCase("")) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.OlePatron.PATRON_LOST_BARCODE_FLD, newOlePatronDocument.getBarcode());
            List<OlePatronLostBarcode> olePatronLostBarcodes = (List<OlePatronLostBarcode>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronLostBarcode.class, map);
            map.clear();
            map.put("barcode",oldBarcode);
            List<OlePatronDocument> patronDocuments = (List<OlePatronDocument>) KRADServiceLocator.getBusinessObjectService().findMatching(OlePatronDocument.class, map);
            if (olePatronLostBarcodes.size() > 0 || patronDocuments.size()>0) {
                 isBarcodeExistInOLE=true;
            }
            if(!isBarcodeExistInOLE){
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS,OLEConstants.OlePatron.PATRON_BARCODE_DOES_NOT_EXIST_REINSTATE,new String[]{oldBarcode});
            }
        }

        for (OlePatronLostBarcode olePatronLostBarcode : newOlePatronDocument.getLostBarcodes()) {
            if (olePatronLostBarcode.isActive()) {
                newOlePatronDocument.setBarcode(olePatronLostBarcode.getInvalidOrLostBarcodeNumber());
                olePatronLostBarcode.setDescription(newOlePatronDocument.getLostDescription());
                olePatronLostBarcode.setStatus(newOlePatronDocument.getLostStatus());
            }
            if(olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(oldBarcode)){
                olePatronLostBarcode.setDescription("LOST");
                olePatronLostBarcode.setStatus("LOST");
                olePatronLostBarcode.setActive(false);
            }
        }
        boolean isOldBarcodeExist=false;
        for (OlePatronLostBarcode olePatronLostBarcode : newOlePatronDocument.getLostBarcodes()) {
            if (olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equalsIgnoreCase(oldBarcode)) {
                isOldBarcodeExist=true;
            }
        }
        if (oldBarcode != null && !oldBarcode.equalsIgnoreCase("")) {
            if (!isOldBarcodeExist && isBarcodeExistInOLE) {
                OlePatronLostBarcode olePatronLostBarcode = new OlePatronLostBarcode();
                olePatronLostBarcode.setInvalidOrLostBarcodeNumber(oldBarcode);
                olePatronLostBarcode.setInvalidOrLostBarcodeEffDate(new java.sql.Date(getDateTimeService().getCurrentDate().getTime()));
                olePatronLostBarcode.setRevertBarcode(true);
                olePatronLostBarcode.setDescription("LOST");
                olePatronLostBarcode.setStatus("LOST");
                newOlePatronDocument.getLostBarcodes().add(olePatronLostBarcode);
            }
        }
        newOlePatronDocument.setBarcodeEditable(false);
        newOlePatronDocument.setBarcodeChanged(true);
        newOlePatronDocument.setReinstated(true);
        newOlePatronDocument.setSkipBarcodeValidation(true);
        newOlePatronDocument.setReinstateBarcode(false);
        return getUIFModelAndView(mainForm);
    }

    @RequestMapping(params = "methodToCall=refreshInvalidOrLostBarcodeSection")
    public ModelAndView refreshInvalidOrLostBarcodeSection(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                                           HttpServletRequest request, HttpServletResponse response) {

        LOG.debug(" Inside route method of patron maintenance controller ");
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        for (OlePatronLostBarcode lostBarcode : newOlePatronDocument.getLostBarcodes()) {
            if (lostBarcode.getId() != null) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(OLEConstants.OlePatron.PATRON_ID, lostBarcode.getId());
                OlePatronLostBarcode olePatronLostBarcode = (OlePatronLostBarcode) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronLostBarcode.class, map);
                if (olePatronLostBarcode != null) {
                    lostBarcode.setActive(olePatronLostBarcode.isActive());
                } else {
                    lostBarcode.setActive(false);
                }
            }
            if(lostBarcode.getId() == null) {
                lostBarcode.setActive(false);
            }
        }
        return navigate(form, result, request, response);
    }

    @RequestMapping(params = "methodToCall=uploadImage")
    public ModelAndView uploadImage(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        MultipartFile multipartFile = mainForm.getAttachmentFile();
        if (multipartFile == null) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_PHOTOGRAPH_WITHOUT_FILE);
            return getUIFModelAndView(mainForm);
        }
        String fileName = multipartFile.getOriginalFilename();
        if (validateFile(multipartFile.getOriginalFilename())) {
            try {
                byte[] fileContent = multipartFile.getBytes();
                BufferedImage patronImage = ImageIO.read(new ByteArrayInputStream(fileContent));
                if (patronImage.getWidth() >= 100 || patronImage.getHeight() >= 100) {
                    BufferedImage resizedImage = new BufferedImage(100, 100, 1);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(patronImage, 0, 0, 100, 100, null);
                    g.dispose();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImage, "jpg", baos);
                    byte[] res = baos.toByteArray();
                    imageInByte = baos.toByteArray();
                    newOlePatronDocument.setPatronPhotograph(res);
                } else {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_PHOTOGRAPH_SIZE);
                    return getUIFModelAndView(mainForm);
                }
            } catch (Exception ex) {
                LOG.error("Exception while uploading image", ex);
            }
        } else if (fileName != null && fileName.isEmpty()) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_PHOTOGRAPH_WITHOUT_FILE);
            return getUIFModelAndView(mainForm);
        } else {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.GLOBAL_ERRORS, OLEConstants.OlePatron.ERROR_PATRON_PHOTOGRAPH_FORMAT);
            return getUIFModelAndView(mainForm);
        }
        return getUIFModelAndView(mainForm);
    }

    @RequestMapping(params = "methodToCall=deleteImage")
    public ModelAndView deleteImage(@ModelAttribute("KualiForm") DocumentFormBase form, BindingResult result,
                                    HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.setPatronPhotograph(null);
        return getUIFModelAndView(mainForm);
    }

    /**
     * This method validate the image file type.
     *
     * @param inputFile
     * @return boolean
     */
    public boolean validateFile(String inputFile) {
        return (inputFile.contains(".jpg") || inputFile.contains(".png") || inputFile.contains(".jpeg") || inputFile.contains(".gif") ? true : false);
    }

    @RequestMapping(params = "methodToCall=getImage")
    public ModelAndView getImage(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        try {
            String inquiry = "false";
            if (request.getParameter("patronInquiryFlag") != null) {
                inquiry = (String) request.getParameter("patronInquiryFlag");
            }

            if (inquiry.equalsIgnoreCase("false")) {


                String patronId = request.getParameter(OLEConstants.OlePatron.PATRON_ID);
                if (patronId != null && !patronId.equals("")) {
                    Map patronMap = new HashMap();
                    patronMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
                    OlePatronDocument olePatronDocument = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                    byte[] patronPhoto = olePatronDocument.getPatronPhotograph();
                    if (patronPhoto != null) {
                        if (imageInByte == null || !patronPhoto.equals(imageInByte)) {
                            response.setContentType("image/jpg");
                            response.getOutputStream().write(patronPhoto);
                        } else {
                            response.setContentType("image/jpg");
                            response.getOutputStream().write(imageInByte);
                        }
                    } else {
                        response.setContentType("image/jpg");
                        response.getOutputStream().write(imageInByte);
                    }
                } else {
                    if (imageInByte != null) {
                        response.setContentType("image/jpg");
                        response.getOutputStream().write(imageInByte);
                    }
                }
            } else {
                Map patronMap = new HashMap();
                String patronId = request.getParameter(OLEConstants.OlePatron.PATRON_ID);
                if (patronId != null && !patronId.equals("")) {
                    patronMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
                    OlePatronDocument olePatronDocument = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                    byte[] patronPhoto = olePatronDocument.getPatronPhotograph();
                    if (patronPhoto != null) {

                        response.setContentType("image/jpg");
                        response.getOutputStream().write(patronPhoto);

                    }
                }
            }

        } catch (Exception ex) {
            LOG.error("Exception while getting image", ex);
        }
        return null;
    }

    @RequestMapping(params = "methodToCall=getImageForLoan")
    public ModelAndView getImageForLoan(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        try {
            String patronId = request.getParameter(OLEConstants.OlePatron.PATRON_ID);
            if (patronId != null && !patronId.equals("")) {
                Map patronMap = new HashMap();
                patronMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
                OlePatronDocument olePatronDocument = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, patronMap);
                byte[] patronPhoto = olePatronDocument.getPatronPhotograph();
                if (patronPhoto != null) {
                    response.setContentType("image/jpg");
                    response.getOutputStream().write(patronPhoto);
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while getting image for loan", ex);
        }
        return null;
    }

    @RequestMapping(params = "methodToCall=refreshProgGroup")
    public ModelAndView refreshProgGroup(@ModelAttribute("KualiForm") MaintenanceDocumentForm form, BindingResult result,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView;
        MaintenanceDocumentForm mainForm = (MaintenanceDocumentForm) form;
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        if (newOlePatronDocument.getOleLoanDocuments() != null) {
            for (OleLoanDocument loanDocument : newOlePatronDocument.getOleLoanDocuments()) {
                if(loanDocument.isMissingPieceFlag() && loanDocument.getMissingPiecesCount()==null || (loanDocument.getMissingPiecesCount()!=null && loanDocument.getMissingPiecesCount().equalsIgnoreCase(""))){
                    loanDocument.setNoOfMissingPiecesEditable(true);
                } else {
                    loanDocument.setNoOfMissingPiecesEditable(false);
                }

                if(loanDocument.isMissingPieceFlag() && loanDocument.getItemNumberOfPieces()==null || (loanDocument.getItemNumberOfPieces()!=null && loanDocument.getItemNumberOfPieces().toString().equalsIgnoreCase(""))){
                    loanDocument.setNoOfPiecesEditable(true);
                } else {
                    loanDocument.setNoOfPiecesEditable(false);
                }
            }
        }

        return getUIFModelAndView(form);
    }

    /**
     * This method initiate LoanProcessor.
     *
     * @return LoanProcessor
     */
    private LoanProcessor getLoanProcessor() {
        if (loanProcessor == null) {
            loanProcessor = new LoanProcessor();
        }
        return loanProcessor;
    }

    private void prepareOleAddressForSave(OlePatronDocument newOlePatronDocument) {
        if (newOlePatronDocument.getOleEntityAddressBo() != null) {
            newOlePatronDocument.getOleAddresses().clear();
            int i=0;
            for (OleEntityAddressBo oleEntityAddressBo : newOlePatronDocument.getOleEntityAddressBo()) {
                if (oleEntityAddressBo.getEntityAddressBo().getId() == null) {
                    if (newOlePatronDocument.getEntity() != null && newOlePatronDocument.getEntity().getEntityTypeContactInfos() != null && newOlePatronDocument.getEntity().getEntityTypeContactInfos().size() > 0) {
                        EntityTypeContactInfoBo entityTypeContactInfo = (EntityTypeContactInfoBo) newOlePatronDocument.getEntity().getEntityTypeContactInfos().get(0);
                        if (entityTypeContactInfo.getAddresses() != null && entityTypeContactInfo.getAddresses().size() > 0 && entityTypeContactInfo.getAddresses().size()>i) {
                            if(oleEntityAddressBo.getEntityAddressBo()!=null){
                               entityTypeContactInfo.getAddresses().get(i).setDefaultValue(oleEntityAddressBo.getEntityAddressBo().isDefaultValue());
                                entityTypeContactInfo.getAddresses().get(i).setActive(oleEntityAddressBo.getEntityAddressBo().isActive());
                            }
                            oleEntityAddressBo.setEntityAddressBo(entityTypeContactInfo.getAddresses().get(i));
                        }
                    }
                }
                i++;
            }
            for (OleEntityAddressBo oleEntityAddressBo : newOlePatronDocument.getOleEntityAddressBo()) {
                if (oleEntityAddressBo.getEntityAddressBo() != null && oleEntityAddressBo.getEntityAddressBo().getId() != null) {

                    oleEntityAddressBo.getOleAddressBo().setOleAddressId(KRADServiceLocator.getSequenceAccessorService().getNextAvailableSequenceNumber("OLE_DLVR_ADD_S").toString());

                    oleEntityAddressBo.getOleAddressBo().setId(oleEntityAddressBo.getEntityAddressBo().getId());
                    oleEntityAddressBo.getOleAddressBo().setOlePatronId(newOlePatronDocument.getOlePatronId());
                    oleEntityAddressBo.getOleAddressBo().setVersionNumber(null);
                    oleEntityAddressBo.getOleAddressBo().setObjectId(null);
                    oleEntityAddressBo.getOleAddressBo().setAddressSource(oleEntityAddressBo.getOleAddressBo().getAddressSource());
                    oleEntityAddressBo.getOleAddressBo().setAddressValidFrom(oleEntityAddressBo.getOleAddressBo().getAddressValidFrom());
                    oleEntityAddressBo.getOleAddressBo().setAddressValidTo(oleEntityAddressBo.getOleAddressBo().getAddressValidTo());
                    oleEntityAddressBo.getOleAddressBo().setAddressVerified(oleEntityAddressBo.getOleAddressBo().isAddressVerified());
                }
                OleAddressBo oleAddressBo = (OleAddressBo) ObjectUtils.deepCopy(oleEntityAddressBo.getOleAddressBo());
                EntityAddressBo entityAddressBo = (EntityAddressBo) ObjectUtils.deepCopy(oleEntityAddressBo.getEntityAddressBo());
                oleEntityAddressBo.getOleAddressBo().setEntityAddress(entityAddressBo);
                newOlePatronDocument.getOleAddresses().add(oleAddressBo);
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteAddress")
    public ModelAndView deleteAddress(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedOleEntityAddressBo().add(newOlePatronDocument.getOleEntityAddressBo().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deletePhoneNumber")
    public ModelAndView deletePhoneNumber(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedPhones().add(newOlePatronDocument.getPhones().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);

    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteEmailAddress")
    public ModelAndView deleteEmailAddress(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedEmails().add(newOlePatronDocument.getEmails().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);

    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteProxyPatron")
    public ModelAndView deleteProxyPatron(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedOleProxyPatronDocuments().add(newOlePatronDocument.getOleProxyPatronDocuments().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);

    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteNotes")
    public ModelAndView deleteNotes(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedNotes().add(newOlePatronDocument.getNotes().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);

    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteOlePatronLocalIds")
    public ModelAndView deleteOlePatronLocalIds(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedOlePatronLocalIds().add(newOlePatronDocument.getOlePatronLocalIds().get(Integer.parseInt(selectedLineIndex)));
        return deleteLine(uifForm, result, request, response);

    }



    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deletePatronAffiliations")
    public ModelAndView deletePatronAffiliations(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                          HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        String selectedLineIndex = form.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        newOlePatronDocument.getDeletedPatronAffiliations().add(newOlePatronDocument.getPatronAffiliations().get(Integer.parseInt(selectedLineIndex)));
        newOlePatronDocument.getDeletedEmployments().addAll(newOlePatronDocument.getPatronAffiliations().get(Integer.parseInt(selectedLineIndex)).getEmployments());

        return deleteLine(uifForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteEmploymentBo")
    public ModelAndView deleteEmploymentBo(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                                 HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Initialized addLine method");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        Map<String,String> actionParameters = form.getActionParameters();
        String subCollectionIndex = actionParameters.get(UifParameters.SELECTED_LINE_INDEX);
        String mainCollectionIndex= StringUtils.substringBefore(StringUtils.substringAfter(actionParameters.get(UifParameters.SELLECTED_COLLECTION_PATH),"["),"]");
        MaintenanceDocument document = (MaintenanceDocument) form.getDocument();
        OlePatronDocument newOlePatronDocument = (OlePatronDocument) document.getNewMaintainableObject().getDataObject();
        EntityEmploymentBo entityEmploymentBo=newOlePatronDocument.getPatronAffiliations().get(Integer.parseInt(mainCollectionIndex)).getEmployments().get(Integer.parseInt(subCollectionIndex));
        if(entityEmploymentBo!=null){
            newOlePatronDocument.getDeletedEmployments().add(entityEmploymentBo);
            newOlePatronDocument.getPatronAffiliations().get(Integer.parseInt(mainCollectionIndex)).getEmployments().remove(Integer.parseInt(subCollectionIndex));
            return getUIFModelAndView(uifForm);
        }else{
            newOlePatronDocument.getDeletedEmployments().add(entityEmploymentBo);
            return deleteLine(uifForm, result, request, response);
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=showPatronLoanedItem")
    public ModelAndView showPatronLoanedItem(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : showing Patron Loaned Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        try {
            olePatronDocument.setOleLoanDocuments(getLoanProcessor().getPatronLoanedItemBySolr(olePatronDocument.getOlePatronId()));
        } catch (Exception e) {
            LOG.error("While fetching loan records error occured" + e);
        }
        olePatronDocument.setShowLoanedRecords(true);
        return getUIFModelAndView(form);
    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=hidePatronLoanedItem")
    public ModelAndView hidePatronLoanedItem(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : Hiding Patron Loaned Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        //olePatronDocument.setOleLoanDocuments(new ArrayList<OleLoanDocument>());
        olePatronDocument.setShowLoanedRecords(false);
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=showPatronRequestedRecords")
    public ModelAndView showPatronRequestedRecords(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : showing Patron Requested Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        try {
            OleDeliverRequestDocumentHelperServiceImpl requestService = new OleDeliverRequestDocumentHelperServiceImpl();
            List<OleDeliverRequestBo> oleDeliverRequestBoList = olePatronDocument.getOleDeliverRequestBos();
            if (oleDeliverRequestBoList.size() > 0) {
                for (int i = 0; i < oleDeliverRequestBoList.size(); i++) {
                    OleItemSearch oleItemSearch = requestService.getItemDetailsForPatron(oleDeliverRequestBoList.get(i).getItemUuid());
                    if (oleItemSearch != null && oleItemSearch.getItemBarCode() != null) {
                        oleDeliverRequestBoList.get(i).setTitle(oleItemSearch.getTitle());
                        oleDeliverRequestBoList.get(i).setCallNumber(oleItemSearch.getCallNumber());
                    }
                }
            }
            olePatronDocument.setOleDeliverRequestBos(loanProcessor.getPatronRequestRecords(olePatronDocument.getOlePatronId()));
        } catch (Exception e) {
            LOG.error("While fetching Patron Requested Records error occured" + e);
        }
        olePatronDocument.setShowRequestedItems(true);
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=hidePatronRequestedRecords")
    public ModelAndView hidePatronRequestedRecords(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : Hiding Patron Loaned Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        olePatronDocument.setOleDeliverRequestBos(new ArrayList<OleDeliverRequestBo>());
        olePatronDocument.setShowRequestedItems(false);
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=showTemporaryCirculationHistoryRecords")
    public ModelAndView showTemporaryCirculationHistoryRecords(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : showing Patron TemporaryCirculationHistory Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        try {
            olePatronDocument.setOleTemporaryCirculationHistoryRecords(loanProcessor.getPatronTemporaryCirculationHistoryRecords(olePatronDocument.getOlePatronId()));
        } catch (Exception e) {
            LOG.error("While fetching Patron TemporaryCirculationHistory Records error occured" + e);
        }

        olePatronDocument.setShowTemporaryCirculationHistoryRecords(true);
        return getUIFModelAndView(form);
    }
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=hideTemporaryCirculationHistoryRecords")
    public ModelAndView hideTemporaryCirculationHistoryRecords(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : Hiding Patron Loaned Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        olePatronDocument.setOleTemporaryCirculationHistoryRecords(new ArrayList<OleTemporaryCirculationHistory>());
        olePatronDocument.setShowTemporaryCirculationHistoryRecords(false);
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=refreshLoanedItemSection")
    public ModelAndView refreshLoanedItemSection(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("Patron View : Hiding Patron Loaned Records");
        MaintenanceDocumentForm form = (MaintenanceDocumentForm) uifForm;
        OlePatronDocument olePatronDocument=(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=refreshProxyPatron")
    public ModelAndView refreshProxyPatron(@ModelAttribute("KualiForm") UifFormBase uifForm, BindingResult result,
                                           HttpServletRequest request, HttpServletResponse response) throws Exception {

        if ((uifForm instanceof OlePatronMaintenanceDocumentForm)
        		&& !((OlePatronMaintenanceDocumentForm) uifForm).isFilterProxySection()) {
        	
        	OlePatronMaintenanceDocumentForm form = (OlePatronMaintenanceDocumentForm) uifForm;
            OlePatronDocument olePatron =
            		(OlePatronDocument)form.getDocument().getNewMaintainableObject().getDataObject();
        	
            List<OleProxyPatronDocument> oleProxyPatronDocuments = olePatron.getOleProxyPatronDocuments();
            List<OleProxyPatronDocument> proxyPatronDocumentList = new ArrayList<OleProxyPatronDocument>();
            List<OlePatronDocument> olePatronDocuments = new ArrayList<OlePatronDocument>();
			ProcessLogger.trace("patron:proxy:begin:"
					+ oleProxyPatronDocuments.size());
            if (oleProxyPatronDocuments.size() > 0) {
                for (Iterator<OleProxyPatronDocument> proxyPatronIterator = oleProxyPatronDocuments.iterator(); proxyPatronIterator.hasNext(); ) {
                    OleProxyPatronDocument oleProxyPatronDocument = (OleProxyPatronDocument) proxyPatronIterator.next();
                    Map map = new HashMap();
                    map.put(OLEConstants.OlePatron.PATRON_ID, oleProxyPatronDocument.getProxyPatronId());
        			ProcessLogger.trace("patron:proxy:"
        					+ oleProxyPatronDocument.getProxyPatronId());
                    OlePatronDocument olePatronDocument = (OlePatronDocument) getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, map);
                   // if (olePatronDocument.isActiveIndicator()) {
                        oleProxyPatronDocument.setProxyPatronBarcode(olePatronDocument.getBarcode());
                        oleProxyPatronDocument.setProxyPatronFirstName(olePatronDocument.getEntity().getNames().get(0).getFirstName());
                        oleProxyPatronDocument.setProxyPatronLastName(olePatronDocument.getEntity().getNames().get(0).getLastName());
                        proxyPatronDocumentList.add(oleProxyPatronDocument);
                   // }
                }
                olePatron.setOleProxyPatronDocuments(proxyPatronDocumentList);
                ProcessLogger.trace("patron:proxy:end");
            }
        }
        
        return super.refresh(uifForm, result, request, response);
    }

}
