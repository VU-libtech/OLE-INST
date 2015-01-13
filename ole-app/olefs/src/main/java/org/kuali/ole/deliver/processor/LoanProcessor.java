package org.kuali.ole.deliver.processor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.DataCarrierService;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.OLEParameterConstants;
import org.kuali.ole.deliver.batch.OleDeliverBatchServiceImpl;
import org.kuali.ole.deliver.batch.OleMailer;
import org.kuali.ole.deliver.batch.OleNoticeBo;
import org.kuali.ole.deliver.bo.*;
import org.kuali.ole.deliver.calendar.bo.OleCalendar;
import org.kuali.ole.deliver.calendar.service.OleCalendarService;
import org.kuali.ole.deliver.calendar.service.impl.OleCalendarServiceImpl;
import org.kuali.ole.deliver.form.OleLoanForm;
import org.kuali.ole.deliver.service.OLEDeliverNoticeHelperService;
import org.kuali.ole.deliver.service.OleDeliverRequestDocumentHelperServiceImpl;
import org.kuali.ole.deliver.service.OleLoanDocumentDaoOjb;
import org.kuali.ole.describe.bo.OleInstanceItemType;
import org.kuali.ole.describe.bo.OleItemAvailableStatus;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.describe.bo.OleLocationLevel;
import org.kuali.ole.describe.keyvalue.LocationValuesBuilder;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.*;
import org.kuali.ole.docstore.common.document.Item;
import org.kuali.ole.docstore.common.document.content.bib.marc.BibMarcRecord;
import org.kuali.ole.docstore.common.document.content.enums.DocType;
import org.kuali.ole.docstore.common.document.content.instance.xstream.HoldingOlemlRecordProcessor;
import org.kuali.ole.docstore.common.document.content.instance.xstream.ItemOlemlRecordProcessor;
import org.kuali.ole.docstore.common.search.*;
import org.kuali.ole.docstore.model.xmlpojo.work.bib.marc.DataField;
import org.kuali.ole.docstore.model.xmlpojo.work.bib.marc.SubField;
import org.kuali.ole.docstore.common.document.content.instance.AccessInformation;
import org.kuali.ole.docstore.common.document.content.instance.CallNumber;
import org.kuali.ole.docstore.common.document.content.instance.CheckInLocation;
import org.kuali.ole.docstore.common.document.content.instance.ItemStatus;
import org.kuali.ole.docstore.common.document.content.instance.ItemType;
import org.kuali.ole.docstore.common.document.content.instance.Location;
import org.kuali.ole.docstore.common.document.content.instance.LocationLevel;
import org.kuali.ole.docstore.common.document.content.instance.Note;
import org.kuali.ole.docstore.common.document.content.instance.NumberOfCirculations;
import org.kuali.ole.docstore.common.document.content.instance.OleHoldings;
import org.kuali.ole.docstore.common.document.content.instance.ShelvingScheme;
import org.kuali.ole.ingest.pojo.MatchBo;
import org.kuali.ole.pojo.bib.BibliographicRecord;
import org.kuali.ole.service.OleCirculationPolicyService;
import org.kuali.ole.service.OleCirculationPolicyServiceImpl;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.util.DocstoreUtil;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.mail.EmailBody;
import org.kuali.rice.core.api.mail.EmailFrom;
import org.kuali.rice.core.api.mail.EmailSubject;
import org.kuali.rice.core.api.mail.EmailTo;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.identity.address.EntityAddressBo;
import org.kuali.rice.kim.impl.identity.email.EntityEmailBo;
import org.kuali.rice.kim.impl.identity.entity.EntityBo;
import org.kuali.rice.kim.impl.identity.name.EntityNameBo;
import org.kuali.rice.kim.impl.identity.phone.EntityPhoneBo;
import org.kuali.rice.kim.impl.identity.type.EntityTypeContactInfoBo;
import org.kuali.rice.kim.impl.role.RoleServiceImpl;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krms.api.KrmsApiServiceLocator;
import org.kuali.rice.krms.api.engine.*;
import org.kuali.rice.krms.api.repository.agenda.AgendaDefinition;
import org.kuali.rice.krms.api.repository.context.ContextDefinition;
import org.kuali.rice.krms.impl.repository.KrmsRepositoryServiceLocator;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Loan Processor  class acts between the controller and the service layers in delegating the appropriate service
 * to the functions called from the controller.
 */
public class LoanProcessor {
    private static final Logger LOG = Logger.getLogger(LoanProcessor.class);

    private BusinessObjectService businessObjectService;
    private DocstoreUtil docstoreUtil;
    private OleCirculationPolicyService oleCirculationPolicyService;
    private OleDeliverRequestDocumentHelperServiceImpl oleDeliverRequestDocumentHelperService;
    private String circDeskId;
    private static final String NAMESPACE_CODE_SELECTOR = "namespaceCode";
    private static final String NAME_SELECTOR = "name";
    private String lostDescription;
    private String lostStatus;
    private String lostPatronId;
    private OleLoanDocumentDaoOjb oleLoanDocumentDaoOjb;
    private OLEDeliverNoticeHelperService oleDeliverNoticeHelperService;
    private static Map<String, String> locationName = new HashMap<>();

    private DocstoreClientLocator docstoreClientLocator;

    private ItemOlemlRecordProcessor itemOlemlRecordProcessor;

    private  DataCarrierService dataCarrierService;

    private String itemStatus;

    public DataCarrierService getDataCarrierService() {
        if(dataCarrierService == null){
            dataCarrierService = SpringContext.getBean(DataCarrierService.class);
        }
        return dataCarrierService;
    }

    public DocstoreUtil getDocstoreUtil() {
        if(docstoreUtil == null){
            docstoreUtil = new DocstoreUtil();
        }
        return docstoreUtil;
    }

    public void setDataCarrierService(DataCarrierService dataCarrierService) {
        this.dataCarrierService = dataCarrierService;
    }

