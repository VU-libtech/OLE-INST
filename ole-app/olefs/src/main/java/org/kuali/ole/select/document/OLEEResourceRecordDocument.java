package org.kuali.ole.select.document;

import org.joda.time.DateTime;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.describe.bo.OleStatisticalSearchingCodes;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.EHoldings;
import org.kuali.ole.docstore.common.document.Holdings;
import org.kuali.ole.docstore.common.document.content.instance.Link;
import org.kuali.ole.docstore.common.document.content.instance.OleHoldings;
import org.kuali.ole.docstore.common.document.content.instance.xstream.HoldingOlemlRecordProcessor;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderType;
import org.kuali.ole.select.bo.*;
import org.kuali.ole.select.businessobject.OleCopy;
import org.kuali.ole.select.form.OLEEResourceRecordForm;
import org.kuali.ole.service.OLEEResourceSearchService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.krad.document.TransactionalDocumentBase;
import org.kuali.rice.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: srinivasane
 * Date: 6/21/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEEResourceRecordDocument extends TransactionalDocumentBase {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OLEEResourceRecordDocument.class);
    private BusinessObjectService boService = KRADServiceLocator.getBusinessObjectService();
    private OLEEResourceSearchService oleEResourceSearchService = null;

    private String oleERSIdentifier;
    private String title;
    private String description;
    private String publisher;
    private String gokbIdentifier;
    private String isbn;
    private String ISBN;
    private String oclc;
    private String platformProvider;
    private String statusId;
    private String statusName;
    private String statusDate;
    private String fundCode;
    private Integer vendorDetailAssignedIdentifier;
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorName;
    private String vendorId;
    private String estimatedPrice;
    private BigDecimal orderTypeId;
    private String paymentTypeId;
    private String packageTypeId;
    private String packageScopeId;
    private boolean breakable;
    private boolean fixedTitleList;
    private String noteId;
    private String publicDisplayNote;
    private String reqSelComment;
    private String reqPriority;
    private String techRequirements;
    private String accessTypeId;
    private String numOfSimultaneousUsers;
    private String authenticationTypeId;
    private String accessLocationId;
    private List<String> accessLocation = new ArrayList<>();
    private boolean trialNeeded;
    private String trialStatus;
    private boolean licenseNeeded;
    private String licenseReqStatus;
    private String orderPayStatus;
    private String activationStatus;
    private String documentNumber;
    private boolean selectFlag;
    private Integer statisticalSearchingCodeId;
    private String statisticalSearchingCode;
    private String selectInstance;
    private String defaultCoverage;
    private String defaultCoverageView;
    private String defaultPerpetualAccess;
    private String defaultPerpetualAccessView;
    private OLEEResourceInstance oleERSInstance;
    private boolean eInstanceFlag;
    private String dummyDefaultCoverage;
    private String dummyDefaultPerpetualAccess;
    private boolean isCovEdited = false;
    private boolean isPerAccEdited = false;
    private String covStartDate;
    private String covEndDate;
    private String perAccStartDate;
    private String perAccEndDate;

    private OLEAccessType oleAccessType;
    private OLEPackageScope olePackageScope;
    private OLEPackageType olePackageType;
    private OLEPaymentType olePaymentType;
    private OLERequestPriority oleRequestPriority;
    private OLEAuthenticationType oleAuthenticationType;
    private OleStatisticalSearchingCodes oleStatisticalCode;
    private OLEEResourceStatus oleeResourceStatus;
    private VendorDetail vendorDetail;
    private PurchaseOrderType orderType;

    private List<OLEMaterialTypeList> oleMaterialTypes = new ArrayList<OLEMaterialTypeList>();
    private List<OLEContentTypes> oleContentTypes = new ArrayList<OLEContentTypes>();
    private List<OLEFormatTypeList> oleFormatTypes = new ArrayList<OLEFormatTypeList>();
    private List<OLEEResourceNotes> eresNotes = new ArrayList<OLEEResourceNotes>();
    private List<OLEEResourceRequestor> requestors = new ArrayList<OLEEResourceRequestor>();
    private List<OLEEResourceSelector> selectors = new ArrayList<OLEEResourceSelector>();
    private List<OLEEResourceReqSelComments> reqSelComments = new ArrayList<OLEEResourceReqSelComments>();
    private List<OLEEResourceEventLog> oleERSEventLogs = new ArrayList<OLEEResourceEventLog>();
    private List<OLEEResourceLicense> oleERSLicenseRequests = new ArrayList<OLEEResourceLicense>();
    private List<OLEEResourceInstance> oleERSInstances = new ArrayList<OLEEResourceInstance>();
    private List<OLEEResourceInvoices> oleERSInvoices = new ArrayList<>();
    private List<OLEEResourceInvoices> eRSInvoices = new ArrayList<>();
    private List<OleCopy> copyList = new ArrayList<>();
    private List<OLEEResourcePO> oleERSPOItems = new ArrayList<>();
    private List<OLEEResourcePO> eRSPOItems = new ArrayList<>();
    private OLEEResourceRecordForm form;
    private String status=null;
    private DocstoreClientLocator docstoreClientLocator;

    public DocstoreClientLocator getDocstoreClientLocator() {

        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }
    public OLEEResourceRecordDocument() {
        getOleMaterialTypes().add(new OLEMaterialTypeList());
        getOleFormatTypes().add(new OLEFormatTypeList());
        getOleContentTypes().add(new OLEContentTypes());
        getRequestors().add(new OLEEResourceRequestor());
        getSelectors().add(new OLEEResourceSelector());
        getReqSelComments().add(new OLEEResourceReqSelComments());
        getEresNotes().add(new OLEEResourceNotes());
    }

    public String getStatusName() {
        if (getStatusId() != null) {
            oleeResourceStatus = boService.findBySinglePrimaryKey(OLEEResourceStatus.class, statusId);
            statusName = oleeResourceStatus.getOleEResourceStatusName();
        }
        return statusName;
    }

    public OLEEResourceSearchService getOleEResourceSearchService() {
        if (oleEResourceSearchService == null) {
            oleEResourceSearchService = GlobalResourceLoader.getService(OLEConstants.OLEEResourceRecord.ERESOURSE_SEARCH_SERVICE);
        }
        return oleEResourceSearchService;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public OLEEResourceRecordForm getForm() {
        return form;
    }

    public void setForm(OLEEResourceRecordForm form) {
        this.form = form;
    }

    public String getOleERSIdentifier() {
        return oleERSIdentifier;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getOclc() {
        return oclc;
    }

    public void setOclc(String oclc) {
        this.oclc = oclc;
    }

    public void setOleERSIdentifier(String oleERSIdentifier) {
        this.oleERSIdentifier = oleERSIdentifier;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getStatisticalSearchingCode() {
        return statisticalSearchingCode;
    }

    public void setStatisticalSearchingCode(String statisticalSearchingCode) {
        this.statisticalSearchingCode = statisticalSearchingCode;
    }

    public Integer getStatisticalSearchingCodeId() {
        return statisticalSearchingCodeId;
    }

    public void setStatisticalSearchingCodeId(Integer statisticalSearchingCodeId) {
        this.statisticalSearchingCodeId = statisticalSearchingCodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getGokbIdentifier() {
        return gokbIdentifier;
    }

    public void setGokbIdentifier(String gokbIdentifier) {
        this.gokbIdentifier = gokbIdentifier;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPlatformProvider() {
        return platformProvider;
    }

    public void setPlatformProvider(String platformProvider) {
        this.platformProvider = platformProvider;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getStatusDate() {
        if (this.statusDate != null) {
            return statusDate.substring(0, 10);
        } else
            return new Date(System.currentTimeMillis()).toString().substring(0, 10);
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(String estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public BigDecimal getOrderTypeId() {
        return orderTypeId;
    }

    public void setOrderTypeId(BigDecimal orderTypeId) {
        this.orderTypeId = orderTypeId;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getPackageTypeId() {
        return packageTypeId;
    }

    public void setPackageTypeId(String packageTypeId) {
        this.packageTypeId = packageTypeId;
    }

    public String getPackageScopeId() {
        return packageScopeId;
    }

    public void setPackageScopeId(String packageScopeId) {
        this.packageScopeId = packageScopeId;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

    public boolean isFixedTitleList() {
        return fixedTitleList;
    }

    public void setFixedTitleList(boolean fixedTitleList) {
        this.fixedTitleList = fixedTitleList;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getPublicDisplayNote() {
        return publicDisplayNote;
    }

    public void setPublicDisplayNote(String publicDisplayNote) {
        this.publicDisplayNote = publicDisplayNote;
    }

    public String getReqSelComment() {
        return reqSelComment;
    }

    public void setReqSelComment(String reqSelComment) {
        this.reqSelComment = reqSelComment;
    }

    public String getReqPriority() {
        return reqPriority;
    }

    public void setReqPriority(String reqPriority) {
        this.reqPriority = reqPriority;
    }

    public String getTechRequirements() {
        return techRequirements;
    }

    public void setTechRequirements(String techRequirements) {
        this.techRequirements = techRequirements;
    }

    public String getAccessTypeId() {
        return accessTypeId;
    }

    public void setAccessTypeId(String accessTypeId) {
        this.accessTypeId = accessTypeId;
    }

    public String getNumOfSimultaneousUsers() {
        return numOfSimultaneousUsers;
    }

    public void setNumOfSimultaneousUsers(String numOfSimultaneousUsers) {
        this.numOfSimultaneousUsers = numOfSimultaneousUsers;
    }

    public String getAuthenticationTypeId() {
        return authenticationTypeId;
    }

    public void setAuthenticationTypeId(String authenticationTypeId) {
        this.authenticationTypeId = authenticationTypeId;
    }

    public String getAccessLocationId() {
        return accessLocationId;
    }

    public void setAccessLocationId(String accessLocationId) {
        this.accessLocationId = accessLocationId;
    }

    public List<String> getAccessLocation() {
        return accessLocation;
    }

    public void setAccessLocation(List<String> accessLocation) {
        this.accessLocation = accessLocation;
    }

    public boolean isTrialNeeded() {
        return trialNeeded;
    }

    public void setTrialNeeded(boolean trialNeeded) {
        this.trialNeeded = trialNeeded;
    }

    public String getTrialStatus() {
        return trialStatus;
    }

    public void setTrialStatus(String trialStatus) {
        this.trialStatus = trialStatus;
    }

    public boolean isLicenseNeeded() {
        return licenseNeeded;
    }

    public void setLicenseNeeded(boolean licenseNeeded) {
        this.licenseNeeded = licenseNeeded;
    }

    public String getLicenseReqStatus() {
        return licenseReqStatus;
    }

    public void setLicenseReqStatus(String licenseReqStatus) {
        this.licenseReqStatus = licenseReqStatus;
    }

    public String getOrderPayStatus() {
        return orderPayStatus;
    }

    public void setOrderPayStatus(String orderPayStatus) {
        this.orderPayStatus = orderPayStatus;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public OLEAccessType getOleAccessType() {
        return oleAccessType;
    }

    public void setOleAccessType(OLEAccessType oleAccessType) {
        this.oleAccessType = oleAccessType;
    }

    public OLEPackageScope getOlePackageScope() {
        return olePackageScope;
    }

    public void setOlePackageScope(OLEPackageScope olePackageScope) {
        this.olePackageScope = olePackageScope;
    }

    public OLEPackageType getOlePackageType() {
        return olePackageType;
    }

    public void setOlePackageType(OLEPackageType olePackageType) {
        this.olePackageType = olePackageType;
    }

    public OLEPaymentType getOlePaymentType() {
        return olePaymentType;
    }

    public void setOlePaymentType(OLEPaymentType olePaymentType) {
        this.olePaymentType = olePaymentType;
    }

    public OLERequestPriority getOleRequestPriority() {
        return oleRequestPriority;
    }

    public void setOleRequestPriority(OLERequestPriority oleRequestPriority) {
        this.oleRequestPriority = oleRequestPriority;
    }

    public OLEAuthenticationType getOleAuthenticationType() {
        return oleAuthenticationType;
    }

    public void setOleAuthenticationType(OLEAuthenticationType oleAuthenticationType) {
        this.oleAuthenticationType = oleAuthenticationType;
    }

    public OLEEResourceStatus getOleeResourceStatus() {
        return oleeResourceStatus;
    }

    public void setOleeResourceStatus(OLEEResourceStatus oleeResourceStatus) {
        this.oleeResourceStatus = oleeResourceStatus;
    }

    public List<OLEFormatTypeList> getOleFormatTypes() {
        return oleFormatTypes;
    }

    public void setOleFormatTypes(List<OLEFormatTypeList> oleFormatTypes) {
        this.oleFormatTypes = oleFormatTypes;
    }

    public List<OLEMaterialTypeList> getOleMaterialTypes() {
        return oleMaterialTypes;
    }

    public void setOleMaterialTypes(List<OLEMaterialTypeList> oleMaterialTypes) {
        this.oleMaterialTypes = oleMaterialTypes;
    }

    public List<OLEContentTypes> getOleContentTypes() {
        return oleContentTypes;
    }

    public void setOleContentTypes(List<OLEContentTypes> oleContentTypes) {
        this.oleContentTypes = oleContentTypes;
    }

    public List<OLEEResourceNotes> getEresNotes() {
        return eresNotes;
    }

    public void setEresNotes(List<OLEEResourceNotes> eresNotes) {
        this.eresNotes = eresNotes;
    }

    public List<OLEEResourceRequestor> getRequestors() {
        return requestors;
    }

    public void setRequestors(List<OLEEResourceRequestor> requestors) {
        this.requestors = requestors;
    }

    public List<OLEEResourceSelector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<OLEEResourceSelector> selectors) {
        this.selectors = selectors;
    }

    public List<OLEEResourceReqSelComments> getReqSelComments() {
        return reqSelComments;
    }

    public void setReqSelComments(List<OLEEResourceReqSelComments> reqSelComments) {
        this.reqSelComments = reqSelComments;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public List<OLEEResourceEventLog> getOleERSEventLogs() {
        return oleERSEventLogs;
    }

    public void setOleERSEventLogs(List<OLEEResourceEventLog> oleERSEventLogs) {
        this.oleERSEventLogs = oleERSEventLogs;
    }

    public List<OLEEResourceLicense> getOleERSLicenseRequests() {
        return oleERSLicenseRequests;
    }

    public void setOleERSLicenseRequests(List<OLEEResourceLicense> oleERSLicenseRequests) {
        this.oleERSLicenseRequests = oleERSLicenseRequests;
    }

    public boolean isSelectFlag() {
        return selectFlag;
    }

    public void setSelectFlag(boolean selectFlag) {
        this.selectFlag = selectFlag;
    }

    public String getSelectInstance() {
        return selectInstance;
    }

    public void setSelectInstance(String selectInstance) {
        this.selectInstance = selectInstance;
    }

    public String getDefaultCoverage() {
        return defaultCoverage;
    }

    public void setDefaultCoverage(String defaultCoverage) {
        this.defaultCoverage = defaultCoverage;
    }

    public String getDefaultPerpetualAccess() {
        return defaultPerpetualAccess;
    }

    public void setDefaultPerpetualAccess(String defaultPerpetualAccess) {
        this.defaultPerpetualAccess = defaultPerpetualAccess;
    }

    public String getDefaultCoverageView() {
        return defaultCoverageView;
    }

    public void setDefaultCoverageView(String defaultCoverageView) {
        this.defaultCoverageView = defaultCoverageView;
    }

    public String getDefaultPerpetualAccessView() {
        return defaultPerpetualAccessView;
    }

    public void setDefaultPerpetualAccessView(String defaultPerpetualAccessView) {
        this.defaultPerpetualAccessView = defaultPerpetualAccessView;
    }

    public List<OLEEResourceInstance> getOleERSInstances() {
        return oleERSInstances;
    }

    public void setOleERSInstances(List<OLEEResourceInstance> oleERSInstances) {
        this.oleERSInstances = oleERSInstances;
    }

    public List<OleCopy> getCopyList() {
        return copyList;
    }

    public void setCopyList(List<OleCopy> copyList) {
        this.copyList = copyList;
    }

    public List<OLEEResourcePO> getOleERSPOItems() {
        return oleERSPOItems;
    }

    public void setOleERSPOItems(List<OLEEResourcePO> oleERSPOItems) {
        this.oleERSPOItems = oleERSPOItems;
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }

    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }

    public PurchaseOrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(PurchaseOrderType orderType) {
        this.orderType = orderType;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public OleStatisticalSearchingCodes getOleStatisticalCode() {
        return oleStatisticalCode;
    }

    public void setOleStatisticalCode(OleStatisticalSearchingCodes oleStatisticalCode) {
        this.oleStatisticalCode = oleStatisticalCode;
    }

    public OLEEResourceInstance getOleERSInstance() {
        return oleERSInstance;
    }

    public void setOleERSInstance(OLEEResourceInstance oleERSInstance) {
        this.oleERSInstance = oleERSInstance;
    }

    public List<OLEEResourceInvoices> getOleERSInvoices() {
        return oleERSInvoices;
    }

    public void setOleERSInvoices(List<OLEEResourceInvoices> oleERSInvoices) {
        this.oleERSInvoices = oleERSInvoices;
    }

    public boolean iseInstanceFlag() {
        return eInstanceFlag;
    }

    public void seteInstanceFlag(boolean eInstanceFlag) {
        this.eInstanceFlag = eInstanceFlag;
    }

    public String getDummyDefaultCoverage() {
        return dummyDefaultCoverage;
    }

    public void setDummyDefaultCoverage(String dummyDefaultCoverage) {
        this.dummyDefaultCoverage = dummyDefaultCoverage;
    }

    public String getDummyDefaultPerpetualAccess() {
        return dummyDefaultPerpetualAccess;
    }

    public void setDummyDefaultPerpetualAccess(String dummyDefaultPerpetualAccess) {
        this.dummyDefaultPerpetualAccess = dummyDefaultPerpetualAccess;
    }

    public boolean isCovEdited() {
        return isCovEdited;
    }

    public void setCovEdited(boolean covEdited) {
        isCovEdited = covEdited;
    }

    public boolean isPerAccEdited() {
        return isPerAccEdited;
    }

    public void setPerAccEdited(boolean perAccEdited) {
        isPerAccEdited = perAccEdited;
    }

    public String getCovStartDate() {
        return covStartDate;
    }

    public void setCovStartDate(String covStartDate) {
        this.covStartDate = covStartDate;
    }

    public String getCovEndDate() {
        return covEndDate;
    }

    public void setCovEndDate(String covEndDate) {
        this.covEndDate = covEndDate;
    }

    public String getPerAccStartDate() {
        return perAccStartDate;
    }

    public void setPerAccStartDate(String perAccStartDate) {
        this.perAccStartDate = perAccStartDate;
    }

    public String getPerAccEndDate() {
        return perAccEndDate;
    }

    public void setPerAccEndDate(String perAccEndDate) {
        this.perAccEndDate = perAccEndDate;
    }

    public List<OLEEResourceInvoices> geteRSInvoices() {
        return eRSInvoices;
    }

    public void seteRSInvoices(List<OLEEResourceInvoices> eRSInvoices) {
        this.eRSInvoices = eRSInvoices;
    }

    public List<OLEEResourcePO> geteRSPOItems() {
        return eRSPOItems;
    }

    public void seteRSPOItems(List<OLEEResourcePO> eRSPOItems) {
        this.eRSPOItems = eRSPOItems;
    }

    public void setResultDetails(DocumentSearchResult searchResult, List<OLESearchCondition> oleSearchEresources) {
        List<DocumentAttribute> documentAttributes = searchResult.getDocumentAttributes();
        for (DocumentAttribute docAttribute : documentAttributes) {
            String name = docAttribute.getName();
            if (OLEConstants.OLEEResourceRecord.ERESOURCE_RESULT_FIELDS.contains(name)) {
                if (name.equals(OLEConstants.OLEEResourceRecord.ERESOURCE_TITLE)) {
                    name = OLEConstants.OLEEResourceRecord.ERESOURCE_TITLE;
                }
                Method getMethod;
                try {
                    getMethod = getSetMethod(OLEEResourceRecordDocument.class, name, new Class[]{String.class});
                    getMethod.invoke(this, docAttribute.getValue().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Method getSetMethod(Class targetClass, String attr, Class[] objectAttributes) throws Exception {
        Method method = targetClass.getMethod("set" + StringUtils.capitalize(attr), objectAttributes);
        return method;
    }

    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        // TODO Auto-generated method stub
        // first populate, then call super
        super.prepareForSave(event);
        try {
            LOG.debug("###########Inside OLEEResourceRecordDocument " + "prepareForSave###########");
            if (this.getStatisticalSearchingCode() != null && (!"".equals(this.getStatisticalSearchingCode().trim()))) {
                this.setStatisticalSearchingCodeId(Integer.parseInt(this.getStatisticalSearchingCode()));
                Map statisticalCodeMap = new HashMap<>();
                statisticalCodeMap.put(OLEConstants.OLEEResourceRecord.STATISTICAL_SEARCH_CD_ID, this.getStatisticalSearchingCodeId());
                OleStatisticalSearchingCodes oleStatisticalSearchingCodes = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OleStatisticalSearchingCodes.class, statisticalCodeMap);
                if(oleStatisticalSearchingCodes != null) {
                    this.setOleStatisticalCode(oleStatisticalSearchingCodes);
                }
            }
            String vendorId = this.getVendorId();
            if (vendorId != null && !vendorId.isEmpty()) {
                String[] vendorDetails = vendorId.split("-");
                this.setVendorHeaderGeneratedIdentifier(vendorDetails.length > 0 ? Integer.parseInt(vendorDetails[0]) : 0);
                this.setVendorDetailAssignedIdentifier(vendorDetails.length > 1 ? Integer.parseInt(vendorDetails[1]) : 0);
                Map vendorMap = new HashMap<>();
                vendorMap.put(OLEConstants.OLEEResourceRecord.VENDOR_HEADER_GEN_ID, this.getVendorHeaderGeneratedIdentifier());
                vendorMap.put(OLEConstants.OLEEResourceRecord.VENDOR_DETAILED_ASSIGNED_ID, this.getVendorDetailAssignedIdentifier());
                VendorDetail vendorDetailDoc = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(VendorDetail.class, vendorMap);
                if (vendorDetailDoc != null) {
                    this.setVendorName(vendorDetailDoc.getVendorName());
                }
            }
            String accessId = "";
            if (this.getAccessLocation().size() > 0) {
                List<String> accessLocationId = this.getAccessLocation();
                if (accessLocationId.size() > 0) {
                    for (String accessLocation : accessLocationId) {
                        accessId += accessLocation;
                        accessId += OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR;
                    }
                    this.setAccessLocationId(accessId.substring(0, (accessId.lastIndexOf(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR))));
                }
            }
            this.setLicenseReqStatus("");
            List<OLEEResourceLicense> oleERSLicenses = this.getOleERSLicenseRequests();
            List<DateTime> modifiedDateList = new ArrayList<>();
            DateTime lastModifiedDate = null;
            if (oleERSLicenses.size() > 0) {
                for (int i = oleERSLicenses.size()-1; i >= 0; i--) {
                    DateTime appStatus = oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatusDate();
                    if (!OLEConstants.OLEEResourceRecord.ERESOURCE_STATUSES.contains(appStatus) &&
                            (!oleERSLicenses.get(i).getDocumentRouteHeaderValue().getAppDocStatus().equalsIgnoreCase(
                                    OLEConstants.OLEEResourceRecord.LICENSE_FINAL_STATUS))) {
                        modifiedDateList.add(appStatus);
                    }
                    DocumentRouteHeaderValue documentRouteHeaderValue = oleERSLicenses.get(i).getDocumentRouteHeaderValue();
                    if(documentRouteHeaderValue != null) {
                        String licenceTitle = documentRouteHeaderValue.getDocTitle();
                        if(licenceTitle != null && !licenceTitle.isEmpty()) {
                            licenceTitle = licenceTitle.substring(26);
                        }
                        oleERSLicenses.get(i).setDocumentDescription(licenceTitle);
                    }
                }
                for (int modifiedDate = 0; modifiedDate<modifiedDateList.size(); modifiedDate++) {
                    DateTime dateTime = modifiedDateList.get(modifiedDate);
                    if (lastModifiedDate == null) {
                        lastModifiedDate = dateTime;
                    } else {
                        if (dateTime.isAfter(lastModifiedDate)) {
                            lastModifiedDate = dateTime;
                        }
                    }
                }
                for (int i = oleERSLicenses.size()-1; i >= 0; i--) {
                    if (lastModifiedDate != null && lastModifiedDate.equals(oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatusDate())) {
                        this.setLicenseReqStatus(oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatus());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception during prepareForSave()", e);
            throw new RuntimeException(e);
        }
        Map statusMap = new HashMap<>();
        statusMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, this.getOleERSIdentifier());
        OLEEResourceRecordDocument oleERSDoc = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, statusMap);
        if (oleERSDoc != null) {
            status = oleERSDoc.getStatusName();
        }
        OLEEResourceEventLog oleEResourceEventLog = new OLEEResourceEventLog();
        if (status == null){
            oleEResourceEventLog.setCurrentTimeStamp();
            oleEResourceEventLog.setEventUser(GlobalVariables.getUserSession().getPrincipalName());
            oleEResourceEventLog.setEventType(OLEConstants.OLEEResourceRecord.SYSTEM);
            oleEResourceEventLog.setEventNote(OLEConstants.OLEEResourceRecord.STATUS_IS+getStatusName());
            this.getOleERSEventLogs().add(oleEResourceEventLog);
        }
        else if (!status.equals(getStatusName())){
            oleEResourceEventLog.setCurrentTimeStamp();
            oleEResourceEventLog.setEventUser(GlobalVariables.getUserSession().getPrincipalName());
            oleEResourceEventLog.setEventType(OLEConstants.OLEEResourceRecord.SYSTEM);
            oleEResourceEventLog.setEventNote(OLEConstants.OLEEResourceRecord.STATUS_FROM+ status +OLEConstants.OLEEResourceRecord.STATUS_TO+getStatusName());
            this.getOleERSEventLogs().add(oleEResourceEventLog);
        }
        status=getStatusName();
        String defaultCov = this.getDummyDefaultCoverage();
        if(defaultCov != null && !defaultCov.isEmpty() && !this.isCovEdited()) {
            this.setCovEdited(true);
            getOleEResourceSearchService().getDefaultCovDatesToPopup(this,defaultCov);
        }
        String defaultPerAcc = this.getDummyDefaultPerpetualAccess();
        if(defaultPerAcc != null && !defaultPerAcc.isEmpty() && !this.isPerAccEdited()) {
            this.setPerAccEdited(true);
            getOleEResourceSearchService().getDefaultPerAccDatesToPopup(this,defaultPerAcc);
        }
        getOleEResourceSearchService().saveDefaultCoverageDate(this);
        getOleEResourceSearchService().saveDefaultPerpetualAccessDate(this);
        try {
            getOleEResourceSearchService().saveEResourceInstanceToDocstore(this);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void processAfterRetrieve() {
        super.processAfterRetrieve();
        if (this.getStatisticalSearchingCodeId() != null) {
            this.setStatisticalSearchingCode(this.getStatisticalSearchingCodeId().toString());
        }
        String accessLocationId = this.getAccessLocationId();
        if (accessLocationId != null && !accessLocationId.isEmpty()) {
            String[] accessLocation = accessLocationId.split(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR);
            List<String> accessLocations = new ArrayList<>();
            for (String accessLocId : accessLocation) {
                accessLocations.add(accessLocId);
            }
            this.setAccessLocation(accessLocations);
        }
        this.setLicenseReqStatus("");
        List<OLEEResourceLicense> oleERSLicenses = this.getOleERSLicenseRequests();
        List<DateTime> modifiedDateList = new ArrayList<>();
        DateTime lastModifiedDate = null;
        if (oleERSLicenses.size() > 0) {
            for (int i = oleERSLicenses.size()-1; i >= 0; i--) {
                DateTime appStatus = oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatusDate();
                if (!OLEConstants.OLEEResourceRecord.ERESOURCE_STATUSES.contains(appStatus) &&
                        (!oleERSLicenses.get(i).getDocumentRouteHeaderValue().getAppDocStatus().equalsIgnoreCase(
                                OLEConstants.OLEEResourceRecord.LICENSE_FINAL_STATUS))) {
                    modifiedDateList.add(appStatus);
                }
                DocumentRouteHeaderValue documentRouteHeaderValue = oleERSLicenses.get(i).getDocumentRouteHeaderValue();
                if(documentRouteHeaderValue != null) {
                    String licenceTitle = documentRouteHeaderValue.getDocTitle();
                    if(licenceTitle != null && !licenceTitle.isEmpty()) {
                        licenceTitle = licenceTitle.substring(26);
                    }
                    oleERSLicenses.get(i).setDocumentDescription(licenceTitle);
                }
            }
            for (int modifiedDate = 0; modifiedDate<modifiedDateList.size(); modifiedDate++) {
                DateTime dateTime = modifiedDateList.get(modifiedDate);
                if (lastModifiedDate == null) {
                    lastModifiedDate = dateTime;
                } else {
                    if (dateTime.isAfter(lastModifiedDate)) {
                        lastModifiedDate = dateTime;
                    }
                }
            }
            for (int i = oleERSLicenses.size()-1; i >= 0; i--) {
                if (lastModifiedDate!=null && lastModifiedDate.equals(oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatusDate())) {
                    this.setLicenseReqStatus(oleERSLicenses.get(i).getDocumentRouteHeaderValue().getApplicationDocumentStatus());
                }
            }
        }
        List<OLEEResourceInstance> oleERSInstances = this.getOleERSInstances();
        OLEEResourceInstance oleeResourceInstance = null;
        List<Holdings> holdingsList = new ArrayList<Holdings>();
        List<String> instanceId = new ArrayList<String>();
        if (oleERSInstances.size() > 0) {
            for (OLEEResourceInstance oleERSInstance : oleERSInstances) {
                    instanceId.add(oleERSInstance.getInstanceId());
            }
        }
        if (instanceId.size() > 0) {
            for(String id:instanceId){
                try {
                    holdingsList.add(getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(id));
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        for (Holdings holdings:holdingsList){
            if(holdings instanceof EHoldings){
                HoldingOlemlRecordProcessor holdingOlemlRecordProcessor=new HoldingOlemlRecordProcessor();
                OleHoldings oleHoldings=holdingOlemlRecordProcessor.fromXML(holdings.getContent());
                for (OLEEResourceInstance oleERSInstance : oleERSInstances) {
                    if (holdings.getId().equals(oleERSInstance.getInstanceId())) {
                        oleeResourceInstance = oleERSInstance;
                        StringBuffer urls = new StringBuffer();
                        for(Link link :oleHoldings.getLink()){
                            urls.append(link.getUrl());
                            urls.append(",");
                        }
                        if (urls != null && urls.length() > 0) {
                            String url = urls.substring(0, urls.lastIndexOf(","));
                            oleeResourceInstance.setUrl(url);
                        }
                    }
                }
            }

        }
        getOleEResourceSearchService().getDefaultCovergeDate(this);
        getOleEResourceSearchService().getDefaultPerpetualAccessDate(this);
        getOleEResourceSearchService().getPOInvoiceForERS(this);
        getOleEResourceSearchService().getPOAndInvoiceItemsWithoutDuplicate(this);
    }
}
