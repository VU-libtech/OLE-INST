package org.kuali.ole.ncip.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.DataCarrierService;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.OLEParameterConstants;
import org.kuali.ole.deliver.bo.*;
import org.kuali.ole.deliver.calendar.bo.OleCalendar;
import org.kuali.ole.deliver.calendar.service.OleCalendarService;
import org.kuali.ole.deliver.calendar.service.impl.OleCalendarServiceImpl;
import org.kuali.ole.deliver.processor.LoanProcessor;

import org.kuali.ole.deliver.service.OleDeliverRequestDocumentHelperServiceImpl;
import org.kuali.ole.describe.bo.OleInstanceItemType;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.describe.bo.OleLocationLevel;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.*;
import org.kuali.ole.docstore.common.document.HoldingsTree;
import org.kuali.ole.docstore.common.document.content.bib.marc.BibMarcRecord;
import org.kuali.ole.docstore.common.document.content.bib.marc.BibMarcRecords;
import org.kuali.ole.docstore.common.document.content.bib.marc.xstream.BibMarcRecordProcessor;
import org.kuali.ole.docstore.common.document.content.instance.*;
import org.kuali.ole.docstore.common.document.content.instance.Item;
import org.kuali.ole.docstore.common.document.content.instance.xstream.HoldingOlemlRecordProcessor;

import org.kuali.ole.docstore.common.document.content.instance.xstream.ItemOlemlRecordProcessor;

import org.kuali.ole.docstore.common.search.SearchResponse;
import org.kuali.ole.docstore.common.search.SearchResult;
import org.kuali.ole.docstore.common.search.SearchResultField;

import org.kuali.ole.docstore.model.xmlpojo.ingest.*;

import org.kuali.ole.ncip.bo.*;