    public DocstoreClientLocator getDocstoreClientLocator() {

        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);

        }
        return docstoreClientLocator;
    }


    public ItemOlemlRecordProcessor getItemOlemlRecordProcessor() {
        if(itemOlemlRecordProcessor == null){
            itemOlemlRecordProcessor = SpringContext.getBean(ItemOlemlRecordProcessor.class);
        }
        return itemOlemlRecordProcessor;
    }

    public OleDeliverRequestDocumentHelperServiceImpl getOleDeliverRequestDocumentHelperService() {
        if (oleDeliverRequestDocumentHelperService == null) {
            oleDeliverRequestDocumentHelperService = SpringContext.getBean(OleDeliverRequestDocumentHelperServiceImpl.class);
        }
        return oleDeliverRequestDocumentHelperService;
    }


    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setOleCirculationPolicyService(OleCirculationPolicyService oleCirculationPolicyService) {
        this.oleCirculationPolicyService = oleCirculationPolicyService;
    }

    public void setOleDeliverRequestDocumentHelperService(OleDeliverRequestDocumentHelperServiceImpl oleDeliverRequestDocumentHelperService) {
        this.oleDeliverRequestDocumentHelperService = oleDeliverRequestDocumentHelperService;
    }

    public void setDocstoreClientLocator(DocstoreClientLocator docstoreClientLocator) {
        this.docstoreClientLocator = docstoreClientLocator;
    }

    public void setItemOlemlRecordProcessor(ItemOlemlRecordProcessor itemOlemlRecordProcessor) {
        this.itemOlemlRecordProcessor = itemOlemlRecordProcessor;
    }

    public String getLostDescription() {
        return lostDescription;
    }

    public void setLostDescription(String lostDescription) {
        this.lostDescription = lostDescription;
    }

    public String getLostStatus() {
        return lostStatus;
    }

    public void setLostStatus(String lostStatus) {
        this.lostStatus = lostStatus;
    }

    public String getLostPatronId() {
        return lostPatronId;
    }

    public void setLostPatronId(String lostPatronId) {
        this.lostPatronId = lostPatronId;
    }

    /**
     * Gets the businessObjectService attribute.
     *
     * @return Returns the businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    /**
     * Gets the oleCirculationPolicyService attribute.
     *
     * @return Returns the oleCirculationPolicyService
     */
    public OleCirculationPolicyService getOleCirculationPolicyService() {
        if (null == oleCirculationPolicyService) {
            oleCirculationPolicyService = SpringContext.getBean(OleCirculationPolicyServiceImpl.class);
        }
        return oleCirculationPolicyService;
    }


    /**
     * Validates the patron barcode for general blocks and address verified.
     *
     * @param oleLoanDocument
     * @return
     * @throws Exception
     */
    private boolean checkAddressVerifiedAndBlocks(OleLoanDocument oleLoanDocument) throws Exception {
        boolean isAddressVerified = false;
        try {
            isAddressVerified = (boolean) getOleCirculationPolicyService().isAddressVerified(oleLoanDocument.getOlePatron(),oleLoanDocument.getPatronId());
            if (!isAddressVerified || oleLoanDocument.getOlePatron().isGeneralBlock()) {
                oleLoanDocument.setAddressVerified(true);
            }
        } catch (Exception e) {
            LOG.error("Exception while checking address verified & blocks", e);
            throw e;
        }
        return isAddressVerified;
    }

    /**
     * Retrieve information about patron name,borrorwer type and patron loaned items
     *
     * @param barcode
     * @return OleLoanDocument
     */
    public OleLoanDocument getLoanDocument(String barcode, String realPtrnBarcode, boolean selfCheckOut,boolean isProxyToRealPatron) throws Exception {
        LOG.debug("Inside the getLoanDocument method");

        OleLoanDocument oleLoanDocument = new OleLoanDocument();
        String patronInactiveMessage=OLEConstants.PATRON_INVALID_BARCODE_MESSAGE;
        if (barcode != null && checkLostPatronBarcode(barcode)) {
            //oleLoanDocument.setErrorMessage(OLEConstants.H4 + OLEConstants.BARCODE_LOST_OR_STOLEN + OLEConstants.H4_CLOSE);
            String url="<a target=\"_blank\" href="+OLEConstants.ASSIGN_EDIT_PATRON_ID + getLostPatronId() + OLEConstants.ASSIGN_PATRON_MAINTENANCE_EDIT+">"+getLostPatronId()+"</a>";
            String patronUrl=OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + "Patron Details : " + OLEConstants.TEXT_BOLD_TAG_CLOSE +url+OLEConstants.H5_CLOSE + OLEConstants.H5;
            oleLoanDocument.setErrorMessage(patronInactiveMessage+patronUrl+OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + OLEConstants.PATRON_STATUS_LABEL + OLEConstants.TEXT_BOLD_TAG_CLOSE + (getLostStatus()!=null?getLostStatus():"") + OLEConstants.H5_CLOSE + OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + OLEConstants.PATRON_DESCRIPTION_LABEL + OLEConstants.TEXT_BOLD_TAG_CLOSE + (getLostDescription()!=null?getLostDescription():"") + OLEConstants.H5_CLOSE);
            oleLoanDocument.setBlockLoan(true);
            oleLoanDocument.setLostPatron(true);
            return oleLoanDocument;
        } else if (realPtrnBarcode != null && checkLostPatronBarcode(realPtrnBarcode)) {
            //oleLoanDocument.setErrorMessage(OLEConstants.H4 + OLEConstants.BARCODE_LOST_OR_STOLEN + OLEConstants.H4_CLOSE);
            String url="<a target=\"_blank\" href="+OLEConstants.ASSIGN_EDIT_PATRON_ID + getLostPatronId() + OLEConstants.ASSIGN_PATRON_MAINTENANCE_EDIT+">"+getLostPatronId()+"</a>";
            String patronUrl=OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + "Patron Details : " + OLEConstants.TEXT_BOLD_TAG_CLOSE +url+OLEConstants.H5_CLOSE + OLEConstants.H5;
            oleLoanDocument.setErrorMessage(patronInactiveMessage+patronUrl+OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + OLEConstants.PATRON_STATUS_LABEL + OLEConstants.TEXT_BOLD_TAG_CLOSE + (getLostStatus()!=null?getLostStatus():"") + OLEConstants.H5_CLOSE + OLEConstants.H5 + OLEConstants.TEXT_BOLD_TAG_START + OLEConstants.PATRON_DESCRIPTION_LABEL + OLEConstants.TEXT_BOLD_TAG_CLOSE + (getLostDescription()!=null?getLostDescription():"") + OLEConstants.H5_CLOSE);
            oleLoanDocument.setBlockLoan(true);
            oleLoanDocument.setLostPatron(true);
            return oleLoanDocument;
        }
        oleLoanDocument = getPatronBarcodeRecord(barcode);
        oleLoanDocument.setRealPatronBarcode(realPtrnBarcode);
        Date expirationDate = oleLoanDocument.getOlePatron().getExpirationDate();
        List<OlePatronDocument> proxyPatron = null;
        if (!selfCheckOut && !isProxyToRealPatron) {
            proxyPatron = (List<OlePatronDocument>) getOleCirculationPolicyService().isProxyPatron(oleLoanDocument.getOlePatron());
        }
        String agendaName = OLEConstants.CHECK_OUT_GEN_AGENDA_NM;
        OleLoanDocument oleLoanDocumentForRealPatron = new OleLoanDocument();
        String digitRoutine = getParameter(OLEConstants.PATRON_DIGIT_ROUTINE);
        String pattern = getParameter(OLEConstants.PATRON_DIGIT_ROUTINE_PATTERN);
        HashMap<String, Object> termValues = new HashMap<String, Object>();
        // Check for Proxy Patron.
        if (!isProxyToRealPatron) {
            if (proxyPatron != null && proxyPatron.size() > 0 && !isProxyToRealPatron) {
                // Display real patron in pop up, if it is more than one. Or continue if the proxy patron as single real patron.
                if (realPtrnBarcode == null && proxyPatron.size() > 0) {
                    oleLoanDocument.setRealPatron(proxyPatron);
                    return oleLoanDocument;
                }
                oleLoanDocumentForRealPatron = getPatronBarcodeRecord(realPtrnBarcode);      // retrieves Loan Document for real patron.
                oleLoanDocument.setProxyPatronId(oleLoanDocumentForRealPatron.getPatronId());
                oleLoanDocument.setRealPatronName(getPatronName(oleLoanDocumentForRealPatron.getPatronId()));
                oleLoanDocument.setRealPatronType(oleLoanDocumentForRealPatron.getBorrowerTypeCode());
                boolean isAddressVerifiedReal = checkAddressVerifiedAndBlocks(oleLoanDocumentForRealPatron); // Checks isAddressVerified for real patron.
                termValues.put(OLEConstants.GENERAL_BLOCK, oleLoanDocumentForRealPatron.getOlePatron().isGeneralBlock() ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.ADDR_VERIFIED, isAddressVerifiedReal ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.EXPIR_DATE, oleLoanDocumentForRealPatron.getOlePatron().getExpirationDate());  // Checks Expiration Date for real patron.
                termValues.put(OLEConstants.PROXY_EXPIR_DATE, oleLoanDocumentForRealPatron.getOlePatron().getExpirationDate());                                 // Checks Expiration Date for proxy patron.
                termValues.put(OLEConstants.PATRON_BAR, realPtrnBarcode);  // Checks Barcode for real patron.
                termValues.put(OLEConstants.IS_PROXY_PATRON, OLEConstants.TRUE);
                termValues.put(OLEConstants.IS_ACTIVE_PATRON, oleLoanDocument.getOlePatron().isActiveIndicator() ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.IS_ACTIVE_PROXY_PATRON, oleLoanDocumentForRealPatron.getOlePatron().isActiveIndicator() ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.PROXY_PATRON_ACTIVATION_DATE, oleLoanDocumentForRealPatron.getOlePatron().getActivationDate());
                termValues.put(OLEConstants.PATRON_ACTIVATION_DATE, oleLoanDocument.getOlePatron().getActivationDate());
                termValues.put(OLEConstants.PROXY_PATRON_ACTIVATION_DATE_STRING, oleLoanDocumentForRealPatron.getOlePatron().getActivationDate() != null ? oleLoanDocumentForRealPatron.getOlePatron().getActivationDate().toString() : "null");
                termValues.put(OLEConstants.PROXY_PATRON_EXPIRATION_DATE_STRING, oleLoanDocumentForRealPatron.getOlePatron().getExpirationDate() != null ? oleLoanDocumentForRealPatron.getOlePatron().getExpirationDate().toString() : "null");
            } else {
                boolean isAddressVerified = checkAddressVerifiedAndBlocks(oleLoanDocument);
                termValues.put(OLEConstants.GENERAL_BLOCK, oleLoanDocument.getOlePatron().isGeneralBlock() ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.EXPIR_DATE, expirationDate);
                termValues.put(OLEConstants.ADDR_VERIFIED, isAddressVerified ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.PATRON_BAR, barcode);
                termValues.put(OLEConstants.IS_ACTIVE_PATRON, oleLoanDocument.getOlePatron().isActiveIndicator() ? OLEConstants.TRUE : OLEConstants.FALSE);
                termValues.put(OLEConstants.PATRON_ACTIVATION_DATE, oleLoanDocument.getOlePatron().getActivationDate());
            }
        } else {
            OleLoanDocument oleLoanDocumentProxyToReal = new OleLoanDocument();
            oleLoanDocumentProxyToReal=getPatronBarcodeRecord(barcode);
            boolean isAddressVerified = checkAddressVerifiedAndBlocks(oleLoanDocumentProxyToReal);
            termValues.put(OLEConstants.GENERAL_BLOCK, oleLoanDocumentProxyToReal.getOlePatron().isGeneralBlock() ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.EXPIR_DATE, expirationDate);
            termValues.put(OLEConstants.ADDR_VERIFIED, isAddressVerified ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.PATRON_BAR, barcode);
            termValues.put(OLEConstants.IS_ACTIVE_PATRON, oleLoanDocumentProxyToReal.getOlePatron().isActiveIndicator() ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.PATRON_ACTIVATION_DATE, oleLoanDocumentProxyToReal.getOlePatron().getActivationDate());
        }
        termValues.put(OLEConstants.ACTIVATION_DATE_STRING, oleLoanDocument.getOlePatron().getActivationDate() != null ? oleLoanDocument.getOlePatron().getActivationDate().toString() : "null");
        termValues.put(OLEConstants.EXPIRATION_DATE_STRING, oleLoanDocument.getOlePatron().getExpirationDate() != null ? oleLoanDocument.getOlePatron().getExpirationDate().toString() : "null");
        termValues.put(OLEConstants.DIGIT_ROUTINE, digitRoutine);
        termValues.put(OLEConstants.PATTERN, pattern);
        EngineResults engineResults = getEngineResults(agendaName, termValues);
        HashMap<String, String> errorsAndPermission = new HashMap<>();
        errorsAndPermission = (HashMap<String, String>) engineResults.getAttribute(OLEConstants.ERRORS_AND_PERMISSION);
        StringBuffer failures = new StringBuffer();
        oleLoanDocument.getErrorsAndPermission().clear();
        int i = 1;
        if (errorsAndPermission != null) {
            Set<String> errorMessage = errorsAndPermission.keySet();
            if (errorMessage != null && errorMessage.size() > 0) {
                //TODO KRMS Code Cleanup
                for (String errMsg : errorMessage) {
                    if (StringUtils.isNotEmpty(errMsg)) {
                        oleLoanDocument.getErrorsAndPermission().putAll(errorsAndPermission);
                        if (errMsg.contains(OLEConstants.LOST_STOLEN)) {
                            failures.append(OLEConstants.H4 + i++ + ". " + errMsg + OLEConstants.H4_CLOSE);
                            oleLoanDocument.setBlockLoan(true);
                        } else {
                            if (oleLoanDocument.getOlePatron().isGeneralBlock()) {
                                if (errMsg.equalsIgnoreCase(OLEConstants.GENERAL_BLOCK_MESSAGE)) {
                                    failures.append(i++ + ". " + errMsg + " " + OLEConstants.SEMI_COLON + " " + oleLoanDocument.getOlePatron().getGeneralBlockNotes() + OLEConstants.BREAK);
                                } else {
                                    failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
                                }
                            } else {
                                failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
                            }

                        }
                    }
                }
            }
            errorsAndPermission.clear();
        }
        List<String> errorMessage = (List<String>) engineResults.getAttribute(OLEConstants.ERROR_ACTION);
        if (errorMessage != null && errorMessage.size() > 0) {
            for (String errMsg : errorMessage) {
                if (StringUtils.isNotEmpty(errMsg)) {
                    if (errMsg.contains(OLEConstants.LOST_STOLEN)) {
                        failures.append(OLEConstants.H4 + i++ + ". " + errMsg + OLEConstants.H4_CLOSE);
                        oleLoanDocument.setBlockLoan(true);
                    } else {
                        failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
                    }
                }
            }
        }
        if (!failures.toString().isEmpty()) {
            oleLoanDocument.setErrorMessage(failures.toString());
        }
        getDataCarrierService().addData(OLEConstants.ERROR_ACTION, null);
        getDataCarrierService().addData(OLEConstants.ERRORS_AND_PERMISSION, null);
        return oleLoanDocument;
    }


    /**
     * Checks whether the patron barcode is Lost/Stolen.
     *
     * @param barcode
     * @return
     * @throws Exception
     */
    private boolean checkLostPatronBarcode(String barcode) throws Exception {
        LOG.debug("Inside the checkLostPatronBarcode method");
        Map barMap = new HashMap();
        barMap.put("invalidOrLostBarcodeNumber", barcode);
        List<OlePatronLostBarcode> olePatronLostBarcodes = (List<OlePatronLostBarcode>) getBusinessObjectService().findMatching(OlePatronLostBarcode.class, barMap);
        boolean lostInvalidBarcode = false;
        if (olePatronLostBarcodes != null) {
            for (int lostBarcode = 0; lostBarcode < olePatronLostBarcodes.size(); lostBarcode++) {
                OlePatronLostBarcode olePatronLostBarcode = (OlePatronLostBarcode) olePatronLostBarcodes.get(lostBarcode);
                if (olePatronLostBarcode.getInvalidOrLostBarcodeNumber().equals(barcode) && (!olePatronLostBarcode.isActive())) {
                    setLostDescription(olePatronLostBarcode.getDescription());
                    setLostStatus(olePatronLostBarcode.getStatus());
                    lostInvalidBarcode = true;
                    setLostPatronId(olePatronLostBarcode.getOlePatronId());
                    return lostInvalidBarcode;
                }
            }
        }
        return lostInvalidBarcode;
    }

    /**
     * Retrieves Patron preferred address.
     *
     * @param entityTypeContactInfoBo
     * @param loanDocument
     * @return
     * @throws Exception
     */
    private OleLoanDocument getPatronPreferredAddress(EntityTypeContactInfoBo entityTypeContactInfoBo, OleLoanDocument loanDocument) throws Exception {
        LOG.debug("Inside the getPatronPreferredAddress method");
        if (entityTypeContactInfoBo.getAddresses() != null) {
            for (EntityAddressBo entityAddressBo : entityTypeContactInfoBo.getAddresses()) {
                if (entityAddressBo.isDefaultValue()) {
                    String address = "";
                    if (entityAddressBo.getLine1() != null) {
                        if (!entityAddressBo.getLine1().isEmpty()) {
                            address += entityAddressBo.getLine1() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getLine2() != null) {
                        if (!entityAddressBo.getLine2().isEmpty()) {
                            address += entityAddressBo.getLine2() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getLine3() != null) {
                        if (!entityAddressBo.getLine3().isEmpty()) {
                            address += entityAddressBo.getLine3() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getCity() != null) {
                        if (!entityAddressBo.getCity().isEmpty()) {
                            address += entityAddressBo.getCity() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getStateProvinceCode() != null) {
                        if (!entityAddressBo.getStateProvinceCode().isEmpty()) {
                            address += entityAddressBo.getStateProvinceCode() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getCountryCode() != null) {
                        if (!entityAddressBo.getCountryCode().isEmpty()) {
                            address += entityAddressBo.getCountryCode() + OLEConstants.COMMA;
                        }
                    }

                    if (entityAddressBo.getPostalCode() != null) {
                        if (!entityAddressBo.getPostalCode().isEmpty()) {
                            address += entityAddressBo.getPostalCode();
                        }
                    }

                    loanDocument.setPreferredAddress(address);
                    break;
                }
            }
        }

        return loanDocument;
    }

    /**
     * Retrieves Patron home phone number.
     *
     * @param entityTypeContactInfoBo
     * @param loanDocument
     * @return
     * @throws Exception
     */
    private OleLoanDocument getPatronHomePhoneNumber(EntityTypeContactInfoBo entityTypeContactInfoBo, OleLoanDocument loanDocument) throws Exception {
        LOG.debug("Inside the getPatronHomePhoneNumber method");
        if (entityTypeContactInfoBo.getPhoneNumbers() != null) {
            for (EntityPhoneBo entityPhoneBo : entityTypeContactInfoBo.getPhoneNumbers()) {
                if (entityPhoneBo.isDefaultValue()) {
                    loanDocument.setPhoneNumber(entityPhoneBo.getPhoneNumber());
                    break;
                }
            }
        }
        return loanDocument;
    }

    /**
     * Retrieves Patron home email id.
     *
     * @param entityTypeContactInfoBo
     * @param loanDocument
     * @return
     * @throws Exception
     */
    private OleLoanDocument getPatronHomeEmailId(EntityTypeContactInfoBo entityTypeContactInfoBo, OleLoanDocument loanDocument) throws Exception {
        LOG.debug("Inside the getPatronHomeEmailId method");
        if (entityTypeContactInfoBo.getEmailAddresses() != null) {
            for (EntityEmailBo entityEmailBo : entityTypeContactInfoBo.getEmailAddresses()) {
                if (entityEmailBo.isDefaultValue()) {
                    loanDocument.setEmail(entityEmailBo.getEmailAddress());
                    break;
                }
            }
        }
        return loanDocument;
    }

    /**
     * Retrieves Patron user note.
     *
     * @param olePatronNotes
     * @param loanDocument
     * @return
     * @throws Exception
     */
    private OleLoanDocument getPatronNote(List<OlePatronNotes> olePatronNotes, OleLoanDocument loanDocument) throws Exception {
        LOG.debug("Inside the getPatronNote method");
        for (OlePatronNotes patronNotes : olePatronNotes) {
            if (patronNotes.getOlePatronNoteType() != null) {
                if (patronNotes.getOlePatronNoteType().getPatronNoteTypeCode().equalsIgnoreCase(OLEConstants.USER)) {
                    loanDocument.setPatronUserNotes(patronNotes.getPatronNoteText());
                    loanDocument.setPatronNoteTypeId((String) patronNotes.getPatronNoteTypeId());
                    break;
                }
            }
        }
        return loanDocument;
    }

    /**
     * This method returns Patron information using patron barcode.
     *
     * @param barcode
     * @return loanDocument
     * @throws Exception
     */
    private OleLoanDocument getPatronBarcodeRecord(String barcode) throws Exception {
        LOG.debug("Inside the getPatronBarcodeRecord method");
        OleLoanDocument loanDocument = new OleLoanDocument();
        StringBuffer values_StringBuffer = new StringBuffer();
        try {
            Map barMap = new HashMap();
            barMap.put(OLEConstants.OlePatron.BARCODE, barcode);
            List<OlePatronDocument> matching = (List<OlePatronDocument>) getBusinessObjectService().findMatching(OlePatronDocument.class, barMap);
            List<OleLoanDocument> loanDocuments = matching.get(0).getOleLoanDocuments();
            List<OleLoanDocument> LoanDocumentList = new ArrayList<>();
            List<OleLoanDocument> indefiniteLoanDocumentList = new ArrayList<>();
            for (OleLoanDocument loanDoc : loanDocuments) {
                if (loanDoc.getLoanDueDate() != null && !(loanDoc.getLoanDueDate().toString().isEmpty())) {
                    LoanDocumentList.add(loanDoc);
                } else {
                    indefiniteLoanDocumentList.add(loanDoc);
                }
            }
            Collections.sort(LoanDocumentList, new Comparator<OleLoanDocument>() {
                public int compare(OleLoanDocument o1, OleLoanDocument o2) {
                    return o1.getLoanDueDate().compareTo(o2.getLoanDueDate());
                }
            });
            LoanDocumentList.addAll(indefiniteLoanDocumentList);
            if (matching != null && matching.size() > 0) {
                OlePatronDocument olePatronDocument = matching.get(0);
                olePatronDocument.setOleLoanDocuments(LoanDocumentList);
                loanDocument.setBorrowerTypeId(olePatronDocument.getBorrowerType());
                OleBorrowerType oleBorrowerType = olePatronDocument.getOleBorrowerType();
                loanDocument.setBorrowerTypeName(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeName() != null ? oleBorrowerType.getBorrowerTypeName() : null);
                loanDocument.setBorrowerTypeCode(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeCode() != null ? oleBorrowerType.getBorrowerTypeCode() : null);
                EntityBo entityBo = olePatronDocument.getEntity();
                loanDocument.setPatronName(getPatronName(entityBo));
                loanDocument.setPatronId(olePatronDocument.getOlePatronId());
                List<OlePatronNotes> olePatronNotes = olePatronDocument.getNotes();
                if (olePatronNotes != null) {
                    loanDocument = getPatronNote(olePatronNotes, loanDocument);
                }
                EntityTypeContactInfoBo entityTypeContactInfoBo = entityBo!=null ? entityBo.getEntityTypeContactInfos().get(0) : null;
                if (entityTypeContactInfoBo != null) {
                    loanDocument = getPatronPreferredAddress(entityTypeContactInfoBo, loanDocument);
                    loanDocument = getPatronHomePhoneNumber(entityTypeContactInfoBo, loanDocument);
                    loanDocument = getPatronHomeEmailId(entityTypeContactInfoBo, loanDocument);
                }
                loanDocument.setOlePatron(olePatronDocument);
                return loanDocument;
            } else {
                LOG.error(OLEConstants.PTRN_BARCD_NOT_EXT);
                throw new Exception(OLEConstants.PTRN_BARCD_NOT_EXT);
            }
        } catch (Exception e) {
            LOG.error(OLEConstants.PTRN_BARCD_NOT_EXT + e, e);
            values_StringBuffer.append(OLEConstants.PTRN_BARCD_NOT_EXT + "  " + OLEConstants.PTRN_START_LINK + ConfigContext.getCurrentContextConfig().getProperty("ole.fs.url.base") + OLEConstants.PTRN_END_LINK);
            throw new Exception(values_StringBuffer.toString());
        }

    }

    /**
     * This method returns BorrowerTypeName using borrowerId
     *
     * @param borrowerId
     * @return String
     */
    public OleBorrowerType getborrowerTypeName(String borrowerId) {
        LOG.debug("Inside the getborrowerTypeName method");
        OleBorrowerType oleBorrowerType = getBusinessObjectService().findBySinglePrimaryKey(OleBorrowerType.class, borrowerId);
        return oleBorrowerType;
    }

    /**
     * This method returns PatronName using entityId
     *
     * @param entityId
     * @return String
     */
    private String getPatronName(String entityId) {
        LOG.debug("Inside the getPatronName method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.ENTITY_ID, entityId);
        List<EntityNameBo> entityNameBo = (List<EntityNameBo>) getBusinessObjectService().findMatching(EntityNameBo.class, barMap);
        return entityNameBo.get(0).getFirstName() + " " + entityNameBo.get(0).getLastName();
    }

    /**
     * This method returns PatronName using entityBo
     *
     * @param entityBo
     * @return String
     */
    private String getPatronName(EntityBo entityBo) {
        LOG.debug("Inside the getPatronName method");
        List<EntityNameBo> entityNameBo = entityBo.getNames();
        return entityNameBo!=null && entityNameBo.size() > 0 ? entityNameBo.get(0).getFirstName() + " " + entityNameBo.get(0).getLastName() : "";
    }



    /**
     * This method returns PatronLoanedItem using patronId
     *
     * @param patronId
     * @return List
     * @throws Exception
     */
    public List<OleLoanDocument> getPatronLoanedItem(String patronId) throws Exception {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        LOG.debug("Inside the getPatronLoanedItem method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.PTRN_ID, patronId);
        List<OleLoanDocument> matchingLoan = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class, barMap);
        List<String> idsToExport= new ArrayList<>();
        HashMap<String,Object> loanMap = new HashMap<>();
        for (int itemid = 0; itemid < matchingLoan.size(); itemid++) {
            String itemUuid = matchingLoan.get(itemid).getItemUuid();
            idsToExport.add(itemUuid);
            loanMap.put(itemUuid,matchingLoan.get(itemid)) ;
        }
        List<Item> itemList = new ArrayList<>();
        if(idsToExport.size()==1){
            org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(idsToExport.get(0));
            itemList.add(item);
        }else{
            itemList = getDocstoreClientLocator().getDocstoreClient().retrieveItems(idsToExport);
        }
        int count = 0;
        for(Item item : itemList) {
            // org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(itemUuid);
            org.kuali.ole.docstore.common.document.content.instance.Item itemContent = getItemOlemlRecordProcessor().fromXML(item.getContent());
            if(itemContent.isClaimsReturnedFlag()){
                count++;
            }
            OleHoldings oleHoldings = new HoldingOlemlRecordProcessor().fromXML(item.getHolding().getContent());

            OleLoanDocument oleLoanDocument = (OleLoanDocument)loanMap.get(item.getId());
            oleLoanDocument.setBibUuid(item.getHolding().getBib().getId());
            oleLoanDocument.setTitle(item.getHolding().getBib().getTitle());
            oleLoanDocument.setAuthor(item.getHolding().getBib().getAuthor());
            oleLoanDocument.setInstanceUuid(item.getHolding().getId());
            oleLoanDocument.setItemUuid(item.getId());
            oleLoanDocument.setClaimsReturnedIndicator(itemContent.isClaimsReturnedFlag());
            if (oleLoanDocument.isClaimsReturnedIndicator()) {
                oleLoanDocument.setClaimsReturnNote(itemContent.getClaimsReturnedNote());
                if (itemContent.getClaimsReturnedFlagCreateDate() != null)
                    oleLoanDocument.setClaimsReturnedDate(new Timestamp(df.parse(itemContent.getClaimsReturnedFlagCreateDate()).getTime()));
            }
            if(oleLoanDocument.getOlePatron()!=null){
                oleLoanDocument.getOlePatron().setNumberOfClaimsReturned(count);
            }
            String callNumber;
             if(itemContent.getCallNumber()!=null && !StringUtils.isEmpty(itemContent.getCallNumber().getNumber())){
                callNumber = itemContent.getCallNumber().getNumber();
            }else {
                callNumber = getItemCallNumber(oleHoldings.getCallNumber());
            }
            String itemType;
            if(itemContent.getTemporaryItemType() != null && itemContent.getTemporaryItemType().getCodeValue()!=null){
                itemType = itemContent.getTemporaryItemType().getCodeValue();
            }else{
                itemType = itemContent.getItemType().getCodeValue();
            }
            oleLoanDocument.setItemCallNumber(callNumber);
            getCopyNumber(itemContent,oleHoldings,oleLoanDocument);
           // oleLoanDocument.setItemCopyNumber(itemContent.getCopyNumber());
            oleLoanDocument.setEnumeration(itemContent.getEnumeration());
            oleLoanDocument.setChronology(itemContent.getChronology());
            oleLoanDocument.setItemStatus(itemContent.getItemStatus().getCodeValue());
            oleLoanDocument.setItemType(itemType);
            oleLoanDocument.setItemDamagedStatus(itemContent.isItemDamagedStatus());
            oleLoanDocument.setItemDamagedNote(itemContent.getDamagedItemNote());
            oleLoanDocument.setMissingPieceNote(itemContent.getMissingPieceFlagNote());
            oleLoanDocument.setMissingPieceFlag(itemContent.isMissingPieceFlag());
            oleLoanDocument.setMissingPiecesCount(itemContent.getMissingPiecesCount());
            if(itemContent.getNumberOfPieces()!=null && !itemContent.getNumberOfPieces().equalsIgnoreCase("")){
                oleLoanDocument.setItemNumberOfPieces(new Integer(itemContent.getNumberOfPieces()));
                oleLoanDocument.setBackUpNoOfPieces(itemContent.getNumberOfPieces());
            }
            if (itemContent.getLocation().getLocationLevel() != null) {
                Location physicalLocation = itemContent.getLocation();
                LocationLevel locationLevel = null;
                locationLevel = physicalLocation.getLocationLevel();
                getOleLocationLevel(oleLoanDocument, locationLevel);
            } else {
                //OleHoldings oleHoldings = getOleHoldings(oleLoanDocument.getInstanceUuid());
                if (oleHoldings != null) {
                    Location physicalLocation = oleHoldings.getLocation();
                    LocationLevel locationLevel = null;
                    locationLevel = physicalLocation.getLocationLevel();


                    getOleLocationLevel(oleLoanDocument, locationLevel);
                }
            }
            if(itemContent.getProxyBorrower()!=null){
                Map<String,String> map=new HashMap<String,String>();
                map.put("entityId",itemContent.getProxyBorrower());
                EntityNameBo entityNameBo=(EntityNameBo) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(EntityNameBo.class,map);
                if(entityNameBo!=null){
                    oleLoanDocument.setRealPatronName(entityNameBo.getFirstName()+"  "+entityNameBo.getLastName());
                }
            }


        }

        return sortLoanDocumentByDueDate(matchingLoan);
    }


    public  List<OleLoanDocument>  sortLoanDocumentByDueDate(List<OleLoanDocument>  loanDocuments){
        List<OleLoanDocument> LoanDocumentList = new ArrayList<>();
        List<OleLoanDocument> indefiniteLoanDocumentList = new ArrayList<>();
        for (OleLoanDocument loanDoc : loanDocuments) {
            if (loanDoc.getLoanDueDate() != null && !(loanDoc.getLoanDueDate().toString().isEmpty())) {
                LoanDocumentList.add(loanDoc);
            } else {

                indefiniteLoanDocumentList.add(loanDoc);
            }
        }
        Collections.sort(LoanDocumentList, new Comparator<OleLoanDocument>() {
            public int compare(OleLoanDocument o1, OleLoanDocument o2) {
                return o1.getLoanDueDate().compareTo(o2.getLoanDueDate());
            }
        });
        LoanDocumentList.addAll(indefiniteLoanDocumentList);
        return LoanDocumentList;
    }


    public List<OleLoanDocument> getPatronLoanedItemBySolr(String patronId) throws Exception{
      LOG.debug("Inside the getPatronLoanedItemBySolr method");
      Long b1 = System.currentTimeMillis();
      SearchResponse searchResponse = new SearchResponse();
        HashMap<String,Object> loanMap = new HashMap<>();
      if(patronId != null){
          String itemStatusParam = getParameter(OLEParameterConstants.ITEM_STATUS_FOR_RET_LOAN);
          String[] itemStatus = new String[0];
          if(itemStatusParam != null){
             itemStatus = itemStatusParam.split("[|]");
          }
         SearchParams searchParams = new SearchParams();
         List<SearchCondition> searchConditions = new ArrayList<>();
          int count = itemStatus.length;
          for(int i=0; i< count;i++) {
              if(i == (count-1)) {
                  searchConditions.add(searchParams.buildSearchCondition("", searchParams.buildSearchField("item", "ItemStatus_search", itemStatus[i]), "AND"));
              }
              else {
                  searchConditions.add(searchParams.buildSearchCondition("", searchParams.buildSearchField("item", "ItemStatus_search", itemStatus[i]), "OR"));
              }
          }

          searchConditions.add(searchParams.buildSearchCondition("phrase", searchParams.buildSearchField("item", "currentBorrower", patronId), "AND"));

         searchParams.setPageSize(Integer.parseInt(OLEConstants.MAX_PAGE_SIZE_FOR_LOAN));
         buildSearchParams(searchParams);
         searchParams.getSearchConditions().addAll(searchConditions);
         searchResponse = getDocstoreClientLocator().getDocstoreClient().search(searchParams);
      }
        List<OleLoanDocument> matchingLoan = buildSearchResultsFields(searchResponse,loanMap);


        Long b2 = System.currentTimeMillis();
        Long total = b2 - b1;
        LOG.info("The time taken for Docstore call :"+total);
        return sortLoanDocumentByDueDate(matchingLoan);
    }
    /**
     * This method returns PatronTemporaryCirculationHistoryRecord using patronId
     *
     * @param patronId
     * @return List
     * @throws Exception
     */
    public List<OleTemporaryCirculationHistory> getPatronTemporaryCirculationHistoryRecords(String patronId) throws Exception {
        LOG.debug("Inside the getPatronTemporaryCirculationHistoryRecords method");
        Map barMap = new HashMap();
        barMap.put("olePatronId", patronId);
        List<OleTemporaryCirculationHistory> matchingLoan = (List<OleTemporaryCirculationHistory>) getBusinessObjectService().findMatching(OleTemporaryCirculationHistory.class, barMap);
        //List<String> itemUuids = new ArrayList<String>();
        for (int itemid = 0; itemid < matchingLoan.size(); itemid++) {
            String itemUuid = matchingLoan.get(itemid).getItemUuid();


            org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(itemUuid);


            org.kuali.ole.docstore.common.document.content.instance.Item itemContent = getItemOlemlRecordProcessor().fromXML(item.getContent());
            HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
            OleHoldings oleHoldings = holdingOlemlRecordProcessor.fromXML(item.getHolding().getContent());
            OleTemporaryCirculationHistory oleTemporaryCirculationHistory = matchingLoan.get(itemid);
            if (oleTemporaryCirculationHistory.getItemUuid().equals(item.getId())) {
                oleTemporaryCirculationHistory.setTitle(item.getHolding().getBib().getTitle());
                oleTemporaryCirculationHistory.setAuthor(item.getHolding().getBib().getAuthor());

                oleTemporaryCirculationHistory.setItemUuid(item.getId());


                if(itemContent.getCallNumber()!=null && !StringUtils.isEmpty(itemContent.getCallNumber().getNumber())){
                   /* String[] callNumber = item.getCallNumber().split(" ");
                    if (callNumber.length > 0) {
                        item.setCallNumber(callNumber != null ? callNumber[0] : item.getCallNumber());
                    }
                    oleTemporaryCirculationHistory.setCallNumber(getItemCallNumber(item.getCallNumberType(), item.getCallNumberPrefix(), item.getCallNumber()));*/
                    oleTemporaryCirculationHistory.setCallNumber(itemContent.getCallNumber().getNumber());
                } else {
                   /* String[] callNumber = item.getHolding().getCallNumber().split(" ");
                    if (callNumber.length > 0) {
                        item.getHolding().setCallNumber(callNumber != null ? callNumber[0] : item.getHolding().getCallNumber());
                    }
                    oleTemporaryCirculationHistory.setCallNumber(getItemCallNumber(item.getHolding().getCallNumberType(), item.getHolding().getCallNumberPrefix(), item.getHolding().getCallNumber()));*/
                    String callNumber = getItemCallNumber(oleHoldings.getCallNumber());
                    oleTemporaryCirculationHistory.setCallNumber(callNumber);
                }
                oleTemporaryCirculationHistory.setCopyNumber(itemContent.getCopyNumber());
                oleTemporaryCirculationHistory.setVolumeNumber(itemContent.getVolumeNumber());
                oleTemporaryCirculationHistory.setItemStatus(itemContent.getItemStatus().getFullValue());
                oleTemporaryCirculationHistory.setItemType(itemContent.getItemType().getCodeValue());

                Map<String, String> map = new HashMap<String, String>();
                map.put("circulationDeskId", oleTemporaryCirculationHistory.getCirculationLocationId());
                List<OleCirculationDesk> oleCirculationDesks = (List<OleCirculationDesk>) getBusinessObjectService().findMatching(OleCirculationDesk.class, map);
                for (OleCirculationDesk val : oleCirculationDesks) {

                    oleTemporaryCirculationHistory.setCirculationLocationCode(val.getCirculationDeskCode());

                }
                if (oleTemporaryCirculationHistory.getCirculationLocationId() != null && !oleTemporaryCirculationHistory.getCirculationLocationId().equals("")) {
                    StringBuffer locationLevel = new StringBuffer("");
                    LocationValuesBuilder locationValuesBuilder = new LocationValuesBuilder();
                    locationValuesBuilder.getLocation(itemContent, oleTemporaryCirculationHistory, item.getHolding().getId());
                    /*oleTemporaryCirculationHistory.setShelvingLocation(getShelvingLocation(itemContent.getLocation().getLocationLevel().getName()));
                    if (oleTemporaryCirculationHistory.getShelvingLocation() == null || (oleTemporaryCirculationHistory.getShelvingLocation() != null && oleTemporaryCirculationHistory.getShelvingLocation().isEmpty())) {
                        oleTemporaryCirculationHistory.setShelvingLocation(getShelvingLocation(oleHoldings.getLocation().getLocationLevel().getName()));
                    }*/
                }
                //break;

            }
        }


        return matchingLoan;
    }


    /**
     * This method returns PatronRequestRecords  using patronId
     *
     * @param patronId
     * @return List
     * @throws Exception
     */
    public List<OleDeliverRequestBo> getPatronRequestRecords(String patronId) throws Exception {
        LOG.debug("Inside the getPatronRequestRecords method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.OleDeliverRequest.BORROWER_ID, patronId);
        List<OleDeliverRequestBo> matchingLoan = (List<OleDeliverRequestBo>) getBusinessObjectService().findMatching(OleDeliverRequestBo.class, barMap);

        for (int itemid = 0; itemid < matchingLoan.size(); itemid++) {
            String itemUuid = matchingLoan.get(itemid).getItemUuid();


            org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(itemUuid);
            org.kuali.ole.docstore.common.document.content.instance.Item itemContent = getItemOlemlRecordProcessor().fromXML(item.getContent());
            HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
            OleHoldings oleHoldings = holdingOlemlRecordProcessor.fromXML(item.getHolding().getContent());
            OleDeliverRequestBo oleDeliverRequestBo = matchingLoan.get(itemid);
            if (oleDeliverRequestBo.getItemUuid().equals(item.getId())) {
                oleDeliverRequestBo.setTitle(item.getHolding().getBib().getTitle());
                oleDeliverRequestBo.setAuthor(item.getHolding().getBib().getAuthor());
                if(itemContent.getCallNumber()!=null && !StringUtils.isEmpty(itemContent.getCallNumber().getNumber())){

                    oleDeliverRequestBo.setCallNumber(itemContent.getCallNumber().getNumber());
                    // oleDeliverRequestBo.setCallNumber(getItemCallNumber(item.getCallNumberType(), item.getCallNumberPrefix(), item.getCallNumber()));
                } else {
              //      String callNumber = getItemCallNumber(itemContent, item.getHolding().getId());

                    oleDeliverRequestBo.setCallNumber(oleHoldings.getCallNumber().getNumber());
                    //oleDeliverRequestBo.setCallNumber(getItemCallNumber(item.getHolding().getCallNumberType(), item.getHolding().getCallNumberPrefix(), item.getHolding().getCallNumber()));
                }
                oleDeliverRequestBo.setCopyNumber(itemContent.getCopyNumber());
                oleDeliverRequestBo.setVolumeNumber(itemContent.getVolumeNumber());
                oleDeliverRequestBo.setItemStatus(itemContent.getItemStatus().getCodeValue());
                oleDeliverRequestBo.setItemType(itemContent.getItemType().getCodeValue());


                if (oleDeliverRequestBo.getCirculationLocationCode() != null && !oleDeliverRequestBo.getCirculationLocationCode().equals("")) {
                    StringBuffer locationLevel = new StringBuffer("");
                    LocationValuesBuilder locationValuesBuilder = new LocationValuesBuilder();
                    locationValuesBuilder.getLocation(itemContent, oleDeliverRequestBo, item.getHolding().getId());
                   /* oleDeliverRequestBo.setShelvingLocation(getShelvingLocation(itemContent.getLocation().getLocationLevel().getName()));
                    if (oleDeliverRequestBo.getShelvingLocation() == null || (oleDeliverRequestBo.getShelvingLocation() != null && oleDeliverRequestBo.getShelvingLocation().isEmpty())) {


                        oleDeliverRequestBo.setShelvingLocation(getShelvingLocation(oleHoldings.getLocation().getLocationLevel().getName()));
                    }*/
                }
                break;
            }


        }

        return matchingLoan;
    }


    private String getShelvingLocation(String location) throws Exception {
        if (location != null) {
            String[] arr = location.split("/");
            for (int i = arr.length; i >= 0; i--) {
                return arr[i - 1];
            }
        }
        return location;
    }


    public String getShelvingLocation(LocationLevel oleLocationLevel) {
        String location = null;
        if (oleLocationLevel != null) {
            if (OLEConstants.LOCATION_LEVEL_SHELVING.equalsIgnoreCase(oleLocationLevel.getLevel()))
                location = oleLocationLevel.getName();
        } else {
            location = getShelvingLocation(oleLocationLevel.getLocationLevel());
        }
        if ("".equals(location) || location == null) {
            return null;
        }
        return location;
    }

    public String getItemCallNumber(org.kuali.ole.docstore.common.document.content.instance.Item oleItem, String instanceUUID) throws Exception {
        CallNumber callNumber = null;
        if (oleItem.getCallNumber() != null && oleItem.getCallNumber().getNumber() != null && !oleItem.getCallNumber().getNumber().isEmpty()) {
            callNumber = oleItem.getCallNumber();
        } else {
            OleHoldings oleHoldings = getOleHoldings(instanceUUID);

            callNumber = oleHoldings.getCallNumber();
        }
        return getItemCallNumber(callNumber);
    }

    /**
     * Retrieves Item call number.
     *
     * @param callNumber
     * @return
     * @throws Exception
     */
    public String getItemCallNumber(CallNumber callNumber) throws Exception {
        LOG.debug("Inside the getItemCallNumber method");
        String itemCallNumber = "";
        if (callNumber != null) {

            if (StringUtils.isNotBlank(callNumber.getType())) {
                itemCallNumber += callNumber.getType() + OLEConstants.DELIMITER_DASH;
            }
            if (StringUtils.isNotBlank(callNumber.getPrefix())) {
                itemCallNumber += callNumber.getPrefix() + OLEConstants.DELIMITER_DASH;
            }
            if (callNumber.getNumber() != null) {
                itemCallNumber += callNumber.getNumber();
            }

        }
        return itemCallNumber;
    }

    /**
     * Retrieves item xml using itemuuid.
     *
     * @param itemUUID
     * @return
     * @throws Exception
     */
    public String getItemXML(String itemUUID) throws Exception {
        LOG.debug("Inside the getItemXML method");
        String itemXml = "";
        try {
            org.kuali.ole.docstore.common.document.Item item = new ItemOleml();
            item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(itemUUID);
            itemXml = item.getContent();
            if (LOG.isDebugEnabled()){
                LOG.debug("item XML ----- > " + itemXml);
            }
        } catch (Exception e) {
            LOG.error(OLEConstants.ITM_BARCD_NT_AVAL_DOC, e);
            throw new Exception(OLEConstants.ITM_BARCD_NT_AVAL_DOC);
        }
        return itemXml;
    }

    /**
     * Converts item xml to item pojo.
     *
     * @param itemXml
     * @return
     * @throws Exception
     */
    public org.kuali.ole.docstore.common.document.content.instance.Item getItemPojo(String itemXml) throws Exception {
        LOG.debug("Inside the getItemPojo method");
        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = null;
        try {
            oleItem = getItemOlemlRecordProcessor().fromXML(itemXml);
        } catch (Exception e) {
            LOG.error(OLEConstants.PAR_EXP, e);
            throw new Exception(OLEConstants.PAR_EXP);
        }
        return oleItem;
    }

    public void getDefaultHoldingLocation(OleLoanDocument oleLoanDocument) throws Exception {
        try {
            OleHoldings oleHoldings = getOleHoldings(oleLoanDocument.getInstanceUuid());
            if (oleHoldings != null) {
                Location physicalLocation = oleHoldings.getLocation();
                LocationLevel locationLevel = null;
                locationLevel = physicalLocation.getLocationLevel();
                getOleLocationLevel(oleLoanDocument, locationLevel);
            }
        } catch (Exception holdingException) {
            LOG.error("--------------Invalid Holding location data.---------------", holdingException);

            throw new Exception(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVAL_LOC));
        }
    }

    private void getCopyNumber( org.kuali.ole.docstore.common.document.content.instance.Item  item,OleHoldings oleHoldings,OleLoanDocument oleLoanDocument) throws Exception{
        if(item.getCopyNumber()!=null && !item.getCopyNumber().equals("")){
            oleLoanDocument.setItemCopyNumber(item.getCopyNumber());
        }else if(oleHoldings.getCopyNumber() != null && !oleHoldings.getCopyNumber().equals("")){
            oleLoanDocument.setItemCopyNumber(oleHoldings.getCopyNumber());
        }
    }

    /**
     * Retrieved item object from docstore using itemid .Fetch location code,item available status from item object and
     * retrieve title from bib object.
     *
     * @param patronBarcode
     * @param itemBarcode
     * @param oleLoanDocument
     * @return OleLoanDocument
     */
    public OleLoanDocument addLoan(String patronBarcode, String itemBarcode, OleLoanDocument oleLoanDocument,String operatorId) throws Exception {

        LOG.debug("Inside the addLoan method");
        String itemUUID= oleLoanDocument.getItemUuid();
        String holdingsId = null;
        Long begin = System.currentTimeMillis();
        if(itemUUID==null){
            try {
                org.kuali.ole.docstore.common.document.Item item = new ItemOleml();
                org.kuali.ole.docstore.common.search.SearchParams search_Params = new org.kuali.ole.docstore.common.search.SearchParams();
                SearchResponse searchResponse = null;
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), item.ITEM_BARCODE, itemBarcode), ""));
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), "id"));
//                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.HOLDINGS.getCode(), "id"));
                searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
                for (SearchResult searchResult : searchResponse.getSearchResults()) {
                    for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                        String fieldName = searchResultField.getFieldName();
                        String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
//                        if (fieldName.equalsIgnoreCase("id") && !fieldValue.isEmpty() && searchResultField.getDocType().equalsIgnoreCase("holdings")) {
//                            holdingsId = fieldValue;
//                        } else {
                        itemUUID = fieldValue;
                        oleLoanDocument.setItemUuid(itemUUID);
//                        }
                    }
                }
            } catch (Exception ex) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, "Item Exists");
                LOG.error(OLEConstants.ITEM_EXIST + ex);
            }
        }
        Map<String,Object> detailMap = getOleDeliverRequestDocumentHelperService().retrieveBIbItemHoldingData(itemUUID);
        Bib bib = (Bib)detailMap.get(OLEConstants.BIB);
        oleLoanDocument.setTitle(bib.getTitle());
        oleLoanDocument.setAuthor(bib.getAuthor());
        org.kuali.ole.docstore.common.document.content.instance.Item item = (org.kuali.ole.docstore.common.document.content.instance.Item)detailMap.get(OLEConstants.ITEM);
        OleHoldings oleHoldings = (OleHoldings)detailMap.get(OLEConstants.HOLDING);
        org.kuali.ole.docstore.common.document.Item item1 = (org.kuali.ole.docstore.common.document.Item)detailMap.get("documentItem");
        oleLoanDocument.setInstanceUuid(oleHoldings.getHoldingsIdentifier());
        Long end = System.currentTimeMillis();
        Long timeTaken = end-begin;
        LOG.info("The Time Taken for Docstore call in Add Item"+timeTaken);
        Date pastDueDate = oleLoanDocument.getLoanDueDate();
        boolean itemValidation = itemValidation(item);
        if (!itemValidation) {
            if (oleLoanDocument.isRenewalItemFlag()) {
                oleLoanDocument.setErrorMessage(OLEConstants.INVAL_ITEM);
                return oleLoanDocument;
            } else {
                throw new Exception(OLEConstants.INVAL_ITEM);
            }
        }
        oleLoanDocument.setOleItem(item);
        try {
            getCopyNumber(item,oleHoldings,oleLoanDocument);
            if(item.getCallNumber()!=null && !StringUtils.isEmpty(item.getCallNumber().getNumber())){
                oleLoanDocument.setItemCallNumber(item.getCallNumber().getNumber());
            }else {
                oleLoanDocument.setItemCallNumber(getItemCallNumber(oleHoldings.getCallNumber()));
            }
            oleLoanDocument.setEnumeration(item.getEnumeration());
            oleLoanDocument.setChronology(item.getChronology());
            String status = item.getItemStatus().getCodeValue();
            oleLoanDocument.setItemLoanStatus(status);
        } catch (Exception e) {
            LOG.error(OLEConstants.ITM_STS_NT_AVAL, e);
            if (oleLoanDocument.isRenewalItemFlag()) {
                oleLoanDocument.setErrorMessage(OLEConstants.ITM_STS_NT_AVAL);
                return oleLoanDocument;
            } else {
                throw new Exception(OLEConstants.ITM_STS_NT_AVAL);
            }
        }
        oleLoanDocument.setItemId(itemBarcode);
        oleLoanDocument.setMissingPieceFlag(item.isMissingPieceFlag());
        oleLoanDocument.setMissingPiecesCount(item.getMissingPiecesCount());
        try {
            if (item.getTemporaryItemType() != null && item.getTemporaryItemType().getCodeValue() != "") {
                OleInstanceItemType oleInstanceItemType = getItemTypeIdByItemType(item.getTemporaryItemType().getCodeValue());
                oleLoanDocument.setOleInstanceItemType(oleInstanceItemType);
                oleLoanDocument.setItemType(oleInstanceItemType.getInstanceItemTypeCode());
                oleLoanDocument.setItemTypeName(oleInstanceItemType.getInstanceItemTypeCode());
            }
            else if (item.getItemType() != null && item.getItemType().getCodeValue() != "") {
                OleInstanceItemType oleInstanceItemType = getItemTypeIdByItemType(item.getItemType().getCodeValue());
                oleLoanDocument.setOleInstanceItemType(oleInstanceItemType);
                oleLoanDocument.setItemType(oleInstanceItemType.getInstanceItemTypeCode());
                oleLoanDocument.setItemTypeName(oleInstanceItemType.getInstanceItemTypeCode());
            }
        } catch (Exception e) {
            LOG.error(OLEConstants.INVAL_ITEM, e);
            throw new Exception(OLEConstants.INVAL_ITEM);
        }
        Long beginLocation = System.currentTimeMillis();
        getLocation(item, oleLoanDocument,item1);
        if (oleLoanDocument.getLocation() == null || oleLoanDocument.getLocation().isEmpty()) {
            getDefaultHoldingLocation(oleLoanDocument);
        }
        Long endLocation = System.currentTimeMillis();
        Long timeTakenLocation = endLocation-beginLocation;
        LOG.info("The Time Taken for Location call in Add Item"+timeTakenLocation);
        setLoan(oleLoanDocument);
        oleLoanDocument.setPatronBarcode(patronBarcode);
    /*    HashMap overdueItems = new HashMap();*/
      /*  Integer loanedItems = 0;*/
        Date expirationDate = null;
        Integer numberOfClaimsReturned = 0;
        String patronType = "";
        HashMap keyLoanMap = new HashMap();

        if (oleLoanDocument.getProxyPatronId() != null) {
            // Checking for real patron.
            keyLoanMap=getOleCirculationPolicyService().getLoanedKeyMap(oleLoanDocument.getProxyPatronId(),oleLoanDocument.isRenewalItemFlag());
            expirationDate = getOleCirculationPolicyService().getPatronMembershipExpireDate(oleLoanDocument.getRealPatronBarcode());
            /**
             * For performance issue I commenting the below code. The number of claims will be recorded in the patron document.
             */
            OlePatronDocument olePatronDocument = (OlePatronDocument) keyLoanMap.get("patronDetails");
            numberOfClaimsReturned = olePatronDocument!=null?olePatronDocument.getNumberOfClaimsReturned():0; //oleCirculationPolicyService.getNumberOfClaimsReturned(oleLoanDocument.getProxyPatronId());
            patronType = oleLoanDocument.getRealPatronType();

        } else {
            keyLoanMap=getOleCirculationPolicyService().getLoanedKeyMap(oleLoanDocument.getPatronId(),oleLoanDocument.isRenewalItemFlag());
            if (oleLoanDocument.getOlePatron() != null && oleLoanDocument.getOlePatron().getBarcode().equalsIgnoreCase(oleLoanDocument.getPatronBarcode())) {
                expirationDate=oleLoanDocument.getOlePatron().getExpirationDate();
            } else {
                expirationDate = getOleCirculationPolicyService().getPatronMembershipExpireDate(oleLoanDocument.getPatronBarcode());
            }
            /**
             * For performance issue I commenting the below code. The number of claims will be recorded in the patron document.
             */
            OlePatronDocument olePatronDocument = (OlePatronDocument) keyLoanMap.get("patronDetails");
            numberOfClaimsReturned = olePatronDocument!=null?olePatronDocument.getNumberOfClaimsReturned():0;
            patronType = oleLoanDocument.getBorrowerTypeCode();

        }


        oleLoanDocument.setExpirationDate(expirationDate);
        StringBuffer failures = new StringBuffer();
        List<FeeType> feeTypeList = oleCirculationPolicyService.getPatronBillPayment(oleLoanDocument.getPatronId());

        List<Integer> listOfOverDueDays = (List<Integer>)keyLoanMap.get(OLEConstants.LIST_OF_OVERDUE_DAYS);
        getDataCarrierService().addData(OLEConstants.LIST_OVERDUE_DAYS, listOfOverDueDays);
        getDataCarrierService().addData(OLEConstants.HOURS_DIFF, oleCirculationPolicyService.getHoursDiff(oleLoanDocument.getLoanDueDate(), new Date()));
        getDataCarrierService().addData(OLEConstants.DUEDATE, oleLoanDocument.getLoanDueDate());
        getDataCarrierService().addData(OLEConstants.CHECKINDATE, oleLoanDocument.getCheckInDate());
        getDataCarrierService().addData(OLEConstants.LIST_RECALLED_OVERDUE_DAYS, (List<Integer>) keyLoanMap.get(OLEConstants.LIST_RECALLED_OVERDUE_DAYS));
        String patronId = oleLoanDocument.getPatronId()!=null ?  oleLoanDocument.getPatronId() : "";
        String itemId = oleLoanDocument.getItemId()!=null ?  oleLoanDocument.getItemId() : "";
        getDataCarrierService().removeData(patronId + itemId);
        if (oleLoanDocument.isRenewalItemFlag()) {
            getDataCarrierService().addData(OLEConstants.EXCLUDE_TIME, true);
        }
        HashMap<String,Integer> itemTypeMap  = (HashMap<String, Integer>) keyLoanMap.get("itemTypeMap");
        getDataCarrierService().addData("itemTypeMap", getItemTypeFromCurrentLoan(itemTypeMap, oleLoanDocument));
        Integer overdueFineAmt = 0;
        Integer replacementFeeAmt = 0;
        Integer serviceFeeAmt = 0;
        for (FeeType feeType : feeTypeList) {
            Integer fineAmount = feeType.getFeeAmount().subtract(feeType.getPaidAmount()).intValue();
            overdueFineAmt += feeType.getOleFeeType().getFeeTypeName().equalsIgnoreCase(OLEConstants.OVERDUE_FINE) ? fineAmount : 0;
            replacementFeeAmt += feeType.getOleFeeType().getFeeTypeName().equalsIgnoreCase(OLEConstants.REPLACEMENT_FEE) ? fineAmount : 0;
            serviceFeeAmt += feeType.getOleFeeType().getFeeTypeName().equalsIgnoreCase(OLEConstants.SERVICE_FEE) ? fineAmount : 0;
        }

        OleCirculationDesk oleCirculationDesk = oleLoanDocument.getCirculationLocationId() != null ? getOleCirculationDesk(oleLoanDocument.getCirculationLocationId()) : null;
        oleLoanDocument.setOleCirculationDesk(oleCirculationDesk);
      /* if (oleCirculationDesk != null && oleCirculationDesk.getCalendarGroupId() != null) {
            OleCalendarService oleCalendarService = new OleCalendarServiceImpl();
            OleCalendar oleCalendar = oleCalendarService.getActiveCalendar(new Timestamp(System.currentTimeMillis()), oleCirculationDesk.getCalendarGroupId());
            if (oleCalendar == null) {
                throw new Exception("Calendar does not exist");
            }
        }
        getDataCarrierService().addData(OLEConstants.GROUP_ID, oleCirculationDesk != null ? oleCirculationDesk.getCalendarGroupId() : "");*/
        getDataCarrierService().addData(OLEConstants.GROUP_ID, oleCirculationDesk != null ? oleCirculationDesk.getCalendarGroupId() : "");
        String operatorsCirculationLocation = circulationDeskLocations(oleCirculationDesk);
        if (LOG.isDebugEnabled()){
            LOG.debug("operatorsCirculationLocation---->" + operatorsCirculationLocation);
        }
        String requestType = null;
        OlePatronDocument oleRequestPatron = null;
        OleDeliverRequestBo oleDeliverRequestForQueue = null;
        OleDeliverRequestBo oleDeliverRequestBo = getPrioritizedRequest(oleLoanDocument.getItemUuid());
        if (oleDeliverRequestBo != null) {
            oleLoanDocument.setOleDeliverRequestBo(oleDeliverRequestBo);
            requestType = oleDeliverRequestBo.getOleDeliverRequestType().getRequestTypeCode();
            oleRequestPatron = getOlePatronDocument(oleDeliverRequestBo.getBorrowerId());
            oleLoanDocument.setOleRequestPatron(oleRequestPatron);
            String requestPatronId = oleRequestPatron != null ? oleRequestPatron.getOlePatronId() : null;
            if (oleLoanDocument.getPatronId().equals(requestPatronId)) {
                oleLoanDocument.setRequestPatron(true);
                oleLoanDocument.setOleRequestId(oleDeliverRequestBo.getRequestId());
            } else {
                oleDeliverRequestForQueue = getCurrentQueue(oleLoanDocument.getPatronId(), oleLoanDocument.getItemUuid());
                oleLoanDocument.setOleRequestId(oleDeliverRequestForQueue != null ? oleDeliverRequestForQueue.getRequestId() : null);
            }
        }

        String digitRoutine = getParameter(OLEParameterConstants.ITEM_DIGIT_ROUTINE);
        String pattern = getParameter(OLEParameterConstants.ITEM_DIGIT_ROUTINE_PATTERN);
        String itemCircLoction = getFullPathLocation(getLocationByLocationCode(oleLoanDocument.getItemLocation()));
        DateFormat formatter = new SimpleDateFormat(OLEConstants.DDMMYYYYHHMMSS);
        Date loanDueDate = oleLoanDocument.getLoanDueDate() != null ? new Date(oleLoanDocument.getLoanDueDate().getTime()) : null;
        String dateToString = oleLoanDocument.getLoanDueDate() != null ? formatter.format(oleLoanDocument.getLoanDueDate()) : null;
        checkReplacementFineExist(oleLoanDocument);
        checkOverdueExist(oleLoanDocument);
        isItemLoanedByDifferentPatron(oleLoanDocument);
        checkItemDamagedStatus(oleLoanDocument);
        isMissingPieceFlagActive(oleLoanDocument);
        String agendaName = oleLoanDocument.isRenewalItemFlag() ? OLEConstants.RENEWAL_AGENDA_NM : OLEConstants.CHECK_OUT_AGENDA_NM;
        Integer noOfRenewals = Integer.parseInt(oleLoanDocument.getNumberOfRenewals())+1;
        HashMap<String, Object> termValues = new HashMap<String, Object>();
        termValues.put(OLEConstants.BORROWER_TYPE, patronType);
        termValues.put(OLEConstants.ITEM_TYPE, oleLoanDocument.getItemTypeName());
        termValues.put(OLEConstants.NUM_ITEMS_CHECKED_OUT, keyLoanMap.get(OLEConstants.LOANED_ITEM_COUNT));
        termValues.put(OLEConstants.NUM_OVERDUE_ITEMS_CHECKED_OUT, (Integer) keyLoanMap.get(OLEConstants.OVERDUE_COUNT));
        termValues.put(OLEConstants.NUM_OVERDUE_RECALLED_ITEMS_CHECKED_OUT, (Integer) keyLoanMap.get(OLEConstants.RECALL_COUNT));
        termValues.put(OLEConstants.NUM_CLAIMS_RETURNED, numberOfClaimsReturned);
        termValues.put(OLEConstants.OVERDUE_FINE_AMT, overdueFineAmt);
        termValues.put(OLEConstants.REPLACEMENT_FEE_AMT, replacementFeeAmt);
        termValues.put(OLEConstants.ALL_CHARGES, overdueFineAmt + replacementFeeAmt + serviceFeeAmt);
        termValues.put(OLEConstants.IS_RENEWAL, oleLoanDocument.isRenewalItemFlag() ? OLEConstants.TRUE : OLEConstants.FALSE);
        termValues.put(OLEConstants.NUM_RENEWALS, noOfRenewals.toString());
        termValues.put(OLEConstants.ITEMS_DUE_DATE, loanDueDate);
        termValues.put(OLEConstants.ITEMS_DUE_DATE_STRING, dateToString);
        termValues.put(OLEConstants.DIGIT_ROUTINE, digitRoutine);
        termValues.put(OLEConstants.PATTERN, pattern);
        termValues.put(OLEConstants.ITEM_BARCODE, itemBarcode);
        termValues.put(OLEConstants.ITEM_LOCATION, itemCircLoction);
        termValues.put(OLEConstants.CIRCULATION_LOCATION, operatorsCirculationLocation);
        termValues.put(OLEConstants.IS_PATRON_POSITION_ONE, oleLoanDocument.getPatronId().equals(oleRequestPatron != null ? oleRequestPatron.getOlePatronId() : null) ? OLEConstants.TRUE : OLEConstants.FALSE);
        termValues.put(OLEConstants.REQUEST_TYPE, requestType);
      /*  termValues.put(OLEConstants.ITEM_STATUS, oleLoanDocument.getItemLoanStatus());*/
        termValues.put(OLEConstants.ITEM_SHELVING, oleLoanDocument.getItemLocation());
        termValues.put(OLEConstants.ITEM_COLLECTION, oleLoanDocument.getItemCollection());
        termValues.put(OLEConstants.ITEM_LIBRARY, oleLoanDocument.getItemLibrary());
        termValues.put(OLEConstants.ITEM_CAMPUS, oleLoanDocument.getItemCampus());
        termValues.put(OLEConstants.ITEM_INSTITUTION, oleLoanDocument.getItemInstitution());
        termValues.put(OLEConstants.REPLACEMENT_FEE_EXIST, oleLoanDocument.isReplacementFeeExist());
        termValues.put(OLEConstants.OVERDUE_FINE_EXIST, oleLoanDocument.isOverdueFineExist());
        termValues.put(OLEConstants.DIFF_PATRON_FLD, oleLoanDocument.isDifferentPatron());
        termValues.put(OLEConstants.ITEM_DAMAGED_STATUS_FLD, oleLoanDocument.isItemDamagedStatus());
        termValues.put(OLEConstants.ITEM_MISING_PICS_FLAG_FLD, oleLoanDocument.isMissingPieceFlag());
        termValues.put(OLEConstants.PATRON_ID_POLICY, patronId);
        termValues.put(OLEConstants.ITEM_ID_POLICY, itemId);
        EngineResults engineResults = getEngineResults(agendaName, termValues);
        getDataCarrierService().removeData(patronId + itemId);
        Timestamp dueDate = (Timestamp) engineResults.getAttribute(OLEConstants.DUE_DATE);
        BigDecimal fineRate = (BigDecimal) engineResults.getAttribute(OLEConstants.FINE_RATE);
        BigDecimal maxFine = (BigDecimal) engineResults.getAttribute(OLEConstants.MAX_FINE);
        String circulationPolicySetId = (String) engineResults.getAttribute(OLEConstants.CIRCULATION_POLICY_SET_ID);
        oleLoanDocument.setCirculationPolicyId(circulationPolicySetId != null ? circulationPolicySetId : OLEConstants.NO_CIRC_POLICY_FOUND);
        oleLoanDocument.setFineRate(maxFine != null ? maxFine : fineRate);
        oleLoanDocument.getErrorsAndPermission().clear();
        HashMap<String, String> errorsAndPermission = new HashMap<>();
        if( oleLoanDocument.isRenewalItemFlag() && checkPendingRequestforItem(oleLoanDocument.getItemUuid())){
            failures.append(OLEConstants.PENDING_RQST_RENEWAL_ITM_INFO+ OLEConstants.OR);
        }
        errorsAndPermission = (HashMap<String, String>) engineResults.getAttribute(OLEConstants.ERRORS_AND_PERMISSION);
        PermissionService service = KimApiServiceLocator.getPermissionService();
        if (dueDate == null) {
            oleLoanDocument.setDueDateEmpty(true);
        }
        int i = 1;
        if (errorsAndPermission != null) {
            Set<String> errorMessage = errorsAndPermission.keySet();
            if (errorMessage != null && errorMessage.size() > 0) {
                for (String errMsg : errorMessage) {
                    if (StringUtils.isNotEmpty(errMsg)) {
                        oleLoanDocument.getErrorsAndPermission().putAll(errorsAndPermission);
                        if (oleLoanDocument.isRenewalItemFlag()) {
                            String permission = errorsAndPermission.get(errMsg);
                            if(operatorId == null){
                                if(GlobalVariables.getUserSession()!=null){
                                   operatorId = GlobalVariables.getUserSession().getPrincipalId();
                                }
                            }
                            boolean hasRenewPermission = service.hasPermission(operatorId, OLEConstants.DLVR_NMSPC, permission);
                            if (!hasRenewPermission) {
                                oleLoanDocument.setRenewPermission(false);
                            } else {
                                oleLoanDocument.setRenewPermission(true);
                            }
                            failures.append(errMsg + OLEConstants.OR);
                        } else {
                            failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
                        }
                    }
                }
            }
            errorsAndPermission.clear();
        }
        List<String> errorMessage = (List<String>) engineResults.getAttribute(OLEConstants.ERROR_ACTION);
        if (errorMessage != null && errorMessage.size() > 0) {
            for (String errMsg : errorMessage) {
                if (StringUtils.isNotEmpty(errMsg)) {
                    if (errMsg.equalsIgnoreCase("Inform the current borrower that this item has some missing pieces before proceeding with checkout.")) {
                        if (item != null && item.getMissingPiecesCount() != null) {
                            errMsg = errMsg.replace("some", item.getMissingPiecesCount());
                        }
                    }
                    if (oleLoanDocument.isRenewalItemFlag()) {
                        failures.append(errMsg + OLEConstants.OR);
                    } else {
                        if (errMsg.equalsIgnoreCase("Item status is Lost")) {
                            //oleLoanDocument.setStatusLost(true);
                            failures.append(i++ + "." + errMsg + "-" + OLEConstants.ITEMSTATUSLOST + OLEConstants.BREAK);
                        } else {
                            failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
                        }
                    }
                }
            }
        }
        if(oleLoanDocument.getItemLoanStatus().equalsIgnoreCase(OLEConstants.ITEM_STATUS_CHECKEDOUT)&& !oleLoanDocument.isRenewalItemFlag()){
            Map barMap = new HashMap();
            barMap.put("itemId", oleLoanDocument.getItemId());
            List<OleLoanDocument> oleLoanDocuments = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class, barMap);
            if (oleLoanDocuments != null && oleLoanDocuments.size()>0) {
                String url = "<a target=\"_blank\" href=" + OLEConstants.ASSIGN_INQUIRY_PATRON_ID + oleLoanDocuments.get(0).getPatronId() + OLEConstants.ASSIGN_PATRON_INQUIRY + ">" + oleLoanDocuments.get(0).getPatronId() + "</a>";
                failures.append(i++ + ". " + OLEConstants.ITEM_STATUS_LOANED_ANOTHER_PATRON_PERMISSION + "&nbsp;&nbsp;:&nbsp;" + url + OLEConstants.BREAK);
            }
        }
        getDataCarrierService().addData(OLEConstants.ERROR_ACTION, null);
        getDataCarrierService().addData(OLEConstants.ERRORS_AND_PERMISSION, null);
        if (item.getItemType().getCodeValue().equalsIgnoreCase(OLEConstants.NONCIRC) || circulationPolicySetId == null) {
            oleLoanDocument.setNonCirculatingItem(true);
        }
        if ((!oleLoanDocument.isRenewalItemFlag() || (oleLoanDocument.isRenewalItemFlag() && failures.toString().isEmpty()))) {
            oleLoanDocument.setLoanDueDate(dueDate != null ? dueDate : null);
        }
        if ((!oleLoanDocument.isRenewalItemFlag() || (oleLoanDocument.isRenewalItemFlag()))) {
            oleLoanDocument.setRenewalLoanDueDate(dueDate != null ? dueDate : null);
        }
        if (!failures.toString().isEmpty()) {
            oleLoanDocument.setErrorMessage(failures.toString());
            return oleLoanDocument;
        }

        if (oleLoanDocument.isRenewalItemFlag()) {
            if (oleLoanDocument.getNumberOfRenewals() == null) {
                oleLoanDocument.setNumberOfRenewals(OLEConstants.ZERO);
            }
            String noOfRenewal = "" + (Integer.parseInt(oleLoanDocument.getNumberOfRenewals()) + 1);
            if(noOfRenewal!=null && !noOfRenewal.isEmpty()){
                oleLoanDocument.setNumberOfOverdueNoticesSent("0");
            }
            oleLoanDocument.setNumberOfRenewals(noOfRenewal);
            oleLoanDocument.setPastDueDate(pastDueDate);
            oleLoanDocument.setRenewalItemFlag(false);
        }
        Long beginSaveLoan = System.currentTimeMillis();
        saveLoan(oleLoanDocument);
        Long endSaveLoan = System.currentTimeMillis();
        Long timeTakenSaveLoan = endSaveLoan-beginSaveLoan;
        LOG.info("The Time Taken for save loan in Add Item"+timeTakenSaveLoan);
        return oleLoanDocument;
    }

    public String circulationDeskLocations(OleCirculationDesk oleCirculationDesk) throws Exception {
        String operatorsCirculationLocation = "";
        if (oleCirculationDesk != null) {
            List<OleLocation> oleLocations = new ArrayList<OleLocation>();
            if (oleCirculationDesk.getOleCirculationDeskLocations() != null) {
                for (OleCirculationDeskLocation oleCirculationDeskLocation : oleCirculationDesk.getOleCirculationDeskLocations()) {
                    oleLocations.add(oleCirculationDeskLocation.getLocation());
                }
            } else {
                throw new Exception(OLEConstants.NO_LOC_CIR_DESK);
            }
            StringBuffer location = new StringBuffer();
            for (OleLocation oleLocation : oleLocations) {
                location.append(getFullPathLocation(oleLocation)).append(OLEConstants.DELIMITER_HASH);
            }
            operatorsCirculationLocation = location.toString();
        }
        return operatorsCirculationLocation;
    }

    public String getFullPathLocationByName(OleLocation oleLocation) throws Exception {
        String levelFullCode = "";
        if (oleLocation != null) {
            levelFullCode = oleLocation.getLocationName();
            boolean parentId = oleLocation.getParentLocationId() != null ? true : false;
            while (parentId) {
                Map criteriaMap = new HashMap();
                criteriaMap.put(OLEConstants.LOCATION_ID, oleLocation.getParentLocationId());
                OleLocation location = businessObjectService.findByPrimaryKey(OleLocation.class,
                        criteriaMap);
                if (levelFullCode != null) {
                    levelFullCode = location.getLocationName() + "-" + levelFullCode;
                }
                parentId = location.getParentLocationId() != null ? true : false;
                oleLocation = location;
            }
        }
        return levelFullCode;
    }

    public String getFullPathLocation(OleLocation oleLocation) throws Exception {
        String levelFullCode = "";
        if (oleLocation != null) {
            levelFullCode = oleLocation.getLocationCode();
            boolean parentId = oleLocation.getParentLocationId() != null ? true : false;
            while (parentId) {
                Map criteriaMap = new HashMap();
                criteriaMap.put(OLEConstants.LOCATION_ID, oleLocation.getParentLocationId());
                OleLocation location = businessObjectService.findByPrimaryKey(OleLocation.class,
                        criteriaMap);
                if (levelFullCode != null) {
                    levelFullCode = location.getLocationCode() + OLEConstants.SLASH + levelFullCode;
                }
                parentId = location.getParentLocationId() != null ? true : false;
                oleLocation = location;
            }
        }
        return levelFullCode;
    }

    /**
     * Retrieves Holding Object for given instance UUID.
     *
     * @param instanceUUID
     * @return
     * @throws Exception
     */
    public OleHoldings getOleHoldings(String instanceUUID) throws Exception {
        LOG.debug("--Inside getOleHoldings---");

        Holdings holdings = new Holdings();


        holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(instanceUUID);


        HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
        OleHoldings oleHoldings = holdingOlemlRecordProcessor.fromXML(holdings.getContent());


        return oleHoldings;
    }


    /*private boolean CheckItemStatusIsCheckedOut(Item oleItem) {
        LOG.debug("Inside the CheckItemStatusIsCheckedOut method");
        String itemStatus = oleItem.getItemStatus();
        if(itemStatus.equalsIgnoreCase(OLEConstants.ITEM_STATUS_CHECKEDOUT)){
            return true;
        }else {
            return false;
        }
    }*/

   /* *//**
     * Check item barcode exist in docstore.
     * @param barcode
     * @return
     * @throws Exception
     *//*
    public boolean validateFastAddItem(String barcode) throws Exception{
        LOG.debug("Inside the validateFastAddItem method");
        SolrRequestReponseHandler solrRequestReponseHandler = new SolrRequestReponseHandler();
        List<HashMap<String, Object>> documentList = null;
        if(barcode != null){
          documentList= solrRequestReponseHandler.retriveResults(OLEConstants.ITEM_BARCODE_DISPLAY+barcode);
        }
        if(documentList != null && documentList.size() >0){
            return false;
        }else{
            return true;
        }
    }*/

    /**
     * Compares expiration date with due date.
     *
     * @param oleLoanDocument
     */
    private void compareExpirationDateWithDueDate(OleLoanDocument oleLoanDocument) {
        LOG.debug("Inside the compareExpirationDateWithDueDate method");
        if (oleLoanDocument.getExpirationDate() != null && oleLoanDocument.getLoanDueDate()!=null) {
            Timestamp expirationDate = new Timestamp(oleLoanDocument.getExpirationDate().getTime());
           /* if (oleLoanDocument.getLoanDueDate() == null) {
                oleLoanDocument.setLoanDueDate(expirationDate);
            } else*/
            if (expirationDate.compareTo(oleLoanDocument.getLoanDueDate()) < 0) {
                oleLoanDocument.setLoanDueDate(expirationDate);
            }
        }
    }


    public EngineResults getEngineResults(String agendaName, HashMap<String, Object> termValues) throws Exception {
        Long begin = System.currentTimeMillis();
        LOG.debug("Inside the getEngineResults method");
        EngineResults engineResult = null;
        try {
            Engine engine = KrmsApiServiceLocator.getEngine();
            ContextDefinition contextDefinition = KrmsRepositoryServiceLocator.getContextBoService().getContextByNameAndNamespace("OLE-CONTEXT","OLE");
            AgendaDefinition agendaDefinition = KrmsRepositoryServiceLocator.getAgendaBoService().getAgendaByNameAndContextId(agendaName,contextDefinition.getId());
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.AGENDA_NAME,agendaDefinition.getName());
            List<MatchBo> matchBos = (List<MatchBo>) getBusinessObjectService().findMatching(MatchBo.class, map);

            SelectionCriteria selectionCriteria =
                    SelectionCriteria.createCriteria(null, getSelectionContext(contextDefinition.getName()), getAgendaContext(agendaName));
/*
            HashMap<String, Object> agendaValue = new HashMap<String, Object>();
            agendaValue.put(OLEConstants.NAME_NM, agendaName);
            List<AgendaBo> agendaBos = (List<AgendaBo>) getBusinessObjectService().findMatching(AgendaBo.class, agendaValue);
            AgendaBo agendaBo = agendaBos.get(0);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(OLEConstants.AGENDA_NAME, agendaBo.getName());
            List<MatchBo> matchBos = (List<MatchBo>) getBusinessObjectService().findMatching(MatchBo.class, map);

            SelectionCriteria selectionCriteria =
                    SelectionCriteria.createCriteria(null, getSelectionContext(agendaBo.getContext().getName()), getAgendaContext(agendaName));*/

            Long end = System.currentTimeMillis();
            Long timeTaken = end - begin;
            LOG.info("-----------TimeTaken to complete inside KRMS Call-----------"+timeTaken);
            ExecutionOptions executionOptions = new ExecutionOptions();
            executionOptions.setFlag(ExecutionFlag.LOG_EXECUTION, true);

            Facts.Builder factBuilder = Facts.Builder.create();

            for (Iterator<MatchBo> matchBoIterator = matchBos.iterator(); matchBoIterator.hasNext(); ) {
                MatchBo matchBo = matchBoIterator.next();
                factBuilder.addFact(matchBo.getTermName(), termValues.get((matchBo.getTermName())));
            }
            long b1 = System.currentTimeMillis();
            engineResult = engine.execute(selectionCriteria, factBuilder.build(), executionOptions);
            long b2 = System.currentTimeMillis();
            long tot = b2 - b1;
            LOG.info("-----------TimeTaken to complete Only KRMS Call -----------"+tot);
        } catch (Exception krmsException) {
            LOG.error("-----------KRMS EXCEPTION------------------", krmsException);

            throw new RuntimeException(krmsException);
        }
        Long end = System.currentTimeMillis();
        Long timeTaken = end - begin;
        LOG.info("-----------TimeTaken to complete KRMS Call-----------"+agendaName+"-------"+timeTaken);
        return engineResult;
    }

    /**
     * This method returns selectionContext using contextName.
     *
     * @param contextName
     * @return Map
     * @throws Exception
     */
    protected Map<String, String> getSelectionContext(String contextName) throws Exception {
        LOG.debug("Inside the getSelectionContext method");
        Map<String, String> selector = new HashMap<String, String>();
        selector.put(NAMESPACE_CODE_SELECTOR, OLEConstants.OLE_NAMESPACE);
        selector.put(NAME_SELECTOR, contextName);
        return selector;
    }

    /**
     * This method returns agendaContext using agendaName.
     *
     * @param agendaName
     * @return Map
     * @throws Exception
     */
    protected Map<String, String> getAgendaContext(String agendaName) throws Exception {
        LOG.debug("Inside the getAgendaContext method");
        Map<String, String> selector = new HashMap<String, String>();
        selector.put(NAME_SELECTOR, agendaName);
        return selector;
    }


    /**
     * This method returns ItemTypeId using itemTypeName.
     *
     * @param itemTypeCode
     * @return OleInstanceItemType
     * @throws Exception
     */
    public OleInstanceItemType getItemTypeIdByItemType(String itemTypeCode) throws Exception {
        LOG.debug("Inside the getItemTypeIdByItemType method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.OleInstanceItemType.INSTANCE_ITEM_TYPE_CD, itemTypeCode);
        List<OleInstanceItemType> matchingItemType = (List<OleInstanceItemType>) getBusinessObjectService().findMatching(OleInstanceItemType.class, barMap);
        return matchingItemType.get(0);
    }

    /**
     * * This method returns location level using location name.
     *
     * @param levelName
     * @return List
     * @throws Exception
     */
    public OleLocationLevel getLocationLevelByName(String levelName) throws Exception {
        LOG.debug("Inside the getLocationLevelByName method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.LEVEL_NAME, levelName);
        List<OleLocationLevel> matchingLocationLevel = (List<OleLocationLevel>) getBusinessObjectService().findMatching(OleLocationLevel.class, barMap);
        return matchingLocationLevel.get(0);
    }

    private List<OleLocationLevel> getLocationLevel() throws Exception {
        LOG.debug("Inside the getLocationLevel method");
        List<OleLocationLevel> locationLevel = (List<OleLocationLevel>) getBusinessObjectService().findAllOrderBy(OleLocationLevel.class, OLEConstants.LEVEL_ID, true);

        return locationLevel;
    }

    /**
     * This method returns location using location code.
     *
     * @param locationCode
     * @return
     * @throws Exception
     */
    public OleLocation getLocationByLocationCode(String locationCode) throws Exception {
        LOG.debug("Inside the getLocationByLocationCode method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.LOC_CD, locationCode);
        List<OleLocation> matchingLocation = (List<OleLocation>) getBusinessObjectService().findMatching(OleLocation.class, barMap);
        return matchingLocation!=null && matchingLocation.size() >0 ? matchingLocation.get(0) : null;
    }

    /**
     * This method returns Location using locationId.
     *
     * @param locationId
     * @return OleLocation
     * @throws Exception
     */
    public OleLocation getLocationByLocationId(String locationId) throws Exception {
        LOG.debug("Inside the getLocationByLocationId method");
        Map location = new HashMap();
        location.put(OLEConstants.LOCATION_ID, locationId);
        OleLocation oleLocation = getBusinessObjectService().findByPrimaryKey(OleLocation.class, location);
        return oleLocation;
    }


    /**
     * This method returns LoanStatusId.
     *
     * @return String
     * @throws Exception
     */
    private String getLoanStatusId() throws Exception {
        LOG.debug("Inside the getLoanStatusId method");
        Map barMap1 = new HashMap();
        barMap1.put(OLEConstants.OleItemAvailableStatus.ITEM_AVAILABLE_STATUS_CD, OLEConstants.ITEM_STATUS_CHECKEDOUT);
        List<OleItemAvailableStatus> matchingOleLoanStatus = (List<OleItemAvailableStatus>) getBusinessObjectService().findMatching(OleItemAvailableStatus.class, barMap1);
        if (matchingOleLoanStatus.size() > 0)
            return matchingOleLoanStatus.get(0).getItemAvailableStatusId();
        return null;
    }

    /**
     * sets the value for location levels in Loan Document.
     *
     * @param locationLevelName
     * @param locationCode
     * @param oleLoanDoc
     * @return OleLoanDocument
     * @throws Exception
     */
    private OleLoanDocument setLocation(StringBuffer location,StringBuffer locationCodeBuff ,String locationLevelName, String locationCode, String locationName, OleLoanDocument oleLoanDoc) throws Exception {
        LOG.debug("Inside the setLocation method");
        if (locationCode != null) {
            if (locationLevelName.equalsIgnoreCase(OLEConstants.LOCATION_LEVEL_SHELVING)) {
                location.append(locationName);
                locationCodeBuff.append(locationCode);
                oleLoanDoc.setItemLocation(locationCode);
            } else if (locationLevelName.equalsIgnoreCase(OLEConstants.LOCATION_LEVEL_COLLECTION)) {

                location.append(locationName + "-");
                locationCodeBuff.append(locationCode + "/");
                oleLoanDoc.setItemCollection(locationCode);

            } else if (locationLevelName.equalsIgnoreCase(OLEConstants.LOCATION_LEVEL_LIBRARY)) {
                location.append(locationName + "-");
                locationCodeBuff.append(locationCode + "/");
                oleLoanDoc.setItemLibrary(locationCode);
            } else if (locationLevelName.equalsIgnoreCase(OLEConstants.LOCATION_LEVEL_INSTITUTION)) {
                location.append(locationName + "-");
                locationCodeBuff.append(locationCode + "/");
                oleLoanDoc.setItemInstitution(locationCode);
            } else if (locationLevelName.equalsIgnoreCase(OLEConstants.LOCATION_LEVEL_CAMPUS)) {
                location.append(locationName + "-");
                locationCodeBuff.append(locationCode + "/");
                oleLoanDoc.setItemCampus(locationCode);
            }
        }
        return oleLoanDoc;

    }

    /**
     * This method returns location details.
     *
     * @param oleItem
     * @param oleLoanDoc
     * @throws Exception
     */
    public void getLocation(org.kuali.ole.docstore.common.document.content.instance.Item oleItem, OleLoanDocument oleLoanDoc) throws Exception {
        LOG.debug("Inside the getLocation method");
        try {
            Location physicalLocation = oleItem.getLocation();
            LocationLevel locationLevel = null;
            locationLevel = physicalLocation.getLocationLevel();
            getOleLocationLevel(oleLoanDoc, locationLevel);
        } catch (Exception itemException) {
            LOG.error("--------------Invalid Item location data.---------------");
            try {
                OleHoldings oleHoldings = getOleHoldings(oleLoanDoc.getInstanceUuid());
                if (oleHoldings != null) {
                    Location physicalLocation = oleHoldings.getLocation();
                    LocationLevel locationLevel = null;
                    locationLevel = physicalLocation.getLocationLevel();
                    getOleLocationLevel(oleLoanDoc, locationLevel);
                }
            } catch (Exception holdingException) {
                LOG.error("--------------Invalid Holding location data.---------------" + holdingException, holdingException);
                throw new Exception(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVAL_LOC));
            }
        }
    }

    /**
     * This method returns location details.
     *
     * @param oleItem
     * @param oleLoanDoc
     * @throws Exception
     */
    public void getLocation(org.kuali.ole.docstore.common.document.content.instance.Item oleItem, OleLoanDocument oleLoanDoc,org.kuali.ole.docstore.common.document.Item item) throws Exception {
        LOG.debug("Inside the getLocation method");
        try {
            Location physicalLocation = oleItem.getLocation();
            LocationLevel locationLevel = null;
            locationLevel = physicalLocation.getLocationLevel();
            getOleLocationLevel(oleLoanDoc, locationLevel);
        } catch (Exception itemException) {
            LOG.error("--------------Invalid Item location data.---------------");
            try {
                OleHoldings oleHoldings = new HoldingOlemlRecordProcessor().fromXML(item.getHolding().getContent());
                if (oleHoldings != null) {
                    Location physicalLocation = oleHoldings.getLocation();
                    LocationLevel locationLevel = null;
                    locationLevel = physicalLocation.getLocationLevel();
                    getOleLocationLevel(oleLoanDoc, locationLevel);
                }
            } catch (Exception holdingException) {
                LOG.error("--------------Invalid Holding location data.---------------" + holdingException, holdingException);
                throw new Exception(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVAL_LOC));
            }
        }
    }

    /**
     * Populate location levels.
     *
     * @param oleLoanDocument
     * @param locationLevel
     * @throws Exception
     */
    public void getOleLocationLevel(OleLoanDocument oleLoanDocument, LocationLevel locationLevel) throws Exception {
        LOG.debug("Inside the getOleLocationLevel method");
        StringBuffer location = new StringBuffer();
        StringBuffer locationCodeBuff = new StringBuffer();
        while (locationLevel.getLocationLevel() != null) {
            OleLocationLevel oleLocationLevel = getLocationLevelByName(locationLevel.getLevel());
            OleLocation oleLocation = new OleLocation();
            if (!"".equals(locationLevel.getName())) {
                oleLocation = getLocationByLocationCode(locationLevel.getName());
                oleLoanDocument.setOleLocation(oleLocation);
            }
            setLocation(location, locationCodeBuff,oleLocationLevel.getLevelName(), oleLocation.getLocationCode(), oleLocation.getLocationName(), oleLoanDocument);
            OleCirculationDesk oleCirculationDesk = getCirculationDeskByLocationId(oleLocation.getLocationId());
            if(oleCirculationDesk!=null){
                oleLoanDocument.setLocationCode(oleCirculationDesk.getCirculationDeskCode());
                oleLoanDocument.setRouteToLocationName(oleCirculationDesk.getCirculationDeskPublicName());
            }
            locationLevel = locationLevel.getLocationLevel();
        }

        oleLoanDocument.setItemLocation(locationLevel.getName());
        OleLocation oleLocation = getLocationByLocationCode(oleLoanDocument.getItemLocation());
        OleCirculationDesk oleCirculationDesk = getCirculationDeskByLocationId(oleLocation.getLocationId());
        OleDeliverRequestBo oleDeliverRequestBo = getPrioritizedRequest(oleLoanDocument.getItemUuid());
        OleCirculationDesk pickUpCircDesk = oleDeliverRequestBo != null && oleDeliverRequestBo.getPickUpLocationId() != null ? getOleCirculationDesk(oleDeliverRequestBo.getPickUpLocationId()) : null;
        OleCirculationDesk destinationCircDesk = oleDeliverRequestBo != null && oleDeliverRequestBo.getCirculationLocationId() != null ? getOleCirculationDesk(oleDeliverRequestBo.getCirculationLocationId()) : null;
        if (pickUpCircDesk != null) {
            oleLoanDocument.setLocationCode(pickUpCircDesk.getCirculationDeskCode());
            oleLoanDocument.setRouteToLocationName(pickUpCircDesk.getCirculationDeskPublicName());
        } else if (destinationCircDesk != null) {
            oleLoanDocument.setLocationCode(destinationCircDesk.getCirculationDeskCode());
            oleLoanDocument.setRouteToLocationName(destinationCircDesk.getCirculationDeskPublicName());
        } else if (oleCirculationDesk != null) {
            oleLoanDocument.setLocationCode(oleCirculationDesk.getCirculationDeskCode());
            oleLoanDocument.setRouteToLocationName(oleCirculationDesk.getCirculationDeskPublicName());
        } else {
            oleLoanDocument.setRouteToLocationName(oleLocation.getLocationName());
        }
        location.append(oleLocation.getLocationName());
        locationCodeBuff.append(oleLocation.getLocationCode());
        oleLoanDocument.setLocation(location.toString());
        oleLoanDocument.setOleLocationCode(locationCodeBuff.toString());
        // }
    }


    /**
     * This method sets Title,loan Status,loan approver,loan operator,borrowerLimit and Item Unavailable in Loan Document..
     *
     * @param oleLoanDocument
     * @return OleLoanDocument
     * @throws Exception
     */
    public OleLoanDocument setLoan(OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the setLoan method");
        if (StringUtils.isEmpty(oleLoanDocument.getTitle())) {
            org.kuali.ole.docstore.common.document.Item item = new ItemOleml();
            org.kuali.ole.docstore.common.search.SearchParams search_Params = new org.kuali.ole.docstore.common.search.SearchParams();
            SearchResponse searchResponse = null;
            if (oleLoanDocument.getBibUuid() == null) {
                if (oleLoanDocument.getItemId() != null) {
                    search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), item.ITEM_BARCODE, oleLoanDocument.getItemId()), ""));
                } else {
                    search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.ID, oleLoanDocument.getItemUuid()), ""));
                }
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.BIBIDENTIFIER));
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.TITLE));
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.AUTHOR));
            } else {
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), Bib.ID, oleLoanDocument.getBibUuid()), ""));
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(DocType.BIB.getCode(), Bib.TITLE));
                search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), Bib.AUTHOR));
            }
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
            for (SearchResult searchResult : searchResponse.getSearchResults()) {
                for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                    String fieldName = searchResultField.getFieldName();
                    String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";
                    if (fieldName.equalsIgnoreCase(Bib.TITLE) && !fieldValue.isEmpty()) {
                        oleLoanDocument.setTitle(fieldValue);
                    } else if (fieldName.equalsIgnoreCase(Bib.AUTHOR) && !fieldValue.isEmpty()) {
                        oleLoanDocument.setAuthor(fieldValue);
                    }
                }
            }
        }
        oleLoanDocument.setLoanStatusId(getLoanStatusId());
        //  String circPolciyId = oleLoanDocument.getCirculationPolicySetId();
        //  oleLoanDocument.setCirculationPolicyId(circPolciyId!=null?circPolciyId :OLEConstants.NO_CIRC_POLICY_FOUND);
        //commented for jira OLE-5675
        //oleLoanDocument.setMachineId("MACH12233");  // TODO: Need to fetch machine IP.
        oleLoanDocument.setCreateDate(new Timestamp(new Date().getTime()));
        String principalId = "";
        if (oleLoanDocument.getLoanOperatorId() == null) {
            oleLoanDocument.setLoanOperatorId(principalId);
        }
        //oleLoanDocument.setLoanApproverId("");
        if (GlobalVariables.getUserSession() != null) {
            principalId = GlobalVariables.getUserSession().getPrincipalId();
            String principalName = GlobalVariables.getUserSession().getPrincipalName();
            /*if (GlobalVariables.getUserSession().getPerson() != null) {
                oleLoanDocument.setLoanApproverId(GlobalVariables.getUserSession().getPerson().getEntityId());
            }*/
            GlobalVariables.getUserSession().clearBackdoorUser();
            if (!principalId.equals(GlobalVariables.getUserSession().getPrincipalId())) {
                oleLoanDocument.setLoanOperatorId(principalId);
                GlobalVariables.getUserSession().setBackdoorUser(principalName);
            } else
                oleLoanDocument.setLoanOperatorId(principalId);
        }
        return oleLoanDocument;
    }

    /**
     * persist the loan document and update item status to docstore.
     *
     * @param oleLoanDocument
     */
    public void saveLoan(OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the saveLoan method");
        if (oleLoanDocument != null) {
            if (oleLoanDocument.getProxyPatronId() != null) {
                String proxyPatronId = oleLoanDocument.getProxyPatronId();
                oleLoanDocument.setProxyPatronId(oleLoanDocument.getPatronId());
                oleLoanDocument.setPatronId(proxyPatronId);
            }
            if (oleLoanDocument.getFineRate() != null) {
                if (oleLoanDocument.getOlePatron() == null) {
                    oleLoanDocument.setOlePatron(getOlePatronDocument(oleLoanDocument.getPatronId()));
                }
                if (oleLoanDocument.getFineRate().intValue() > 0)
                    generatePatronBillPayment(oleLoanDocument, OLEConstants.OVERDUE_FINE, oleLoanDocument.getFineRate());
            }
            compareExpirationDateWithDueDate(oleLoanDocument);
            getBusinessObjectService().save(oleLoanDocument);
            if (oleLoanDocument.isRequestPatron() || (!oleLoanDocument.isRequestPatron() && oleLoanDocument.getOleRequestId() != null && !"".equalsIgnoreCase(oleLoanDocument.getOleRequestId()))) {
                String operatorId = (oleLoanDocument.getLoanApproverId() != null && !oleLoanDocument.getLoanApproverId().isEmpty()) ? oleLoanDocument.getLoanApproverId() : oleLoanDocument.getLoanOperatorId();
                getOleDeliverRequestDocumentHelperService().deleteRequest(oleLoanDocument.getOleRequestId(), oleLoanDocument.getItemUuid(), operatorId, oleLoanDocument.getLoanId());
                OleDeliverRequestBo oleDeliverRequestBo = getPrioritizedRequest(oleLoanDocument.getItemUuid());
                if (oleDeliverRequestBo != null && oleDeliverRequestBo.getRequestTypeCode() != null &&
                        (oleDeliverRequestBo.getRequestTypeCode().contains(OLEConstants.OleDeliverRequest.RECALL_DELIVERY) ||
                                oleDeliverRequestBo.getRequestTypeCode().contains(OLEConstants.OleDeliverRequest.RECALL_HOLD))) {
                    getDocstoreUtil().isItemAvailableInDocStore(oleDeliverRequestBo);
                    getOleDeliverRequestDocumentHelperService().executeEngineResults(oleDeliverRequestBo);
                    oleLoanDocument.setSuccessMessage(OLEConstants.DUE_DATE_INFO);
                }
            }
            org.kuali.ole.docstore.common.document.content.instance.Item oleItem = oleLoanDocument.getOleItem();
            if(oleItem!=null){
                oleItem.setCurrentBorrower(oleLoanDocument.getPatronId());
                oleItem.setProxyBorrower(oleLoanDocument.getProxyPatronId());
        oleItem.setCheckOutDateTime(convertDateToString(oleLoanDocument.getCreateDate(),"MM/dd/yyyy HH:mm:ss"));
                if (oleLoanDocument.getLoanDueDate() != null) {
                    oleItem.setDueDateTime(convertToString(oleLoanDocument.getLoanDueDate()));
                }else{
                    oleItem.setDueDateTime("");
                }
                oleItem.setNumberOfRenew(Integer.parseInt(oleLoanDocument.getNumberOfRenewals()));
                postLoan(oleItem);
            }
            if (getItemStatus() != null) {
                oleLoanDocument.setItemStatus(getItemStatus());
            }
            if(oleLoanDocument.isRenewalItemFlag()){
                oleLoanDocument.setRenewalItemFlag(false);
                getOleDeliverNoticeHelperService().deleteDeliverNotices(oleLoanDocument.getLoanId());
            }
            getOleDeliverNoticeHelperService().generateDeliverNotices(oleLoanDocument);
        }
    }

    /**
     * This method is used to convert the date to string based on the given input format
     * @param format
     * @param date
     * @return
     */
    public String convertDateToString(Date date ,String format){
        LOG.info("Date Format : " +format + "Date : " + date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String dateValue ="";
         try{
            dateValue = simpleDateFormat.format(date);
        }catch(Exception e){
             LOG.error(e,e);
         }
        LOG.info("Formatted Date : " + dateValue);
       return dateValue;
    }

    public String convertToString(Timestamp date) {
//        Timestamp timestamp = Timestamp.valueOf(new SimpleDateFormat(OLEConstants.CHECK_IN_DATE_TIME_FORMAT).format(date).concat(" ").concat(new SimpleDateFormat("HH:mm:ss").format(new Date())));
        SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date itemDate = null;
        try {
            itemDate = format2.parse(date.toString());
        } catch (ParseException e) {
            LOG.error("format string to Date " + e);
        }
        return format1.format(itemDate).toString();
    }

    private XMLGregorianCalendar getGregorianCalendar(Date date) throws DatatypeConfigurationException {
        GregorianCalendar gregory = new GregorianCalendar();
        gregory.setTime(date);
        XMLGregorianCalendar dealCloseDate = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(gregory);
        return dealCloseDate;
    }

    public Collection getLoanObjectsFromDAO(List<OleLoanDocument> loanDocuments,String patronId){
        List<String> itemIds = new ArrayList<>();
        for(OleLoanDocument loanDocument : loanDocuments){
            itemIds.add(loanDocument.getItemUuid());
        }
        Collection loanObjects = getOleLoanDocumentDaoOjb().getLoanDocumentsUsingItemIdsAndPatronId(patronId,itemIds);
        return loanObjects;
    }

    public OleLoanDocument getLoanDocumentsUsingItemIdAndPatronId(String itemId ,String patronId){
        Map loanMap = new HashMap();
        loanMap.put("itemUuid",itemId);
        loanMap.put("patronId",patronId);
        List<OleLoanDocument> oleLoanDocuments = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class,loanMap);
        return oleLoanDocuments!=null && oleLoanDocuments.size()>0 ? oleLoanDocuments.get(0):null;
    }
    /**
     * Update loan due date in loan record.
     *
     * @param updateDueDate
     * @throws Exception
     */
    public boolean updateLoan(List<OleLoanDocument> updateDueDate, String patronId, boolean claimsReturn, boolean removeClaimsReturnFlag,String borrowerCode) throws Exception {
        Collection loanObjects = getLoanObjectsFromDAO(updateDueDate, patronId);
        int i = 0;
        LOG.debug("Inside the updateLoan method");
        if (loanObjects != null && loanObjects.size() > 0) {
            for (Object loanObject : loanObjects) {
                OleLoanDocument oleLoanDocument = (OleLoanDocument) loanObject;
                OleLoanDocument existingLoanObject = updateDueDate.size() > i ? updateDueDate.get(i) : null;
                if (existingLoanObject != null) {
                    existingLoanObject.setCheckNo(false);
                    if (existingLoanObject.getLoanDueDateToAlter() != null) {
                        Timestamp timestamp;
                        Pattern pattern;
                        Matcher matcher;
                        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OlePatron.PATRON_MAINTENANCE_DATE_FORMAT);
                        boolean timeFlag = false;
                        if (existingLoanObject.getLoanDueDateTimeToAlter() != null && !existingLoanObject.getLoanDueDateTimeToAlter().isEmpty()) {
                            String[] str = existingLoanObject.getLoanDueDateTimeToAlter().split(":");
                            pattern = Pattern.compile(OLEConstants.TIME_24_HR_PATTERN);
                            matcher = pattern.matcher(existingLoanObject.getLoanDueDateTimeToAlter());
                            timeFlag = matcher.matches();
                            if (timeFlag) {
                                if (str != null && str.length <= 2) {
                                    existingLoanObject.setLoanDueDateTimeToAlter(existingLoanObject.getLoanDueDateTimeToAlter() + OLEConstants.CHECK_IN_TIME_MS);
                                }
                                timestamp = Timestamp.valueOf(new SimpleDateFormat(OLEConstants.CHECK_IN_DATE_TIME_FORMAT).
                                        format(existingLoanObject.getLoanDueDateToAlter()).concat(" ").concat(existingLoanObject.getLoanDueDateTimeToAlter()));
                            } else {
                                return false;
                            }
                        } else if (fmt.format(existingLoanObject.getLoanDueDateToAlter()).compareTo(fmt.format(new Date())) == 0) {
                            String defaultCloseTime = getParameter(OLEParameterConstants.DEF_CLOSE_TIME);
                            timestamp = Timestamp.valueOf(new SimpleDateFormat(OLEConstants.CHECK_IN_DATE_TIME_FORMAT).
                                    format(new Date()).concat(" ").concat(defaultCloseTime));
                        } else {
                            String defaultCloseTime = getParameter(OLEParameterConstants.DEF_CLOSE_TIME);
                            timestamp = Timestamp.valueOf(new SimpleDateFormat(OLEConstants.CHECK_IN_DATE_TIME_FORMAT).
                                    format(existingLoanObject.getLoanDueDateToAlter()).concat(" ").concat(defaultCloseTime));
                        }
                        existingLoanObject.setLoanDueDate(timestamp);
                        oleLoanDocument.setLoanDueDate(timestamp);
                    }
                    if (claimsReturn && existingLoanObject != null) {
                        oleLoanDocument.setClaimsReturnNote(existingLoanObject.getClaimsReturnNote());
                        oleLoanDocument.setClaimsReturnedDate(existingLoanObject.getClaimsReturnedDate());
                        oleLoanDocument.setClaimsReturnedIndicator(existingLoanObject.isClaimsReturnedIndicator());
                    }
                    getBusinessObjectService().save(oleLoanDocument);
                    i++;

                    String itemXmlContent = getItemXML(existingLoanObject.getItemUuid());
                    org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(existingLoanObject.getItemUuid());

                    org.kuali.ole.docstore.common.document.content.instance.Item oleItem = getItemPojo(itemXmlContent);
                    if (claimsReturn && !removeClaimsReturnFlag) {
                        oleItem.setClaimsReturnedFlag(existingLoanObject.isClaimsReturnedIndicator());
                        if (existingLoanObject.getClaimsReturnedDate() != null) {
                            oleItem.setClaimsReturnedFlagCreateDate(convertToString(existingLoanObject.getClaimsReturnedDate()));
                        }
                        oleItem.setClaimsReturnedNote(existingLoanObject.getClaimsReturnNote());
                        getOleDeliverRequestDocumentHelperService().cancelPendingRequestForClaimsReturnedItem(oleItem.getItemIdentifier());
                    } else if (removeClaimsReturnFlag) {
                        oleItem.setClaimsReturnedFlag(false);
                        oleItem.setClaimsReturnedFlagCreateDate(null);
                        oleItem.setClaimsReturnedNote(null);
                    } else {
                        if (existingLoanObject.getLoanDueDate() != null) {
                            oleItem.setDueDateTime(convertToString(existingLoanObject.getLoanDueDate()));
                        }
                    }
                    item.setId(existingLoanObject.getItemUuid());
                    item.setCategory(OLEConstants.WORK_CATEGORY);
                    item.setType(DocType.ITEM.getCode());
                    item.setFormat(OLEConstants.OLEML_FORMAT);
                    item.setContent(getItemOlemlRecordProcessor().toXML(oleItem));
                    getDocstoreClientLocator().getDocstoreClient().updateItem(item);
                    if(!claimsReturn){
                        Map<String,String> locationMap = existingLoanObject.getItemFullLocation()!= null ?
                                getOleDeliverRequestDocumentHelperService().getLocationMap(existingLoanObject.getItemFullLocation()) : new HashMap<String,String>();
                        if(oleLoanDocument.getOleCirculationDesk()==null){
                            OleCirculationDesk oleCirculationDesk = oleLoanDocument.getCirculationLocationId() != null ? getOleCirculationDesk(oleLoanDocument.getCirculationLocationId()) : null;
                            oleLoanDocument.setOleCirculationDesk(oleCirculationDesk);
                        }
                        if(existingLoanObject.getItemTypeName()==null){
                            if(oleItem!=null){
                                if(oleItem.getTemporaryItemType()!=null){
                                    existingLoanObject.setItemTypeName(oleItem.getTemporaryItemType().getCodeValue());
                                }else if(oleItem.getItemType()!=null){
                                    existingLoanObject.setItemTypeName(oleItem.getItemType().getCodeValue());
                                }
                            }
                        }
                        if(locationMap.size()>0){
                            existingLoanObject.setItemLibrary(locationMap.get(OLEConstants.ITEM_LIBRARY));
                            existingLoanObject.setItemInstitution(locationMap.get(OLEConstants.ITEM_INSTITUTION));
                            existingLoanObject.setItemCampus(locationMap.get(OLEConstants.ITEM_CAMPUS));
                            existingLoanObject.setItemCollection(locationMap.get(OLEConstants.ITEM_COLLECTION));
                            existingLoanObject.setItemLocation(locationMap.get(OLEConstants.ITEM_SHELVING));
                        }
                    }
                    getOleDeliverNoticeHelperService().deleteDeliverNotices(oleLoanDocument.getLoanId());
                    getOleDeliverNoticeHelperService().generateDeliverNotices(oleLoanDocument.getPatronId(), oleLoanDocument.getItemUuid(),
                            oleLoanDocument.getOleCirculationDesk()!=null ? oleLoanDocument.getOleCirculationDesk().getCirculationDeskCode() : null,
                            borrowerCode,existingLoanObject.getItemTypeName(), existingLoanObject.getItemStatus(),
                            existingLoanObject.isClaimsReturnedIndicator() ? OLEConstants.TRUE : OLEConstants.FALSE,
                            oleLoanDocument.getRepaymentFeePatronBillId() != null ? OLEConstants.TRUE : OLEConstants.FALSE,
                            existingLoanObject.getItemLocation(), existingLoanObject.getItemCollection(), existingLoanObject.getItemLibrary(),
                            existingLoanObject.getItemCampus(), existingLoanObject.getItemInstitution(), oleLoanDocument.getLoanDueDate(),oleLoanDocument.getLoanId());
                }
            }
        }
        return true;
    }


    public void updateItem(List<OleLoanDocument> loanList, boolean claimsReturn, boolean removeClaimsReturnFlag) throws Exception {
        if (loanList != null && !loanList.isEmpty()) {
            for (int existingLoan = 0; existingLoan < loanList.size(); existingLoan++) {
                OleLoanDocument oleLoanDocument = (OleLoanDocument) loanList.get(existingLoan);
                if (oleLoanDocument != null) {
                    String itemXmlContent = getItemXML(oleLoanDocument.getItemUuid());
                    org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(oleLoanDocument.getItemUuid());

                    org.kuali.ole.docstore.common.document.content.instance.Item oleItem = getItemPojo(itemXmlContent);
                    if (claimsReturn && !removeClaimsReturnFlag) {
                        oleItem.setClaimsReturnedFlag(oleLoanDocument.isClaimsReturnedIndicator());
                        if (oleLoanDocument.getClaimsReturnedDate() != null) {
                            oleItem.setClaimsReturnedFlagCreateDate(convertToString(oleLoanDocument.getClaimsReturnedDate()));
                        }
                        oleItem.setClaimsReturnedNote(oleLoanDocument.getClaimsReturnNote());
                        getOleDeliverRequestDocumentHelperService().cancelPendingRequestForClaimsReturnedItem(oleItem.getItemIdentifier());
                    } else if (removeClaimsReturnFlag) {
                        oleItem.setClaimsReturnedFlag(false);
                        oleItem.setClaimsReturnedFlagCreateDate(null);
                        oleItem.setClaimsReturnedNote(null);
                    } else {
                        if (oleLoanDocument.getLoanDueDate() != null) {
                            oleItem.setDueDateTime(convertToString(oleLoanDocument.getLoanDueDate()));
                        }
                    }
                    item.setId(oleLoanDocument.getItemUuid());
                    item.setCategory(OLEConstants.WORK_CATEGORY);
                    item.setType(DocType.ITEM.getCode());
                    item.setFormat(OLEConstants.OLEML_FORMAT);
                    item.setContent(getItemOlemlRecordProcessor().toXML(oleItem));
                    getDocstoreClientLocator().getDocstoreClient().updateItem(item);

                    //LOG.info(itemRecordUpdateResponse);
                }
            }
        }
    }

    /**
     * Compares the checked items from existing and current loan session and form a common list.
     *
     * @param loanList
     * @param existingLoanList
     * @param claimsFlag
     * @param claimsReturnNote
     * @return
     */
    public List<OleLoanDocument> setListValues(List<OleLoanDocument> loanList, List<OleLoanDocument> existingLoanList, boolean claimsFlag, String claimsReturnNote, boolean removeClaimsReturnFlag) {
        LOG.debug("Inside the setListValues method");
        List<OleLoanDocument> commonList = new ArrayList<OleLoanDocument>();
        List<OleLoanDocument> changeList = new ArrayList<OleLoanDocument>();
        if (loanList != null && !loanList.isEmpty()) {
            for (int curremtLoan = 0; curremtLoan < loanList.size(); curremtLoan++) {
                OleLoanDocument oleLoanDocument = (OleLoanDocument) loanList.get(curremtLoan);
                if (oleLoanDocument.isCheckNo()) {
                    if (claimsFlag) {
                        oleLoanDocument.setClaimsReturnNote(claimsReturnNote);
                        oleLoanDocument.setClaimsReturnedIndicator(claimsFlag);
                        oleLoanDocument.setClaimsReturnedDate(new Timestamp(new Date().getTime()));
                    } else if (removeClaimsReturnFlag) {
                        oleLoanDocument.setClaimsReturnedIndicator(false);
                        oleLoanDocument.setClaimsReturnedDate(null);
                        oleLoanDocument.setClaimsReturnNote(null);
                    } else {
                        if (oleLoanDocument.getLoanDueDate() != null) {
                        oleLoanDocument.setPastDueDate(oleLoanDocument.getLoanDueDate());
                        oleLoanDocument.setLoanDueDateToAlter(new Date(oleLoanDocument.getLoanDueDate().getTime()));
                        }
                    }
                    commonList.add(oleLoanDocument);
                }
            }
        }
        if (existingLoanList != null && !existingLoanList.isEmpty()) {
            for (int existingLoan = 0; existingLoan < existingLoanList.size(); existingLoan++) {
                OleLoanDocument oleLoanDocument = (OleLoanDocument) existingLoanList.get(existingLoan);
                if (oleLoanDocument.isCheckNo()) {
                    if (claimsFlag) {
                        oleLoanDocument.setClaimsReturnNote(claimsReturnNote);
                        oleLoanDocument.setClaimsReturnedIndicator(true);
                        oleLoanDocument.setClaimsReturnedDate(new Timestamp(new Date().getTime()));
                    } else if (removeClaimsReturnFlag) {
                        oleLoanDocument.setClaimsReturnedIndicator(false);
                        oleLoanDocument.setClaimsReturnedDate(null);
                        oleLoanDocument.setClaimsReturnNote(null);
                    } else {
                        if (oleLoanDocument.getLoanDueDate() != null) {
                            oleLoanDocument.setPastDueDate(oleLoanDocument.getLoanDueDate());
                            oleLoanDocument.setLoanDueDateToAlter(new Date(oleLoanDocument.getLoanDueDate().getTime()));
                        }
                    }
                commonList.add(oleLoanDocument);
            }
        }
        }

        return commonList;
    }

    /**
     * This method creates and return itemContent using OleItem
     *
     * @param oleItem
     * @return String
     * @throws Exception
     */
    public String buildItemContent(org.kuali.ole.docstore.common.document.content.instance.Item oleItem) throws Exception {
        LOG.debug("Inside the buildItemContent method");
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.setCodeValue(OLEConstants.ITEM_STATUS_CHECKEDOUT);
        itemStatus.setFullValue(OLEConstants.ITEM_STATUS_CHECKEDOUT);
        this.setItemStatus(OLEConstants.ITEM_STATUS_CHECKEDOUT);
        oleItem.setItemStatus(itemStatus);
        oleItem.setItemStatusEffectiveDate(String.valueOf(new SimpleDateFormat(OLEConstants.DAT_FORMAT_EFFECTIVE).format(new Date())));
        String itemContent = getItemOlemlRecordProcessor().toXML(oleItem);
        return itemContent;
    }

    /**
     * This method invokes docStore to store item and returns itemRecordUpdateResponse.
     *
     * @param oleItem
     * @throws Exception
     */
    public void postLoan(org.kuali.ole.docstore.common.document.content.instance.Item oleItem) throws Exception {
        LOG.debug("Inside the postLoan method");
        try {
            String itemUuid = oleItem.getItemIdentifier();
            String itemXmlContent = buildItemContent(oleItem);
            Item item = new ItemOleml();
            item.setId(itemUuid);
            item.setContent(itemXmlContent);
            item.setCategory(OLEConstants.WORK_CATEGORY);
            item.setType(DocType.ITEM.getCode());
            item.setFormat(OLEConstants.OLEML_FORMAT);
            item.setStaffOnly(oleItem.isStaffOnlyFlag());
            getDocstoreClientLocator().getDocstoreClient().updateItem(item);
        } catch (Exception e) {
            LOG.error(OLEConstants.ITM_STS_TO_DOC_FAIL + e, e);
            throw new Exception(OLEConstants.ITM_STS_TO_DOC_FAIL);
        }
    }


    public BibliographicRecord getBibliographicRecord(String title, String author) {
        LOG.debug("Inside the getBibliographicRecord method");

        BibliographicRecord bibliographicRecord = new BibliographicRecord();

        List<DataField> dataFieldList = new ArrayList<DataField>();
        DataField titleDataField = new DataField();
        titleDataField.setTag(OLEConstants.MARC_EDITOR_TITLE_245);
        List<SubField> subFields = new ArrayList<SubField>();
        SubField subField = new SubField();
        subField.setCode(OLEConstants.A);
        subField.setValue(title);
        subFields.add(subField);
        titleDataField.setSubFields(subFields);
        dataFieldList.add(titleDataField);

        if (author != null && !author.trim().isEmpty()) {
            DataField authorDataField = new DataField();
            authorDataField.setTag(OLEConstants.MARC_EDITOR_TITLE_100);
            subFields = new ArrayList<SubField>();
            subField = new SubField();
            subField.setCode(OLEConstants.A);
            subField.setValue(author);
            subFields.add(subField);
            authorDataField.setSubFields(subFields);
            dataFieldList.add(authorDataField);
        }
        bibliographicRecord.setDatafields(dataFieldList);
        return bibliographicRecord;

    }

    public BibMarcRecord getBibMarcRecord(String title, String author) {
        LOG.debug("Inside the getBibliographicRecord method");

        BibMarcRecord bibMarcRecord = new BibMarcRecord();
        List<org.kuali.ole.docstore.common.document.content.bib.marc.DataField> dataFieldList = new ArrayList<org.kuali.ole.docstore.common.document.content.bib.marc.DataField>();
        org.kuali.ole.docstore.common.document.content.bib.marc.DataField titleDataField = new org.kuali.ole.docstore.common.document.content.bib.marc.DataField();
        titleDataField.setTag(OLEConstants.MARC_EDITOR_TITLE_245);
        List<org.kuali.ole.docstore.common.document.content.bib.marc.SubField> subFields = new ArrayList<org.kuali.ole.docstore.common.document.content.bib.marc.SubField>();
        org.kuali.ole.docstore.common.document.content.bib.marc.SubField subField = new org.kuali.ole.docstore.common.document.content.bib.marc.SubField();
        subField.setCode(OLEConstants.A);
        subField.setValue(title);
        subFields.add(subField);
        titleDataField.setSubFields(subFields);
        dataFieldList.add(titleDataField);

        if (author != null && !author.trim().isEmpty()) {
            org.kuali.ole.docstore.common.document.content.bib.marc.DataField authorDataField = new org.kuali.ole.docstore.common.document.content.bib.marc.DataField();
            authorDataField.setTag(OLEConstants.MARC_EDITOR_TITLE_100);
            subFields = new ArrayList<org.kuali.ole.docstore.common.document.content.bib.marc.SubField>();
            subField = new org.kuali.ole.docstore.common.document.content.bib.marc.SubField();
            subField.setCode(OLEConstants.A);
            subField.setValue(author);
            subFields.add(subField);
            authorDataField.setSubFields(subFields);
            dataFieldList.add(authorDataField);
        }

        bibMarcRecord.setDataFields(dataFieldList);
        return bibMarcRecord;
    }

    public LocationLevel createLocationLevel(String locationName, LocationLevel locationLevel) {
        LOG.debug("Inside the createLocationLevel method");
        if (locationName != null && !locationName.equalsIgnoreCase("")) {
            BusinessObjectService businessObjectService = KRADServiceLocator.getBusinessObjectService();
            String[] names = locationName.split("/");
            Map parentCriteria = new HashMap();
            parentCriteria.put(OLEConstants.LOC_CD, names[0]);
            OleLocation oleLocationCollection = businessObjectService.findByPrimaryKey(OleLocation.class, parentCriteria);
            String levelName = oleLocationCollection.getLocationName();
            String locationCode = oleLocationCollection.getLocationCode();
            String levelCode = oleLocationCollection.getOleLocationLevel().getLevelName();
            locationLevel.setName(locationCode);
            locationLevel.setLevel(levelCode);
            String locName = "";
            if (locationName.contains(OLEConstants.SLASH))
                locName = locationName.replace(names[0] + OLEConstants.SLASH, "");
            else
                locName = locationName.replace(names[0], "");
            if (locName != null && !locName.equals("")) {
                LocationLevel newLocationLevel = new LocationLevel();
                locationLevel.setLocationLevel(createLocationLevel(locName, newLocationLevel));
            }
        }
        return locationLevel;
    }

    public OleHoldings getHoldingRecord(OleLoanFastAdd oleLoanFastAdd) {
        OleHoldings oleHolding = new OleHoldings();
        LocationLevel locationLevel = new LocationLevel();
        locationLevel = createLocationLevel(oleLoanFastAdd.getLocationName(), locationLevel);
        Location location = new Location();
        location.setPrimary(OLEConstants.TRUE);
        location.setStatus(OLEConstants.PERMANENT);
        location.setLocationLevel(locationLevel);

        oleHolding.setLocation(location);
        oleHolding.setCallNumber(getCallNumber(oleLoanFastAdd));
        oleHolding.setPrimary(OLEConstants.TRUE);
        //oleHolding.setExtension(getExtension());
        return oleHolding;
    }

    public org.kuali.ole.docstore.common.document.content.instance.Item getItemRecord(OleLoanFastAdd oleLoanFastAdd) {
        LOG.debug("Inside the getItemRecord method");
        org.kuali.ole.docstore.common.document.content.instance.Item item = new org.kuali.ole.docstore.common.document.content.instance.Item();

        ItemType itemType = new ItemType();
        itemType.setCodeValue(oleLoanFastAdd.getItemType());
        item.setItemType(itemType);
        item.setCallNumber(getCallNumber(oleLoanFastAdd));
        item.setCopyNumberLabel(oleLoanFastAdd.getCopyNumber());
        item.setEnumeration(oleLoanFastAdd.getEnumeration());
        item.setCheckinNote(oleLoanFastAdd.getCheckinNote());
        item.setFastAddFlag(true);
        item.setNumberOfPieces(oleLoanFastAdd.getNumberOfPieces());
    /* LocationLevel locationLevel=new LocationLevel();
        locationLevel.setName(oleLoanFastAdd.getLocationName());
     Location value=new Location();
*/


        List<Note> notes = new ArrayList<Note>();
        Note note = new Note();
        note.setValue(oleLoanFastAdd.getNote());
        notes.add(note);
        item.setNote(notes);

        AccessInformation accessInformation = new AccessInformation();
        accessInformation.setBarcode(oleLoanFastAdd.getBarcode());

        item.setAccessInformation(accessInformation);
        OleItemAvailableStatus itemAvailableStatus = validateAndGetItemStatus(getParameter(OLEParameterConstants.FAST_ADD_ITEM_DEFAULT_STATUS));
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.setCodeValue(itemAvailableStatus != null ? itemAvailableStatus.getItemAvailableStatusCode() : null);
        itemStatus.setFullValue(itemAvailableStatus != null ? itemAvailableStatus.getItemAvailableStatusCode() : null);
        item.setItemStatus(itemStatus);
        item.setCopyNumber(oleLoanFastAdd.getCopyNumber());
        //item.setExtension(getExtension());

        return item;
    }

    public OleItemAvailableStatus validateAndGetItemStatus(String itemStatusCode) {
        Map criteriaMap = new HashMap();
        criteriaMap.put(OLEConstants.ITEM_STATUS_CODE, itemStatusCode);
        OleItemAvailableStatus itemAvailableStatus = getBusinessObjectService().findByPrimaryKey(OleItemAvailableStatus.class, criteriaMap);
        return itemAvailableStatus;
    }

    public boolean canOverrideLoan(String principalId) {
        LOG.debug("Inside the canOverrideLoan method");
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CAN_OVERRIDE_LOAN);
    }

    /*public boolean canLoan(String principalId) {
        LOG.debug("Inside the canLoan method");
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CAN_LOAN);
    }*/

    public boolean hasCirculationDesk(String principalId) {
        boolean hasCirculationDesk = false;
        String circkDesk = null;
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("operatorId", principalId);
        List<OleCirculationDeskDetail> oleCirculationDeskDetails = (List<OleCirculationDeskDetail>) KRADServiceLocator.getBusinessObjectService().findMatching(OleCirculationDeskDetail.class, userMap);
        if (oleCirculationDeskDetails != null && oleCirculationDeskDetails.size() > 0) {
            hasCirculationDesk = true;
        }
        return hasCirculationDesk;
    }

    public boolean canEdit(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.EDIT_PATRON);
    }

    public String patronNameURL(String loginUser, String patronId) {
        boolean canEdit = canEdit(loginUser);
        String patronNameURL = "";
        if (canEdit) {
            patronNameURL = OLEConstants.ASSIGN_EDIT_PATRON_ID + patronId + OLEConstants.ASSIGN_PATRON_MAINTENANCE_EDIT;
        } else {
            patronNameURL = OLEConstants.ASSIGN_INQUIRY_PATRON_ID + patronId + OLEConstants.ASSIGN_PATRON_INQUIRY;
        }
        return patronNameURL;
    }

    public boolean checkOverRidePermission(String principalId, OleLoanForm oleLoanForm) {
        LOG.debug("Inside the checkOverRidePermission method");
        boolean overRideFlag = true;
        PermissionService service = KimApiServiceLocator.getPermissionService();
        if (oleLoanForm.getErrorsAndPermission() != null) {
            Set<String> errorMessages = oleLoanForm.getErrorsAndPermission().keySet();
            for (String errorMessage : errorMessages) {
                if (StringUtils.isNotEmpty(errorMessage)) {
                    String permission = oleLoanForm.getErrorsAndPermission().get(errorMessage);
                    boolean hasPermission = service.hasPermission(principalId, OLEConstants.DLVR_NMSPC, permission);
                    if (!hasPermission) {
                        if (errorMessage.equalsIgnoreCase(OLEConstants.GENERAL_BLOCK_MESSAGE)) {
                            errorMessage = errorMessage + " " + OLEConstants.SEMI_COLON + " " + oleLoanForm.getDummyLoan().getOlePatron().getGeneralBlockNotes();
                        }
                        oleLoanForm.setOverrideErrorMessage(oleLoanForm.getOverrideErrorMessage() == null ?
                                errorMessage + OLEConstants.BREAK : oleLoanForm.getOverrideErrorMessage() + errorMessage + OLEConstants.BREAK);
                        overRideFlag = false;
                    }
                }
            }
            if (StringUtils.isNotEmpty(oleLoanForm.getMessage())) {
                if (oleLoanForm.getMessage().contains(OLEConstants.OVERDUE_DAY_LIMIT_ERROR)) {
                    boolean hasOverdueDayPermission = service.hasPermission(principalId, OLEConstants.DLVR_NMSPC, OLEConstants.PATRON_OVERDUE_DAY);
                    if (!hasOverdueDayPermission) {
                        oleLoanForm.setOverrideErrorMessage(oleLoanForm.getOverrideErrorMessage() == null ?
                                OLEConstants.OVERDUE_DAY_LIMIT_ERROR + OLEConstants.BREAK : oleLoanForm.getOverrideErrorMessage() + OLEConstants.OVERDUE_DAY_LIMIT_ERROR + OLEConstants.BREAK);
                        overRideFlag = false;
                    }
                }
                if (oleLoanForm.getMessage().contains(OLEConstants.RECALL_OVERDUE_DAY_LIMIT_ERROR)) {
                    boolean hasRecalledOverdueDayPermission = service.hasPermission(principalId, OLEConstants.DLVR_NMSPC, OLEConstants.PATRON_RECALLED_OVERDUE_DAY);
                    if (!hasRecalledOverdueDayPermission) {
                        oleLoanForm.setOverrideErrorMessage(oleLoanForm.getOverrideErrorMessage() == null ?
                                OLEConstants.RECALL_OVERDUE_DAY_LIMIT_ERROR + OLEConstants.BREAK : oleLoanForm.getOverrideErrorMessage() + OLEConstants.RECALL_OVERDUE_DAY_LIMIT_ERROR + OLEConstants.BREAK);
                        overRideFlag = false;
                    }
                }
                if (oleLoanForm.getMessage().contains(OLEConstants.ITEM_STATUS_LOANED_ANOTHER_PATRON_PERMISSION)) {
                    boolean hasRecalledOverdueDayPermission = service.hasPermission(principalId, OLEConstants.DLVR_NMSPC,OLEConstants.ITEM_STATUS_LOANED_ANOTHER_PATRON_PERMISSION);
                    if (!hasRecalledOverdueDayPermission) {
                        oleLoanForm.setOverrideErrorMessage(oleLoanForm.getOverrideErrorMessage() == null ?
                                OLEConstants.ITEM_STATUS_LOANED_ANOTHER_PATRON_PERMISSION + OLEConstants.BREAK : oleLoanForm.getOverrideErrorMessage() + OLEConstants.ITEM_STATUS_LOANED_ANOTHER_PATRON_PERMISSION + OLEConstants.BREAK);
                        overRideFlag = false;
                    }
                }
            }
        }
        return overRideFlag;
    }

    public boolean itemValidation(org.kuali.ole.docstore.common.document.content.instance.Item oleItem) {
        LOG.debug("Inside the itemValidation method");
        boolean itemValidation = true;
        if (oleItem == null)
            return false;
        else if ("".equals(oleItem.getItemType()) || oleItem.getItemType() == null)
            itemValidation = itemValidation && false;
        else if ("".equals(oleItem.getItemStatus()) || oleItem.getItemStatus() == null || validateAndGetItemStatus(oleItem.getItemStatus().getCodeValue()) == null)
            itemValidation = itemValidation && false;

        return itemValidation;
    }

    public String getItemValidationLocation(LocationLevel oleLocationLevel) {
        String location = null;
        if (OLEConstants.LOCATION_LEVEL_SHELVING.equalsIgnoreCase(oleLocationLevel.getLevel()))
            location = oleLocationLevel.getName();
        else
            location = getItemValidationLocation(oleLocationLevel.getLocationLevel());
        if ("".equals(location) || location == null)
            return null;
        return location;
    }

    public void deletePatronUserNote(String patronId, String patronNoteTypeId) throws Exception {
        LOG.debug("Inside the deletePatronUserNote method");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(OLEConstants.OlePatron.PATRON_ID, patronId);
        map.put(OLEConstants.PATRON_NOTE_TYPE_ID, patronNoteTypeId);
        getBusinessObjectService().deleteMatching(OlePatronNotes.class, map);
    }

    /**
     * Return the loaned Items
     *
     * @param itemBarcode
     * @param oleLoanDocument
     * @return OleLoanDocument
     */
    public OleLoanDocument returnLoan(String itemBarcode, OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the returnLoan method");
        long begin = System.currentTimeMillis();
        org.kuali.ole.docstore.common.document.Item item = new ItemOleml();

        if (itemBarcode != null && !itemBarcode.isEmpty()) {

            org.kuali.ole.docstore.common.search.SearchParams search_Params = new org.kuali.ole.docstore.common.search.SearchParams();
            SearchResponse searchResponse = null;
            search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), item.ITEM_BARCODE, itemBarcode), ""));


            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), OLEConstants.ID));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Holdings.HOLDINGSIDENTIFIER));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.BIBIDENTIFIER));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.TITLE));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.AUTHOR));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Item.LOCATION));
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Holdings.DESTINATION_FIELD_LOCATION_LEVEL_1));
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
            if (searchResponse.getSearchResults() != null && searchResponse.getSearchResults().size() > 0) {

                for (SearchResult searchResult : searchResponse.getSearchResults()) {
                    boolean isItemLevelLocationExist = false;
                    for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                        String fieldName = searchResultField.getFieldName();
                        String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";

                        if (fieldName.equalsIgnoreCase(Holdings.HOLDINGSIDENTIFIER) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setInstanceUuid(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(OLEConstants.ID) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setItemUuid(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(Bib.BIBIDENTIFIER) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setBibUuid(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(Bib.TITLE) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setTitle(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(Bib.AUTHOR) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setAuthor(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(Item.LOCATION) && !fieldValue.isEmpty()) {
                            isItemLevelLocationExist = true;
                            oleLoanDocument.setLocation(fieldValue);
                        } else if (fieldName.equalsIgnoreCase(Holdings.DESTINATION_FIELD_LOCATION_LEVEL_1) && !fieldValue.isEmpty()) {
                            if (!isItemLevelLocationExist) {
                                oleLoanDocument.setLocation(fieldValue);
                            }
                        }

                    }
                }
            } else {
                throw new Exception(OLEConstants.ITM_BARCD_NT_AVAL_DOC);
            }


        } else {
            item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(oleLoanDocument.getItemUuid());


            oleLoanDocument.setBibUuid(item.getHolding().getBib().getId());
            oleLoanDocument.setAuthor(item.getHolding().getBib().getAuthor());
            oleLoanDocument.setTitle(item.getHolding().getBib().getTitle());

            oleLoanDocument.setInstanceUuid(item.getHolding().getId());
            oleLoanDocument.setItemUuid(item.getId());
            oleLoanDocument.setItemLocation(item.getLocation());
        }


        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = null;
        String itemXml = null;
        if(item.getContent()==null){
            item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(oleLoanDocument.getItemUuid());
        }
        itemXml = item.getContent()!=null ? item.getContent() : getItemXML(oleLoanDocument.getItemUuid());

        oleItem = getItemPojo(itemXml);
        OleHoldings oleHoldings = new HoldingOlemlRecordProcessor().fromXML(item.getHolding().getContent());
        boolean itemValidation = itemValidation(oleItem);
        if (!itemValidation)
            throw new Exception(OLEConstants.INVAL_ITEM);
        oleLoanDocument.setOleItem(oleItem);
        try {
            if(oleItem.getCallNumber()!=null && !StringUtils.isEmpty(oleItem.getCallNumber().getNumber())){
                oleLoanDocument.setItemCallNumber(oleItem.getCallNumber().getNumber());
            }else {
                oleLoanDocument.setItemCallNumber(getItemCallNumber(oleHoldings.getCallNumber()));
            }
            getCopyNumber(oleItem,oleHoldings,oleLoanDocument);
            oleLoanDocument.setEnumeration(oleItem.getEnumeration());
            oleLoanDocument.setChronology(oleItem.getChronology());
            String status = oleItem.getItemStatus().getCodeValue();
            oleLoanDocument.setItemLoanStatus(status);
            oleLoanDocument.setItemPrice(new BigDecimal(oleItem.getPrice() != null && !oleItem.getPrice().isEmpty() ? oleItem.getPrice() : OLEConstants.ZERO));
        } catch (Exception e) {
            LOG.error("Exception", e);
            if (oleLoanDocument.isRenewalItemFlag()) {
                oleLoanDocument.setErrorMessage(OLEConstants.ITM_STS_NT_AVAL);
                return oleLoanDocument;
            } else
                throw new Exception(OLEConstants.ITM_STS_NT_AVAL);
        }
        /*if(oleLoanDocument.getItemLoanStatus().equalsIgnoreCase(OLEConstants.NOT_CHECK_OUT_STATUS)){
            throw new Exception(OLEConstants.ITM_CHECKIN_MSG);
        }*/
        oleLoanDocument.setItemId(itemBarcode);
        if (oleItem.getTemporaryItemType() != null && oleItem.getTemporaryItemType().getCodeValue() != "") {
            OleInstanceItemType oleInstanceItemType = getItemTypeIdByItemType(oleItem.getTemporaryItemType().getCodeValue());
            oleLoanDocument.setOleInstanceItemType(oleInstanceItemType);
            oleLoanDocument.setItemTypeName(oleInstanceItemType.getInstanceItemTypeCode());
        }
        else if (oleItem.getItemType() != null && oleItem.getItemType().getCodeValue() != "") {
            OleInstanceItemType oleInstanceItemType = getItemTypeIdByItemType(oleItem.getItemType().getCodeValue());
            oleLoanDocument.setOleInstanceItemType(oleInstanceItemType);
            oleLoanDocument.setItemTypeName(oleInstanceItemType.getInstanceItemTypeCode());
        }
        getLocation(oleItem, oleLoanDocument,item);
        if (oleLoanDocument.getLocation() == null || (oleLoanDocument.getLocation() != null && oleLoanDocument.getLocation().equalsIgnoreCase(""))) {
            getDefaultHoldingLocation(oleLoanDocument);
        }
        if (oleLoanDocument.getOleLocation() != null && oleLoanDocument.getOleLocation().getLocationId() != null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("parentLocationId", oleLoanDocument.getOleLocation().getLocationId());
            map.put("locationCode", oleLoanDocument.getItemLocation());
            List<OleLocation> oleLocations = (List<OleLocation>) getBusinessObjectService().findMatching(OleLocation.class, map);
            oleLoanDocument.setLocationId(oleLocations != null && oleLocations.size() > 0 ? oleLocations.get(0).getLocationId() : oleLoanDocument.getOleLocation().getLocationId());
        }
        OleCirculationDesk oleCirculationDesk = null;
        if (oleLoanDocument.getCirculationLocationId() != null) {
            oleCirculationDesk = getOleCirculationDesk(oleLoanDocument.getCirculationLocationId());
            oleLoanDocument.setOleCirculationDesk(oleCirculationDesk);
        }
        OleDeliverRequestBo oleDeliverRequestBo = getPrioritizedRequest(oleLoanDocument.getItemUuid());
        /*Removed for the jira -4139 Damaged Checkin option from return view screen */
        /*if (oleLoanDocument.isDamagedCheckInOption()) {
            updateItemStatus(oleItem, OLEConstants.ITEM_STATUS_RETURNED_DAMAGED);
            OleItemAvailableStatus itemAvailableStatus = validateAndGetItemStatus(OLEConstants.ITEM_STATUS_RETURNED_DAMAGED);
            oleLoanDocument.setItemStatus(itemAvailableStatus != null ? itemAvailableStatus.getItemAvailableStatusName() : null);
            oleLoanDocument.setItemStatusCode(OLEConstants.ITEM_STATUS_RETURNED_DAMAGED);
            oleLoanDocument.setRouteToLocation(getCirculationDeskDefaultValue(oleLoanDocument.getLocationId()));
            oleLoanDocument.setOleDeliverRequestBo(oleDeliverRequestBo != null ? oleDeliverRequestBo : null);
            return oleLoanDocument;
        }*/
        oleLoanDocument.setItemDamagedStatus(oleItem.isItemDamagedStatus());
        if (!oleLoanDocument.isSkipDamagedCheckIn()) {
            if (oleLoanDocument.isItemDamagedStatus()) {
                return oleLoanDocument;
            }
        }
        List<OleLocation> oleLocations = new ArrayList<OleLocation>();
        if (oleCirculationDesk.getOleCirculationDeskLocations() != null) {
            for (OleCirculationDeskLocation oleCirculationDeskLocation : oleCirculationDesk.getOleCirculationDeskLocations()) {
                oleLocations.add(oleCirculationDeskLocation.getLocation());
            }
        } else {
            throw new Exception(OLEConstants.NO_LOC_CIR_DESK);
        }
        StringBuffer location = new StringBuffer();
        for (OleLocation oleLocation : oleLocations) {
            String operatorsCirculationLocation = getFullPathLocation(oleLocation);
            location.append(operatorsCirculationLocation).append(OLEConstants.DELIMITER_HASH);
        }
        String operatorsCirculationLocation = location.toString();
        if (LOG.isDebugEnabled()){
            LOG.debug("operatorsCirculationLocation---->" + operatorsCirculationLocation);
        }
        String shelvingLagTime = oleCirculationDesk.getShelvingLagTime() != null ? oleCirculationDesk.getShelvingLagTime() : getParameter(OLEConstants.SHELVING_LAG_TIME);
        Integer shelvingLagTimInt = new Integer(shelvingLagTime.isEmpty() ? OLEConstants.ZERO : shelvingLagTime);
        oleLoanDocument.setOperatorsCirculationLocation(operatorsCirculationLocation);
        String digitRoutine = getParameter(OLEParameterConstants.ITEM_DIGIT_ROUTINE);
        String pattern = getParameter(OLEParameterConstants.ITEM_DIGIT_ROUTINE_PATTERN);
        String agendaName = OLEConstants.CHECK_IN_AGENDA_NM;
        HashMap<String, Object> termValues = new HashMap<String, Object>();
        termValues.put(OLEConstants.SHEL_LAG_TIME, shelvingLagTimInt);
        StringBuffer failures = new StringBuffer();
        EngineResults engineResults = null;
        String requestType = null;
        OlePatronDocument oleRequestPatron = null;

        if (oleDeliverRequestBo != null) {
            oleLoanDocument.setOleDeliverRequestBo(oleDeliverRequestBo);
            requestType = oleDeliverRequestBo.getOleDeliverRequestType().getRequestTypeCode();
            oleRequestPatron = getOlePatronDocument(oleDeliverRequestBo.getBorrowerId());
            OleCirculationDesk pickUpCircDesk = oleDeliverRequestBo.getPickUpLocationId() != null ? getOleCirculationDesk(oleDeliverRequestBo.getPickUpLocationId()) : null;
            String pickUpLocationFullPath = circulationDeskLocations(pickUpCircDesk);
            OleCirculationDesk destinationCircDesk = oleDeliverRequestBo.getCirculationLocationId() != null ? getOleCirculationDesk(oleDeliverRequestBo.getCirculationLocationId()) : null;
            String destinationLocationFullPath = circulationDeskLocations(destinationCircDesk);
            termValues.put(OLEConstants.ITEM_PICKUP_LOCATION, pickUpLocationFullPath);
            termValues.put(OLEConstants.DESTINATION_LOCATION, destinationLocationFullPath);
            termValues.put(OLEConstants.REQUEST_TYPE, requestType);
            if (pickUpCircDesk != null) {
                oleLoanDocument.setRouteToLocation(pickUpCircDesk.getCirculationDeskCode());
                oleLoanDocument.setRouteToLocationName(pickUpCircDesk.getCirculationDeskPublicName());
            } else if (destinationCircDesk != null) {
                oleLoanDocument.setRouteToLocation(destinationCircDesk.getCirculationDeskCode());
                oleLoanDocument.setRouteToLocationName(destinationCircDesk.getCirculationDeskPublicName());
            } else {
                // oleLoanDocument.setRouteToLocation(oleCirculationDesk!=null?oleCirculationDesk.getCirculationDeskCode():null);
                oleLoanDocument.setRouteToLocation(oleLoanDocument.getLocationCode());
            }
            oleLoanDocument.setOleRequestPatron(oleRequestPatron);
        } else {
            //oleLoanDocument.setRouteToLocation(oleCirculationDesk!=null?oleCirculationDesk.getCirculationDeskCode():null);
            oleLoanDocument.setRouteToLocation(oleLoanDocument.getLocationCode());
        }
        termValues.put(OLEConstants.ITEM_SHELVING, oleLoanDocument.getItemLocation());
        termValues.put(OLEConstants.ITEM_COLLECTION, oleLoanDocument.getItemCollection());
        termValues.put(OLEConstants.ITEM_LIBRARY, oleLoanDocument.getItemLibrary());
        termValues.put(OLEConstants.ITEM_CAMPUS, oleLoanDocument.getItemCampus());
        termValues.put(OLEConstants.ITEM_INSTITUTION, oleLoanDocument.getItemInstitution());
        termValues.put(OLEConstants.OleDeliverRequest.CLAIMS_RETURNED_FLAG, oleItem.isClaimsReturnedFlag());
        termValues.put(OLEConstants.REPLACEMENT_FEE_EXIST, oleLoanDocument.isReplacementFeeExist());
        String itemFullPathLocation = getFullPathLocation(getLocationByLocationCode(oleLoanDocument.getItemLocation()));
        oleLoanDocument.setItemFullPathLocation(itemFullPathLocation);
        String patronId = oleLoanDocument.getPatronId()!=null ?  oleLoanDocument.getPatronId() : "";
        String itemId = oleLoanDocument.getItemId()!=null ?  oleLoanDocument.getItemId() : "";
        termValues.put(OLEConstants.PATRON_ID_POLICY, patronId);
        termValues.put(OLEConstants.ITEM_ID_POLICY, itemId);
        if (oleLoanDocument.getOlePatron() == null) {
            getDataCarrierService().addData(patronId+itemId, true);
            termValues.put(OLEConstants.ITEM_BARCODE, itemBarcode);
            termValues.put(OLEConstants.ITEM_LOCATION, itemFullPathLocation);
            termValues.put(OLEConstants.CIRCULATION_LOCATION, operatorsCirculationLocation);
            termValues.put(OLEConstants.ITEM_STATUS, oleLoanDocument.getItemLoanStatus());
            termValues.put(OLEConstants.IS_ITEM_PRICE, oleLoanDocument.getItemPrice().compareTo(OLEConstants.BIGDECIMAL_DEF_VALUE) <= 0 ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.DIGIT_ROUTINE, digitRoutine);
            termValues.put(OLEConstants.PATTERN, pattern);
            engineResults = getEngineResults(agendaName, termValues);
        } else {
            getDataCarrierService().removeData(patronId+itemId);
            getDataCarrierService().addData(OLEConstants.HOURS_DIFF, getOleCirculationPolicyService().getHoursDiff(oleLoanDocument.getLoanDueDate(), oleLoanDocument.getCheckInDate()));
            getDataCarrierService().addData("DUEDATE", oleLoanDocument.getLoanDueDate());
            getDataCarrierService().addData("CHECKINDATE", oleLoanDocument.getCheckInDate());
            termValues.put(OLEConstants.ITEM_BARCODE, itemBarcode);
            termValues.put(OLEConstants.BORROWER_TYPE, oleLoanDocument.getBorrowerTypeCode());
            termValues.put(OLEConstants.ITEM_TYPE, oleLoanDocument.getItemTypeName());
            termValues.put(OLEConstants.ITEM_LOCATION, itemFullPathLocation);
            termValues.put(OLEConstants.CIRCULATION_LOCATION, operatorsCirculationLocation);
            termValues.put(OLEConstants.ITEMS_DUE_DATE, oleLoanDocument.getLoanDueDate());
            termValues.put(OLEConstants.ITEM_STATUS, oleLoanDocument.getItemLoanStatus());
            termValues.put(OLEConstants.DELIVERY_PRIVILEGES, oleLoanDocument.getOlePatron().isDeliveryPrivilege() ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.IS_ITEM_PRICE, oleLoanDocument.getItemPrice().compareTo(OLEConstants.BIGDECIMAL_DEF_VALUE) <= 0 ? OLEConstants.TRUE : OLEConstants.FALSE);
            termValues.put(OLEConstants.DIGIT_ROUTINE, digitRoutine);
            termValues.put(OLEConstants.PATTERN, pattern);
            engineResults = getEngineResults(agendaName, termValues);
            BigDecimal fineRate = (BigDecimal) engineResults.getAttribute(OLEConstants.FINE_RATE);
            Boolean checkOut = (Boolean) engineResults.getAttribute(OLEConstants.CHECKOUT);
            BigDecimal maxFine = (BigDecimal) engineResults.getAttribute(OLEConstants.MAX_FINE);
            oleLoanDocument.setFineRate(maxFine != null ? maxFine : fineRate);
            oleLoanDocument.setCheckOut(checkOut != null ? checkOut : false);
        }
        getDataCarrierService().removeData(patronId+itemId);
        HashMap<String, String> errorsAndPermission = new HashMap<>();
        errorsAndPermission = (HashMap<String, String>) engineResults.getAttribute(OLEConstants.ERRORS_AND_PERMISSION);
        PermissionService service = KimApiServiceLocator.getPermissionService();
        int i = 1;
        if (errorsAndPermission != null) {
            Set<String> errorMessage = errorsAndPermission.keySet();
            if (errorMessage != null && errorMessage.size() > 0) {
                for (String errMsg : errorMessage) {
                    if (StringUtils.isNotEmpty(errMsg)) {
                        oleLoanDocument.getErrorsAndPermission().putAll(errorsAndPermission);
                        if (errorMessage != null && errorMessage.size() > 0) {
                            for (String errorMsg : errorMessage) {
                                failures.append(i++ + ". " + errorMsg + OLEConstants.BREAK);
                            }
                        }
                    }
                }
            }
            errorsAndPermission.clear();
        }
        List<String> errorMessage = (List<String>) engineResults.getAttribute(OLEConstants.ERROR_ACTION);
        String itemStatusCode = (String) engineResults.getAttribute(OLEConstants.ITEM_STATUS);
        BigDecimal replacementBill = (BigDecimal) engineResults.getAttribute(OLEConstants.REPLACEMENT_BILL);
        oleLoanDocument.setReplacementBill(replacementBill);
        if (itemStatusCode != null) {
            oleLoanDocument.setItemStatusCode(itemStatusCode);
        }
        if (errorMessage != null && errorMessage.size() > 0) {
            for (String errMsg : errorMessage) {
                failures.append(i++ + ". " + errMsg + OLEConstants.BREAK);
            }
        }
        getDataCarrierService().addData(OLEConstants.ERROR_ACTION, null);
        getDataCarrierService().addData(OLEConstants.ERRORS_AND_PERMISSION, null);
        boolean returnLoan = false;
        if (!failures.toString().isEmpty()) {
            oleLoanDocument.setErrorMessage(failures.toString());
            returnLoan = true;
        }
        if (requestType != null && requestType.equalsIgnoreCase(OLEConstants.COPY_REQUEST)) {
            oleLoanDocument.setCopyRequest(true);
            returnLoan = true;
        }
        if (returnLoan) {
            return oleLoanDocument;

        }
        oleLoanDocument = returnLoan(oleLoanDocument);
        long end = System.currentTimeMillis();
        long total = end - begin;
        LOG.info("Time taken Inside returnloan"+total);
        return oleLoanDocument;
    }

    public OleDeliverRequestBo getPrioritizedRequest(String itemUuid) {
        LOG.debug("Inside the getPrioritizedRequest method");
        Map requestMap = new HashMap();
        requestMap.put("itemUuid", itemUuid);
        requestMap.put("borrowerQueuePosition", 1);
        List<OleDeliverRequestBo> oleDeliverRequestBos = (List<OleDeliverRequestBo>) getBusinessObjectService().findMatching(OleDeliverRequestBo.class, requestMap);
        return oleDeliverRequestBos != null && oleDeliverRequestBos.size() > 0 ? oleDeliverRequestBos.get(0) : null;
    }

    public OleDeliverRequestBo getCurrentQueue(String patronid, String itemUuid) throws Exception {
        LOG.debug("Inside the getCurrentQueue method");
        Map requestMap = new HashMap();
        requestMap.put("borrowerId", patronid);
        requestMap.put("itemUuid", itemUuid);
        List<OleDeliverRequestBo> oleDeliverRequestBos = (List<OleDeliverRequestBo>) getBusinessObjectService().findMatching(OleDeliverRequestBo.class, requestMap);
        return oleDeliverRequestBos != null && oleDeliverRequestBos.size() > 0 ? oleDeliverRequestBos.get(0) : null;
    }

    public OleCirculationDesk getOleCirculationDesk(String id) {
        LOG.debug("Inside the getOleCirculationDesk method");
        OleCirculationDesk oleCirculationDesk = getBusinessObjectService().findBySinglePrimaryKey(OleCirculationDesk.class, id);
        return oleCirculationDesk;
    }

    public String generatePatronBillPayment(OleLoanDocument oleLoanDocument, String feeTypeName, BigDecimal fineAmount) {
        long begin = System.currentTimeMillis();
        StringBuffer contentForSendMail = new StringBuffer();
        LOG.debug("Inside the generatePatronBillPayment method");
        // OlePatronDocument olePatronDocument = oleLoanDocument.getOlePatron();
        //   EntityNameBo nameBo = olePatronDocument.getEntity().getNames().get(0);
        PatronBillPayment patronBillPayment;
        OlePaymentStatus olePaymentStatus = getPaymentStatus();
        FeeType feeType = new FeeType();
        feeType.setFeeType(getFeeTypeId(feeTypeName));
        feeType.setFeeAmount(new KualiDecimal(fineAmount));
        feeType.setItemBarcode(oleLoanDocument.getItemId());
        feeType.setItemType(oleLoanDocument.getItemTypeName());
        feeType.setItemTitle(oleLoanDocument.getTitle());
        feeType.setItemUuid(oleLoanDocument.getItemUuid());
        feeType.setPaymentStatus(olePaymentStatus.getPaymentStatusId());
        feeType.setBalFeeAmount(new KualiDecimal(fineAmount));
        feeType.setFeeSource(OLEConstants.SYSTEM);
        feeType.setDueDate(oleLoanDocument.getLoanDueDate());
        feeType.setCheckInDate(oleLoanDocument.getCheckInDate());
        feeType.setCheckOutDate(oleLoanDocument.getCreateDate());
        List<FeeType> feeTypes = new ArrayList<FeeType>();
        feeTypes.add(feeType);
        Date billdate = new Date();
       /* patronBillPayment = getPatronBillPayment(oleLoanDocument.getPatronId());
        if (patronBillPayment != null) {
            feeTypes.addAll(patronBillPayment.getFeeType());
            patronBillPayment.setFeeType(feeTypes);
            patronBillPayment.setBillDate(oleLoanDocument.getCheckInDate() != null ? new java.sql.Date(oleLoanDocument.getCheckInDate().getTime()) : new java.sql.Date(billdate.getTime()));
            patronBillPayment.setTotalAmount(patronBillPayment.getTotalAmount().add(fineAmount));
            patronBillPayment.setUnPaidBalance(patronBillPayment.getUnPaidBalance().add(fineAmount));
            patronBillPayment.setOlePatron(null);  // Set null to resolve  org.kuali.rice.core.api.util.cache.CacheException: unable to complete deepCopy from src 'org.kuali.ole.deliver.bo.PatronBillPayment' JIRA OLE-4364


        } else {*/
        patronBillPayment = new PatronBillPayment();
        patronBillPayment.setBillDate(oleLoanDocument.getCheckInDate() != null ? new java.sql.Date(oleLoanDocument.getCheckInDate().getTime()) : new java.sql.Date(billdate.getTime()));
        patronBillPayment.setFeeType(feeTypes);
        //patronBillPayment.setMachineId(oleLoanDocument.getMachineId());  //commented for jira OLE-5675
        patronBillPayment.setPatronId(oleLoanDocument.getPatronId());
        patronBillPayment.setProxyPatronId(oleLoanDocument.getProxyPatronId());
        patronBillPayment.setTotalAmount(new KualiDecimal(fineAmount));
        patronBillPayment.setUnPaidBalance(new KualiDecimal(fineAmount));

       /* }*/

        PatronBillPayment patronBillPayments = getBusinessObjectService().save(patronBillPayment);

        Map<String, String> patronMap = new HashMap<String, String>();
        BusinessObjectService businessObjectService = KRADServiceLocator.getBusinessObjectService();
        patronMap.put(OLEConstants.OlePatron.PATRON_ID, oleLoanDocument.getPatronId());
        List<OlePatronDocument> olePatronDocumentList = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
        OlePatronDocument olePatronDocument = olePatronDocumentList.get(0);
        String patronMail = "";
        try {
            patronMail = getOleDeliverRequestDocumentHelperService().getPatronHomeEmailId(olePatronDocument.getEntity().getEntityTypeContactInfos().get(0));
        } catch (Exception e) {
            LOG.error("Ecxeption while getting patron home mail id", e);
        }
        OleDeliverBatchServiceImpl oleDeliverBatchService = new OleDeliverBatchServiceImpl();
        contentForSendMail.append(oleDeliverBatchService.generateMailContentFromPatronBill(oleLoanDocument, olePatronDocument, feeTypeName, String.valueOf(fineAmount), patronBillPayment));
        OleMailer oleMail = GlobalResourceLoader.getService("oleMailer");
        oleMail.sendEmail(new EmailFrom(getParameter(OLEParameterConstants.NOTICE_FROM_MAIL)), new EmailTo(patronMail), new EmailSubject(feeTypeName), new EmailBody(contentForSendMail.toString()), true);
        if (LOG.isInfoEnabled()){
            LOG.info("Mail send successfully to " + patronMail);
        }
        String billNumber = patronBillPayments.getBillNumber();
        long end = System.currentTimeMillis();
        long total = end - begin;
        LOG.info("Time taken Inside generatePatronBillPayment"+total);
        return billNumber;
    }

    private PatronBillPayment getPatronBillPayment(String patronId) {
        LOG.debug("Inside the getPatronBillPayment method");
        Map billMap = new HashMap();
        billMap.put("patronId", patronId);
        List<PatronBillPayment> patronBillPaymentList = (List<PatronBillPayment>) getBusinessObjectService().findMatching(PatronBillPayment.class, billMap);
        return patronBillPaymentList != null && patronBillPaymentList.size() > 0 ? patronBillPaymentList.get(0) : null;
    }

    private OlePaymentStatus getPaymentStatus() {
        LOG.debug("Inside the getPaymentStatus method");
        Map statusMap = new HashMap();
        statusMap.put("paymentStatusName", OLEConstants.PAYMENT_STATUS_OUTSTANDING);
        List<OlePaymentStatus> olePaymentStatusList = (List<OlePaymentStatus>) getBusinessObjectService().findMatching(OlePaymentStatus.class, statusMap);
        return olePaymentStatusList != null && olePaymentStatusList.size() > 0 ? olePaymentStatusList.get(0) : null;
    }

    private String getFeeTypeId(String feeTypeName) {
        LOG.debug("Inside the getOverdueFeeTypeId method");
        Map feeMap = new HashMap();
        feeMap.put("feeTypeName", feeTypeName);
        List<OleFeeType> oleFeeTypes = (List<OleFeeType>) getBusinessObjectService().findMatching(OleFeeType.class, feeMap);
        return oleFeeTypes != null && oleFeeTypes.size() > 0 ? oleFeeTypes.get(0).getFeeTypeId() : null;
    }

    public OleLoanDocument returnLoan(OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the returnLoan method");
        long begin = System.currentTimeMillis();
        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = oleLoanDocument.getOleItem();
        OlePatronDocument olePatronDocument = oleLoanDocument.getOlePatron();
        OlePatronDocument oleRequestPatron = oleLoanDocument.getOleRequestPatron();
        String circulationLocationId = oleLoanDocument.getCirculationLocationId();
        Integer numberOfPieces = Integer.parseInt(oleItem != null && oleItem.getNumberOfPieces() != null && !oleItem.getNumberOfPieces().isEmpty() ? oleItem.getNumberOfPieces() : "0");
        if (!oleLoanDocument.isContinueCheckIn() && numberOfPieces > 1) {
            oleLoanDocument.setItemNumberOfPieces(numberOfPieces);
            oleLoanDocument.setNumberOfPieces(true);
            return oleLoanDocument;
        }
        if (oleLoanDocument.getFineRate() != null && oleLoanDocument.getFineRate().compareTo(BigDecimal.ZERO) > 0) {
            generatePatronBillPayment(oleLoanDocument, OLEConstants.OVERDUE_FINE, oleLoanDocument.getFineRate());
            oleLoanDocument.setBillName(oleLoanDocument.getFineRate().toString());
            updateReplacementFeeBill(oleLoanDocument);
        }
        if (oleLoanDocument.getReplacementBill() != null && oleLoanDocument.getFineRate() != null && oleLoanDocument.getFineRate().compareTo(BigDecimal.ZERO) > 0) {
            generatePatronBillPayment(oleLoanDocument, OLEConstants.REPLACEMENT_FEE, oleLoanDocument.getReplacementBill());
            BigDecimal fineRate = oleLoanDocument.getFineRate() != null ? oleLoanDocument.getFineRate() : OLEConstants.BIGDECIMAL_DEF_VALUE;
            oleLoanDocument.setBillName(oleLoanDocument.getReplacementBill().add(fineRate).toString());
            if (oleItem.isStaffOnlyFlag()) {
                oleItem.setStaffOnlyFlag(false);
            }
        }
        if (oleLoanDocument.getRepaymentFeePatronBillId() != null) {
            if (oleItem.isStaffOnlyFlag()) {
                oleItem.setStaffOnlyFlag(false);
            }
            oleLoanDocument.setBillName(OLEConstants.REPLACEMENT_FEE);
        }
        if (oleLoanDocument.getItemStatusCode() != null) {
            incrementNumberOfCirculations(oleLoanDocument);
            oleItem.setCurrentBorrower(null);
            oleItem.setDueDateTime(null);
            oleItem.setCheckOutDateTime(null);
            updateItemStatus(oleItem, oleLoanDocument.getItemStatusCode());
            //updateMissingPiecesItemInfo(oleLoanDocument);
            OleItemAvailableStatus itemAvailableStatus = validateAndGetItemStatus(oleLoanDocument.getItemStatusCode());
            oleLoanDocument.setItemStatus(itemAvailableStatus != null ? itemAvailableStatus.getItemAvailableStatusName() : null);
        }
        if (olePatronDocument != null) {
            createCirculationHistoryAndTemporaryHistoryRecords(oleLoanDocument);
            getOleDeliverNoticeHelperService().deleteDeliverNotices(oleLoanDocument.getLoanId());
            getBusinessObjectService().delete(oleLoanDocument);
        }
        OleDeliverRequestBo oleDeliverRequestBo = oleLoanDocument.getOleDeliverRequestBo();
        OleLoanDocument checkOutLoanDocument;
        if (oleLoanDocument.isCheckOut()) {
            checkOutLoanDocument = checkOutItem(oleRequestPatron, oleItem, circulationLocationId, oleLoanDocument.getCheckInDate(), oleDeliverRequestBo);
            checkOutLoanDocument.setCheckOut(true);
            checkOutLoanDocument.setBackGroundCheckOut(true);
            checkOutLoanDocument.setRouteToLocation(oleLoanDocument.getRouteToLocation());
            checkOutLoanDocument.setOleCirculationDesk(oleLoanDocument.getOleCirculationDesk());
            return checkOutLoanDocument;
        }
        if (oleLoanDocument.getItemStatusCode() != null && oleLoanDocument.getItemStatusCode().equalsIgnoreCase(OLEConstants.RECENTLY_RETURNED)) {
            createOrUpdateRecentlyReturnedRecord(oleLoanDocument.getItemUuid(), oleLoanDocument.getOleCirculationDesk().getCirculationDeskId());
        }
        long end = System.currentTimeMillis();
        long total = end - begin;
        LOG.info("Time taken Inside inner returnloan"+total);
        return oleLoanDocument;
    }

    private void createOrUpdateRecentlyReturnedRecord(String itemUUID, String circulationDeskId) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("itemUuid", itemUUID);
        OleRecentlyReturned oleRecentlyReturned = getBusinessObjectService().findByPrimaryKey(OleRecentlyReturned.class, map);
        if (oleRecentlyReturned != null) {
            oleRecentlyReturned.setCirculationDeskId(circulationDeskId);
        } else {
            oleRecentlyReturned = new OleRecentlyReturned();
            oleRecentlyReturned.setCirculationDeskId(circulationDeskId);
            oleRecentlyReturned.setItemUuid(itemUUID);
        }
        getBusinessObjectService().save(oleRecentlyReturned);
    }

    public void incrementNumberOfCirculations(OleLoanDocument oleLoanDocument) {
        org.kuali.ole.docstore.common.document.content.instance.Item item = oleLoanDocument.getOleItem();
        NumberOfCirculations numberOfCirculations = item.getNumberOfCirculations();
        if (numberOfCirculations == null) {
            numberOfCirculations = new NumberOfCirculations();
            numberOfCirculations.setCheckInLocation(new ArrayList<CheckInLocation>());
        }
        List<CheckInLocation> checkInLocations = numberOfCirculations.getCheckInLocation();
        CheckInLocation checkInLocation = null;
        int count = 1;
        for (CheckInLocation checkInLoc : checkInLocations) {
            if (checkInLoc.getName().equals(oleLoanDocument.getItemFullPathLocation())) {
                if (oleLoanDocument.getLoanId() == null || oleLoanDocument.getLoanId().isEmpty()) {
                    if (checkInLoc.getInHouseCount() != null) {
                        if (oleLoanDocument.getItemStatusCode().equals("INTRANSIT") || oleLoanDocument.getItemStatusCode().equals("INTRANSIT-FOR-HOLD") || oleLoanDocument.getItemStatusCode().equals("INTRANSIT-PER-STAFF-REQUEST")) {
                            count = checkInLoc.getInHouseCount();
                        } else {
                            count = checkInLoc.getInHouseCount() + 1;
                        }
                    }
                } else {
                    if (checkInLoc.getCount() != null) {
                        count = checkInLoc.getCount() + 1;
                    }
                }
                checkInLocation = checkInLoc;
                break;
            }
        }
        if (checkInLocation == null) {
            checkInLocation = new CheckInLocation();
            checkInLocation.setName(oleLoanDocument.getItemFullPathLocation());
            if (oleLoanDocument.getLoanId() == null || oleLoanDocument.getLoanId().isEmpty())
                checkInLocation.setInHouseCount(count);
            else
                checkInLocation.setCount(count);
            checkInLocations.add(checkInLocation);
        } else {
            if (oleLoanDocument.getLoanId() == null || oleLoanDocument.getLoanId().isEmpty())
                checkInLocation.setInHouseCount(count);
            else
                checkInLocation.setCount(count);
        }
        if (item.getNumberOfCirculations() != null) {
            oleLoanDocument.getOleItem().getNumberOfCirculations().setCheckInLocation(checkInLocations);
        } else {
            numberOfCirculations.setCheckInLocation(checkInLocations);
            oleLoanDocument.getOleItem().setNumberOfCirculations(numberOfCirculations);
        }

    }

    private OleLoanDocument checkOutItem(OlePatronDocument olePatronDocument, org.kuali.ole.docstore.common.document.content.instance.Item oleItem, String circulationLocationId, Timestamp checkInDate, OleDeliverRequestBo oleDeliverRequestBo) throws Exception {
        long begin = System.currentTimeMillis();
        LOG.debug("Inside the checkOutItem method");
        OleLoanDocument oleLoanDocument = new OleLoanDocument();
        oleLoanDocument.setOlePatron(olePatronDocument);
        oleLoanDocument.setPatronId(olePatronDocument.getOlePatronId());
        oleLoanDocument.setCirculationLocationId(circulationLocationId);
        oleLoanDocument.setBorrowerTypeId(olePatronDocument.getBorrowerType());
        oleLoanDocument.setBorrowerTypeName(olePatronDocument.getBorrowerTypeName());
        oleLoanDocument.setBorrowerTypeCode(olePatronDocument.getBorrowerTypeCode());
        addLoan(olePatronDocument.getBarcode(), oleItem.getAccessInformation().getBarcode(), oleLoanDocument,null);
        //  deleteRequestRecord(oleDeliverRequestBo);
        OleItemAvailableStatus oleItemAvailableStatus = validateAndGetItemStatus(oleLoanDocument.getOleItem().getItemStatus().getCodeValue());
        oleLoanDocument.setItemStatus(oleItemAvailableStatus != null ? oleItemAvailableStatus.getItemAvailableStatusName() : null);
        oleLoanDocument.setItemStatusCode(oleLoanDocument.getOleItem().getItemStatus().getCodeValue());
        oleLoanDocument.setCheckInDate(checkInDate);
        long end = System.currentTimeMillis();
        long total = end - begin;
        LOG.info("Time taken Inside checkOutItem"+total);
        return oleLoanDocument;
    }

    public void deleteRequestRecord(OleDeliverRequestBo oleDeliverRequestBo) {
        getOleDeliverRequestDocumentHelperService().cancelDocument(oleDeliverRequestBo);
    }

    public void createCirculationHistoryAndTemporaryHistoryRecords(OleLoanDocument oleLoanDocument) {
        LOG.debug("Inside the createCirculationHistoryAndTemporaryHistoryRecords method");
        OlePatronDocument olePatronDocument = oleLoanDocument.getOlePatron();
        OleCirculationHistory oleCirculationHistory = new OleCirculationHistory();
        oleCirculationHistory.setLoanId(oleLoanDocument.getLoanId());
        oleCirculationHistory.setCirculationPolicyId(oleLoanDocument.getCirculationPolicyId());
        oleCirculationHistory.setBibAuthor(oleLoanDocument.getAuthor());
        oleCirculationHistory.setBibTitle(oleLoanDocument.getTitle());
        oleCirculationHistory.setCheckInDate(oleLoanDocument.getCheckInDate() != null ? oleLoanDocument.getCheckInDate() : new Timestamp(System.currentTimeMillis()));
        //oleCirculationHistory.setCheckInMachineId(oleLoanDocument.getMachineId());  //commented for jira OLE-5675
        oleCirculationHistory.setCreateDate(oleLoanDocument.getCreateDate());
        oleCirculationHistory.setCirculationLocationId(oleLoanDocument.getCirculationLocationId());
        oleCirculationHistory.setDueDate(oleLoanDocument.getLoanDueDate());
        oleCirculationHistory.setItemId(oleLoanDocument.getItemId());
        //oleCirculationHistory.setMachineId(oleLoanDocument.getMachineId());      //commented for jira OLE-5675
        oleCirculationHistory.setNumberOfOverdueNoticesSent(oleLoanDocument.getNumberOfOverdueNoticesSent());
        oleCirculationHistory.setNumberOfRenewals(oleLoanDocument.getNumberOfRenewals());
        oleCirculationHistory.setStatisticalCategory(olePatronDocument.getStatisticalCategory());
        oleCirculationHistory.setRepaymentFeePatronBillId(oleLoanDocument.getRepaymentFeePatronBillId());
        oleCirculationHistory.setProxyPatronId(olePatronDocument.getProxyPatronId());
        oleCirculationHistory.setPatronTypeId(oleLoanDocument.getBorrowerTypeId());
        oleCirculationHistory.setPatronId(oleLoanDocument.getPatronId());
        oleCirculationHistory.setPastDueDate(oleLoanDocument.getPastDueDate());
        oleCirculationHistory.setOverdueNoticeDate(oleLoanDocument.getOverDueNoticeDate());
        oleCirculationHistory.setOleRequestId(oleLoanDocument.getOleRequestId());
        oleCirculationHistory.setItemUuid(oleLoanDocument.getItemUuid());
        getBusinessObjectService().save(oleCirculationHistory);

        OleTemporaryCirculationHistory oleTemporaryCirculationHistory = new OleTemporaryCirculationHistory();
        oleTemporaryCirculationHistory.setCirculationLocationId(oleLoanDocument.getCirculationLocationId());
        oleTemporaryCirculationHistory.setOlePatronId(oleLoanDocument.getPatronId());
        oleTemporaryCirculationHistory.setItemId(oleLoanDocument.getItemId());
        oleTemporaryCirculationHistory.setCheckInDate(oleLoanDocument.getCheckInDate() != null ? oleLoanDocument.getCheckInDate() : new Timestamp(System.currentTimeMillis()));
        oleTemporaryCirculationHistory.setItemUuid(oleLoanDocument.getItemUuid());
        oleTemporaryCirculationHistory.setDueDate(oleLoanDocument.getLoanDueDate());
        oleTemporaryCirculationHistory.setCheckOutDate(oleLoanDocument.getCreateDate());
        getBusinessObjectService().save(oleTemporaryCirculationHistory);

    }

    /**
     * This method invokes docStore to store item and returns itemRecordUpdateResponse.
     *
     * @param oleItem
     * @param itemStatus
     * @throws Exception
     */
    public void updateItemStatus(org.kuali.ole.docstore.common.document.content.instance.Item oleItem, String itemStatus) throws Exception {
        LOG.debug("Inside the updateItemStatus method");
        try {
            String itemUuid = oleItem.getItemIdentifier();
            String itemXmlContent = buildItemContentWithItemStatus(oleItem, itemStatus);
            if (LOG.isDebugEnabled()){
                LOG.debug("itemXmlContent" + itemXmlContent);
            }
            Item item = new ItemOleml();
            item.setId(itemUuid);
            item.setContent(itemXmlContent);
            item.setCategory(OLEConstants.WORK_CATEGORY);
            item.setType(DocType.ITEM.getCode());
            item.setFormat(OLEConstants.OLEML_FORMAT);
            getDocstoreClientLocator().getDocstoreClient().updateItem(item);
        } catch (Exception e) {
            LOG.error(OLEConstants.ITM_STS_TO_DOC_FAIL + e, e);
            throw new Exception(OLEConstants.ITM_STS_TO_DOC_FAIL);
        }
    }

    /**
     * This method invokes docStore to remove check in note and store item.
     *
     * @param oleItem
     * @throws Exception
     */
    public void removeCheckInNote(org.kuali.ole.docstore.common.document.content.instance.Item oleItem) throws Exception {
        LOG.debug("Inside the removeCheckInNote method");
        try {
            String itemUuid = oleItem.getItemIdentifier();
            oleItem.setCheckinNote("");
            String itemXmlContent = getItemOlemlRecordProcessor().toXML(oleItem);
            Item item = new ItemOleml();
            item.setId(itemUuid);
            item.setContent(itemXmlContent);
            item.setCategory(OLEConstants.WORK_CATEGORY);
            item.setType(DocType.ITEM.getCode());
            item.setFormat(OLEConstants.OLEML_FORMAT);
            getDocstoreClientLocator().getDocstoreClient().updateItem(item);
        } catch (Exception e) {
            LOG.error("removeCheckInNote" + e, e);
            throw new Exception("Exception in removeCheckInNote method" + e);
        }
    }

    /**
     * This method creates and return itemContent using OleItem
     *
     * @param oleItem
     * @param itemStatus
     * @return String
     * @throws Exception
     */
    public String buildItemContentWithItemStatus(org.kuali.ole.docstore.common.document.content.instance.Item oleItem, String itemStatus) throws Exception {
        LOG.debug("Inside the buildItemContentWithItemStatus method");
        ItemStatus itemStatus1 = new ItemStatus();
        itemStatus1.setCodeValue(itemStatus);
        itemStatus1.setFullValue(itemStatus);
        oleItem.setItemStatus(itemStatus1);
        oleItem.setItemStatusEffectiveDate(String.valueOf(new SimpleDateFormat(OLEConstants.DAT_FORMAT_EFFECTIVE).format(new Date())));
        String itemContent = getItemOlemlRecordProcessor().toXML(oleItem);
        return itemContent;
    }

    public OleLoanDocument getOleLoanDocumentUsingItemUUID(String itemUuid) {
        LOG.debug("Inside the getOleLoanDocumentUsingItemBarcode method");
        OleLoanDocument oleLoanDocument = null;
        Map barMap = new HashMap();
        barMap.put(OLEConstants.ITEM_UUID, itemUuid);
        List<OleLoanDocument> oleLoanDocuments = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class, barMap);
        if (oleLoanDocuments != null && oleLoanDocuments.size() > 0) {
            oleLoanDocument = oleLoanDocuments.get(0);
            barMap = new HashMap();
            barMap.put(OLEConstants.OlePatron.PATRON_ID, oleLoanDocument.getPatronId());
            OlePatronDocument olePatronDocument = getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, barMap);
            oleLoanDocument.setBorrowerTypeId(olePatronDocument.getBorrowerType());
            OleBorrowerType oleBorrowerType = olePatronDocument.getOleBorrowerType();
            oleLoanDocument.setBorrowerTypeName(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeName() != null ? oleBorrowerType.getBorrowerTypeName() : null);
            oleLoanDocument.setBorrowerTypeCode(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeCode() != null ? oleBorrowerType.getBorrowerTypeCode() : null);
            oleLoanDocument.setOlePatron(olePatronDocument);
        }
        return oleLoanDocument;
    }

    public OleLoanDocument getOleLoanDocumentUsingItemBarcode(String itemBarcode) {
        LOG.debug("Inside the getOleLoanDocumentUsingItemBarcode method");
        OleLoanDocument oleLoanDocument = null;
        Map barMap = new HashMap();
        barMap.put(OLEConstants.OleDeliverRequest.ITEM_ID, itemBarcode);
        List<OleLoanDocument> oleLoanDocuments = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class, barMap);
        if (oleLoanDocuments != null && oleLoanDocuments.size() > 0) {
            oleLoanDocument = oleLoanDocuments.get(0);
           /* barMap = new HashMap();
            barMap.put(OLEConstants.OlePatron.PATRON_ID, oleLoanDocument.getPatronId());*/
            OlePatronDocument olePatronDocument = oleLoanDocument.getOlePatron();
            oleLoanDocument.setBorrowerTypeId(olePatronDocument.getBorrowerType());
            OleBorrowerType oleBorrowerType = olePatronDocument.getOleBorrowerType();
            oleLoanDocument.setBorrowerTypeName(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeName() != null ? oleBorrowerType.getBorrowerTypeName() : null);
            oleLoanDocument.setBorrowerTypeCode(oleBorrowerType != null && oleBorrowerType.getBorrowerTypeCode() != null ? oleBorrowerType.getBorrowerTypeCode() : null);
            oleLoanDocument.setOlePatron(olePatronDocument);
        }
        return oleLoanDocument;
    }

    public OlePatronDocument getOlePatronDocument(String patronId) {
        LOG.debug("Inside the getOlePatronDocument method");
        Map barMap = new HashMap();
        barMap = new HashMap();
        barMap.put(OLEConstants.OlePatron.PATRON_ID, patronId);
        List<OlePatronDocument> olePatronDocumentList = (List<OlePatronDocument>) getBusinessObjectService().findMatching(OlePatronDocument.class, barMap);
        return olePatronDocumentList != null && olePatronDocumentList.size() > 0 ? olePatronDocumentList.get(0) : null;
    }

    public void updateItemStatusWithMissingItem(OleLoanDocument oleLoanDocument) throws Exception {
        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = oleLoanDocument.getOleItem();
        oleItem.setCheckinNote(oleLoanDocument.getDescription());
        updateItemStatus(oleItem, OLEConstants.ITEM_STATUS_RETURNED_MISSING);
        OleItemAvailableStatus itemAvailableStatus = validateAndGetItemStatus(OLEConstants.ITEM_STATUS_RETURNED_MISSING);
        oleLoanDocument.setItemStatus(itemAvailableStatus != null ? itemAvailableStatus.getItemAvailableStatusName() : null);
        oleLoanDocument.setItemStatusCode(OLEConstants.ITEM_STATUS_RETURNED_MISSING);
    }

    /**
     * This method returns PatronLoanedItem using itemId
     *
     * @param itemId
     * @return List
     * @throws Exception
     */
    public OleLoanDocument getPatronRenewalItem(String itemId) throws Exception {
        LOG.debug("Inside the getPatronRenewalItem method");
        Map itemBarMap = new HashMap();
        itemBarMap.put(OLEConstants.OleDeliverRequest.ITEM_ID, itemId);
        List<OleLoanDocument> matchingLoan = (List<OleLoanDocument>) getBusinessObjectService().findMatching(OleLoanDocument.class, itemBarMap);
        OleLoanDocument oleLoanDocument = matchingLoan.get(0);
        setBibInfo(oleLoanDocument);
        Map map = new HashMap();
        map.put(OLEConstants.OlePatron.PATRON_ID, oleLoanDocument.getPatronId());
        OlePatronDocument olePatronDocument = (OlePatronDocument) getBusinessObjectService().findByPrimaryKey(OlePatronDocument.class, map);
        oleLoanDocument.setBorrowerTypeId(olePatronDocument.getOleBorrowerType().getBorrowerTypeId());
        oleLoanDocument.setBorrowerTypeName(olePatronDocument.getOleBorrowerType().getBorrowerTypeName());
        oleLoanDocument.setBorrowerTypeCode(olePatronDocument.getOleBorrowerType().getBorrowerTypeCode());
        return oleLoanDocument;
    }

    public void setBibInfo(OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the setBibInfo method");
        String itemUuid = oleLoanDocument.getItemId();
        org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(itemUuid);
        oleLoanDocument.setTitle(item.getHolding().getBib().getTitle());
        oleLoanDocument.setAuthor(item.getHolding().getBib().getAuthor());
        oleLoanDocument.setInstanceUuid(item.getHolding().getId());
        oleLoanDocument.setItemUuid(item.getId());
        String itemXml = item.getContent()!=null ? item.getContent() : getItemXML(item.getId());
        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = getItemPojo(itemXml);
        OleHoldings oleHoldings = new HoldingOlemlRecordProcessor().fromXML(item.getHolding().getContent());
        if(oleItem.getCallNumber()!=null && !StringUtils.isEmpty(oleItem.getCallNumber().getNumber())){
            oleLoanDocument.setItemCallNumber(oleItem.getCallNumber().getNumber());
        }else {
            oleLoanDocument.setItemCallNumber(getItemCallNumber(oleHoldings.getCallNumber()));
        }
        getLocation(oleItem, oleLoanDocument,item);

    }

    public void updateClaimsReturnedInLoanDocument(OleLoanDocument oleLoanDocument, boolean claimsReturned) {
        LOG.debug("Inside the updateClaimsReturnedInLoanDocument method");
        oleLoanDocument.setClaimsReturnedIndicator(claimsReturned);
        getBusinessObjectService().save(oleLoanDocument);
    }

    /**
     * This method will return url for view link in patron record.
     *
     * @return Url
     */

    public String getUrl(String patronId) {
        String url = "patronbill?viewId=BillView&amp;methodToCall=start&amp;patronId=" + patronId;
        return url;
    }

    public boolean checkPendingRequestforItem(String itemUUID) {
        if (getOleDeliverRequestDocumentHelperService().getOleDeliverRequestBo(itemUUID) != null)
            return true;
        return false;
    }

    public String getParameter(String name) {
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT,name);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        if(parameter==null){
            parameterKey = ParameterKey.create(OLEConstants.APPL_ID_OLE, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT,name);
            parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        }
        return parameter!=null?parameter.getValue():null;
    }

    public OleLoanDocument overrideSaveLoanForRenewal(OleLoanDocument oleLoanDocument) throws Exception {
        Date pastDueDate = oleLoanDocument.getLoanDueDate();
        oleLoanDocument.setLoanDueDate(oleLoanDocument.getRenewalLoanDueDate());
        compareExpirationDateWithDueDate(oleLoanDocument);
        if (oleLoanDocument.isRenewalItemFlag()) {
            if (oleLoanDocument.getNumberOfRenewals() == null)
                oleLoanDocument.setNumberOfRenewals("0");
            String noOfRenewal = "" + (Integer.parseInt(oleLoanDocument.getNumberOfRenewals()) + 1);
            if(noOfRenewal!=null && !noOfRenewal.isEmpty()){
                oleLoanDocument.setNumberOfOverdueNoticesSent("0");
            }
            oleLoanDocument.setNumberOfRenewals(noOfRenewal);
            oleLoanDocument.setPastDueDate(pastDueDate);
            oleLoanDocument.setRenewalItemFlag(false);
            saveLoan(oleLoanDocument);
        }
        return oleLoanDocument;
    }

    /**
     * This method returns DefaultCirculationDesk using principleId
     *
     * @param principleId
     * @return OleCirculationDeskDetail
     */
    public OleCirculationDeskDetail getDefaultCirculationDesk(String principleId) {
        LOG.debug("Inside the getDefaultCirculationDesk method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.OPTR_ID, principleId);
        List<OleCirculationDeskDetail> oleCirculationDeskDetails = (List<OleCirculationDeskDetail>) getBusinessObjectService().findMatching(OleCirculationDeskDetail.class, barMap);
        for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetails) {
            if (oleCirculationDeskDetail.isDefaultLocation()) {
                return oleCirculationDeskDetail;
            }
        }
        return null;
    }

    public boolean isValidCirculationDesk() {
        String value = getCircDesk();
        if (value==null) {
            return false;
        }
        circDeskId = value;
        return true;
    }

    public boolean isAuthorized(String principalId) {
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("operatorId", principalId);
        Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(principalId);
        return principal != null;
    }

    public List<String> getCircDeskList() {
        List<String> circkDesk = new ArrayList<String>();
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("operatorId", GlobalVariables.getUserSession().getPrincipalId());
        Collection<OleCirculationDeskDetail> oleCirculationDeskDetails = KRADServiceLocator.getBusinessObjectService().findMatching(OleCirculationDeskDetail.class, userMap);

        for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetails) {
            if (oleCirculationDeskDetail.isDefaultLocation() && oleCirculationDeskDetail.getOleCirculationDesk().isActive()) {
                circkDesk.add(oleCirculationDeskDetail.getOleCirculationDesk().getCirculationDeskId());
            }
        }
        for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetails) {
            if (!oleCirculationDeskDetail.isDefaultLocation() && oleCirculationDeskDetail.getOleCirculationDesk().isActive()) {
                circkDesk.add(oleCirculationDeskDetail.getOleCirculationDesk().getCirculationDeskId());
            }
        }
        return circkDesk;
    }

    /*public String getErrorMessage() {
        String errorMessage = "";
        if (getPermissionService().hasPermission(GlobalVariables.getUserSession().getPrincipalId(), OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CAN_OVERRIDE_LOAN) || getPermissionService().hasPermission(GlobalVariables.getUserSession().getPrincipalId(), OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CAN_LOAN)) {
            errorMessage = GlobalVariables.getUserSession().getPrincipalName() + " " + OLEConstants.OleCirculationDesk.OLE_CIRCULATION_DESK_VALIDATIONS;
        } else {
            errorMessage = GlobalVariables.getUserSession().getPrincipalName() + " " + OLEConstants.UNAUTHORIZED_LOAN_USER;
        }
        return errorMessage;
    }*/

    public CallNumber getCallNumber(OleLoanFastAdd oleLoanFastAdd) {
        CallNumber callNumber = new CallNumber();
        callNumber.setNumber(oleLoanFastAdd.getCallNumber());
        ShelvingScheme shelvingScheme = new ShelvingScheme();
        shelvingScheme.setCodeValue(oleLoanFastAdd.getCallNumberType());
        callNumber.setShelvingScheme(shelvingScheme);
        return callNumber;
    }

    public OleCirculationDesk getCirculationDeskByLocationId(String locationId) {
        OleCirculationDeskLocation oleCirculationDeskLocation = getOleCirculationDeskLocationByLocationId(locationId);
        if (oleCirculationDeskLocation != null) {
            Map<String, String> userMap = new HashMap<String, String>();
            userMap.put("circulationDeskId", oleCirculationDeskLocation.getCirculationDeskId());
            List<OleCirculationDesk> oleCirculationDesks = (List<OleCirculationDesk>) getBusinessObjectService().findMatching(OleCirculationDesk.class, userMap);
            return oleCirculationDesks != null && oleCirculationDesks.size() > 0 ? oleCirculationDesks.get(0) : null;
        }
        return null;
    }

    public OleCirculationDeskLocation getOleCirculationDeskLocationByLocationId(String locationId) {
        Map<String, String> locationMap = new HashMap<String, String>();
        locationMap.put("circulationDeskLocation", locationId);
        List<OleCirculationDeskLocation> oleCirculationDeskLocations = (List<OleCirculationDeskLocation>) getBusinessObjectService().findMatching(OleCirculationDeskLocation.class, locationMap);
        return oleCirculationDeskLocations != null && oleCirculationDeskLocations.size() > 0 ? oleCirculationDeskLocations.get(0) : null;
    }

    public boolean checkPermissionForRemoveNote(String principalId) {
        PermissionService service = KimApiServiceLocator.getPermissionService();
        return service.hasPermission(principalId, OLEConstants.DLVR_NMSPC, OLEConstants.CAN_REMOVE_NOTE);
    }

    public OleNoticeBo getNotice(OleLoanDocument oleLoanDocument) {

        OleLocation oleLocation = null;
        OleLoanDocument newLoanDocument;
        OleCirculationDesk oleCirculationDesk = null;
        Map<String, String> patronMap = new HashMap<String, String>();
        patronMap.put(OLEConstants.OleDeliverRequest.PATRON_ID, oleLoanDocument.getPatronId());
        List<OlePatronDocument> patronDocumentList = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
        if (patronDocumentList.size() == 0) {
            return null;
        }
        OlePatronDocument olePatronDocument = patronDocumentList.get(0);
        EntityTypeContactInfoBo entityTypeContactInfoBo = olePatronDocument.getEntity().getEntityTypeContactInfos().get(0);

        if (oleLoanDocument.getCirculationLocationId() != null) {
            oleCirculationDesk = this.getOleCirculationDesk(oleLoanDocument.getCirculationLocationId());
            oleLocation = oleCirculationDesk != null ? oleCirculationDesk.getOleCirculationDeskLocations().get(0).getLocation() : null;
        }
        String locationName = oleLocation != null ? oleLocation.getLocationName() : null;
        OleNoticeBo oleNoticeBo = new OleNoticeBo();
        oleNoticeBo.setTitle(oleLoanDocument.getTitle() != null ? oleLoanDocument.getTitle() : "");
        oleNoticeBo.setAuthor(oleLoanDocument.getAuthor() != null ? oleLoanDocument.getAuthor() : "");
        oleNoticeBo.setItemId(oleLoanDocument.getItemId() != null ? oleLoanDocument.getItemId() : "");
        oleNoticeBo.setNoticeName("Return With Missing Piece Notice");
        oleNoticeBo.setNoticeSpecificContent("The following item(s) returned by you is missing one or more of its pieces.Please return the missing piece(s) to the library shown above or contact the library about this matter to avoid incurring any penalties.");
        oleNoticeBo.setCirculationDeskAddress("");
        oleNoticeBo.setCirculationDeskEmailAddress("");
        if (oleCirculationDesk != null)
            oleNoticeBo.setCirculationDeskName(oleCirculationDesk.getCirculationDeskPublicName());
        oleNoticeBo.setCirculationDeskPhoneNumber("");
        oleNoticeBo.setItemCallNumber(oleLoanDocument.getItemCallNumber() != null ? oleLoanDocument.getItemCallNumber() : "");
        oleNoticeBo.setItemShelvingLocation(oleLoanDocument.getItemLocation() != null ? oleLoanDocument.getItemLocation() : "");
        oleNoticeBo.setVolumeNumber(oleLoanDocument.getItemVolumeNumber() != null ? oleLoanDocument.getItemVolumeNumber() : "");
        oleNoticeBo.setPatronName(olePatronDocument.getEntity().getNames().get(0).getFirstName() + "  " + olePatronDocument.getEntity().getNames().get(0).getLastName());
        oleNoticeBo.setOleItem(oleLoanDocument.getOleItem());
        try {
            newLoanDocument = this.getPatronPreferredAddress(entityTypeContactInfoBo, oleLoanDocument);
            newLoanDocument = this.getPatronHomeEmailId(entityTypeContactInfoBo, oleLoanDocument);
            newLoanDocument = this.getPatronHomePhoneNumber(entityTypeContactInfoBo, oleLoanDocument);
            oleNoticeBo.setPatronAddress(newLoanDocument.getPreferredAddress() != null ? newLoanDocument.getPreferredAddress() : "");
            oleNoticeBo.setPatronEmailAddress(newLoanDocument.getEmail() != null ? newLoanDocument.getEmail() : "");
            oleNoticeBo.setPatronPhoneNumber(newLoanDocument.getPhoneNumber() != null ? newLoanDocument.getPhoneNumber() : "");
        } catch (Exception e) {
            LOG.error("Exception occured while setting patron Details" + e, e);
        }

        return oleNoticeBo;

    }

    public String getCircDeskId() {
        return circDeskId;
    }

    public void setCircDeskId(String circDeskId) {
        this.circDeskId = circDeskId;
    }

    public List<String> getLoanUserList(List<String> loginUserList, StringBuffer buffer) {
        int count = loginUserList.size() - 1;
        String user = loginUserList.size() > 0 ? loginUserList.get(count) : "";
        if (!user.equalsIgnoreCase(buffer.toString())) {
            loginUserList.add(buffer.toString());
        }
        return loginUserList;
    }

    public void setErrorFlagForItem(OleLoanDocument oleLoanDocument, OleLoanForm oleLoanForm) {
        boolean hasRole = false;
        Role role;
        RoleServiceImpl roleServiceImpl = new RoleServiceImpl();
        List<String> roles = new ArrayList<>();
        role = roleServiceImpl.getRoleByNamespaceCodeAndName(OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CIRC_DESK_ATTENDANT_ONE);
        roles.add(role.getId());
        role = roleServiceImpl.getRoleByNamespaceCodeAndName(OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CIRC_DESK_ATTENDANT_TWO);
        roles.add(role.getId());

        hasRole = roleServiceImpl.principalHasRole(GlobalVariables.getUserSession().getPrincipalId(), roles, null);
        if (StringUtils.isNotEmpty(oleLoanForm.getRoleName())) {
            if (oleLoanDocument.isBlockCheckinItem()) {
                oleLoanForm.setBlockItem(true);
                oleLoanForm.setBlockPatron(false);
            }
        } else if (StringUtils.isEmpty(oleLoanForm.getRoleName()) && !hasRole) {
            oleLoanForm.setBlockItem(true);
            oleLoanForm.setBlockPatron(false);
        } else {
            oleLoanForm.setBlockItem(true);
            oleLoanForm.setBlockPatron(true);
        }
    }

    public void setErrorFlagForPatron(OleLoanDocument oleLoanDocument, OleLoanForm oleLoanForm) {
        boolean hasRole = false;
        RoleServiceImpl roleServiceImpl = new RoleServiceImpl();
        List<String> roles = new ArrayList<>();

        if (StringUtils.isEmpty(oleLoanForm.getRoleName()) && oleLoanForm.getRoleName() == null) {
            Role role = roleServiceImpl.getRoleByNamespaceCodeAndName(OLEConstants.OlePatron.PATRON_NAMESPACE, OLEConstants.CIRC_DESK_ATTENDANT_ONE);
            roles.add(role.getId());
            hasRole = roleServiceImpl.principalHasRole(GlobalVariables.getUserSession().getPrincipalId(), roles, null);
        }
        if (hasRole) {
            oleLoanForm.setBlockItem(true);
            oleLoanForm.setBlockPatron(true);
        } else {
            oleLoanForm.setBlockItem(false);
            oleLoanForm.setBlockPatron(true);
        }
    }


    public String getCircDesk() {
        String circkDesk = null;
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("operatorId", GlobalVariables.getUserSession().getPrincipalId());
        Collection<OleCirculationDeskDetail> oleCirculationDeskDetails = KRADServiceLocator.getBusinessObjectService().findMatching(OleCirculationDeskDetail.class, userMap);

        for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetails) {
            if (oleCirculationDeskDetail.isDefaultLocation() && oleCirculationDeskDetail.getOleCirculationDesk().isActive()) {
                circkDesk = (oleCirculationDeskDetail.getOleCirculationDesk().getCirculationDeskId());
                break;
            }
        }
        return circkDesk;
    }

    public void checkReplacementFineExist(OleLoanDocument oleLoanDocument) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(OLEConstants.ITEM_BARCODE, oleLoanDocument.getItemId());
        map.put(OLEConstants.FEE_TYPE_FIELD, getOleFeeTypeCode(OLEConstants.FEE_TYPE_CODE_REPL_FEE));
        List<FeeType> feeTypes = (List<FeeType>) KRADServiceLocator.getBusinessObjectService().findMatching(FeeType.class, map);
        if (feeTypes != null && feeTypes.size() > 0) {
            oleLoanDocument.setReplacementFeeExist(true);
           /* for(FeeType feeType:feeTypes){
                if(feeType.getBalFeeAmount().compareTo(OLEConstants.BIGDECIMAL_DEF_VALUE)!=0){
                    oleLoanDocument.setReplacementFeePaidStatus(false);
                    break;
                }
            }*/
        }

    }

    public void checkOverdueExist(OleLoanDocument oleLoanDocument) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(OLEConstants.ITEM_BARCODE, oleLoanDocument.getItemId());
        map.put(OLEConstants.FEE_TYPE_FIELD, getOleFeeTypeCode(OLEConstants.FEE_TYPE_CODE_OVERDUE));
        List<FeeType> feeTypes = (List<FeeType>) KRADServiceLocator.getBusinessObjectService().findMatching(FeeType.class, map);
        if (feeTypes != null && feeTypes.size() > 0) {
            for (FeeType feeType : feeTypes) {
                if (feeType.getBalFeeAmount().compareTo(OLEConstants.KUALI_BIGDECIMAL_DEF_VALUE) != 0) {
                    oleLoanDocument.setOverdueFineExist(true);
                    break;
                }
            }
        }

    }

    public String getOleFeeTypeCode(String typeCode){
        Map<String, String> map = new HashMap<String, String>();
        map.put(OLEConstants.FEE_TYPE_CODE, typeCode);
        List<OleFeeType> feeTypes = (List<OleFeeType>) KRADServiceLocator.getBusinessObjectService().findMatching(OleFeeType.class, map);
        if(CollectionUtils.isNotEmpty(feeTypes)){
            return feeTypes.get(0).getFeeTypeId();
        }
        return "";
    }

    public void isItemLoanedByDifferentPatron(OleLoanDocument oleLoanDocument) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(OLEConstants.OleDeliverRequest.ITEM_ID, oleLoanDocument.getItemId());
        List<OleLoanDocument> oleLoanDocuments = (List<OleLoanDocument>) KRADServiceLocator.getBusinessObjectService().findMatching(OleLoanDocument.class, map);
        if (oleLoanDocuments != null && oleLoanDocuments.size() > 0) {
            for (OleLoanDocument loanDocument : oleLoanDocuments) {
                if (loanDocument.getItemId().equalsIgnoreCase(oleLoanDocument.getItemId()) && (oleLoanDocument.getPatronId() != loanDocument.getPatronId())) {
                    oleLoanDocument.setDifferentPatron(true);
                    break;
                }
            }
        }
    }

    public void checkItemDamagedStatus(OleLoanDocument oleLoanDocument) {
        if (oleLoanDocument.getOleItem().isItemDamagedStatus()) {
            oleLoanDocument.setItemDamagedStatus(true);
        }
    }

    public void isMissingPieceFlagActive(OleLoanDocument oleLoanDocument) {
        if (oleLoanDocument.getOleItem().isMissingPieceFlag()) {
            oleLoanDocument.setMissingPieceFlag(true);
        }
    }

    public boolean isClaimsReturnedItem(String itemBarcode, OleLoanDocument oleLoanDocument) throws Exception {
       /* Map itemUuid = QueryServiceImpl.getInstance().getItemDetails(itemBarcode, oleLoanDocument.getItemUuid());
        oleLoanDocument.setInstanceUuid((String) itemUuid.get(OLEConstants.INSTANCE_UUID));
        oleLoanDocument.setItemUuid((String) itemUuid.get(OLEConstants.ITEM_UUID));
        oleLoanDocument.setBibUuid((String) itemUuid.get(OLEConstants.BIB_UUID));*/
        LOG.debug("Inside the addLoan method");
        org.kuali.ole.docstore.common.document.Item item = new ItemOleml();
        org.kuali.ole.docstore.common.search.SearchParams search_Params = new org.kuali.ole.docstore.common.search.SearchParams();
        SearchResponse searchResponse = null;
        search_Params.getSearchConditions().add(search_Params.buildSearchCondition("phrase", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), item.ITEM_BARCODE, itemBarcode), ""));
        search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), "ItemIdentifier_display"));
        search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Holdings.HOLDINGSIDENTIFIER));
        search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), Bib.BIBIDENTIFIER));
        if (itemBarcode != null && !itemBarcode.isEmpty()) {
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
            if (searchResponse.getSearchResults() != null && searchResponse.getSearchResults().size() > 0) {
                for (SearchResult searchResult : searchResponse.getSearchResults()) {
                    for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                        String fieldName = searchResultField.getFieldName();
                        String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";

                        if (fieldName.equalsIgnoreCase( Holdings.HOLDINGSIDENTIFIER) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setInstanceUuid(fieldValue);
                        } else if (fieldName.equalsIgnoreCase( Bib.BIBIDENTIFIER) && !fieldValue.isEmpty()) {
                            oleLoanDocument.setBibUuid(fieldValue);
                        } else {
                            oleLoanDocument.setItemUuid(fieldValue);
                        }

                    }
                }
            } else {
                throw new Exception(OLEConstants.ITM_BARCD_NT_AVAL_DOC);
            }


        } else {

            item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(oleLoanDocument.getItemUuid());


            oleLoanDocument.setInstanceUuid(item.getHolding().getId());
            oleLoanDocument.setItemUuid(item.getId());
            oleLoanDocument.setBibUuid(item.getHolding().getBib().getId());
        }
        String itemXml = getItemXML(oleLoanDocument.getItemUuid());
        org.kuali.ole.docstore.common.document.content.instance.Item oleItem = getItemPojo(itemXml);

        org.kuali.ole.docstore.common.document.Item itemXML = new ItemOleml();
        itemXML.setContent(getItemOlemlRecordProcessor().toXML(oleItem));
        if (oleItem.isClaimsReturnedFlag()) {
            return true;
        }
        return false;
    }

    public OleLoanForm processLoan(OleLoanForm oleLoanForm, OleLoanDocument oleLoanDocument) throws Exception {
        oleLoanDocument.setPatronId(oleLoanForm.getPatronId());
        oleLoanDocument.setProxyPatronId(oleLoanForm.getProxyPatronId());
        oleLoanDocument.setRealPatronBarcode(oleLoanForm.getRealPatronBarcode());
        oleLoanDocument.setRealPatronType(oleLoanForm.getRealPatronType());
        oleLoanDocument.setCirculationLocationId(oleLoanForm.getCirculationDesk());
        oleLoanDocument.setBorrowerTypeId(oleLoanForm.getBorrowerTypeId());
        oleLoanDocument.setBorrowerTypeName(oleLoanForm.getBorrowerType());
        oleLoanDocument.setBorrowerTypeCode(oleLoanForm.getBorrowerCode());
        List<OleLoanDocument> existingItemList = new ArrayList<OleLoanDocument>();
        oleLoanDocument = addLoan(oleLoanForm.getPatronBarcode(), oleLoanForm.getItem(), oleLoanDocument,null);
        oleLoanForm.setItemUuid(oleLoanDocument.getItemUuid());
        oleLoanForm.setNonCirculatingFlag(oleLoanDocument.isNonCirculatingItem());
        oleLoanForm.setInstanceUuid(oleLoanDocument.getInstanceUuid());
        oleLoanForm.setOleItem(oleLoanDocument.getOleItem());
        oleLoanForm.setDueDateMap(oleLoanDocument.getLoanDueDate());
        oleLoanForm.setMessage(oleLoanDocument.getErrorMessage());
        oleLoanForm.setRoleName(oleLoanDocument.getRoleName());
        oleLoanForm.getErrorsAndPermission().putAll(oleLoanDocument.getErrorsAndPermission());
        if (oleLoanForm.getPatronName() == null) {
            oleLoanForm.setPatronName(oleLoanDocument.getPatronName());
        }
        if(oleLoanForm.getRealPatronName()!=null){
            oleLoanDocument.setRealPatronName(oleLoanForm.getRealPatronName());
        }
        if (oleLoanDocument.getErrorMessage() == null) {
            existingItemList.add(oleLoanDocument);
            oleLoanForm.setBlockItem(false);
            oleLoanForm.setBlockPatron(false);
            oleLoanForm.setItem("");
            oleLoanForm.setInformation("");
        } else {
            oleLoanForm.setDueDateEmpty(oleLoanDocument.isDueDateEmpty());
            oleLoanForm.setDummyLoan(oleLoanDocument);
            oleLoanForm.setSuccess(false);
            if (oleLoanDocument.getOleItem() != null) {
                oleLoanForm.setDescription(oleLoanDocument.getOleItem().getCheckinNote());
            }
            oleLoanForm.setInformation("");
            setErrorFlagForItem(oleLoanDocument, oleLoanForm);
        }
        if (oleLoanForm.getLoanList() != null && !oleLoanForm.getLoanList().isEmpty()) {
            existingItemList.addAll(oleLoanForm.getLoanList());
        }
        if (oleLoanDocument.getItemLoanStatus() != null && oleLoanDocument.getItemLoanStatus().equalsIgnoreCase(OLEConstants.ITEM_STATUS_LOST) || oleLoanDocument.isReplacementFeeExist()) {
            oleLoanForm.setBlockUser(true);
        }
        else if (oleLoanDocument.isOverdueFineExist() && oleLoanDocument.isDifferentPatron()) {
            oleLoanForm.setBlockUser(true);
        }else {
            oleLoanForm.setBlockUser(false);
        }
        oleLoanForm.setLoanList(existingItemList);
        List<OleLoanDocument> oleLoanDocumentList = new ArrayList<OleLoanDocument>();
        oleLoanDocumentList.add(oleLoanDocument);

        if (getParameter(OLEConstants.PRINT_DUE_DATE_PER_TRANSACTION).equalsIgnoreCase("Yes")) {
            oleLoanForm.setDueDateSlip(true);
        }
        oleLoanForm.setItemFocus(true);
        oleLoanForm.setPatronFocus(false);
        String audioOption = getParameter(OLEConstants.AUDIO_OPTION);
        oleLoanForm.setAudioEnable(audioOption != null && !audioOption.isEmpty() && audioOption.equalsIgnoreCase(OLEConstants.TRUE));
        oleLoanForm.setItemUuid(null);
        oleLoanForm.setInstanceUuid(null);
        oleLoanForm.setSuccessMessage(oleLoanDocument.getSuccessMessage());
        return oleLoanForm;
    }

    public void updateMissingPiecesItemInfo(OleLoanDocument oleLoanDocument) throws Exception {
        LOG.debug("Inside the updateItemStatus method for missing pieces");
        try {
            String itemXmlContent = getItemXML(oleLoanDocument.getItemUuid());
            org.kuali.ole.docstore.common.document.content.instance.Item item = getItemPojo(itemXmlContent);
            item.setMissingPieceFlag(true);
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            String parsedDate = df.format((new Date()));
            item.setMissingPieceEffectiveDate(parsedDate);
            if (oleLoanDocument.getMissingPieceNote() != null && !oleLoanDocument.getMissingPieceNote().isEmpty()) {
                item.setMissingPieceFlagNote(oleLoanDocument.getMissingPieceNote());
            }
            int newMissingPieceCount = 0;
            if (oleLoanDocument.getMissingPiecesCount() != null && !oleLoanDocument.getMissingPiecesCount().equalsIgnoreCase("")) {
                newMissingPieceCount = Integer.parseInt(oleLoanDocument.getMissingPiecesCount());
            }
            if (newMissingPieceCount == 0) {
                item.setMissingPiecesCount(item.getMissingPiecesCount());
            } else {
                if (oleLoanDocument.getOleItem() != null && oleLoanDocument.getOleItem().getMissingPiecesCount() != null && (!oleLoanDocument.getOleItem().getMissingPiecesCount().equalsIgnoreCase(""))) {
                    int oldMissingPieceCount = Integer.parseInt(item.getMissingPiecesCount());
                    item.setMissingPiecesCount(oldMissingPieceCount + newMissingPieceCount + "");
                } else {
                    item.setMissingPiecesCount(newMissingPieceCount + "");
                }
            }
            oleLoanDocument.setOleItem(item);
            itemXmlContent = getItemOlemlRecordProcessor().toXML(item);
            if (LOG.isDebugEnabled()){
                LOG.debug("itemXmlContent" + itemXmlContent);
            }
            Item oleItem = new ItemOleml();
            oleItem.setContent(itemXmlContent);
            oleItem.setCategory(OLEConstants.WORK_CATEGORY);
            oleItem.setType(DocType.ITEM.getCode());
            oleItem.setFormat(OLEConstants.OLEML_FORMAT);
            oleItem.setId(oleLoanDocument.getItemUuid());
            getDocstoreClientLocator().getDocstoreClient().updateItem(oleItem);
        } catch (Exception e) {
            LOG.error(OLEConstants.ITM_STS_TO_DOC_FAIL + e, e);
            throw new Exception(OLEConstants.ITM_STS_TO_DOC_FAIL);
        }
    }


    public HashMap<String,Integer> getItemTypeMap(List<OleLoanDocument> oleLoanDocuments,OleLoanDocument currentLoan){
        Long begin = System.currentTimeMillis();
        HashMap<String,Integer> itemTypeMap = new HashMap<>();
        for(OleLoanDocument oleLoanDocument : oleLoanDocuments){
            if(itemTypeMap.containsKey(oleLoanDocument.getItemType())){
                Integer count = itemTypeMap.get(oleLoanDocument.getItemType());
                count++;
                itemTypeMap.put(oleLoanDocument.getItemType(),count);
            }else{
                itemTypeMap.put(oleLoanDocument.getItemType(),1);
            }
        }
        if(itemTypeMap.containsKey(currentLoan.getItemType())){
            Integer count = itemTypeMap.get(currentLoan.getItemType());
            count++;
            itemTypeMap.put(currentLoan.getItemType(),count);
        }else{
            itemTypeMap.put(currentLoan.getItemType(),1);
        }
        Long end = System.currentTimeMillis();
        Long timeTaken = end - begin;
        LOG.info("The Time Taken for getItemTypeMap in Add Item"+timeTaken);
        return itemTypeMap;
    }


    public HashMap<String,Integer> getItemTypeFromCurrentLoan(HashMap<String,Integer> itemTypeMap,OleLoanDocument currentLoan){
        if(itemTypeMap!=null){
            if(itemTypeMap.containsKey(currentLoan.getItemType())){
                Integer count = itemTypeMap.get(currentLoan.getItemType());
                count++;
                itemTypeMap.put(currentLoan.getItemType(),count);
            }else{
                itemTypeMap.put(currentLoan.getItemType(),1);
            }
        }
        return itemTypeMap;
    }

    private String getPaymentStatusId(String paymentStatusCode) throws Exception{
        Map<String,String> mapPayStatus = new HashMap();
        mapPayStatus.put(OLEConstants.PAY_STATUS_CODE,paymentStatusCode);
        List<OlePaymentStatus> patronBillPayments = (List<OlePaymentStatus>)getBusinessObjectService().findMatching(OlePaymentStatus.class, mapPayStatus);
        return CollectionUtils.isNotEmpty(patronBillPayments)?patronBillPayments.get(0).getPaymentStatusId():"";
    }

    public boolean checkPatronBill(String patronId)throws Exception{
        Map map = new HashMap();
        map.put(OLEConstants.PTRN_ID, patronId);
        map.put(OLEConstants.FEE_TYPE_PAY_STATUS, getPaymentStatusId(OLEConstants.PAY_STATUS_OUTSTANDING_CODE));
        List<PatronBillPayment> patronBillPayments = (List<PatronBillPayment>) getBusinessObjectService().findMatching(PatronBillPayment.class, map);
        if (patronBillPayments != null && patronBillPayments.size() > 0) {
            return true;
        } else {
            map.clear();
            map.put(OLEConstants.PTRN_ID, patronId);
            map.put(OLEConstants.FEE_TYPE_PAY_STATUS, getPaymentStatusId(OLEConstants.PAY_STATUS_PART_CODE));
            List<PatronBillPayment> payments = (List<PatronBillPayment>) getBusinessObjectService().findMatching(PatronBillPayment.class, map);
            if (payments != null && payments.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public OleCirculationDesk getCirculationDeskByLocationCode(String locationCode) {
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put(OLEConstants.OleCirculationDesk.OLE_CIRCULATION_DESK_CD, locationCode);
        List<OleCirculationDesk> oleCirculationDesks = (List<OleCirculationDesk>) getBusinessObjectService().findMatching(OleCirculationDesk.class, userMap);
        return oleCirculationDesks != null && oleCirculationDesks.size() > 0 ? oleCirculationDesks.get(0) : null;
    }

    public void buildSearchParams(SearchParams searchParams){
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ItemBarcode_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "id"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "bibIdentifier"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "Title_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "Author_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "LocalId_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "holdingsIdentifier"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ClaimsReturnedFlag_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "claimsReturnedFlagCreateDate"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "claimsReturnedNote"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "CallNumber_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "TemporaryItemTypeFullValue_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ItemTypeFullValue_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "Enumeration_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "Chronology_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ItemStatus_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "ItemDamagedStatus_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "DamagedItemNote_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "MissingPieceFlagNote_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "Location_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "HoldingsCopyNumber_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "HoldingsCallNumber_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "HoldingsLocation_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "MissingPieceCount_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "NumberOfPieces_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "CallNumberPrefix_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "CopyNumber_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "proxyBorrower"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "VolumeNumberLabel_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "dueDateTime"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item", "NumberOfRenew_display"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item" , "checkOutDateTime"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item","ClaimsReturnedFlag_search"));
        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField("item","itemStatusEffectiveDate"));
    }


    public void getLocationBySolr(SearchResultField searchResultField,OleLoanDocument oleLoanDocument)throws Exception{
        String locationCode = searchResultField.getFieldValue().split("/")[searchResultField.getFieldValue().split("/").length-1];
        String levelFullName = "";
        if(locationName.containsKey(locationCode)) {
            levelFullName = locationName.get(locationCode);
        }
        else {
            OleLocation oleLocation = getLocationByLocationCode(locationCode);
            levelFullName = getFullPathLocationByName(oleLocation);
            locationName.put(locationCode,levelFullName);
        }

        oleLoanDocument.setItemLocation(levelFullName);
        oleLoanDocument.setLocation(levelFullName);
    }

    public List<OleLoanDocument> buildSearchResultsFields(SearchResponse searchResponse,HashMap<String,Object> loanMap)throws Exception{
        int count = 0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        List<OleLoanDocument> oleLoanDocuments = new ArrayList<>();
        if(searchResponse!=null){
            for (SearchResult searchResult : searchResponse.getSearchResults()) {
                OleLoanDocument oleLoanDocument = null;
                for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {

                    if(searchResultField.getFieldValue()!=null){

                        if(searchResultField.getFieldName().equalsIgnoreCase("ItemBarcode_display")) {
                            oleLoanDocument = new OleLoanDocument();
                            oleLoanDocument.setItemId(searchResultField.getFieldValue());
                        }
                        if (searchResultField.getFieldName().equalsIgnoreCase("id")) {
                           //(OleLoanDocument)loanMap.get((searchResultField.getFieldValue()));
                            if (null == oleLoanDocument) {
                                oleLoanDocument = new OleLoanDocument();
                            }
                            oleLoanDocument.setItemUuid(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("bibIdentifier")) {
                            oleLoanDocument.setBibUuid(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("Title_display")) {
                            oleLoanDocument.setTitle(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("Author_display")) {
                            oleLoanDocument.setAuthor(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("holdingsIdentifier")) {
                            oleLoanDocument.setInstanceUuid(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("claimsReturnedNote")) {
                            oleLoanDocument.setClaimsReturnNote(searchResultField.getFieldValue());
                            //  count++;
                        }

                        else if (searchResultField.getFieldName().equalsIgnoreCase("ClaimsReturnedFlag_display") && searchResultField.getFieldValue().equalsIgnoreCase("true")) {
                            oleLoanDocument.setClaimsReturnedIndicator(true);
                            count++;
                        }
                        else  if (searchResultField.getFieldName().equalsIgnoreCase("Location_display")) {
                            getLocationBySolr(searchResultField,oleLoanDocument);
                            oleLoanDocument.setItemFullLocation(searchResultField.getFieldValue());
                        }
                        else  if (searchResultField.getFieldName().equalsIgnoreCase("HoldingsLocation_search") &&
                                (oleLoanDocument.getItemLocation()==null || oleLoanDocument.getItemLocation().isEmpty())) {
                            getLocationBySolr(searchResultField,oleLoanDocument);
                            oleLoanDocument.setItemFullLocation(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("claimsReturnedFlagCreateDate")) {
                            String[] formatStrings = new String[]{"MM/dd/yyyy hh:mm:ss","MM/dd/yyyy","yyyy-MM-dd hh:mm:ss"};
                            Date date =tryParse(formatStrings,searchResultField.getFieldValue());
                            oleLoanDocument.setClaimsReturnedDate(new Timestamp(date.getTime()));

                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("dueDateTime")) {
                            String[] formatStrings = new String[]{"MM/dd/yyyy hh:mm:ss","MM/dd/yyyy","yyyy-MM-dd hh:mm:ss"};
                             Date date =tryParse(formatStrings,searchResultField.getFieldValue());
                             oleLoanDocument.setLoanDueDate(new Timestamp(date.getTime()));

                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("CallNumber_display")) {
                            oleLoanDocument.setItemCallNumber(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("TemporaryItemTypeFullValue_search")) {
                            oleLoanDocument.setItemType(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("ItemTypeFullValue_display") &&
                                (oleLoanDocument.getItemType()==null || oleLoanDocument.getItemType().isEmpty())) {
                            oleLoanDocument.setItemType(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("TemporaryItemTypeCodeValue_search")) {
                            oleLoanDocument.setItemTypeName(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("ItemTypeCodeValue_display") &&
                                (oleLoanDocument.getItemTypeName()==null || oleLoanDocument.getItemTypeName().isEmpty())) {
                            oleLoanDocument.setItemTypeName(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("Enumeration_display")) {
                            oleLoanDocument.setEnumeration(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("Chronology_display")) {
                            oleLoanDocument.setChronology(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("ItemStatus_display")) {
                            oleLoanDocument.setItemStatus(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("ItemDamagedStatus_display")) {
                            oleLoanDocument.setItemDamagedStatus(searchResultField.getFieldValue().equalsIgnoreCase("true"));
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("DamagedItemNote_search")) {
                            oleLoanDocument.setItemDamagedNote(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("MissingPieceFlagNote_search")) {
                            oleLoanDocument.setMissingPieceNote(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("MissingPieceFlag_display")) {
                            oleLoanDocument.setMissingPieceFlag(searchResultField.getFieldValue().equalsIgnoreCase("true"));
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("CopyNumber_search")) {
                            oleLoanDocument.setItemCopyNumber(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("HoldingsCopyNumber_search") &&
                                (oleLoanDocument.getItemCopyNumber()==null || oleLoanDocument.getItemCopyNumber().isEmpty())) {
                            oleLoanDocument.setItemCopyNumber(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("HoldingsCallNumber_search") &&
                                (oleLoanDocument.getItemCallNumber()==null || oleLoanDocument.getItemCallNumber().isEmpty())) {
                            oleLoanDocument.setItemCallNumber(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("MissingPieceCount_search")) {
                            oleLoanDocument.setMissingPiecesCount(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("NumberOfPieces_search")) {
                            oleLoanDocument.setItemNumberOfPieces(Integer.parseInt(searchResultField.getFieldValue()));
                            oleLoanDocument.setBackUpNoOfPieces(searchResultField.getFieldValue());
                        }
                        else if (searchResultField.getFieldName().equalsIgnoreCase("proxyBorrower")) {
                            oleLoanDocument.setRealPatronName(searchResultField.getFieldValue());
                        }else if(searchResultField.getFieldName().equalsIgnoreCase("NumberOfRenew_display")) {
                            oleLoanDocument.setNumberOfRenewals(searchResultField.getFieldValue());
                        }else if(searchResultField.getFieldName().equalsIgnoreCase("checkOutDateTime")){
                            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                            Date date = formatter.parse(searchResultField.getFieldValue());
                            oleLoanDocument.setCreateDate(date);
                        }
                    }
                }
                if(oleLoanDocument.getOlePatron()!=null){
                    oleLoanDocument.getOlePatron().setNumberOfClaimsReturned(count);
                }
                oleLoanDocuments.add(oleLoanDocument);
            }

        }
        return oleLoanDocuments;
    }

    public Date tryParse(String[] formatStrings, String dateString) throws ParseException {
        StringBuffer exceptionMessage = new StringBuffer("Parse Exception'");
        for (String formatString : formatStrings) {
            try {
                return new SimpleDateFormat(formatString).parse(dateString);
            } catch (ParseException e) {
                exceptionMessage.append(e.getMessage());
            }
        }
        throw new ParseException(exceptionMessage.toString(), 0);
    }
    /**
     * This method is used to set the check-in date in the replacement fee bill generated throuigh batch job
     * @param oleLoanDocument
     */

    public void updateReplacementFeeBill(OleLoanDocument oleLoanDocument){
        long begin = System.currentTimeMillis();
        if(LOG.isInfoEnabled()){
       LOG.info("Inside the updateReplacementFee for the patron barcode :"+oleLoanDocument.getPatronBarcode() + "Item Barcode : " +oleLoanDocument.getItemId() );
        }
        Map map=new HashMap<String,String>();
        map.put(OLEConstants.FEE_TYPE_PATRON_ID,oleLoanDocument.getPatronId());
        map.put(OLEConstants.ITEM_BARCODE,oleLoanDocument.getItemId());
        map.put(OLEConstants.FEE_TYPE_FIELD, "2");
        List<FeeType> feeTypeList = (List<FeeType>) getBusinessObjectService().findMatching(FeeType.class, map);
        if(feeTypeList!=null && feeTypeList.size()>0){
            for(FeeType feeType:feeTypeList){
            if(feeType.getDueDate()!=null && oleLoanDocument.getLoanDueDate()!=null){
                if(feeType.getDueDate().getTime()==oleLoanDocument.getLoanDueDate().getTime()){
                    feeType.setCheckInDate(oleLoanDocument.getCheckInDate());
                    getBusinessObjectService().save(feeType);
                    break;
                }
            }

            }
        }
        long end = System.currentTimeMillis();
        long total = end - begin;
        LOG.info("Time taken Inside updateReplacementFeeBill"+total);
    }

    public OleLoanDocumentDaoOjb getOleLoanDocumentDaoOjb() {
        if(oleLoanDocumentDaoOjb == null){
            oleLoanDocumentDaoOjb = (OleLoanDocumentDaoOjb)SpringContext.getBean("oleLoanDao");
        }
        return oleLoanDocumentDaoOjb;
    }

    public void setOleLoanDocumentDaoOjb(OleLoanDocumentDaoOjb oleLoanDocumentDaoOjb) {
        this.oleLoanDocumentDaoOjb = oleLoanDocumentDaoOjb;
    }

    public void validateCalanderForCirculationDesk(String circulationDeskLocationId) throws Exception{
        OleCirculationDesk oleCirculationDesk = getOleCirculationDesk(circulationDeskLocationId);
        if (oleCirculationDesk != null && oleCirculationDesk.getCalendarGroupId() != null) {
            OleCalendarService oleCalendarService = new OleCalendarServiceImpl();
            OleCalendar oleCalendar = oleCalendarService.getActiveCalendar(new Timestamp(System.currentTimeMillis()), oleCirculationDesk.getCalendarGroupId());
            if (oleCalendar == null) {
                throw new Exception("Calendar does not exist");
            }
        }
       /* getDataCarrierService().addData(OLEConstants.GROUP_ID, oleCirculationDesk != null ? oleCirculationDesk.getCalendarGroupId() : "");*/
    }

    public OleLoanDocument retrieveByPatronAndItem(String patronId,String itemBarcode) throws Exception{
        LOG.debug("Inside the retrieveByPatronAndItem method");
        Long b1 = System.currentTimeMillis();
        SearchResponse searchResponse = new SearchResponse();
        HashMap<String,Object> loanMap = new HashMap<>();
        org.kuali.ole.docstore.common.document.Item item = new ItemOleml();
        if(patronId != null){
            String itemStatusParam = getParameter(OLEParameterConstants.ITEM_STATUS_FOR_RET_LOAN);
            String[] itemStatus = new String[0];
            if(itemStatusParam != null){
                itemStatus = itemStatusParam.split("[|]");
            }
            SearchParams searchParams = new SearchParams();
            List<SearchCondition> searchConditions = new ArrayList<>();
            int count = itemStatus.length;
            for(int i=0; i< count;i++) {
                if(i == (count-1)) {
                    searchConditions.add(searchParams.buildSearchCondition("", searchParams.buildSearchField("item", "ItemStatus_search", itemStatus[i]), "AND"));
                }
                else {
                    searchConditions.add(searchParams.buildSearchCondition("", searchParams.buildSearchField("item", "ItemStatus_search", itemStatus[i]), "OR"));
                }
            }

            searchConditions.add(searchParams.buildSearchCondition("phrase", searchParams.buildSearchField("item", "currentBorrower", patronId), "AND"));
            searchConditions.add(searchParams.buildSearchCondition("phrase", searchParams.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.ITEM.getCode(), item.ITEM_BARCODE, itemBarcode), "AND"));
            searchParams.setPageSize(Integer.parseInt(OLEConstants.MAX_PAGE_SIZE_FOR_LOAN));
            buildSearchParams(searchParams);
            searchParams.getSearchConditions().addAll(searchConditions);
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(searchParams);
        }
        List<OleLoanDocument> matchingLoan = buildSearchResultsFields(searchResponse,loanMap);
        Long b2 = System.currentTimeMillis();
        Long b3 = b2-b1;
        LOG.info("The Time taken retrieveByPatronAndItem--"+b3);
        return matchingLoan!=null && matchingLoan.size()>0 ? matchingLoan.get(0) : null;
    }

    public OLEDeliverNoticeHelperService getOleDeliverNoticeHelperService() {
        return oleDeliverNoticeHelperService;
    }

    public void setOleDeliverNoticeHelperService(OLEDeliverNoticeHelperService oleDeliverNoticeHelperService) {
        this.oleDeliverNoticeHelperService = oleDeliverNoticeHelperService;
    }
}