import org.kuali.ole.ncip.converter.OLECheckInItemConverter;
import org.kuali.ole.ncip.converter.OLECheckOutItemConverter;
import org.kuali.ole.ncip.converter.OLERenewItemConverter;
import org.kuali.ole.ncip.service.OLESIAPIHelperService;
import org.kuali.ole.service.OleCirculationPolicyService;
import org.kuali.ole.service.OleCirculationPolicyServiceImpl;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.util.DocstoreUtil;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.identity.address.EntityAddressBo;
import org.kuali.rice.kim.impl.identity.email.EntityEmailBo;
import org.kuali.rice.kim.impl.identity.entity.EntityBo;
import org.kuali.rice.kim.impl.identity.name.EntityNameBo;
import org.kuali.rice.kim.impl.identity.phone.EntityPhoneBo;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.ole.docstore.common.document.content.instance.Location;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krms.api.engine.EngineResults;
import org.kuali.rice.krms.api.engine.ResultEvent;
import org.kuali.rice.krms.framework.engine.BasicRule;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maheswarang
 * Date: 7/21/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLECirculationHelperServiceImpl {
    private static final Logger LOG = Logger.getLogger(OLECirculationHelperServiceImpl.class);
    private static final String DOCSTORE_URL = "docstore.url";
    private final String CREATE_NEW_DOCSTORE_RECORD_QUERY_STRING = "docAction=ingestContent&stringContent=";
    private BusinessObjectService businessObjectService = KRADServiceLocator.getBusinessObjectService();
    private LoanProcessor loanProcessor = new LoanProcessor();
    private OLECheckInItemConverter oleCheckInItemConverter = new OLECheckInItemConverter();
    private OLECheckOutItemConverter oleCheckOutItemConverter = new OLECheckOutItemConverter();
    private OLESIAPIHelperService oleSIAPIHelperService;
    private OleCirculationPolicyService oleCirculationPolicyService = getOleCirculationPolicyService();
    private DocstoreClientLocator docstoreClientLocator;
    public OleDeliverRequestDocumentHelperServiceImpl oleDeliverRequestDocumentHelperService = new OleDeliverRequestDocumentHelperServiceImpl();
    DocstoreUtil docstoreUtil = new DocstoreUtil();

    public DocstoreClientLocator getDocstoreClientLocator() {

        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);

        }
        return docstoreClientLocator;
    }


    public OleCirculationPolicyService getOleCirculationPolicyService() {
        if (null == oleCirculationPolicyService) {
            oleCirculationPolicyService = new OleCirculationPolicyServiceImpl();
        }
        return oleCirculationPolicyService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public OLESIAPIHelperService getOleSIAPIHelperService() {
        if (oleSIAPIHelperService == null) {
            oleSIAPIHelperService = SpringContext.getBean(OLESIAPIHelperService.class);
        }
        return oleSIAPIHelperService;
    }

    public void setOleSIAPIHelperService(OLESIAPIHelperService oleSIAPIHelperService) {
        this.oleSIAPIHelperService = oleSIAPIHelperService;
    }

    public OLELookupUser initialiseLookupUser(OlePatronDocument olePatronDocument, String agencyId) {
        OLELookupUser oleLookupUser = new OLELookupUser();
        oleLookupUser.setPatronId(olePatronDocument.getBarcode());
        OlePatronEmailBo olePatronEmailBo = getDefaultEmailBo(olePatronDocument.getOlePatronId());
        if (olePatronEmailBo != null) {
            oleLookupUser.setPatronEmail(olePatronEmailBo);
        }
        OlePatronAddressBo olePatronAddressBo = getDefaultAddressBo(olePatronDocument.getOlePatronId());
        if (olePatronAddressBo != null) {
            oleLookupUser.setPatronAddress(olePatronAddressBo);
        }
        OlePatronPhoneBo olePatronPhoneBo = getDefaultPhoneBo(olePatronDocument.getOlePatronId());
        if (olePatronPhoneBo != null) {
            oleLookupUser.setPatronPhone(olePatronPhoneBo);
        }
        if (getPatronPrivilege(olePatronDocument, agencyId) != null) {
            oleLookupUser.setOleUserPrivileges(getPatronPrivilege(olePatronDocument, agencyId));
        }
        OlePatronNameBo olePatronNameBo = getEntityNameBo(olePatronDocument.getOlePatronId());
        if (olePatronNameBo != null) {
            oleLookupUser.setPatronName(olePatronNameBo);
        }
        return oleLookupUser;
    }

    public EntityBo getEntity(String entityId) {
        LOG.info("Inside getEntity : Entity Id : " + entityId);
        Map<String, String> entityMap = new HashMap<>();
        entityMap.put("id", entityId);
        List<EntityBo> entityBoList = (List<EntityBo>) businessObjectService.findMatching(EntityBo.class, entityMap);
        if (entityBoList.size() > 0)
            return entityBoList.get(0);
        return null;
    }

    public OlePatronEmailBo getDefaultEmailBo(String entityId) {
        LOG.info("Inside getDefaultEmailBo : Entity Id : " + entityId);
        EntityBo entityBo = getEntity(entityId);
        OlePatronEmailBo olePatronEmailBo = null;
        if (entityBo != null) {
            if (entityBo.getEntityTypeContactInfos() != null && entityBo.getEntityTypeContactInfos().size() > 0)
                if (entityBo.getEntityTypeContactInfos().get(0).getEmailAddresses() != null && entityBo.getEntityTypeContactInfos().get(0).getEmailAddresses().size() > 0) {
                    for (EntityEmailBo entityEmailBo : entityBo.getEntityTypeContactInfos().get(0).getEmailAddresses()) {
                        if (entityEmailBo.getDefaultValue()) {
                            olePatronEmailBo = new OlePatronEmailBo();
                            olePatronEmailBo.setEmailTypeCode(entityEmailBo.getEmailTypeCode());
                            olePatronEmailBo.setEmailAddress(entityEmailBo.getEmailAddress());
                            return olePatronEmailBo;
                        }
                    }
                }
        }
        return null;
    }

    public OlePatronAddressBo getDefaultAddressBo(String entityId) {
        LOG.info("Inside getDefaultAddressBo : Entity Id : " + entityId);
        EntityBo entityBo = getEntity(entityId);
        EntityAddressBo entityAddressBo = null;
        OlePatronAddressBo olePatronAddressBo = null;
        if (entityBo != null) {
            if (entityBo.getEntityTypeContactInfos() != null && entityBo.getEntityTypeContactInfos().size() > 0)
                if (entityBo.getEntityTypeContactInfos().get(0).getDefaultAddress() != null) {
                    entityAddressBo = entityBo.getEntityTypeContactInfos().get(0).getDefaultAddress();
                    olePatronAddressBo = new OlePatronAddressBo();
                    olePatronAddressBo.setAddressTypeCode(entityAddressBo.getAddressTypeCode());
                    olePatronAddressBo.setCity(entityAddressBo.getCity());
                    olePatronAddressBo.setCountryCode(entityAddressBo.getCountryCode());
                    olePatronAddressBo.setLine1(entityAddressBo.getLine1());
                    olePatronAddressBo.setLine2(entityAddressBo.getLine2());
                    olePatronAddressBo.setLine3(entityAddressBo.getLine3());
                    olePatronAddressBo.setPostalCode(entityAddressBo.getPostalCode());
                    olePatronAddressBo.setStateProvinceCode(entityAddressBo.getStateProvinceCode());
                    return olePatronAddressBo;
                }
        }
        return null;
    }

    public OlePatronNameBo getEntityNameBo(String entityId) {
        LOG.info("Inside getEntityNameBo : Entity Id : " + entityId);
        EntityBo entityBo = getEntity(entityId);
        EntityNameBo entityNameBo = null;
        OlePatronNameBo olePatronNameBo = null;
        if (entityBo != null) {
            if (entityBo.getNames() != null && entityBo.getNames().size() > 0) {
                entityNameBo = entityBo.getNames().get(0);
                olePatronNameBo = new OlePatronNameBo();
                olePatronNameBo.setFirstName(entityNameBo.getFirstName());
                olePatronNameBo.setMiddleName(entityNameBo.getMiddleName());
                olePatronNameBo.setLastName(entityNameBo.getLastName());
                return olePatronNameBo;
            }
        }
        return null;
    }

    public OlePatronPhoneBo getDefaultPhoneBo(String entityId) {
        LOG.info("Inside getDefaultPhoneBo : Entity Id : " + entityId);
        EntityBo entityBo = getEntity(entityId);
        EntityPhoneBo entityPhoneBo = null;
        OlePatronPhoneBo olePatronPhoneBo = null;
        if (entityBo != null) {
            if (entityBo.getEntityTypeContactInfos().get(0) != null && entityBo.getEntityTypeContactInfos().size() > 0) {
                if (entityBo.getEntityTypeContactInfos().get(0).getDefaultPhoneNumber() != null) {
                    entityPhoneBo = entityBo.getEntityTypeContactInfos().get(0).getDefaultPhoneNumber();
                    olePatronPhoneBo = new OlePatronPhoneBo();
                    olePatronPhoneBo.setPhoneTypeCode(entityPhoneBo.getPhoneTypeCode());
                    olePatronPhoneBo.setPhoneNumber(entityPhoneBo.getPhoneNumber());
                    return olePatronPhoneBo;
                }
            }
        }
        return null;
    }

    public List<OLEUserPrivilege> getPatronPrivilege(OlePatronDocument olePatronDocument, String agencyId) {
        List<OLEUserPrivilege> userPrivilegeList;
        if (olePatronDocument != null) {
            userPrivilegeList = new ArrayList<OLEUserPrivilege>();
            OLEUserPrivilege courtesyPrivilege = new OLEUserPrivilege();
            courtesyPrivilege.setUserPrivilegeType(OLEConstants.COURTESY_NOTICE);
            courtesyPrivilege.setUserPrivilegeDescription(OLEConstants.COURTESY_DESCRIPTION);
            if (olePatronDocument.isCourtesyNotice()) {
                courtesyPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.TRUE));
            } else {
                courtesyPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.FALSE));
            }
            userPrivilegeList.add(courtesyPrivilege);
            OLEUserPrivilege deliverPrivilege = new OLEUserPrivilege();
            deliverPrivilege.setUserPrivilegeType(OLEConstants.DELIVERY);
            deliverPrivilege.setUserPrivilegeDescription(OLEConstants.DELIVERY_DESCRIPTION);
            if (olePatronDocument.isDeliveryPrivilege()){
                deliverPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.TRUE));
            }else{
            deliverPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.FALSE));
            }
                userPrivilegeList.add(deliverPrivilege);
            OLEUserPrivilege pagingPrivilege = new OLEUserPrivilege();
            pagingPrivilege.setUserPrivilegeType(OLEConstants.PAGING);
            pagingPrivilege.setUserPrivilegeDescription(OLEConstants.PAGING_DESCRIPTION);
            if (olePatronDocument.isPagingPrivilege()) {
                pagingPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.TRUE));
            }else{
            pagingPrivilege.setUserPrivilegeStatus(String.valueOf(Boolean.FALSE));
            }
            userPrivilegeList.add(pagingPrivilege);
            String borrowerTypeCode = olePatronDocument.getBorrowerTypeCode();
            Map<String, String> borrowerTypeMap = new HashMap<String, String>();
            borrowerTypeMap.put(OLEConstants.BORROWER_TYPE_CODE, borrowerTypeCode);
            List<OleBorrowerType> oleBorrowerTypeList = (List<OleBorrowerType>) businessObjectService.findMatching(OleBorrowerType.class, borrowerTypeMap);
            if (oleBorrowerTypeList.size() > 0) {
                OLEUserPrivilege oleProfilePrivilege = new OLEUserPrivilege();
                oleProfilePrivilege.setUserPrivilegeType(OLEConstants.PROFILE);
                oleProfilePrivilege.setUserPrivilegeStatus(oleBorrowerTypeList.get(0).getBorrowerTypeName());
                oleProfilePrivilege.setUserPrivilegeDescription(OLEConstants.PROFILE_DESCRIPTION);
                userPrivilegeList.add(oleProfilePrivilege);
            }
            if (agencyId != null) {
                String itemType, itemLocation = "";
                HashMap<String, String> agencyPropertyMap = getAgencyPropertyMap(agencyId);
                itemType = agencyPropertyMap.get(OLEConstants.ITEM_TYPE);
                itemLocation = agencyPropertyMap.get(OLEConstants.ITEM_LOCATION);
                OLEUserPrivilege oleUserPrivilege = new OLEUserPrivilege();
                oleUserPrivilege.setUserPrivilegeType(OLEConstants.STATUS);
                oleUserPrivilege.setUserPrivilegeDescription(OLEConstants.STATUS_DESCRIPTION);
                oleUserPrivilege.setUserPrivilegeStatus(OLEConstants.OK);
                if (olePatronDocument.isGeneralBlock() || isPatronExpired(olePatronDocument) || !olePatronDocument.isActiveIndicator() || isPatronActivated(olePatronDocument) || !isAbleToCheckOut(olePatronDocument.getOlePatronId(), borrowerTypeCode, itemType, itemLocation))
                    oleUserPrivilege.setUserPrivilegeStatus(OLEConstants.BLOCKED);
                userPrivilegeList.add(oleUserPrivilege);
            }
            return userPrivilegeList;
        }
        return null;
    }

    public boolean isPatronExpired(OlePatronDocument olePatronDocument) {

        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OleDeliverRequest.DATE_FORMAT);
        Date expirationDate = olePatronDocument.getExpirationDate();
        if (expirationDate != null) {
            if ((fmt.format(expirationDate)).compareTo(fmt.format(new Date(System.currentTimeMillis()))) > 0) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }
    }

    public boolean isPatronActivated(OlePatronDocument olePatronDocument) {

        SimpleDateFormat fmt = new SimpleDateFormat(OLEConstants.OleDeliverRequest.DATE_FORMAT);
        Date activationDate = olePatronDocument.getActivationDate();
        if (activationDate != null) {
            if ((fmt.format(activationDate)).compareTo(fmt.format(new Date(System.currentTimeMillis()))) <= 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isAbleToCheckOut(String patronId, String borrowerType, String itemType, String itemLocation) {
        LOG.info("Inside isAbleToCheckOut method . Patron Id : " +patronId + " Borrower Type : " + borrowerType + " ItemType : "+itemType + " Item Location : "  + itemLocation);
        boolean allowed = true;
        String agendaName = OLEConstants.CHECK_OUT_AGENDA_NM;
        HashMap<String, Object> termValues = new HashMap<String, Object>();
        List<FeeType> feeTypeList = oleCirculationPolicyService.getPatronBillPayment(patronId);
        Integer overdueFineAmt = 0;
        Integer replacementFeeAmt = 0;
        for (FeeType feeType : feeTypeList) {
            overdueFineAmt += feeType.getOleFeeType().getFeeTypeName().equalsIgnoreCase(OLEConstants.OVERDUE_FINE) ? feeType.getFeeAmount().intValue() : 0;
            replacementFeeAmt += feeType.getOleFeeType().getFeeTypeName().equalsIgnoreCase(OLEConstants.REPLACEMENT_FEE) ? feeType.getFeeAmount().intValue() : 0;
        }
        String[] locationArray = itemLocation.split("['/']");
        List<String> locationList = Arrays.asList(locationArray);
        for (String value : locationList) {
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(OLEConstants.LOCATION_CODE, value);
            List<OleLocation> oleLocations = (List<OleLocation>) businessObjectService.findMatching(OleLocation.class, requestMap);
            if (oleLocations != null && oleLocations.size() > 0) {
                String locationLevelId = oleLocations.get(0).getLevelId();
                requestMap.clear();
                requestMap.put(OLEConstants.LEVEL_ID, locationLevelId);
                List<OleLocationLevel> oleLocationLevels = (List<OleLocationLevel>) businessObjectService.findMatching(OleLocationLevel.class, requestMap);
                if (oleLocationLevels != null && oleLocationLevels.size() > 0) {
                    OleLocationLevel oleLocationLevel = new OleLocationLevel();
                    oleLocationLevel = oleLocationLevels.get(0);
                    if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_CAMPUS)) {
                        termValues.put(OLEConstants.ITEM_CAMPUS, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_INSTITUTION)) {
                        termValues.put(OLEConstants.ITEM_INSTITUTION, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_COLLECTION)) {
                        termValues.put(OLEConstants.ITEM_COLLECTION, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_LIBRARY)) {
                        termValues.put(OLEConstants.ITEM_LIBRARY, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_SHELVING)) {
                        termValues.put(OLEConstants.ITEM_SHELVING, value);
                    }
                }
            }
        }
        termValues.put(OLEConstants.BORROWER_TYPE, borrowerType);
        termValues.put(OLEConstants.ITEM_TYPE, itemType);
        //termValues.put(OLEConstants.ITEM_SHELVING, itemLocation);
        termValues.put(OLEConstants.OVERDUE_FINE_AMT, overdueFineAmt);
        termValues.put(OLEConstants.REPLACEMENT_FEE_AMT, replacementFeeAmt);
        termValues.put(OLEConstants.ALL_CHARGES, overdueFineAmt + replacementFeeAmt);
        /*termValues.put(OLEConstants.ITEM_STATUS, "AVAILABLE");
        termValues.put("isCirculationPolicyNotFound","false");*/
        DataCarrierService dataCarrierService = GlobalResourceLoader.getService(OLEConstants.DATA_CARRIER_SERVICE);
        String itemId = "";
        dataCarrierService.removeData(patronId+itemId);
        dataCarrierService.addData(OLEConstants.GROUP_ID, "100");
        HashMap keyLoanMap=new HashMap();
        keyLoanMap=oleCirculationPolicyService.getLoanedKeyMap(patronId,false);
        List<Integer> listOfOverDueDays =(List<Integer>)keyLoanMap.get(OLEConstants.LIST_OF_OVERDUE_DAYS);
        dataCarrierService.addData(OLEConstants.LIST_OVERDUE_DAYS, listOfOverDueDays);

        dataCarrierService.addData(OLEConstants.LIST_RECALLED_OVERDUE_DAYS, (List<Integer>) keyLoanMap.get(OLEConstants.LIST_RECALLED_OVERDUE_DAYS));
        termValues.put(OLEConstants.PATRON_ID_POLICY, patronId);
        termValues.put(OLEConstants.ITEM_ID_POLICY, "");
        try {
            EngineResults engineResults = loanProcessor.getEngineResults(agendaName, termValues);
            dataCarrierService.removeData(patronId+itemId);
            List<ResultEvent> allResults = engineResults.getAllResults();
            for (Iterator<ResultEvent> resultEventIterator = allResults.iterator(); resultEventIterator.hasNext(); ) {
                ResultEvent resultEvent = resultEventIterator.next();
                if (resultEvent.getSource() instanceof BasicRule) {
                    BasicRule basicRule = (BasicRule) resultEvent.getSource();
                    if (resultEvent.getType().equals(OLEConstants.RULE_EVALUATED) && ((basicRule.getName().equals(OLENCIPConstants.CHECK_REPLACEMENT_FEE_AMT) && resultEvent.getResult())
                            || (basicRule.getName().equals(OLENCIPConstants.CHECK_ALL_OVERDUE_FINE_AMT) && resultEvent.getResult()) || (basicRule.getName().equals(OLENCIPConstants.CHECK_OVERALL_CHARGES) && resultEvent.getResult()))) {
                        // renewalExceeds=true;  Check all Overdue fine amount
                        allowed = false;
                        break;
                    }
                }
            }
            List<String> errorMessage = (List<String>) engineResults.getAttribute(OLEConstants.ERROR_ACTION);
            if (errorMessage != null && errorMessage.size() > 0) {
                allowed = false;
            }
        } catch (Exception e) {
            LOG.info("Exception Occured while evaluating the KRMS rules");
            LOG.error(e, e);
        }

        return allowed;
    }

    public String checkOutItem(String patronId, String operatorId, String itemBarcode) {
        LOG.info("In  Check Out Item . Patron Barcode : "+patronId + " OperatorId : " +operatorId + "Item Barcode : "+itemBarcode);
        OlePatronDocument olePatronDocument = null;
        Map<String, String> patronMap = new HashMap<String, String>();
        patronMap.put(OLEConstants.BARCODE, patronId);
        List<OlePatronDocument> olePatronDocumentList = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
        if (olePatronDocumentList.size() > 0) {
            olePatronDocument = olePatronDocumentList.get(0);
        } else {
            OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
            oleCheckOutItem.setCode("002");
            oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_PATRON_INFO));
            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_PATRON_INFO));
            return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
        }
        OleLoanDocument oleLoanDocument;
        try {
            oleLoanDocument = loanProcessor.getLoanDocument(olePatronDocument.getBarcode(), null, true, false);
            oleLoanDocument.setLoanOperatorId(operatorId);
            Map<String, String> circulationDeskDetailMaps = new HashMap<String, String>();
            circulationDeskDetailMaps.put(OLENCIPConstants.OPERATOR_ID, operatorId);
            List<OleCirculationDeskDetail> oleCirculationDeskDetailLists = (List<OleCirculationDeskDetail>) businessObjectService.findMatching(OleCirculationDeskDetail.class, circulationDeskDetailMaps);
            if (oleCirculationDeskDetailLists != null && oleCirculationDeskDetailLists.size() > 0) {
                for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetailLists) {
                    if (oleCirculationDeskDetail.isDefaultLocation()) {
                        String circulationDeskId = oleCirculationDeskDetail.getCirculationDeskId();
                        oleLoanDocument.setCirculationLocationId(circulationDeskId);
                        Map<String, String> circulationMap = new HashMap<String, String>();
                        circulationMap.put(OLEConstants.CIRCULATION_DESK_ID, circulationDeskId);
                        List<OleCirculationDesk> oleCirculationDeskList = (List<OleCirculationDesk>) businessObjectService.findMatching(OleCirculationDesk.class, circulationMap);
                        if (oleCirculationDeskList.size() > 0)
                            oleLoanDocument.setOleCirculationDesk(oleCirculationDeskList.get(0));
                        break;
                    }
                }
            } else {
                OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                oleCheckOutItem.setCode("026");
                oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CIRCULATION_DESK_NOT_MAPPED_OPERATOR));
                LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CIRCULATION_DESK_NOT_MAPPED_OPERATOR));
                return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
            }
            if (oleLoanDocument.getErrorMessage() == null || (oleLoanDocument.getErrorMessage() != null && oleLoanDocument.getErrorMessage().isEmpty())) {
                if (olePatronDocument != null) {
                    oleLoanDocument.setLoanOperatorId(operatorId);
                    oleLoanDocument = loanProcessor.addLoan(olePatronDocument.getBarcode(), itemBarcode, oleLoanDocument, operatorId);
                    if (oleLoanDocument.getErrorMessage() == null || (oleLoanDocument.getErrorMessage() != null && oleLoanDocument.getErrorMessage().isEmpty())) {
                        OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                        oleCheckOutItem.setDueDate(oleLoanDocument.getLoanDueDate() != null ? oleLoanDocument.getLoanDueDate().toString() : "");
                        oleCheckOutItem.setRenewalCount(oleLoanDocument.getNumberOfRenewals());
                        oleCheckOutItem.setUserType(oleLoanDocument.getBorrowerTypeName());
                        oleCheckOutItem.setBarcode(oleLoanDocument.getItemId());
                        oleCheckOutItem.setPatronId(oleLoanDocument.getPatronId());
                        oleCheckOutItem.setPatronBarcode(patronId);
                        if (oleLoanDocument.getOleItem() != null && oleLoanDocument.getOleItem().getItemType() != null) {
                            oleCheckOutItem.setItemType(oleLoanDocument.getOleItem().getItemType().getCodeValue());
                        }
                        oleCheckOutItem.setCode("030");
                        oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.SUCCESSFULLEY_LOANED));
                        LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.SUCCESSFULLEY_LOANED));
                        return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);

                    } else {
                        if (oleLoanDocument.getOleItem() != null && oleLoanDocument.getOleItem().getLocation() == null) {
                            OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                            oleCheckOutItem.setCode("028");
                            oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVAL_LOC));
                            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVAL_LOC));
                            return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
                        } else if (oleLoanDocument.getOleItem() != null && oleLoanDocument.getOleItem().getItemStatus() != null &&
                                oleLoanDocument.getOleItem().getItemStatus().getCodeValue() != null && oleLoanDocument.getOleItem().getItemStatus().getCodeValue().equalsIgnoreCase("LOANED")) {

                            OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                            oleCheckOutItem.setCode("100");
                            oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_IN_LOAN));
                            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_IN_LOAN));
                            return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
                        } else {
                            OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                            oleCheckOutItem.setCode("500");
                            oleCheckOutItem.setMessage(oleLoanDocument.getErrorMessage());
                            LOG.info(oleLoanDocument.getErrorMessage());
                            return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
                        }
                    }
                } else {
                    OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                    oleCheckOutItem.setCode("500");
                    oleCheckOutItem.setMessage(oleLoanDocument.getErrorMessage());
                    LOG.info(oleLoanDocument.getErrorMessage());
                    return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
                }
            } else {
                OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
                oleCheckOutItem.setCode("500");
                oleCheckOutItem.setMessage(oleLoanDocument.getErrorMessage());
                LOG.info(oleLoanDocument.getErrorMessage());
                return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
            }
        } catch (Exception e) {
            OLECheckOutItem oleCheckOutItem = new OLECheckOutItem();
            oleCheckOutItem.setCode("014");
            oleCheckOutItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_BARCODE_DOESNOT_EXISTS));
            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_BARCODE_DOESNOT_EXISTS));
            LOG.error(e,e);
            return oleCheckOutItemConverter.generateCheckOutItemXml(oleCheckOutItem);
        }
    }

    public String checkInItem(String patronBarcode, String operatorId, String itemBarcode, String deleteIndicator) {
        LOG.info("Inside checkInItem method .Patron barcode : " + patronBarcode + " Operator Id : " +operatorId + " Item Barcode : " + itemBarcode );

        OleLoanDocument oleLoanDocument = new OleLoanDocument();
        OLECheckInItem oleCheckInItem = new OLECheckInItem();
        try {
            // oleLoanDocument= loanProcessor.returnLoan(oleLoanDocument);

            Map<String, String> loanMap = new HashMap<String, String>();
            loanMap.put(OLEConstants.OleDeliverRequest.ITEM_ID, itemBarcode);
            List<OleLoanDocument> oleLoanList = (List<OleLoanDocument>) businessObjectService.findMatching(OleLoanDocument.class, loanMap);
            if (oleLoanList.size() > 0) {
                oleLoanDocument = oleLoanList.get(0);
                String olePatronId = oleLoanList.get(0).getPatronId();
                Map<String, String> patronMap = new HashMap<String, String>();
                patronMap.put(OLEConstants.OleDeliverRequest.PATRON_ID, olePatronId);
                List<OlePatronDocument> olePatronDocumentList = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
                if (olePatronDocumentList.size() > 0) {
                    oleLoanDocument.setOlePatron(olePatronDocumentList.get(0));
                    oleLoanDocument.setPatronBarcode(olePatronDocumentList.get(0).getBarcode());
                    if(olePatronDocumentList.get(0).getOleBorrowerType()!=null){
                    oleLoanDocument.setBorrowerTypeCode(olePatronDocumentList.get(0).getOleBorrowerType().getBorrowerTypeCode());
                    oleLoanDocument.setBorrowerTypeName(olePatronDocumentList.get(0).getOleBorrowerType().getBorrowerTypeName());
                    }
                }
            }
            Map<String, String> circulationDeskDetailMap = new HashMap<String, String>();
            circulationDeskDetailMap.put(OLENCIPConstants.OPERATOR_ID, operatorId);
            List<OleCirculationDeskDetail> oleCirculationDeskDetailList = (List<OleCirculationDeskDetail>) businessObjectService.findMatching(OleCirculationDeskDetail.class, circulationDeskDetailMap);
            if (oleCirculationDeskDetailList != null && oleCirculationDeskDetailList.size() > 0) {
                for (OleCirculationDeskDetail oleCirculationDeskDetail : oleCirculationDeskDetailList) {
                    if (oleCirculationDeskDetail.isDefaultLocation()) {
                        String circulationDeskId = oleCirculationDeskDetail.getCirculationDeskId();
                        oleLoanDocument.setCirculationLocationId(circulationDeskId);
                        Map<String, String> circulationMap = new HashMap<String, String>();
                        circulationMap.put(OLEConstants.CIRCULATION_DESK_ID, circulationDeskId);
                        List<OleCirculationDesk> oleCirculationDeskList = (List<OleCirculationDesk>) businessObjectService.findMatching(OleCirculationDesk.class, circulationMap);
                        if (oleCirculationDeskList.size() > 0)
                            oleLoanDocument.setOleCirculationDesk(oleCirculationDeskList.get(0));
                        break;
                    }
                }
                oleLoanDocument = loanProcessor.returnLoan(itemBarcode, oleLoanDocument);
                oleCheckInItem.setAuthor(oleLoanDocument.getAuthor());
                oleCheckInItem.setTitle(oleLoanDocument.getTitle());
                oleCheckInItem.setCallNumber(oleLoanDocument.getItemCallNumber());
                oleCheckInItem.setBarcode(oleLoanDocument.getPatronBarcode());
                oleCheckInItem.setUserId(oleLoanDocument.getPatronBarcode());
                oleCheckInItem.setUserType(oleLoanDocument.getBorrowerTypeName());
                if (oleLoanDocument.getOleItem() != null && oleLoanDocument.getOleItem().getItemType() != null) {
                    oleCheckInItem.setItemType(oleLoanDocument.getOleItem().getItemType().getCodeValue());
                }
                if (oleLoanDocument.getErrorMessage() == null || (oleLoanDocument.getErrorMessage() != null && oleLoanDocument.getErrorMessage().isEmpty())) {
                    oleCheckInItem.setCode("024");
                    oleCheckInItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.SUCCESSFULLEY_CHECKED_IN));
                    LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.SUCCESSFULLEY_CHECKED_IN));
                    if (deleteIndicator!=null && deleteIndicator.equalsIgnoreCase("y")) {

                        org.kuali.ole.docstore.common.document.Item item = getDocstoreClientLocator().getDocstoreClient().retrieveItem(oleLoanDocument.getItemUuid());
                        String bibId = item.getHolding().getBib().getId();

                        getDocstoreClientLocator().getDocstoreClient().deleteBib(bibId);
                    }
                    return oleCheckInItemConverter.generateCheckInItemXml(oleCheckInItem);
                } else {
                    oleCheckInItem.setCode("500");
                    oleCheckInItem.setMessage(oleLoanDocument.getErrorMessage());
                    LOG.info(oleLoanDocument.getErrorMessage());
                    return oleCheckInItemConverter.generateCheckInItemXml(oleCheckInItem);
                }
            } else {
                oleCheckInItem.setCode("025");
                oleCheckInItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CHECK_IN_FAILED));
                LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CHECK_IN_FAILED));
                return oleCheckInItemConverter.generateCheckInItemXml(oleCheckInItem);
            }
        } catch (Exception e) {
            if(e.getMessage()!=null && e.getMessage().equals("")){
                oleCheckInItem.setCode("014");
                oleCheckInItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_BARCODE_DOESNOT_EXISTS));
                LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITEM_BARCODE_DOESNOT_EXISTS));
                LOG.error(e,e);
                return oleCheckInItemConverter.generateCheckInItemXml(oleCheckInItem);
            }

            oleCheckInItem.setCode("025");
            oleCheckInItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CHECK_IN_FAILED));
            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.CHECK_IN_FAILED));
            return oleCheckInItemConverter.generateCheckInItemXml(oleCheckInItem);
        }
    }

    public String acceptItem(String patronBarcode, String operatorId, String itemBarcode, String callNumber, String title, String author, String itemType, String itemLocation, String dateExpires, String requestType, String pickUpLocation) throws Exception {
        LOG.info("Inside Accept Item . Patron Barcode " + patronBarcode + "Operator Id : " +operatorId + "Item barcode :" + itemBarcode + " Call Number : "+callNumber + "Title : "+title + " Author : " +author + "Item Type : "+ itemType + "Item Location : "+itemLocation + "Request Type :" + requestType + "Pick up Location : " + pickUpLocation );
        OLEAcceptItem oleAcceptItem = new OLEAcceptItem();
        String itemIdentifier = null;
        if (docstoreUtil.isItemAvailableInDocStore(itemBarcode)) {
            return itemIdentifier;
        }
        BibMarcRecord bibMarcRecord = loanProcessor.getBibMarcRecord(title, author);

        List<BibMarcRecord> bibMarcRecordList = new ArrayList<>();
        bibMarcRecordList.add(bibMarcRecord);

        BibMarcRecords bibMarcRecords = new BibMarcRecords();
        bibMarcRecords.setRecords(bibMarcRecordList);
        BibMarcRecordProcessor bibMarcRecordProcessor = new BibMarcRecordProcessor();


        Bib bib = new BibMarc();
        bib.setStaffOnly(true);
        bib.setCategory(org.kuali.ole.docstore.common.document.content.enums.DocCategory.WORK.getCode());
        bib.setType(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode());
        bib.setFormat(org.kuali.ole.docstore.common.document.content.enums.DocFormat.MARC.getCode());
        bib.setContent(bibMarcRecordProcessor.toXml(bibMarcRecords));
        bib.setOperation(DocstoreDocument.OperationType.CREATE);


        OleHoldings oleHoldings = new OleHoldings();
        LocationLevel locationLevel = new LocationLevel();
        locationLevel = loanProcessor.createLocationLevel(itemLocation, locationLevel);
        Location holdingsLocation = new Location();
        holdingsLocation.setPrimary(OLEConstants.TRUE);
        holdingsLocation.setStatus(OLEConstants.PERMANENT);
        holdingsLocation.setLocationLevel(locationLevel);
        oleHoldings.setLocation(holdingsLocation);
        oleHoldings.setStaffOnlyFlag(true);
        Item item = new Item();

        AccessInformation accessInformation = new AccessInformation();
        accessInformation.setBarcode(itemBarcode);
        item.setAccessInformation(accessInformation);
        item.setStaffOnlyFlag(true);
        ItemStatus itemStatus = new ItemStatus();
        itemStatus.setCodeValue(OLEConstants.AVAILABLE);
        item.setItemStatus(itemStatus);
        ItemType type = new ItemType();
        type.setCodeValue(itemType);
        item.setItemType(type);
        CallNumber itemCallNumber = new CallNumber();
        itemCallNumber.setNumber(callNumber);
        item.setCallNumber(itemCallNumber);
        ShelvingScheme shelvingScheme = new ShelvingScheme();
        shelvingScheme.setCodeValue(OLEConstants.LCC);
        itemCallNumber.setShelvingScheme(shelvingScheme);
        //item.setExtension(extension);
        item.setLocation(holdingsLocation);
        ItemOlemlRecordProcessor itemOlemlRecordProcessor = new ItemOlemlRecordProcessor();
        org.kuali.ole.docstore.common.document.Item documentItem = new ItemOleml();
        documentItem.setContent(itemOlemlRecordProcessor.toXML(item));
        documentItem.setStaffOnly(true);
        documentItem.setOperation(DocstoreDocument.OperationType.CREATE);
        Holdings holdings = new PHoldings();
        holdings.setStaffOnly(true);
        HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
        holdings.setContent(holdingOlemlRecordProcessor.toXML(oleHoldings));
        holdings.setOperation(DocstoreDocument.OperationType.CREATE);
        HoldingsTree holdingsTree = new HoldingsTree();
        holdingsTree.setHoldings(holdings);
        holdingsTree.getItems().add(documentItem);
        BibTree bibTree = new BibTree();
        bibTree.setBib(bib);
        bibTree.getHoldingsTrees().add(holdingsTree);
        BibTrees bibTrees = new BibTrees();
        bibTrees.getBibTrees().add(bibTree);
        bibTrees=getDocstoreClientLocator().getDocstoreClient().processBibTrees(bibTrees);
        Thread.sleep(200);
        if(bibTrees!=null &&  bibTrees.getBibTrees()!=null && bibTrees.getBibTrees().size()>0  &&bibTrees.getBibTrees().get(0).getHoldingsTrees()!=null  && bibTrees.getBibTrees().get(0).getHoldingsTrees().size()>0
                && bibTrees.getBibTrees().get(0).getHoldingsTrees().get(0).getItems() != null && bibTrees.getBibTrees().get(0).getHoldingsTrees().get(0).getItems().size()>0 ){
            itemIdentifier= bibTrees.getBibTrees().get(0).getHoldingsTrees().get(0).getItems().get(0).getId();
        }else{
            itemIdentifier="";
        }
        LOG.info("Item Created with identifier : " + itemIdentifier);
        return itemIdentifier;
    }

    public HashMap<String, String> getAgencyPropertyMap(String agencyId) {
        HashMap<String, String> agencyPropertyMap = new HashMap<String, String>();
        agencyPropertyMap = getOleSIAPIHelperService().getAgencyPropertyMap(OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT, OLENCIPConstants.NCIPAPI_PARAMETER_NAME, agencyId, agencyPropertyMap);
        return agencyPropertyMap;
    }

    public String renewItem(String patronBarcode, String operatorId, String itemBarcode) {
        LOG.info("Inside Renew Item . Patron Barcode :  " + patronBarcode + "Operator Id : "+ operatorId + " Item Barcode : " +itemBarcode);
        OLERenewItem oleRenewItem = new OLERenewItem();
        OLERenewItemConverter oleRenewItemConverter = new OLERenewItemConverter();
        OlePatronDocument olePatronDocument = null;
        Map<String, String> patronMap = new HashMap<String, String>();
        patronMap.put(OLEConstants.BARCODE, patronBarcode);
        List<OlePatronDocument> patronDocuments = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
        if (patronDocuments.size() > 0) {
            olePatronDocument = patronDocuments.get(0);
        } else {
            oleRenewItem.setCode("002");
            oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_PATRON_INFO));
            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_PATRON_INFO));
            return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
        }
        if (!loanProcessor.hasCirculationDesk(operatorId)) {
            oleRenewItem.setCode("001");
            oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVALID_OPRTR_ID));
            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.INVALID_OPRTR_ID));
            return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
        }
        Map<String, String> loanMap = new HashMap<String, String>();
        loanMap.put(OLEConstants.PATRON_ID, olePatronDocument.getOlePatronId());
        loanMap.put(OLEConstants.OleDeliverRequest.ITEM_ID, itemBarcode);
        List<OleLoanDocument> loanDocuments = (List<OleLoanDocument>) businessObjectService.findMatching(OleLoanDocument.class, loanMap);
        if (loanDocuments.size() > 0) {
            OleLoanDocument oleLoanDocument = loanDocuments.get(0);

            if (patronDocuments.size() > 0) {
                oleLoanDocument.setOlePatron(olePatronDocument);
                oleLoanDocument.setBorrowerTypeCode(olePatronDocument.getBorrowerTypeCode());
                oleLoanDocument.setBorrowerTypeId(olePatronDocument.getBorrowerType());
                oleLoanDocument.setOleBorrowerType(olePatronDocument.getOleBorrowerType());
                oleLoanDocument.setBorrowerTypeName(olePatronDocument.getBorrowerTypeName());
            }
            oleLoanDocument.setRenewalItemFlag(true);
            oleLoanDocument.setErrorMessage(null);
            if (loanProcessor.canOverrideLoan(operatorId)) {
                if (!loanProcessor.checkPendingRequestforItem(oleLoanDocument.getItemUuid())) {
                    Timestamp currentDate = new Timestamp(System.currentTimeMillis());
                    try {
                        oleLoanDocument = loanProcessor.addLoan(oleLoanDocument.getPatronBarcode(), oleLoanDocument.getItemId(), oleLoanDocument, operatorId);
                        if (oleLoanDocument.getErrorMessage() == null || (oleLoanDocument.getErrorMessage() != null && oleLoanDocument.getErrorMessage().trim().isEmpty())) {
                            oleRenewItem.setCode("003");
                            oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.RENEW_SUCCESS));
                            LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.RENEW_SUCCESS));
                            oleRenewItem.setPastDueDate(oleLoanDocument.getPastDueDate().toString());
                            oleRenewItem.setNewDueDate(oleLoanDocument.getLoanDueDate() != null ? oleLoanDocument.getLoanDueDate().toString() : "");
                            oleRenewItem.setRenewalCount(oleLoanDocument.getNumberOfRenewals());
                            return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
                        } else {
                            oleRenewItem.setCode("500");
                            oleRenewItem.setMessage(oleLoanDocument.getErrorMessage());
                            LOG.info(oleLoanDocument.getErrorMessage());
                            return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);

                        }
                    } catch (Exception e) {
                        LOG.error(e,e);
                        return "Exception occured while renewing an item";
                    }

                } else {
                    oleRenewItem.setCode("009");
                    oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.RQST_PNDNG));
                    LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.RQST_PNDNG));
                    return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
                }
            } else {
                oleRenewItem.setCode("010");
                oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_RENEW));
                LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.NO_RENEW));
                return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
            }
        } else {
            oleRenewItem.setCode("011");
            oleRenewItem.setMessage(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITM_NT_LOAN));
           LOG.info(ConfigContext.getCurrentContextConfig().getProperty(OLEConstants.ITM_NT_LOAN));
            return oleRenewItemConverter.generateRenewItemXml(oleRenewItem);
        }
    }


    public boolean validPatron(String patronId) {
        boolean valid = false;
        Map<String, String> patronMap = new HashMap<String, String>();
        patronMap.put(OLEConstants.BARCODE, patronId);
        List<OlePatronDocument> olePatronDocumentList = (List<OlePatronDocument>) businessObjectService.findMatching(OlePatronDocument.class, patronMap);
        if (olePatronDocumentList.size() > 0) {
            valid = true;
        }
        return valid;
    }


    private Map<String, String> getLocationMap(String itemLocation) {
        Map<String, String> locationMap = new HashMap<String, String>();
        String[] locationArray = itemLocation.split("['/']");
        List<String> locationList = Arrays.asList(locationArray);
        for (String value : locationList) {
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put(OLEConstants.LOCATION_CODE, value);
            List<OleLocation> oleLocations = (List<OleLocation>) businessObjectService.findMatching(OleLocation.class, requestMap);
            if (oleLocations != null && oleLocations.size() > 0) {
                String locationLevelId = oleLocations.get(0).getLevelId();
                requestMap.clear();
                requestMap.put(OLEConstants.LEVEL_ID, locationLevelId);
                List<OleLocationLevel> oleLocationLevels = (List<OleLocationLevel>) businessObjectService.findMatching(OleLocationLevel.class, requestMap);
                if (oleLocationLevels != null && oleLocationLevels.size() > 0) {
                    OleLocationLevel oleLocationLevel = new OleLocationLevel();
                    oleLocationLevel = oleLocationLevels.get(0);
                    if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_CAMPUS)) {
                        locationMap.put(OLEConstants.ITEM_CAMPUS, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_INSTITUTION)) {
                        locationMap.put(OLEConstants.ITEM_INSTITUTION, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_COLLECTION)) {
                        locationMap.put(OLEConstants.ITEM_COLLECTION, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_LIBRARY)) {
                        locationMap.put(OLEConstants.ITEM_LIBRARY, value);
                    } else if (oleLocationLevel.getLevelCode().equals(OLEConstants.OLEBatchProcess.LOCATION_LEVEL_SHELVING)) {
                        locationMap.put(OLEConstants.ITEM_SHELVING, value);
                    }
                }
            }
        }
        return locationMap;
    }

    /**
     * This method is used to convert the date in String format to gregorian calendar format
     * @param date
     * @return
     */
    public GregorianCalendar getGregorianCalendarDate(String date) {
        if (date != null) {
             if(date.equals("")){
                 return new GregorianCalendar(2025,1,1);
             }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = null;
            try {
                parsedDate = simpleDateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            return new GregorianCalendar(year, month, day);
        }
        return null;
    }
}




