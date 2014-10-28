package org.kuali.ole.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.describe.form.WorkEInstanceOlemlForm;
import org.kuali.ole.docstore.common.client.DocstoreClientLocator;
import org.kuali.ole.docstore.common.document.EHoldings;
import org.kuali.ole.docstore.common.document.Holdings;
import org.kuali.ole.docstore.common.document.content.instance.*;
import org.kuali.ole.docstore.common.document.content.instance.xstream.HoldingOlemlRecordProcessor;
import org.kuali.ole.docstore.common.search.SearchParams;
import org.kuali.ole.docstore.common.search.SearchResponse;
import org.kuali.ole.docstore.common.search.SearchResult;
import org.kuali.ole.docstore.common.search.SearchResultField;
import org.kuali.ole.module.purap.businessobject.PurApAccountingLine;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderType;
import org.kuali.ole.select.bo.*;
import org.kuali.ole.select.businessobject.*;
import org.kuali.ole.select.document.*;
import org.kuali.ole.select.form.OLEEResourceRecordForm;
import org.kuali.ole.service.OLEEResourceSearchService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentHeaderService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chenchulakshmig
 * Date: 7/10/13
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEEResourceSearchServiceImpl implements OLEEResourceSearchService {

    private static final Logger LOG = Logger.getLogger(OLEEResourceSearchServiceImpl.class);
    private BusinessObjectService businessObjectService;
    private final String calendarYearAgo = getParameter(OLEConstants.OLEEResourceRecord.CALENDAR_OR_YEAR_AGO);
    private final String calendarYearsAgo = getParameter(OLEConstants.OLEEResourceRecord.CALENDAR_OR_YEARS_AGO);
    private final String monthAgo = getParameter(OLEConstants.OLEEResourceRecord.MONTH_AGO);
    private final String monthsAgo = getParameter(OLEConstants.OLEEResourceRecord.MONTHS_AGO);
    private final String weekAgo = getParameter(OLEConstants.OLEEResourceRecord.WEEK_AGO);
    private final String weeksAgo = getParameter(OLEConstants.OLEEResourceRecord.WEEKS_AGO);
    private final String dayAgo = getParameter(OLEConstants.OLEEResourceRecord.DAY_AGO);
    private final String daysAgo = getParameter(OLEConstants.OLEEResourceRecord.DAYS_AGO);
    private final String firstDayOfYear = getParameter(OLEConstants.OLEEResourceRecord.FIRST_DAY_OF_YEAR);
    private final String lastDayOfYear = getParameter(OLEConstants.OLEEResourceRecord.LAST_DAY_OF_YEAR);

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    private DocstoreClientLocator docstoreClientLocator;

    public DocstoreClientLocator getDocstoreClientLocator() {
        if (docstoreClientLocator == null) {
            docstoreClientLocator = SpringContext.getBean(DocstoreClientLocator.class);
        }
        return docstoreClientLocator;
    }

    @Override
    public List<OLEEResourceRecordDocument> findMatching(Map<String, List<String>> map, DocumentSearchCriteria.Builder docSearchCriteria) {
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        if (docSearchCriteria != null) {
            if (!map.isEmpty()) {
                for (String propertyField : map.keySet()) {
                    if (map.get(propertyField) != null) {
                        attributes.put(propertyField, map.get(propertyField));
                    }
                }
            }
        }
        docSearchCriteria.setDocumentAttributeValues(attributes);
        Date currentDate = new Date();
        docSearchCriteria.setDateCreatedTo(new DateTime(currentDate));
        DocumentSearchCriteria docSearchCriteriaDTO = docSearchCriteria.build();
        DocumentSearchResults components = null;
        components = KEWServiceLocator.getDocumentSearchService().lookupDocuments(GlobalVariables.getUserSession().getPrincipalId(), docSearchCriteriaDTO);
        List<DocumentSearchResult> docSearchResults = components.getSearchResults();
        OLEEResourceRecordDocument oleeResourceRecordDocument;
        List<OLEEResourceRecordDocument> oleeResourceRecordDocumentnew = new ArrayList<OLEEResourceRecordDocument>();
        if (!docSearchResults.isEmpty()) {
            for (DocumentSearchResult searchResult : docSearchResults) {
                oleeResourceRecordDocument = new OLEEResourceRecordDocument();
                oleeResourceRecordDocument.setResultDetails(searchResult, new ArrayList());
                if (oleeResourceRecordDocument != null) {
                    oleeResourceRecordDocumentnew.add(oleeResourceRecordDocument);
                }
            }
        }
        return oleeResourceRecordDocumentnew;
    }

    @Override
    public List<OLEEResourceRecordDocument> statusNotNull(List<OLEEResourceRecordDocument> eresourceList, List<String> status) {
        List<OLEEResourceRecordDocument> eresourceStatusList = new ArrayList<OLEEResourceRecordDocument>();
        List<String> listOfStatuses = new ArrayList<>();
        listOfStatuses.addAll(status);
        for (OLEEResourceRecordDocument oleeResourceRecordDocument : eresourceList) {
            if (listOfStatuses.contains(oleeResourceRecordDocument.getStatusId())) {
                eresourceStatusList.add(oleeResourceRecordDocument);
            }
        }
        return eresourceStatusList;
    }

    @Override
    public List<OLEEResourceRecordDocument> performSearch(List<OLESearchCondition> oleSearchConditionsList) throws Exception {
        boolean flag = true;
        Map<String, List<String>> searchCriteriaMap = new HashMap<String, List<String>>();
        List<OLEEResourceRecordDocument> eresourceList = new ArrayList<OLEEResourceRecordDocument>();
        List<OLEEResourceRecordDocument> eresourceDocumentList = new ArrayList<OLEEResourceRecordDocument>();
        DocumentSearchCriteria.Builder docSearchCriteria = DocumentSearchCriteria.Builder.create();
        docSearchCriteria.setDocumentTypeName(OLEConstants.OLEEResourceRecord.OLE_ERS_DOC);
        for (int searchCriteriaCnt = 0; searchCriteriaCnt < oleSearchConditionsList.size(); searchCriteriaCnt++) {
            if (StringUtils.isNotBlank(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy()) && oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria().isEmpty()) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.SERACH_CRITERIA_REQUIRED);
            } else if (!StringUtils.isNotBlank(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy()) && !oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria().isEmpty()) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.SERACH_BY_REQUIRED);
            } else if (!GlobalVariables.getMessageMap().hasMessages() && StringUtils.isNotBlank(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy()) && !oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria().isEmpty()
                    && (OLEConstants.OLEEResourceRecord.AND.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator()) || OLEConstants.OLEEResourceRecord.OR.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator()))) {
                flag = false;
                if (searchCriteriaCnt != 0 && OLEConstants.OLEEResourceRecord.AND.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator())) {
                    if (!searchCriteriaMap.containsKey(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy())) {
                        searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria(), searchCriteriaMap);
                    } else {
                        searchCriteriaMap.clear();
                        break;
                    }
                } else {
                    searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria(), searchCriteriaMap);
                }
                if (searchCriteriaCnt < oleSearchConditionsList.size() - 1) {
                    if (StringUtils.isNotBlank(oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchBy()) && !oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchCriteria().isEmpty()) {
                        if (OLEConstants.OLEEResourceRecord.AND.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator())) {
                            if (!searchCriteriaMap.containsKey(oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchBy())) {
                                searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchCriteria(), searchCriteriaMap);
                                if (searchCriteriaCnt < oleSearchConditionsList.size() - 2 && oleSearchConditionsList.get(searchCriteriaCnt + 2).getSearchBy() != null && !oleSearchConditionsList.get(searchCriteriaCnt + 2).getSearchCriteria().isEmpty()) {
                                    if (OLEConstants.OLEEResourceRecord.AND.equals(oleSearchConditionsList.get(searchCriteriaCnt + 1).getOperator())) {
                                        searchCriteriaCnt++;
                                    }
                                } else {
                                    if (searchCriteriaMap.size() > 0) {
                                        eresourceList = findMatching(searchCriteriaMap, docSearchCriteria);
                                    }
                                    break;
                                }
                            } else {
                                searchCriteriaMap.clear();
                                break;
                            }
                        } else if (OLEConstants.OLEEResourceRecord.OR.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator())) {
                            if (searchCriteriaMap.size() > 0) {
                                eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                                eresourceList.addAll(eresourceDocumentList);
                                searchCriteriaMap.clear();
                            }
                            if (searchCriteriaCnt == oleSearchConditionsList.size() - 2) {
                                searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchCriteria(), searchCriteriaMap);
                                if (searchCriteriaMap.size() > 0) {
                                    eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                                    eresourceList.addAll(eresourceDocumentList);
                                    searchCriteriaMap.clear();
                                }
                                break;
                            }
                            if (OLEConstants.OLEEResourceRecord.AND.equals(oleSearchConditionsList.get(searchCriteriaCnt + 1).getOperator())) {
                                searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt + 1).getSearchCriteria(), searchCriteriaMap);
                                if (searchCriteriaMap.size() > 0) {
                                    eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                                    eresourceList.addAll(eresourceDocumentList);
                                }
                            }
                        }
                    } else {
                        if (!searchCriteriaMap.isEmpty()) {
                            if (searchCriteriaMap.size() > 0) {
                                eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                                eresourceList.addAll(eresourceDocumentList);
                                searchCriteriaMap.clear();
                            }
                        }
                    }
                }
                if (searchCriteriaCnt == oleSearchConditionsList.size() - 1) {
                    if (searchCriteriaMap.size() > 0) {
                        eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                        eresourceList.addAll(eresourceDocumentList);
                        searchCriteriaMap.clear();
                    }
                }
            }
        }
        if (flag) {
            if (!GlobalVariables.getMessageMap().hasMessages()) {
                eresourceList = findMatching(searchCriteriaMap, docSearchCriteria);
            }
        }
        if (eresourceList.size() > 0) {
            for (int searchCriteriaCnt = 0; searchCriteriaCnt < oleSearchConditionsList.size(); searchCriteriaCnt++) {
                if (oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy() != null && StringUtils.isNotEmpty(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy()) && !oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria().isEmpty() &&
                        (OLEConstants.OLEEResourceRecord.NOT.equals(oleSearchConditionsList.get(searchCriteriaCnt).getOperator()))) {
                    searchCriteriaMap.clear();
                    searchCriteriaMap = getSearchCriteriaMap(oleSearchConditionsList.get(searchCriteriaCnt).getSearchBy(), oleSearchConditionsList.get(searchCriteriaCnt).getSearchCriteria(), searchCriteriaMap);
                    if (searchCriteriaMap.size() > 0) {
                        eresourceDocumentList = findMatching(searchCriteriaMap, docSearchCriteria);
                    }
                    List<String> list = new ArrayList<String>();
                    for (OLEEResourceRecordDocument oleEResourceRecordDocument : eresourceDocumentList) {
                        int count = 0;
                        for (OLEEResourceRecordDocument eResourceRecordDocument : eresourceList) {
                            if (oleEResourceRecordDocument.getDocumentNumber().toString().equalsIgnoreCase(eResourceRecordDocument.getDocumentNumber().toString())) {
                                list.add(count + "");
                            }
                            count++;
                        }
                    }
                    for (String str : list) {
                        eresourceList.remove(Integer.parseInt(str));
                    }
                }
            }
        }
        return eresourceList;
    }

    public final String getParameter(String parameterName) {
        ParameterKey parameterKey = ParameterKey.create(OLEConstants.APPL_ID, OLEConstants.SELECT_NMSPC, OLEConstants.SELECT_CMPNT, parameterName);
        Parameter parameter = CoreServiceApiServiceLocator.getParameterRepositoryService().getParameter(parameterKey);
        return parameter != null ? parameter.getValue() : null;
    }

    public void getEResourcesFields(String eResourceId, OleHoldings eHoldings, WorkEInstanceOlemlForm olemlForm) {
        ExtentOfOwnership extentOfOwnership = null;
        if (eHoldings.getExtentOfOwnership().size() > 0) {
            extentOfOwnership = eHoldings.getExtentOfOwnership().get(0);
        } else if (extentOfOwnership == null) {
            extentOfOwnership = new ExtentOfOwnership();
            eHoldings.getExtentOfOwnership().add(extentOfOwnership);
        }
        Coverages coverages = extentOfOwnership.getCoverages();
        if (coverages == null) {
            coverages = new Coverages();
            eHoldings.getExtentOfOwnership().get(0).setCoverages(coverages);
        }
        PerpetualAccesses perpetualAccesses = extentOfOwnership.getPerpetualAccesses();
        if (perpetualAccesses == null) {
            perpetualAccesses = new PerpetualAccesses();
            eHoldings.getExtentOfOwnership().get(0).setPerpetualAccesses(perpetualAccesses);
        }
        List<Coverage> coverage = coverages.getCoverage();
        List<PerpetualAccess> perpetualAccess = perpetualAccesses.getPerpetualAccess();
        //TODO: set the invoice and eResource information
//        Invoice invoice = eHoldings.getInvoice();
        Map ersIdMap = new HashMap();
        ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, eResourceId);
        OLEEResourceRecordDocument eResourceDocument = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, ersIdMap);
        if (eResourceDocument != null && olemlForm.geteResourceId() != null && !olemlForm.geteResourceId().isEmpty()) {
            olemlForm.setTokenId(eResourceDocument.getDocumentNumber());
            eHoldings.setEResourceId(eResourceDocument.getOleERSIdentifier());
            olemlForm.seteResourceTitle(eResourceDocument.getTitle());
            getAccessLocationFromERS(eResourceDocument, eHoldings, olemlForm);
            if (eHoldings.getStatisticalSearchingCode() == null) {
                StatisticalSearchingCode statisticalSearchingCode = new StatisticalSearchingCode();
                statisticalSearchingCode.setCodeValue(eResourceDocument.getOleStatisticalCode() != null ? eResourceDocument.getOleStatisticalCode().getStatisticalSearchingCode() : "");
                eHoldings.setStatisticalSearchingCode(statisticalSearchingCode);
            }
            if (coverage.size() != 0) {
                if (eResourceDocument.getDefaultCoverage() != null && !eResourceDocument.getDefaultCoverage().isEmpty()) {
                    if (eResourceDocument.getDefaultCoverage().contains("-")) {
                        String[] defaultCoverageStart = eResourceDocument.getDefaultCoverage().split(getParameter(OLEConstants.OLEEResourceRecord.COVERAGE_DATE_SEPARATOR));
                        String[] covStartSeparator = defaultCoverageStart[0].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        String[] covEndSeparator = defaultCoverageStart[1].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        for (Coverage coverageList : coverage) {
                            if (coverageList.getCoverageEndDate() == null) {
                                if (covEndSeparator.length > 2 && !covEndSeparator[2].isEmpty()) {
                                    if (covEndSeparator[2].contains("/")) {
                                        coverageList.setCoverageEndDateFormat(covEndSeparator[2]);
                                    } else {
                                        coverageList.setCoverageEndDateString(covEndSeparator[2]);
                                    }
                                    coverageList.setCoverageEndDate(covEndSeparator[2]);
                                }
                                if (covEndSeparator.length > 0 && !covEndSeparator[0].isEmpty()) {
                                    coverageList.setCoverageEndVolume(covEndSeparator[0]);
                                }
                                if (covEndSeparator.length > 1 && !covEndSeparator[1].isEmpty()) {
                                    coverageList.setCoverageEndIssue(covEndSeparator[1]);
                                }
                            }
                            if (coverageList.getCoverageStartDate() == null) {
                                if (covStartSeparator.length > 2 && !covStartSeparator[2].isEmpty()) {
                                    if (covStartSeparator[2].contains("/")) {
                                        coverageList.setCoverageStartDateFormat(covStartSeparator[2]);
                                    } else {
                                        coverageList.setCoverageStartDateString(covStartSeparator[2]);
                                    }
                                    coverageList.setCoverageStartDate(covStartSeparator[2]);
                                }
                                if (covStartSeparator.length > 0 && !covStartSeparator[0].isEmpty()) {
                                    coverageList.setCoverageStartVolume(covStartSeparator[0]);
                                }
                                if (covStartSeparator.length > 1 && !covStartSeparator[1].isEmpty()) {
                                    coverageList.setCoverageStartIssue(covStartSeparator[1]);
                                }
                            }
                        }
                    }
                }
            }
            if (perpetualAccess.size() != 0) {
                if (eResourceDocument.getDefaultPerpetualAccess() != null && !eResourceDocument.getDefaultPerpetualAccess().isEmpty()) {
                    if (eResourceDocument.getDefaultPerpetualAccess().contains("-")) {
                        String[] defaultPerAccStart = eResourceDocument.getDefaultPerpetualAccess().split(getParameter(OLEConstants.OLEEResourceRecord.PERPETUAL_ACCESS_DATE_SEPARATOR));
                        String[] perAccStartSeparator = defaultPerAccStart[0].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        String[] perAccEndSeparator = defaultPerAccStart[1].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        for (PerpetualAccess perpetualAccessList : perpetualAccess) {
                            if (perpetualAccessList.getPerpetualAccessEndDate() == null) {
                                if (perAccEndSeparator.length > 2 && !perAccEndSeparator[2].isEmpty()) {
                                    if (perAccEndSeparator[2].contains("/")) {
                                        perpetualAccessList.setPerpetualAccessEndDateFormat(perAccEndSeparator[2]);
                                    } else {
                                        perpetualAccessList.setPerpetualAccessEndDateString(perAccEndSeparator[2]);
                                    }
                                    perpetualAccessList.setPerpetualAccessEndDate(perAccEndSeparator[2]);
                                }
                                if (perAccEndSeparator.length > 0 && !perAccEndSeparator[0].isEmpty()) {
                                    perpetualAccessList.setPerpetualAccessEndVolume(perAccEndSeparator[0]);
                                }
                                if (perAccEndSeparator.length > 1 && !perAccEndSeparator[1].isEmpty()) {
                                    perpetualAccessList.setPerpetualAccessEndIssue(perAccEndSeparator[1]);
                                }
                            }
                            if (perpetualAccessList.getPerpetualAccessStartDate() == null) {
                                if (perAccStartSeparator.length > 2 && !perAccStartSeparator[2].isEmpty()) {
                                    if (perAccStartSeparator[2].contains("/")) {
                                        perpetualAccessList.setPerpetualAccessStartDateFormat(perAccStartSeparator[2]);
                                    } else {
                                        perpetualAccessList.setPerpetualAccessStartDateString(perAccStartSeparator[2]);
                                    }
                                    perpetualAccessList.setPerpetualAccessStartDate(perAccStartSeparator[2]);
                                }
                                if (perAccStartSeparator.length > 0 && !perAccStartSeparator[0].isEmpty()) {
                                    perpetualAccessList.setPerpetualAccessStartVolume(perAccStartSeparator[0]);
                                }
                                if (perAccStartSeparator.length > 1 && !perAccStartSeparator[1].isEmpty()) {
                                    perpetualAccessList.setPerpetualAccessStartIssue(perAccStartSeparator[1]);
                                }
                            }
                        }
                    }
                }
            }
            if (coverage.size() == 0) {
                boolean coverageFlag = false;
                Coverage cov = new Coverage();
                if (eResourceDocument.getDefaultCoverage() != null && !eResourceDocument.getDefaultCoverage().isEmpty()) {
                    if (eResourceDocument.getDefaultCoverage().contains("-")) {
                        String[] defaultCoverageStart = eResourceDocument.getDefaultCoverage().split(getParameter(OLEConstants.OLEEResourceRecord.COVERAGE_DATE_SEPARATOR));
                        String[] covStartSeparator = defaultCoverageStart[0].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        if (covStartSeparator.length > 2 && !covStartSeparator[2].isEmpty()) {
                            if(covStartSeparator[2].contains("/")) {
                                cov.setCoverageStartDateFormat(covStartSeparator[2]);
                            } else {
                                cov.setCoverageStartDateString(covStartSeparator[2]);
                            }
                            cov.setCoverageStartDate(covStartSeparator[2]);
                            coverageFlag = true;
                        }
                        if (covStartSeparator.length > 0 && !covStartSeparator[0].isEmpty()) {
                            cov.setCoverageStartVolume(covStartSeparator[0]);
                            coverageFlag = true;
                        }
                        if (covStartSeparator.length > 1 && !covStartSeparator[1].isEmpty()) {
                            cov.setCoverageStartIssue(covStartSeparator[1]);
                            coverageFlag = true;
                        }
                        String[] covEndSeparator = defaultCoverageStart[1].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        if (covEndSeparator.length > 2 && !covEndSeparator[2].isEmpty()) {
                            if(covEndSeparator[2].contains("/")) {
                                cov.setCoverageEndDateFormat(covEndSeparator[2]);
                            } else {
                                cov.setCoverageEndDateString(covEndSeparator[2]);
                            }
                            cov.setCoverageEndDate(covEndSeparator[2]);
                            coverageFlag = true;
                        }
                        if (covEndSeparator.length > 0 && !covEndSeparator[0].isEmpty()) {
                            cov.setCoverageEndVolume(covEndSeparator[0]);
                            coverageFlag = true;
                        }
                        if (covEndSeparator.length > 1 && !covEndSeparator[1].isEmpty()) {
                            cov.setCoverageEndIssue(covEndSeparator[1]);
                            coverageFlag = true;
                        }
                        if (coverageFlag) {
                            eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().add(cov);
                        }
                    }
                }
            }
            if (perpetualAccess.size() == 0) {
                boolean perpetualAccFlag = false;
                PerpetualAccess perpetualAcc = new PerpetualAccess();
                if (eResourceDocument.getDefaultPerpetualAccess() != null && !eResourceDocument.getDefaultPerpetualAccess().isEmpty()) {
                    if (eResourceDocument.getDefaultPerpetualAccess().contains("-")) {
                        String[] defaultPerAccStart = eResourceDocument.getDefaultPerpetualAccess().split(getParameter(OLEConstants.OLEEResourceRecord.PERPETUAL_ACCESS_DATE_SEPARATOR));
                        String[] perAccStartSeparator = defaultPerAccStart[0].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        if (perAccStartSeparator.length > 2 && !perAccStartSeparator[2].isEmpty()) {
                            if(perAccStartSeparator[2].contains("/")) {
                                perpetualAcc.setPerpetualAccessStartDateFormat(perAccStartSeparator[2]);
                            } else {
                                perpetualAcc.setPerpetualAccessStartDateString(perAccStartSeparator[2]);
                            }
                            perpetualAcc.setPerpetualAccessStartDate(perAccStartSeparator[2]);
                            perpetualAccFlag = true;
                        }
                        if (perAccStartSeparator.length > 0 && !perAccStartSeparator[0].isEmpty()) {
                            perpetualAcc.setPerpetualAccessStartVolume(perAccStartSeparator[0]);
                            perpetualAccFlag = true;
                        }
                        if (perAccStartSeparator.length > 1 && !perAccStartSeparator[1].isEmpty()) {
                            perpetualAcc.setPerpetualAccessStartIssue(perAccStartSeparator[1]);
                            perpetualAccFlag = true;
                        }
                        String[] perAccEndSeparator = defaultPerAccStart[1].split(getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR));
                        if (perAccEndSeparator.length > 2 && !perAccEndSeparator[2].isEmpty()) {
                            if(perAccEndSeparator[2].contains("/")) {
                                perpetualAcc.setPerpetualAccessEndDateFormat(perAccEndSeparator[2]);
                            } else {
                                perpetualAcc.setPerpetualAccessEndDateString(perAccEndSeparator[2]);
                            }
                            perpetualAcc.setPerpetualAccessEndDate(perAccEndSeparator[2]);
                            perpetualAccFlag = true;
                        }
                        if (perAccEndSeparator.length > 0 && !perAccEndSeparator[0].isEmpty()) {
                            perpetualAcc.setPerpetualAccessEndVolume(perAccEndSeparator[0]);
                            perpetualAccFlag = true;
                        }
                        if (perAccEndSeparator.length > 1 && !perAccEndSeparator[1].isEmpty()) {
                            perpetualAcc.setPerpetualAccessEndIssue(perAccEndSeparator[1]);
                            perpetualAccFlag = true;
                        }
                        if (perpetualAccFlag) {
                            eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().add(perpetualAcc);
                        }
                    }
                }
            }
        } else {
            getAccessLocationFromEInstance(eHoldings, olemlForm);
        }
    }

    public void getAccessLocationFromERS(OLEEResourceRecordDocument eResourceDocument, OleHoldings eHoldings, WorkEInstanceOlemlForm olemlForm) {
        if (eResourceDocument != null) {
            HoldingsAccessInformation accessInformation = eHoldings.getHoldingsAccessInformation();
            if (accessInformation != null&&accessInformation.getAccessLocation()!=null) {
                if (olemlForm.getExtendedEHoldingFields().getAccessLocation() != null && olemlForm.getExtendedEHoldingFields().getAccessLocation().size() > 0) {
                    String accessId = "";
                    for (String accessLoc : olemlForm.getExtendedEHoldingFields().getAccessLocation()) {
                        accessId += accessLoc;
                        accessId += OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR;
                    }
                    accessInformation.setAccessLocation(accessId.substring(0, (accessId.lastIndexOf(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR))));
                    eHoldings.setHoldingsAccessInformation(accessInformation);
                }
                else{
                    String accessLocationId = accessInformation.getAccessLocation();
                    String accessId = "";
                    if (accessLocationId != null && !accessLocationId.isEmpty()) {
                        String[] accessLocation = accessLocationId.split(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR);
                        List<String> accessLocations = new ArrayList<>();
                        for (String accessLocId : accessLocation) {
                            olemlForm.getExtendedEHoldingFields().getAccessLocation().add(accessLocId);
                            accessLocations.add(accessLocId);
                        }
                        for (String accessLoc : accessLocations) {
                            accessId += accessLoc;
                            accessId += OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR;
                        }
                        accessInformation.setAccessLocation(accessId.substring(0, (accessId.lastIndexOf(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR))));
                    }
                }
            }else {
                accessInformation = new HoldingsAccessInformation();
                String accessLocationId = eResourceDocument.getAccessLocationId();
                String accessId = "";
                if (accessLocationId != null && !accessLocationId.isEmpty()) {
                    String[] accessLocation = accessLocationId.split(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR);
                    List<String> accessLocations = new ArrayList<>();
                    for (String accessLocId : accessLocation) {
                        olemlForm.getExtendedEHoldingFields().getAccessLocation().add(accessLocId);
                        accessLocations.add(accessLocId);
                    }
                    for (String accessLoc : accessLocations) {
                        accessId += accessLoc;
                        accessId += OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR;
                    }
                    accessInformation.setAccessLocation(accessId.substring(0, (accessId.lastIndexOf(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR))));
                }

            }
            if (eHoldings.getHoldingsAccessInformation()==null) {
            accessInformation.setAuthenticationType(eResourceDocument.getOleAuthenticationType() != null ? eResourceDocument.getOleAuthenticationType().getOleAuthenticationTypeName() : "");
            accessInformation.setNumberOfSimultaneousUser(eResourceDocument.getNumOfSimultaneousUsers());
            }
            if (eHoldings.getHoldingsAccessInformation()!=null&&eHoldings.getHoldingsAccessInformation().getAuthenticationType()==null) {
            accessInformation.setAuthenticationType(eResourceDocument.getOleAuthenticationType() != null ? eResourceDocument.getOleAuthenticationType().getOleAuthenticationTypeName() : "");
            }
            if (eHoldings.getHoldingsAccessInformation()!=null&&eHoldings.getHoldingsAccessInformation().getNumberOfSimultaneousUser()==null) {
                accessInformation.setNumberOfSimultaneousUser(eResourceDocument.getNumOfSimultaneousUsers());
            }
            eHoldings.setHoldingsAccessInformation(accessInformation);
        }
    }

    public void getAccessLocationFromEInstance(OleHoldings eHoldings, WorkEInstanceOlemlForm olemlForm) {
        HoldingsAccessInformation accessInformation = eHoldings.getHoldingsAccessInformation();
        List<String> accessLocations = new ArrayList<>();
        if (accessInformation != null) {
            if (olemlForm.getExtendedEHoldingFields().getAccessLocation() != null && olemlForm.getExtendedEHoldingFields().getAccessLocation().size() > 0) {
                String accessId = "";
                for (String accessLoc : olemlForm.getExtendedEHoldingFields().getAccessLocation()) {
                    accessId += accessLoc;
                    accessId += OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR;
                }
                accessInformation.setAccessLocation(accessId.substring(0, (accessId.lastIndexOf(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR))));
                eHoldings.setHoldingsAccessInformation(accessInformation);
            } else if (accessInformation.getAccessLocation() != null && !accessInformation.getAccessLocation().isEmpty()) {
                String[] accessId = accessInformation.getAccessLocation().split(OLEConstants.OLEEResourceRecord.ACCESS_LOCATION_SEPARATOR);
                for (String accessLoc : accessId) {
                    accessLocations.add(accessLoc);
                }
                olemlForm.getExtendedEHoldingFields().setAccessLocation(accessLocations);
            }
        }
    }

    public Map<String, List<String>> getSearchCriteriaMap(String searchBy, String searchCriteria, Map<String, List<String>> searchCriteriaMap) throws Exception {
        List<String> listOfBibs = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        org.kuali.ole.docstore.common.search.SearchParams search_Params = new org.kuali.ole.docstore.common.search.SearchParams();
        SearchResponse searchResponse = null;
        if (searchBy.equals(OLEConstants.OLEEResourceRecord.ERESOURCE_ISBN)) {
            if ("001".equals(OLEConstants.COMMON_IDENTIFIER_SEARCH)) {
                String code = "wbm-" + searchCriteria;
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), "id", code), ""));
            } else {
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), OLEConstants.COMMON_IDENTIFIER_SEARCH, searchCriteria), ""));
            }


            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), "id"));


            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
            for (SearchResult searchResult : searchResponse.getSearchResults()) {
                for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                    String fieldName = searchResultField.getFieldName();
                    String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";

                    if (fieldName.equalsIgnoreCase("id") && !fieldValue.isEmpty() && searchResultField.getDocType().equalsIgnoreCase("bibliographic")) {
                        listOfBibs.add(fieldValue);
                    }
                }
            }


            if (listOfBibs.size() > 0) {
                valueList.addAll(listOfBibs);
                searchCriteriaMap.put(searchBy, valueList);


            }
        } else if (searchBy.equals(OLEConstants.OLEEResourceRecord.ERESOURCE_OCLC)) {
            if ("001".equals(OLEConstants.OCLC_SEARCH)) {
                String code = "wbm-" + searchCriteria;
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), "id", code), ""));
            } else {
                search_Params.getSearchConditions().add(search_Params.buildSearchCondition("", search_Params.buildSearchField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), OLEConstants.OCLC_SEARCH, searchCriteria), ""));
            }
            search_Params.getSearchResultFields().add(search_Params.buildSearchResultField(org.kuali.ole.docstore.common.document.content.enums.DocType.BIB.getCode(), "id"));
            searchResponse = getDocstoreClientLocator().getDocstoreClient().search(search_Params);
            for (SearchResult searchResult : searchResponse.getSearchResults()) {
                for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                    String fieldName = searchResultField.getFieldName();
                    String fieldValue = searchResultField.getFieldValue() != null ? searchResultField.getFieldValue() : "";

                    if (fieldName.equalsIgnoreCase("id") && !fieldValue.isEmpty() && searchResultField.getDocType().equalsIgnoreCase("bibliographic")) {
                        listOfBibs.add(fieldValue);
                    }
                }
            }
            if (listOfBibs.size() > 0) {
                valueList.addAll(listOfBibs);
                searchCriteriaMap.put(searchBy, valueList);
            }
        } else {
            valueList.add(searchCriteria);
            searchCriteriaMap.put(searchBy, valueList);
        }
        return searchCriteriaMap;
    }

    public void getInstanceIdFromERS(List<OLEEResourceInstance> oleERSInstanceList, OleHoldings eHoldings) {
        String instanceId = "";
        if (oleERSInstanceList.size() > 0) {
            for (OLEEResourceInstance oleERSInstance : oleERSInstanceList) {
                Map ersIdMap = new HashMap();
                ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleERSInstance.getOleERSIdentifier());
                ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_INSTANCE_ID, oleERSInstance.getInstanceId());
                List<OLEEResourceInstance> eResourceInstance = (List<OLEEResourceInstance>) KRADServiceLocator.getBusinessObjectService().findMatching(OLEEResourceInstance.class, ersIdMap);
                if (eResourceInstance.size() > 0) {
                    for (OLEEResourceInstance eInstanceRec : eResourceInstance) {
                        instanceId += eInstanceRec.getInstanceId();
                        instanceId += ",";
                    }
                    //TODO: setRelatedInstanceIdentifier
//                    eHoldings.setRelatedInstanceIdentifier(instanceId.substring(0, (instanceId.lastIndexOf(","))));
                }
            }
        }
    }

    public void getPOIdFromERS(List<OLEEResourcePO> oleERSPOList, OleHoldings eHoldings) {
        String poId = "";
        if (oleERSPOList.size() > 0) {
            for (OLEEResourcePO oleERSPO : oleERSPOList) {
                Map ersIdMap = new HashMap();
                ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleERSPO.getOleERSIdentifier());
                ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_PO_ITEM_ID, oleERSPO.getOlePOItemId());
                List<OLEEResourcePO> eResourcePO = (List<OLEEResourcePO>) KRADServiceLocator.getBusinessObjectService().findMatching(OLEEResourcePO.class, ersIdMap);
                if (eResourcePO.size() > 0) {
                    for (OLEEResourcePO ePORec : eResourcePO) {
                        poId += ePORec.getOlePOItemId();
                        poId += ",";
                    }
                    //TODO: set PurchaseOrder info
//                    eHoldings.setPurchaseOrderId(poId.substring(0, (poId.lastIndexOf(","))));
                }
            }
        }
    }

    public void getEResourcesLicenseFields(String eResourceId, WorkEInstanceOlemlForm eInstanceOlemlForm) {
        List<OLEEResourceLicense> oleERSLicenses = new ArrayList<OLEEResourceLicense>();
        Map ersIdMap = new HashMap();
        ersIdMap.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, eResourceId);
        OLEEResourceRecordDocument eResourceDocument = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, ersIdMap);
        if (eResourceDocument != null) {
            oleERSLicenses = eResourceDocument.getOleERSLicenseRequests();
            if (oleERSLicenses.size() > 0) {
                for (OLEEResourceLicense oleeResourceLicense : oleERSLicenses) {
                    DocumentRouteHeaderValue documentRouteHeaderValue = oleeResourceLicense.getDocumentRouteHeaderValue();
                    if (documentRouteHeaderValue != null) {
                        String licenceTitle = documentRouteHeaderValue.getDocTitle();
                        if (licenceTitle != null && !licenceTitle.isEmpty()) {
                            licenceTitle = licenceTitle.substring(26);
                        }
                        oleeResourceLicense.setDocumentDescription(licenceTitle);
                    }
                }
                eInstanceOlemlForm.getExtendedEHoldingFields().setLicense(oleERSLicenses);
            }
        }
    }

    public OLEEResourceRecordDocument getNewOleERSDoc(OLEEResourceRecordDocument oleERSDoc) {
        if (oleERSDoc.getOleERSIdentifier() != null && !oleERSDoc.getOleERSIdentifier().isEmpty()) {
            Map<String, String> tempId = new HashMap<String, String>();
            tempId.put(OLEConstants.OLEEResourceRecord.ERESOURCE_IDENTIFIER, oleERSDoc.getOleERSIdentifier());
            OLEEResourceRecordDocument tempDocument = (OLEEResourceRecordDocument) KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OLEEResourceRecordDocument.class, tempId);
            if (tempDocument != null) {
                if (tempDocument.getOleMaterialTypes().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleMaterialTypes());
                }
                if (tempDocument.getOleFormatTypes().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleFormatTypes());
                }
                if (tempDocument.getOleContentTypes().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleContentTypes());
                }
                if (tempDocument.getSelectors().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getSelectors());
                }
                if (tempDocument.getRequestors().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getRequestors());
                }
                if (tempDocument.getReqSelComments().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getReqSelComments());
                }
                if (tempDocument.getEresNotes().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getEresNotes());
                }
                if (tempDocument.getOleERSLicenseRequests().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleERSLicenseRequests());
                }
                if (tempDocument.getOleERSEventLogs().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleERSEventLogs());
                }
                if (tempDocument.getOleERSPOItems().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleERSPOItems());
                }
                if (tempDocument.getOleERSInstances().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleERSInstances());
                }
                if (tempDocument.getOleERSInvoices().size() > 0) {
                    KRADServiceLocator.getBusinessObjectService().delete(tempDocument.getOleERSInvoices());
                }
            }
        }
        return oleERSDoc;
    }

   /* public List<WorkBibDocument> getWorkBibDocuments(List<String> instanceIdsList, String docType) {
        List<LinkedHashMap<String, String>> instanceIdMapList = new ArrayList<LinkedHashMap<String, String>>();
        for (String instanceId : instanceIdsList) {
            LinkedHashMap<String, String> instanceIdMap = new LinkedHashMap<String, String>();
            instanceIdMap.put(docType, instanceId);
            instanceIdMapList.add(instanceIdMap);
        }
        QueryService queryService = QueryServiceImpl.getInstance();
        List<WorkBibDocument> workBibDocuments = new ArrayList<WorkBibDocument>();
        try {
            workBibDocuments = queryService.getWorkBibRecords(instanceIdMapList);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return workBibDocuments;
    }*/

    public void getDefaultCovergeDate(OLEEResourceRecordDocument oleERSDoc) {
        String defaultCoverage = oleERSDoc.getDefaultCoverage();
        String startCov = "";
        String endCov = "";
        String dummyStartCov = "";
        String dummyEndCov = "";
        String separator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR);
        String dateSeparator = getParameter(OLEConstants.OLEEResourceRecord.COVERAGE_DATE_SEPARATOR);
        if (StringUtils.isNotEmpty(defaultCoverage)) {
            if (defaultCoverage.contains(separator)) {
                String[] defaultCoverageDates = defaultCoverage.split(dateSeparator);
                if (defaultCoverageDates[0].contains(separator)) {
                    String[] startCoverage = defaultCoverageDates[0].split(separator);
                    if (startCoverage.length > 0) {
                        if (startCoverage.length > 0 && !startCoverage[0].isEmpty()) {
                            startCov += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL;
                            startCov += startCoverage[0];
                            startCov += separator;
                            dummyStartCov += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL + startCoverage[0] + separator;
                        } else {
                            dummyStartCov += separator;
                        }
                        if (startCoverage.length > 1 && !startCoverage[1].isEmpty()) {
                            startCov += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE;
                            startCov += startCoverage[1];
                            startCov += separator;
                            dummyStartCov += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE + startCoverage[1] + separator;
                        } else {
                            dummyStartCov += separator;
                        }
                        if (startCoverage.length > 2 && !startCoverage[2].isEmpty()) {
                            startCov += OLEConstants.OLEEResourceRecord.SPACE + startCoverage[2] + OLEConstants.OLEEResourceRecord.SPACE;
                            startCov += separator;
                            dummyStartCov += startCoverage[2] + separator;
                        } else {
                            dummyStartCov += separator;
                        }
                        /*for (String covDate : startCoverage) {
                            if (covDate != null && !covDate.isEmpty()) {
                                startCov += covDate;

                            }
                        }*/
                        startCov = startCov.substring(0, startCov.lastIndexOf(separator));
                    } else {
                        startCov = "";
                    }
                }
                if (defaultCoverageDates[1].contains(separator)) {
                    String[] endCoverage = defaultCoverageDates[1].split(separator);
                    if (endCoverage.length > 0) {
                        if (endCoverage.length > 0 && !endCoverage[0].isEmpty()) {
                            endCov += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL;
                            endCov += endCoverage[0];
                            endCov += separator;
                            dummyEndCov += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL + endCoverage[0] + separator;
                        } else {
                            dummyEndCov += separator;
                        }
                        if (endCoverage.length > 1 && !endCoverage[1].isEmpty()) {
                            endCov += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE;
                            endCov += endCoverage[1];
                            endCov += separator;
                            dummyEndCov += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE + endCoverage[1] + separator;
                        } else {
                            dummyEndCov += separator;
                        }
                        if (endCoverage.length > 2 && !endCoverage[2].isEmpty()) {
                            endCov += OLEConstants.OLEEResourceRecord.SPACE + endCoverage[2];
                            endCov += separator;
                            dummyEndCov += endCoverage[2] + separator;
                        } else {
                            dummyEndCov += separator;
                        }
                        /*for (String endDate : endCoverage) {
                            if (endDate != null && !endDate.isEmpty()) {
                                endCov += endDate;
                                endCov += getParameter(OLEConstants.OLEEResourceRecord.DEFAULT_COVERAGE_SEPARATOR);
                            }
                        }*/
                        endCov = endCov.substring(0, endCov.lastIndexOf(separator));
                    } else {
                        endCov = "";
                    }
                }
            }
            if ((endCov != null && !endCov.isEmpty()) && (startCov != null && !startCov.isEmpty())) {
                oleERSDoc.setDefaultCoverageView(startCov + dateSeparator + endCov);
                oleERSDoc.setDummyDefaultCoverage(dummyStartCov + dateSeparator + dummyEndCov);
            } else if (startCov != null && !startCov.isEmpty()) {
                oleERSDoc.setDefaultCoverageView(startCov);
                oleERSDoc.setDummyDefaultCoverage(dummyStartCov);
            } else if (endCov != null && !endCov.isEmpty()) {
                oleERSDoc.setDefaultCoverageView(endCov);
                oleERSDoc.setDummyDefaultCoverage(dummyEndCov);
            } else {
                oleERSDoc.setDefaultCoverageView(null);
                oleERSDoc.setDummyDefaultCoverage(null);
            }
        }
    }

    public void getDefaultPerpetualAccessDate(OLEEResourceRecordDocument oleERSDoc) {
        String defaultPerpetualAcc = oleERSDoc.getDefaultPerpetualAccess();
        String startPerAcc = "";
        String endPerAcc = "";
        String dummyStartPerAcc = "";
        String dummyEndPerAcc = "";
        String separator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR);
        String dateSeparator = getParameter(OLEConstants.OLEEResourceRecord.PERPETUAL_ACCESS_DATE_SEPARATOR);
        if (StringUtils.isNotEmpty(defaultPerpetualAcc)) {
            if (defaultPerpetualAcc.contains(dateSeparator)) {
                String[] defaultPerAccDates = defaultPerpetualAcc.split(dateSeparator);
                if (defaultPerAccDates[0].contains(separator)) {
                    String[] startPerpetualAccess = defaultPerAccDates[0].split(separator);
                    if (startPerpetualAccess.length > 0) {
                        if (startPerpetualAccess.length > 0 && !startPerpetualAccess[0].isEmpty()) {
                            startPerAcc += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL;
                            startPerAcc += startPerpetualAccess[0];
                            startPerAcc += separator;
                            dummyStartPerAcc += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL + startPerpetualAccess[0] + separator;
                        } else {
                            dummyStartPerAcc += separator;
                        }
                        if (startPerpetualAccess.length > 1 && !startPerpetualAccess[1].isEmpty()) {
                            startPerAcc += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE;
                            startPerAcc += startPerpetualAccess[1];
                            startPerAcc += separator;
                            dummyStartPerAcc += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE + startPerpetualAccess[1] + separator;
                        } else {
                            dummyStartPerAcc += separator;
                        }
                        if (startPerpetualAccess.length > 2 && !startPerpetualAccess[2].isEmpty()) {
                            startPerAcc += OLEConstants.OLEEResourceRecord.SPACE + startPerpetualAccess[2] + OLEConstants.OLEEResourceRecord.SPACE;
                            startPerAcc += separator;
                            dummyStartPerAcc += startPerpetualAccess[2] + separator;
                        } else {
                            dummyStartPerAcc += separator;
                        }
                        /*for (String perAccDate : startPerpetualAccess) {
                            if (perAccDate != null && !perAccDate.isEmpty()) {
                                startPerAcc += perAccDate;
                                startPerAcc += getParameter(OLEConstants.OLEEResourceRecord.DEFAULT_PERPETUAL_ACCESS_SEPARATOR);
                            }
                        }*/
                        startPerAcc = startPerAcc.substring(0, startPerAcc.lastIndexOf(separator));
                    } else {
                        startPerAcc = "";
                    }
                }
                if (defaultPerAccDates[1].contains(separator)) {
                    String[] endPerpetualAcc = defaultPerAccDates[1].split(separator);
                    if (endPerpetualAcc.length > 0) {
                        if (endPerpetualAcc.length > 0 && !endPerpetualAcc[0].isEmpty()) {
                            endPerAcc += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL;
                            endPerAcc += endPerpetualAcc[0];
                            endPerAcc += separator;
                            dummyEndPerAcc += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL + endPerpetualAcc[0] + separator;
                        } else {
                            dummyEndPerAcc += separator;
                        }
                        if (endPerpetualAcc.length > 1 && !endPerpetualAcc[1].isEmpty()) {
                            endPerAcc += OLEConstants.OLEEResourceRecord.SPACE + OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE;
                            endPerAcc += endPerpetualAcc[1];
                            endPerAcc += separator;
                            dummyEndPerAcc += OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE + endPerpetualAcc[1] + separator;
                        } else {
                            dummyEndPerAcc += separator;
                        }
                        if (endPerpetualAcc.length > 2 && !endPerpetualAcc[2].isEmpty()) {
                            endPerAcc += OLEConstants.OLEEResourceRecord.SPACE + endPerpetualAcc[2];
                            endPerAcc += separator;
                            dummyEndPerAcc += endPerpetualAcc[2] + separator;
                        } else {
                            dummyEndPerAcc += separator;
                        }
                        /*for (String perAccDate : endPerpetualAcc) {
                            if (perAccDate != null && !perAccDate.isEmpty()) {
                                endPerAcc += perAccDate;
                                endPerAcc += getParameter(OLEConstants.OLEEResourceRecord.DEFAULT_PERPETUAL_ACCESS_SEPARATOR);
                            }
                        }*/
                        endPerAcc = endPerAcc.substring(0, endPerAcc.lastIndexOf(separator));
                    } else {
                        endPerAcc = "";
                    }
                }
            }
            if ((endPerAcc != null && !endPerAcc.isEmpty()) && (startPerAcc != null && !startPerAcc.isEmpty())) {
                oleERSDoc.setDefaultPerpetualAccessView(startPerAcc + dateSeparator + endPerAcc);
                oleERSDoc.setDummyDefaultPerpetualAccess(dummyStartPerAcc + dateSeparator + dummyEndPerAcc);
            } else if (startPerAcc != null && !startPerAcc.isEmpty()) {
                oleERSDoc.setDefaultPerpetualAccessView(startPerAcc);
                oleERSDoc.setDummyDefaultPerpetualAccess(dummyStartPerAcc);
            } else if (endPerAcc != null && !endPerAcc.isEmpty()) {
                oleERSDoc.setDefaultPerpetualAccessView(endPerAcc);
                oleERSDoc.setDummyDefaultPerpetualAccess(dummyEndPerAcc);
            } else {
                oleERSDoc.setDefaultPerpetualAccessView(null);
                oleERSDoc.setDummyDefaultPerpetualAccess(null);
            }

        }
    }

    public OLEEResourceRecordDocument saveDefaultCoverageDate(OLEEResourceRecordDocument oleeResourceRecordDocument) {
        StringBuffer defaultCoverageDate = new StringBuffer();
        OLEEResourceInstance oleERSInstance = oleeResourceRecordDocument.getOleERSInstance();
        String separator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR);
        if (oleERSInstance != null && oleERSInstance.getCovStartDate() != null) {
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovStartVolume()) ? oleERSInstance.getCovStartVolume() + separator : separator);
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovStartIssue()) ? oleERSInstance.getCovStartIssue() + separator : separator);
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovStartDate()) ? oleERSInstance.getCovStartDate() : "");
            if (defaultCoverageDate.length() > 0) {
                defaultCoverageDate.append(getParameter(OLEConstants.OLEEResourceRecord.COVERAGE_DATE_SEPARATOR));
            }
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovEndVolume()) ? oleERSInstance.getCovEndVolume() + separator : separator);
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovEndIssue()) ? oleERSInstance.getCovEndIssue() + separator : separator);
            defaultCoverageDate.append(StringUtils.isNotEmpty(oleERSInstance.getCovEndDate()) ? oleERSInstance.getCovEndDate() : "");
            if (StringUtils.isNotEmpty(defaultCoverageDate)) {
                oleeResourceRecordDocument.setDefaultCoverage(defaultCoverageDate.toString());
            }
        }
        return oleeResourceRecordDocument;
    }

    public OLEEResourceRecordDocument saveDefaultPerpetualAccessDate(OLEEResourceRecordDocument oleeResourceRecordDocument) {
        StringBuffer defaultPerpetualDate = new StringBuffer();
        OLEEResourceInstance oleERSInstance = oleeResourceRecordDocument.getOleERSInstance();
        String separator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR);
        if (oleERSInstance != null && oleERSInstance.getPerpetualAccStartDate() != null) {
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccStartVolume()) ? oleERSInstance.getPerpetualAccStartVolume() + separator : separator);
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccStartIssue()) ? oleERSInstance.getPerpetualAccStartIssue() + separator : separator);
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccStartDate()) ? oleERSInstance.getPerpetualAccStartDate() : "");
            if (defaultPerpetualDate.length() > 0) {
                defaultPerpetualDate.append(getParameter(OLEConstants.OLEEResourceRecord.PERPETUAL_ACCESS_DATE_SEPARATOR));
            }
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccEndVolume()) ? oleERSInstance.getPerpetualAccEndVolume() + separator : separator);
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccEndIssue()) ? oleERSInstance.getPerpetualAccEndIssue() + separator : separator);
            defaultPerpetualDate.append(StringUtils.isNotEmpty(oleERSInstance.getPerpetualAccEndDate()) ? oleERSInstance.getPerpetualAccEndDate() : "");
            if (StringUtils.isNotEmpty(defaultPerpetualDate)) {
                oleeResourceRecordDocument.setDefaultPerpetualAccess(defaultPerpetualDate.toString());
            }
        }
        return oleeResourceRecordDocument;
    }

    public void getNewInstance(OLEEResourceRecordDocument oleERSDoc, String documentNumber) throws Exception {

        if (OleDocstoreResponse.getInstance().getEditorResponse() != null) {
            HashMap<String, OLEEditorResponse> oleEditorResponses = OleDocstoreResponse.getInstance().getEditorResponse();
            OLEEditorResponse oleEditorResponse = oleEditorResponses.get(documentNumber);
            String separator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR);
            String isbnAndissn = "";
            List<String> instanceId = new ArrayList<String>();
            List<OLEEResourceInstance> oleeResourceInstances = oleERSDoc.getOleERSInstances();
            if (oleeResourceInstances.size() == 0) {
                oleeResourceInstances = new ArrayList<OLEEResourceInstance>();
            }
            // List<OleCopy> copyList = new ArrayList<>();
            //getDocstoreClientLocator().getDocstoreClient().retrieveBibTree(oleEditorResponse.getBib().getId());

            if (oleEditorResponse != null && StringUtils.isNotEmpty(oleEditorResponse.getLinkedInstanceId())) {
                instanceId.add(oleEditorResponse.getLinkedInstanceId());
            }
            Holdings holdings = null;
            if (oleEditorResponse != null && oleERSDoc.getSelectInstance() != null && (oleERSDoc.getSelectInstance().equals(OLEConstants.OLEEResourceRecord.LINK_EXIST_INSTANCE)) || oleERSDoc.getSelectInstance().equals(OLEConstants.OLEEResourceRecord.CREATE_NEW_INSTANCE)) {
                holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(oleEditorResponse.getLinkedInstanceId());

            }
            int index = -1;
            if (holdings != null && holdings.getId() != null) {
                HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
                OleHoldings oleHoldings = holdingOlemlRecordProcessor.fromXML(holdings.getContent());
                if (holdings instanceof org.kuali.ole.docstore.common.document.EHoldings) {
                    if (oleEditorResponse != null && oleEditorResponse.getLinkedInstanceId().equalsIgnoreCase(holdings.getId())) {
                        OLEEResourceInstance oleeResourceInstance = new OLEEResourceInstance();
                        if (oleERSDoc.getOleERSInstances() != null && oleERSDoc.getOleERSInstances().size() > 0) {
                            for (OLEEResourceInstance eResourceInstance : oleeResourceInstances) {
                                if (eResourceInstance.getInstanceId().equals(oleEditorResponse.getLinkedInstanceId())) {
                                    index = oleeResourceInstances.indexOf(eResourceInstance);
                                    oleeResourceInstance = eResourceInstance;
                                }
                            }
                        }
                        oleeResourceInstance.setInstanceTitle(holdings.getBib().getTitle());
                        getHoldingsField(oleeResourceInstance, oleHoldings);
                        oleeResourceInstance.setInstancePublisher(oleHoldings.getPublisher());
                        oleeResourceInstance.setPlatForm(oleHoldings.getPlatform().getPlatformName());
                        // oleeResourceInstance.setPublicDisplayNote(workEInstanceDocument.getPublicDisplayNote());
                        StringBuffer urls = new StringBuffer();
                        for(Link link :oleHoldings.getLink()){
                            urls.append(link.getUrl());
                            urls.append(",");
                        }
                        if(urls.toString().contains(",")){
                            String url = urls.substring(0,urls.lastIndexOf(","));
                            oleeResourceInstance.setUrl(url);
                        }

                        SearchParams searchParams = new SearchParams();
                        searchParams.getSearchConditions().add(searchParams.buildSearchCondition(null, searchParams.buildSearchField(OLEConstants.BIB_DOC_TYPE, OLEConstants.BIB_SEARCH, holdings.getBib().getId()), null));
                        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField(OLEConstants.BIB_DOC_TYPE, OLEConstants.OLEEResourceRecord.ERESOURCE_ISBN));
                        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField(OLEConstants.BIB_DOC_TYPE, OLEConstants.OLEEResourceRecord.ERESOURCE_ISSN));
                        SearchResponse searchResponse = getDocstoreClientLocator().getDocstoreClient().search(searchParams);
                        SearchResult searchResult;
                        if (searchResponse.getSearchResults().size() > 0) {
                            searchResult = searchResponse.getSearchResults().get(0);
                            searchResult.getSearchResultFields();
                            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                                isbnAndissn += searchResultField.getFieldValue();
                                isbnAndissn += separator;
                            }
                        }
                        if (StringUtils.isNotEmpty(isbnAndissn)) {
                            isbnAndissn = isbnAndissn.substring(0, isbnAndissn.lastIndexOf(separator));
                        }
                        oleeResourceInstance.setIsbn(isbnAndissn);
                        oleeResourceInstance.setStatus(oleHoldings.getAccessStatus());
                        oleeResourceInstance.setSubscriptionStatus(oleHoldings.getSubscriptionStatus());
                        oleeResourceInstance.setBibId(holdings.getBib().getId());
                        oleeResourceInstance.setInstanceId(holdings.getId());
                        oleeResourceInstance.setInstanceFlag("false");
                        if (index >= 0) {
                            oleeResourceInstances.add(index, oleeResourceInstance);
                        } else {
                            oleeResourceInstances.add(oleeResourceInstance);
                        }
                        updateEResInOleCopy(holdings, oleERSDoc);
                    }
                }
                if (holdings instanceof org.kuali.ole.docstore.common.document.PHoldings) {
                    if (oleEditorResponse != null && oleEditorResponse.getLinkedInstanceId().equalsIgnoreCase(holdings.getId())) {
                        OLEEResourceInstance oleeResourceInstance = new OLEEResourceInstance();
                        if (oleERSDoc.getOleERSInstances() != null && oleERSDoc.getOleERSInstances().size() > 0) {
                            for (OLEEResourceInstance eResourceInstance : oleeResourceInstances) {
                                if (eResourceInstance.getInstanceId().equals(oleEditorResponse.getLinkedInstanceId())) {
                                    index = oleeResourceInstances.indexOf(eResourceInstance);
                                    oleeResourceInstance = eResourceInstance;
                                }
                            }
                        }
                        oleeResourceInstance.setInstanceTitle(holdings.getBib().getTitle());
                        oleeResourceInstance.setInstancePublisher(holdings.getBib().getPublisher());
                        SearchParams searchParams = new SearchParams();
                        searchParams.getSearchConditions().add(searchParams.buildSearchCondition(null, searchParams.buildSearchField(OLEConstants.BIB_DOC_TYPE, OLEConstants.BIB_SEARCH, holdings.getBib().getId()), null));
                        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField(OLEConstants.BIB_DOC_TYPE, OLEConstants.OLEEResourceRecord.ERESOURCE_ISBN));
                        searchParams.getSearchResultFields().add(searchParams.buildSearchResultField(OLEConstants.BIB_DOC_TYPE, OLEConstants.OLEEResourceRecord.ERESOURCE_ISSN));
                        SearchResponse searchResponse = getDocstoreClientLocator().getDocstoreClient().search(searchParams);
                        SearchResult searchResult;
                        if (searchResponse.getSearchResults().size() > 0) {
                            searchResult = searchResponse.getSearchResults().get(0);
                            searchResult.getSearchResultFields();
                            for (SearchResultField searchResultField : searchResult.getSearchResultFields()) {
                                isbnAndissn += searchResultField.getFieldValue();
                                isbnAndissn += separator;
                            }
                        }
                        if (StringUtils.isNotEmpty(isbnAndissn)) {
                            isbnAndissn = isbnAndissn.substring(0, isbnAndissn.lastIndexOf(separator));
                        }
                        oleeResourceInstance.setIsbn(isbnAndissn);
                        oleeResourceInstance.setBibId(holdings.getBib().getId());
                        oleeResourceInstance.setInstanceId(holdings.getId());
                        oleeResourceInstance.setHoldingsId(oleHoldings.getHoldingsIdentifier());
                        oleeResourceInstance.setInstanceFlag("true");
                        if (index >= 0) {
                            oleeResourceInstances.add(index, oleeResourceInstance);
                        } else {
                            oleeResourceInstances.add(oleeResourceInstance);
                        }
                        updateEResInOleCopy(holdings, oleERSDoc);
                    }
                }
            }
            oleERSDoc.setOleERSInstances(oleeResourceInstances);
            OleDocstoreResponse.getInstance().setEditorResponse(null);
        }
    }

    public void getPOInvoiceForERS(OLEEResourceRecordDocument oleERSDoc) {
        try {
            Holdings holdings = null;
            List<OLEEResourcePO> oleeResourcePOs = new ArrayList<>();
            List<OLEEResourceInvoices> oleeResourceInvoiceses = new ArrayList<>();
            for (OLEEResourceInstance oleeResourceInstance : oleERSDoc.getOleERSInstances()) {
                holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(oleeResourceInstance.getInstanceId());
                if (holdings != null) {
                    Map<String, String> criteriaMap = new HashMap<String, String>();
                    criteriaMap.put(OLEConstants.INSTANCE_ID, holdings.getId());
                    List<OleCopy> copies = (List<OleCopy>) getBusinessObjectService().findMatching(OleCopy.class,
                            criteriaMap);
                    for (OleCopy copy : copies) {
                        if (copy.getPoItemId() != null) {
                            OLEEResourcePO oleeResourcePO = new OLEEResourcePO();
                            oleeResourcePO.setTitle(holdings.getBib().getTitle());
                            Map<String, String> criteriaPOIdMap = new HashMap<String, String>();
                            criteriaPOIdMap.put(OLEConstants.OLEEResourceRecord.PO_ITEM_ID, copy.getPoItemId().toString());
                            List<OlePurchaseOrderItem> olePurchaseOrderItems = (List<OlePurchaseOrderItem>) getBusinessObjectService().findMatching(OlePurchaseOrderItem.class, criteriaPOIdMap);
                            if (olePurchaseOrderItems.size() > 0) {
                                for (OlePurchaseOrderItem olePurchaseOrderItem : olePurchaseOrderItems) {
                                    Map map = new HashMap();
                                    map.put(OLEConstants.DOC_NUM, olePurchaseOrderItem.getDocumentNumber());
                                    OlePurchaseOrderDocument olePurchaseOrderDocument = getBusinessObjectService().findByPrimaryKey(OlePurchaseOrderDocument.class, map);
                                    if (olePurchaseOrderDocument != null) {
                                        oleeResourcePO.setOlePOItemId(olePurchaseOrderDocument.getPurapDocumentIdentifier());
                                        Integer poCreatedYear = olePurchaseOrderDocument.getPostingYear();
                                        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                        if (currentYear.compareTo(poCreatedYear) == 0) {
                                            oleeResourcePO.setPaidAmountCurrentFY(olePurchaseOrderItem.getItemInvoicedTotalAmount().intValue());
                                        } else if (currentYear.compareTo(poCreatedYear) == 1) {
                                            oleeResourcePO.setPaidAmountPreviousFY(olePurchaseOrderItem.getItemInvoicedTotalAmount().intValue());
                                        } else if (currentYear.compareTo(poCreatedYear) > 1) {
                                            oleeResourcePO.setPaidAmountTwoYearsPreviousFY(olePurchaseOrderItem.getItemInvoicedTotalAmount().intValue());
                                        }
                                    }
                                }
                            }
                            oleeResourcePOs.add(oleeResourcePO);


                            Map<String, String> criteriaInvIdMap = new HashMap<String, String>();
                            criteriaInvIdMap.put(OLEConstants.OLEEResourceRecord.INV_PO_ITEM_ID, copy.getPoItemId().toString());
                            List<OleInvoiceItem> oleInvoiceItems = (List<OleInvoiceItem>) getBusinessObjectService().findMatching(OleInvoiceItem.class, criteriaInvIdMap);
                            if (oleInvoiceItems.size() > 0) {
                                for (OleInvoiceItem oleInvoiceItem : oleInvoiceItems) {
                                    OLEEResourceInvoices oleEResInvoice = new OLEEResourceInvoices();
                                    oleEResInvoice.setInvoiceId(oleInvoiceItem.getItemIdentifier().toString());
                                    OleInvoiceDocument oleInvoiceDocument = (OleInvoiceDocument) oleInvoiceItem.getInvoiceDocument();
                                    if (oleInvoiceItem.getInvoiceDocument() != null) {
                                        oleEResInvoice.setInvoiceNumber(oleInvoiceItem.getInvoiceDocument().getDocumentNumber());
                                        oleEResInvoice.setInvoiceDate(oleInvoiceItem.getInvoiceDocument().getInvoiceDate());
                                        oleEResInvoice.setVendorName(oleInvoiceItem.getInvoiceDocument().getVendorName());
                                        if (SpringContext.getBean(DocumentHeaderService.class) != null) {
                                            oleInvoiceDocument.setDocumentHeader(SpringContext.getBean(DocumentHeaderService.class).getDocumentHeaderById(oleInvoiceDocument.getDocumentNumber()));
                                            oleEResInvoice.setInvoiceStatus(oleInvoiceDocument.getApplicationDocumentStatus());
                                        }
                                    }
                                    oleEResInvoice.setInvoicedAmount(oleInvoiceItem.getExtendedPrice().toString());
                                    Map map = new HashMap();
                                    map.put(OLEConstants.OLEEResourceRecord.INV_PO_ITEM_ID, oleInvoiceItem.getItemIdentifier());
                                    OlePaymentRequestItem olePaymentRequestItem = getBusinessObjectService().findByPrimaryKey(OlePaymentRequestItem.class, map);
                                    if (olePaymentRequestItem != null) {
                                        oleEResInvoice.setPaidDate(olePaymentRequestItem.getPaymentRequestDocument().getPaymentRequestPayDate());
                                    }
                                    StringBuffer fundCode = new StringBuffer();
                                    if (oleInvoiceItem.getSourceAccountingLines() != null && oleInvoiceItem.getSourceAccountingLines().size() > 0) {
                                        for (PurApAccountingLine accountingLine : oleInvoiceItem.getSourceAccountingLines()) {
                                            map.clear();
                                            map.put(OLEConstants.ACCOUNT_NUMBER, accountingLine.getAccountNumber());
                                            map.put(OLEConstants.OBJECT_CODE, accountingLine.getFinancialObjectCode());
                                            OleVendorAccountInfo oleVendorAccountInfo = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(OleVendorAccountInfo.class, map);
                                            if (oleVendorAccountInfo != null) {
                                                fundCode.append(oleVendorAccountInfo.getVendorRefNumber());
                                                fundCode.append(OLEConstants.COMMA);
                                                fundCode.append(' ');
                                            }
                                        }
                                    }
                                    if (fundCode.length() > 0) {
                                        fundCode.deleteCharAt(fundCode.length() - 2);
                                        oleEResInvoice.setFundCode(fundCode.toString());
                                    }
                                    oleeResourceInvoiceses.add(oleEResInvoice);
                                }
                            }
                        }
                    }
                }
            }
            oleERSDoc.setOleERSPOItems(oleeResourcePOs);
            oleERSDoc.setOleERSInvoices(oleeResourceInvoiceses);
        } catch (Exception e) {
            LOG.error("Exception " + e);
        }
    }

    private void getHoldingsField(OLEEResourceInstance oleeResourceInstance, OleHoldings oleHoldings) {
        Map<String,String> map=new HashMap();
        String start="";
        String end="";
        String holdings = "";
        String startDate = "";
        String endDate = "";
        String space = OLEConstants.OLEEResourceRecord.SPACE;
        String separator = getParameter(OLEConstants.OLEEResourceRecord.COVERAGE_DATE_SEPARATOR);
        String commaSeparator = getParameter(OLEConstants.OLEEResourceRecord.COMMA_SEPARATOR) + space;
        map.put("oleERSIdentifier",oleHoldings.getEResourceId());
        List<OLEEResourceRecordDocument> oleeResourceRecordDocuments = (List<OLEEResourceRecordDocument>) getBusinessObjectService().findMatching(OLEEResourceRecordDocument.class, map);
        if (oleeResourceRecordDocuments.size() > 0) {
            String defaultCoverage = oleeResourceRecordDocuments.get(0).getDefaultCoverage();
            if (defaultCoverage != null) {
                String[] defaultCovDates = defaultCoverage.split("-");
                String defCovStartDat = defaultCovDates.length > 0 ? defaultCovDates[0] : "";
                if (!defCovStartDat.isEmpty()) {
                    String[] covStartDate = defCovStartDat.split(",");
                    if (covStartDate.length > 2 && !covStartDate[2].isEmpty()) {
                        start = covStartDate[2];
                    }
                }
                String defCovEndDat = defaultCovDates.length > 1 ? defaultCovDates[1] : "";
                if (!defCovEndDat.isEmpty()) {
                    String[] covEndDate = defCovEndDat.split(",");
                    if (covEndDate.length > 2 && !covEndDate[2].isEmpty()) {
                        end = covEndDate[2];
                    }
                }
            }
        }
        if (oleHoldings.getExtentOfOwnership().size() > 0 && oleHoldings.getExtentOfOwnership().get(0).getCoverages() != null) {
            List<Coverage> coverageDates = oleHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage();
            if (coverageDates.size() > 0) {
                for (Coverage coverageDate : coverageDates) {
                    startDate = coverageDate.getCoverageStartDate();
                    endDate = coverageDate.getCoverageEndDate();
                    if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                        holdings += startDate + space + separator + space + endDate;
                        holdings += commaSeparator;
                    } else if (startDate != null && !startDate.isEmpty()) {
                        holdings += startDate + space + separator + space + end;
                        holdings += commaSeparator;
                    } else if (endDate != null && !endDate.isEmpty()) {
                        holdings += start + space + separator + space + endDate;
                        holdings += commaSeparator;
                    }
                }
            }
        } else {
            if (start != null && !start.isEmpty() && end != null && !end.isEmpty()) {
                holdings += start + space + separator + space + end;
                holdings += commaSeparator;
            }
            else if(start != null && !start.isEmpty()){
                holdings += start + space + separator + space + end;
                holdings += commaSeparator;
            }
            else if(end != null && !end.isEmpty()){
                holdings += start + space + separator + space + end;
                holdings += commaSeparator;
            }
        }
        if (holdings != null && !holdings.isEmpty()) {
            holdings = holdings.substring(0, holdings.lastIndexOf(commaSeparator));
        }
        oleeResourceInstance.setInstanceHoldings(holdings);
    }

    @Override
    public void getDefaultCovDatesToPopup(OLEEResourceRecordDocument oleeResourceRecordDocument, String defaultCov) {
        String[] defaultCovDates = defaultCov.split("-");
        String defCovStartDat = defaultCovDates.length > 0 ? defaultCovDates[0] : "";
        if (oleeResourceRecordDocument.getOleERSInstance() != null) {
            if (!defCovStartDat.isEmpty()) {
                String[] covStartDate = defCovStartDat.split(",");
                if (covStartDate.length > 0 && !covStartDate[0].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovStartVolume(covStartDate[0].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL) ? covStartDate[0].substring(7, covStartDate[0].length()) : "");
                }
                if (covStartDate.length > 1 && !covStartDate[1].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovStartIssue(covStartDate[1].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE) ? covStartDate[1].substring(6, covStartDate[1].length()) : "");
                }
                if (covStartDate.length > 2 && !covStartDate[2].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovStartDate(covStartDate[2]);
                    oleeResourceRecordDocument.setCovStartDate(oleeResourceRecordDocument.getOleERSInstance().getCovStartDate());
                }
            }
            String defCovEndDat = defaultCovDates.length > 1 ? defaultCovDates[1] : "";
            if (!defCovEndDat.isEmpty()) {
                String[] covEndDate = defCovEndDat.split(",");
                if (covEndDate.length > 0 && !covEndDate[0].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovEndVolume(covEndDate[0].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL) ? covEndDate[0].substring(7, covEndDate[0].length()) : "");
                }
                if (covEndDate.length > 1 && !covEndDate[1].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovEndIssue(covEndDate[1].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE) ? covEndDate[1].substring(6, covEndDate[1].length()) : "");
                }
                if (covEndDate.length > 2 && !covEndDate[2].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setCovEndDate(covEndDate[2]);
                    oleeResourceRecordDocument.setCovEndDate(oleeResourceRecordDocument.getOleERSInstance().getCovEndDate());
                }
            }
        }
    }

    @Override
    public void getDefaultPerAccDatesToPopup(OLEEResourceRecordDocument oleeResourceRecordDocument, String defaultPerpetualAcc) {
        String[] defaultPerAccDates = defaultPerpetualAcc.split("-");
        String defPerAccStartDat = defaultPerAccDates.length > 0 ? defaultPerAccDates[0] : "";
        if (oleeResourceRecordDocument.getOleERSInstance() != null) {
            if (!defPerAccStartDat.isEmpty()) {
                String[] perAccStartDate = defPerAccStartDat.split(",");
                if (perAccStartDate.length > 0 && !perAccStartDate[0].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccStartVolume(perAccStartDate[0].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL) ? perAccStartDate[0].substring(7, perAccStartDate[0].length()) : "");
                }
                if (perAccStartDate.length > 1 && !perAccStartDate[1].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccStartIssue(perAccStartDate[1].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE) ? perAccStartDate[1].substring(6, perAccStartDate[1].length()) : "");
                }
                if (perAccStartDate.length > 2 && !perAccStartDate[2].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccStartDate(perAccStartDate[2]);
                    oleeResourceRecordDocument.setPerAccStartDate(oleeResourceRecordDocument.getOleERSInstance().getPerpetualAccStartDate());
                }
            }
            String defPerAccEndDat = defaultPerAccDates.length > 1 ? defaultPerAccDates[1] : "";
            if (!defPerAccEndDat.isEmpty()) {
                String[] perAccEndDate = defPerAccEndDat.split(",");
                if (perAccEndDate.length > 0 && !perAccEndDate[0].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccEndVolume(perAccEndDate[0].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_VOL) ? perAccEndDate[0].substring(7, perAccEndDate[0].length()) : "");
                }
                if (perAccEndDate.length > 1 && !perAccEndDate[1].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccEndIssue(perAccEndDate[1].contains(OLEConstants.OLEEResourceRecord.DEFAULT_DATE_ISSUE) ? perAccEndDate[1].substring(6, perAccEndDate[1].length()) : "");
                }
                if (perAccEndDate.length > 2 && !perAccEndDate[2].isEmpty()) {
                    oleeResourceRecordDocument.getOleERSInstance().setPerpetualAccEndDate(perAccEndDate[2]);
                    oleeResourceRecordDocument.setPerAccEndDate(oleeResourceRecordDocument.getOleERSInstance().getPerpetualAccEndDate());
                }
            }
        }
    }

    public boolean validateEResourceDocument(OLEEResourceRecordDocument oleeResourceRecordDocument) {
        boolean flag = false;
        boolean isSelectorBlank = false;
        boolean isRequestorBlank = false;
        boolean isReqSelCommentBlank = false;
        if (oleeResourceRecordDocument.getOleMaterialTypes().size() == 0) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(OLEConstants.OLEEResourceRecord.DOCUMENT_MATERIAL_TYPES, OLEConstants.OLEEResourceRecord.MATERIAL_TYPE_REQUIRED, new String[]{"Material Type"});
            flag = true;
        }
        if (oleeResourceRecordDocument.getOleFormatTypes().size() == 0) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(OLEConstants.OLEEResourceRecord.DOCUMENT_FORMAT_TYPES, OLEConstants.OLEEResourceRecord.FORMAT_TYPE_REQUIRED, new String[]{"Format Type"});
            flag = true;
        }
        if (oleeResourceRecordDocument.getOleContentTypes().size() == 0) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(OLEConstants.OLEEResourceRecord.DOCUMENT_CONTENT_TYPES, OLEConstants.OLEEResourceRecord.CONTENT_TYPE_REQUIRED, new String[]{"Content Type"});
            flag = true;
        }
        if (oleeResourceRecordDocument.getRequestors().size() > 0) {
            for (OLEEResourceRequestor oleeResourceRequestor : oleeResourceRecordDocument.getRequestors()) {
                if (oleeResourceRequestor.getRequestorId() == null || oleeResourceRequestor.getRequestorId().equalsIgnoreCase("")) {
                    if (oleeResourceRecordDocument.getRequestors().size() != 1) {
                        isRequestorBlank = true;
                    }
                }
            }
            if (isRequestorBlank) {
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEEResourceRecord.REQUESTOR_SECTION_ID, OLEConstants.OLEEResourceRecord.SHOULD_NOT_BLANK, new String[]{"Requestor Type"});
                flag = true;
            }
        }
        if (oleeResourceRecordDocument.getSelectors().size() > 0) {
            for (OLEEResourceSelector oleeResourceSelector : oleeResourceRecordDocument.getSelectors()) {
                if (oleeResourceSelector.getSelectorId() == null || oleeResourceSelector.getSelectorId().equalsIgnoreCase("")) {
                    if (oleeResourceRecordDocument.getSelectors().size() != 1) {
                        isSelectorBlank = true;
                    }

                }
            }
            if (isSelectorBlank) {
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEEResourceRecord.SELECTOR_SECTION_ID, OLEConstants.OLEEResourceRecord.SHOULD_NOT_BLANK, new String[]{"Selector Type"});
                flag = true;
            }
        }
        if (oleeResourceRecordDocument.getReqSelComments().size() > 0) {
            for (OLEEResourceReqSelComments oleeResourceReqSelComments : oleeResourceRecordDocument.getReqSelComments()) {
                if (oleeResourceReqSelComments.getOleReqSelComments() == null || oleeResourceReqSelComments.getOleReqSelComments().equalsIgnoreCase("")) {
                    if (oleeResourceRecordDocument.getReqSelComments().size() != 1) {
                        isReqSelCommentBlank = true;
                    }
                }
            }
            if (isReqSelCommentBlank) {
                GlobalVariables.getMessageMap().putErrorForSectionId(OLEConstants.OLEEResourceRecord.REQUESTOR_SELECTOR_COMMENT_SECTION_ID, OLEConstants.OLEEResourceRecord.SHOULD_NOT_BLANK, new String[]{"RequestorSelectorComment"});
                flag = true;
            }
        }
        return flag;
    }

    public void saveEResourceInstanceToDocstore(OLEEResourceRecordDocument oleeResourceRecordDocument) throws Exception {
        if (oleeResourceRecordDocument.getOleERSInstances() != null && oleeResourceRecordDocument.getOleERSInstances().size() != 0) {
            List<OLEEResourceInstance> oleeResourceInstanceList = new ArrayList<OLEEResourceInstance>();
            oleeResourceInstanceList = oleeResourceRecordDocument.getOleERSInstances();
            for (OLEEResourceInstance oleeResourceInstance : oleeResourceInstanceList) {
                HoldingOlemlRecordProcessor holdingOlemlRecordProcessor = new HoldingOlemlRecordProcessor();
                String eHoldingsId = oleeResourceInstance.getInstanceId();
                Holdings holdings = new EHoldings();
                holdings = getDocstoreClientLocator().getDocstoreClient().retrieveHoldings(eHoldingsId);
                OleHoldings eHoldings = holdingOlemlRecordProcessor.fromXML(holdings.getContent());
                eHoldings.setEResourceId(oleeResourceRecordDocument.getOleERSIdentifier());
                StatisticalSearchingCode statisticalSearchingCode = new StatisticalSearchingCode();
                if (oleeResourceRecordDocument.getOleStatisticalCode() != null) {
                    statisticalSearchingCode.setCodeValue(oleeResourceRecordDocument.getOleStatisticalCode().getStatisticalSearchingCode());
                }
                if(eHoldings.getStatisticalSearchingCode()==null||eHoldings.getStatisticalSearchingCode().getCodeValue()==null){
                eHoldings.setStatisticalSearchingCode(statisticalSearchingCode);
                }
                if (eHoldings != null && eHoldings.getHoldingsAccessInformation() == null && oleeResourceRecordDocument != null) {
                    eHoldings.getHoldingsAccessInformation().setNumberOfSimultaneousUser(oleeResourceRecordDocument.getNumOfSimultaneousUsers());
                    eHoldings.getHoldingsAccessInformation().setAccessLocation(oleeResourceRecordDocument.getAccessLocationId());
                    eHoldings.getHoldingsAccessInformation().setAuthenticationType(oleeResourceRecordDocument.getOleAuthenticationType().getOleAuthenticationTypeName());
                }
                getHoldingsField(oleeResourceInstance,eHoldings);
                holdings.setId(eHoldingsId);
                holdings.setContent(holdingOlemlRecordProcessor.toXML(eHoldings));
                getDocstoreClientLocator().getDocstoreClient().updateHoldings(holdings);
            }
        }
    }

    public boolean validateCoverageStartDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm) {
        boolean coverageStartFlag = true;
        OLEEResourceInstance oleeResourceInstance = oleeResourceRecordDocument.getOleERSInstance();
        oleERSForm.setDefaultCovStartDateErrorMessage(null);
        String coverageStartDate = "";
        try {
            if (oleeResourceInstance != null) {
                coverageStartDate = oleeResourceInstance.getCovStartDate();
                if (StringUtils.isNotEmpty(coverageStartDate)) {
                    if (coverageStartDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(coverageStartDate);
                        String dateFormat = coverageStartDate;
                        oleeResourceInstance.setCovStartDate(dateFormat);
                    } else if (coverageStartDate.matches(calendarYearAgo)) {
                        String[] coverageStartYear = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartYear.length > 0 && coverageStartYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfCovDate = getYearFormat();
                            previousYearOfCovDate = getFirstDay(previousYearOfCovDate);*/
                            String previousYearOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousYearOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(calendarYearsAgo)) {
                        String[] coverageStartYears = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartYears.length > 0 && !coverageStartYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfCovDate = getYearsFormat(coverageStartYears);
                            previousYearsOfCovDate = getFirstDay(previousYearsOfCovDate);*/
                            String previousYearsOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousYearsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(monthAgo)) {
                        String[] coverageStartMonth = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartMonth.length > 0 && coverageStartMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfCovDate = getMonthFormat();
                            String previousMonthOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousMonthOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(monthsAgo)) {
                        String[] coverageStartMonths = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartMonths.length > 0 && !coverageStartMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfCovDate = getMonthsFormat(coverageStartMonths);
                            String previousMonthsOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousMonthsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(weekAgo)) {
                        String[] coverageStartWeek = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartWeek.length > 0 && coverageStartWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovDate = getWeekFormat();
                            String previousWeekOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousWeekOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(weeksAgo)) {
                        String[] coverageStartWeeks = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartWeeks.length > 0 && !coverageStartWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousYearsOfCovDate = getWeeksFormat(coverageStartWeeks);
                            String previousYearsOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousYearsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(dayAgo)) {
                        String[] coverageStartDay = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartDay.length > 0 && coverageStartDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfCovDate = getDayFormat();
                            String previousDayOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousDayOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else if (coverageStartDate.matches(daysAgo)) {
                        String[] coverageStartDays = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartDays.length > 0 && !coverageStartDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfCovDate = getDaysFormat(coverageStartDays);
                            String previousDaysOfCovDate = coverageStartDate;
                            oleeResourceInstance.setCovStartDate(previousDaysOfCovDate);
                        } else {
                            coverageStartFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                        }
                    } else {
                        coverageStartFlag = false;
                        oleERSForm.setCoverageFlag(true);
                        oleERSForm.setDefaultCovStartDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_START_DATE_FORMAT_INV);
                    }
                }
            }
            oleeResourceRecordDocument.setCovStartDate(coverageStartDate);
        } catch (Exception ex) {
            LOG.error("Exception while validating the coverage start date format in EResource" + ex.getMessage());
            throw new RuntimeException();
        }
        return coverageStartFlag;
    }

    public boolean validateCoverageEndDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm) {
        boolean coverageEndFlag = true;
        OLEEResourceInstance oleeResourceInstance = oleeResourceRecordDocument.getOleERSInstance();
        oleERSForm.setDefaultCovEndDateErrorMessage(null);
        String coverageEndDate = "";
        try {
            if (oleeResourceInstance != null) {
                coverageEndDate = oleeResourceInstance.getCovEndDate();
                if (StringUtils.isNotEmpty(coverageEndDate)) {
                    if (coverageEndDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(coverageEndDate);
                        String dateFormat = coverageEndDate;
                        oleeResourceInstance.setCovEndDate(dateFormat);
                    } else if (coverageEndDate.matches(calendarYearAgo)) {
                        String[] coverageEndYear = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndYear.length > 0 && coverageEndYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfCovDate = getYearFormat();
                            previousYearOfCovDate = getLastDay(previousYearOfCovDate);*/
                            String previousYearOfCovDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousYearOfCovDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(calendarYearsAgo)) {
                        String[] coverageEndYears = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndYears.length > 0 && !coverageEndYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfCovDate = getYearsFormat(coverageEndYears);
                            previousYearsOfCovDate = getLastDay(previousYearsOfCovDate);*/
                            String previousYearsOfCovDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousYearsOfCovDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(monthAgo)) {
                        String[] coverageEndMonth = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndMonth.length > 0 && coverageEndMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfCovDate = getMonthFormat();
                            String previousMonthOfCovDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousMonthOfCovDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(monthsAgo)) {
                        String[] coverageEndMonths = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndMonths.length > 0 && !coverageEndMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfCovDate = getMonthsFormat(coverageEndMonths);
                            String previousMonthsOfCovDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousMonthsOfCovDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(weekAgo)) {
                        String[] coverageEndWeek = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndWeek.length > 0 && coverageEndWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovEndDate = getWeekFormat();
                            String previousWeekOfCovEndDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousWeekOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(weeksAgo)) {
                        String[] coverageEndWeeks = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndWeeks.length > 0 && !coverageEndWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfCovEndDate = getWeeksFormat(coverageEndWeeks);
                            String previousWeeksOfCovEndDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousWeeksOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(dayAgo)) {
                        String[] coverageEndDay = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndDay.length > 0 && coverageEndDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfCovDate = getDayFormat();
                            String previousDayOfCovDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousDayOfCovDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else if (coverageEndDate.matches(daysAgo)) {
                        String[] coverageEndDays = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndDays.length > 0 && !coverageEndDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfCovEndDate = getDaysFormat(coverageEndDays);
                            String previousDaysOfCovEndDate = coverageEndDate;
                            oleeResourceInstance.setCovEndDate(previousDaysOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                            oleERSForm.setCoverageFlag(true);
                            oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                        }
                    } else {
                        coverageEndFlag = false;
                        oleERSForm.setCoverageFlag(true);
                        oleERSForm.setDefaultCovEndDateErrorMessage(OLEConstants.OLEEResourceRecord.COV_END_DATE_FORMAT_INV);
                    }
                }
            }
            oleeResourceRecordDocument.setCovEndDate(coverageEndDate);
        } catch (Exception ex) {
            LOG.error("Exception while validating the coverage end date format in EResource" + ex.getMessage());
            throw new RuntimeException();
        }
        return coverageEndFlag;
    }

    public boolean validatePerpetualAccessStartDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm) {
        boolean perpetualAccessStartFlag = true;
        OLEEResourceInstance oleeResourceInstance = oleeResourceRecordDocument.getOleERSInstance();
        oleERSForm.setDefaultPerAccStartDateErrorMessage(null);
        String perpetualAccessStartDate = "";
        try {
            if (oleeResourceInstance != null) {
                perpetualAccessStartDate = oleeResourceInstance.getPerpetualAccStartDate();
                if (StringUtils.isNotEmpty(perpetualAccessStartDate)) {
                    if (perpetualAccessStartDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(perpetualAccessStartDate);
                        String dateFormat = perpetualAccessStartDate;
                        oleeResourceInstance.setPerpetualAccStartDate(dateFormat);
                    } else if (perpetualAccessStartDate.matches(calendarYearAgo)) {
                        String[] perpetualAccessStartYear = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartYear.length > 0 && perpetualAccessStartYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfPerpetualAccDate = getYearFormat();
                            previousYearOfPerpetualAccDate = getFirstDay(previousYearOfPerpetualAccDate);*/
                            String previousYearOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousYearOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(calendarYearsAgo)) {
                        String[] perpetualAccessStartYears = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartYears.length > 0 && !perpetualAccessStartYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfPerpetualAccDate = getYearsFormat(perpetualAccessStartYears);
                            previousYearsOfPerpetualAccDate = getFirstDay(previousYearsOfPerpetualAccDate);*/
                            String previousYearsOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousYearsOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(monthAgo)) {
                        String[] perpetualAccessStartMonth = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartMonth.length > 0 && perpetualAccessStartMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfPerpetualAccDate = getMonthFormat();
                            String previousMonthOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousMonthOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(monthsAgo)) {
                        String[] perpetualAccessStartMonths = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartMonths.length > 0 && !perpetualAccessStartMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfPerpetualAccDate = getMonthsFormat(perpetualAccessStartMonths);
                            String previousMonthsOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousMonthsOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(weekAgo)) {
                        String[] perpetualAccessStartWeek = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartWeek.length > 0 && perpetualAccessStartWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovEndDate = getWeekFormat();
                            String previousWeekOfCovEndDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousWeekOfCovEndDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(weeksAgo)) {
                        String[] perpetualAccessStartWeeks = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartWeeks.length > 0 && !perpetualAccessStartWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfCovEndDate = getWeeksFormat(perpetualAccessStartWeeks);
                            String previousWeeksOfCovEndDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousWeeksOfCovEndDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(dayAgo)) {
                        String[] perpetualAccessStartDay = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartDay.length > 0 && perpetualAccessStartDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfPerpetualAccDate = getDayFormat();
                            String previousDayOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousDayOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessStartDate.matches(daysAgo)) {
                        String[] perpetualAccessStartDays = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartDays.length > 0 && !perpetualAccessStartDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfPerpetualAccDate = getDaysFormat(perpetualAccessStartDays);
                            String previousDaysOfPerpetualAccDate = perpetualAccessStartDate;
                            oleeResourceInstance.setPerpetualAccStartDate(previousDaysOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                        }
                    } else {
                        perpetualAccessStartFlag = false;
                        oleERSForm.setDefaultPerAccStartDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_START_DATE_FORMAT_INV);
                    }
                }
            }
            oleeResourceRecordDocument.setPerAccStartDate(perpetualAccessStartDate);
        } catch (Exception ex) {
            LOG.error("Exception while validating the perpetual access start date format in EResource" + ex.getMessage());
            throw new RuntimeException();
        }
        return perpetualAccessStartFlag;
    }

    public boolean validatePerpetualAccessEndDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm) {
        boolean perpetualAccessEndFlag = true;
        OLEEResourceInstance oleeResourceInstance = oleeResourceRecordDocument.getOleERSInstance();
        oleERSForm.setDefaultPerAccEndDateErrorMessage(null);
        String perpetualAccessEndDate = "";
        try {
            if (oleeResourceInstance != null) {
                perpetualAccessEndDate = oleeResourceInstance.getPerpetualAccEndDate();
                if (StringUtils.isNotEmpty(perpetualAccessEndDate)) {
                    if (perpetualAccessEndDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(perpetualAccessEndDate);
                        String dateFormat = perpetualAccessEndDate;
                        oleeResourceInstance.setPerpetualAccEndDate(dateFormat);
                    } else if (perpetualAccessEndDate.matches(calendarYearAgo)) {
                        String[] perpetualAccessEndYear = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndYear.length > 0 && perpetualAccessEndYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                           /* String previousYearOfPerpetualAccDate = getYearFormat();
                            previousYearOfPerpetualAccDate = getLastDay(previousYearOfPerpetualAccDate);*/
                            String previousYearOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousYearOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(calendarYearsAgo)) {
                        String[] perpetualAccessEndYears = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndYears.length > 0 && !perpetualAccessEndYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfPerpetualAccDate = getYearsFormat(perpetualAccessEndYears);
                            previousYearsOfPerpetualAccDate = getLastDay(previousYearsOfPerpetualAccDate);*/
                            String previousYearsOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousYearsOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(monthAgo)) {
                        String[] perpetualAccessEndMonth = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndMonth.length > 0 && perpetualAccessEndMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfPerpetualAccDate = getMonthFormat();
                            String previousMonthOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousMonthOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(monthsAgo)) {
                        String[] perpetualAccessEndMonths = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndMonths.length > 0 && !perpetualAccessEndMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfPerpetualAccDate = getMonthsFormat(perpetualAccessEndMonths);
                            String previousMonthsOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousMonthsOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(weekAgo)) {
                        String[] perpetualAccessEndWeek = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndWeek.length > 0 && !perpetualAccessEndWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfPerpetualAccDate = getWeekFormat();
                            String previousWeekOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousWeekOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(weeksAgo)) {
                        String[] perpetualAccessEndWeeks = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndWeeks.length > 0 && !perpetualAccessEndWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfPerpetualAccDate = getWeeksFormat(perpetualAccessEndWeeks);
                            String previousWeeksOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousWeeksOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(dayAgo)) {
                        String[] perpetualAccessEndDay = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndDay.length > 0 && perpetualAccessEndDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfPerpetualAccDate = getDayFormat();
                            String previousDayOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousDayOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else if (perpetualAccessEndDate.matches(daysAgo)) {
                        String[] perpetualAccessEndDays = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndDays.length > 0 && !perpetualAccessEndDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfPerpetualAccDate = getDaysFormat(perpetualAccessEndDays);
                            String previousDaysOfPerpetualAccDate = perpetualAccessEndDate;
                            oleeResourceInstance.setPerpetualAccEndDate(previousDaysOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                            oleERSForm.setPerpetualAccessFlag(true);
                            oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                        }
                    } else {
                        perpetualAccessEndFlag = false;
                        oleERSForm.setDefaultPerAccEndDateErrorMessage(OLEConstants.OLEEResourceRecord.PER_ACC_END_DATE_FORMAT_INV);
                    }
                }
            }
            oleeResourceRecordDocument.setPerAccEndDate(perpetualAccessEndDate);
        } catch (Exception ex) {
            LOG.error("Exception while validating the perpetual access end date format in EResource" + ex.getMessage());
            throw new RuntimeException();
        }
        return perpetualAccessEndFlag;
    }

    public boolean validateDates(OleHoldings eHoldings) {
        boolean dateFlag = true;
        dateFlag &= validateCoverageStartDateForEHolding(eHoldings);
        dateFlag &= validateCoverageEndDateForEHolding(eHoldings);
        dateFlag &= validatePerpetualAccStartDateForEHolding(eHoldings);
        dateFlag &= validatePerpetualAccEndDateForEHolding(eHoldings);
        return dateFlag;
    }

    public boolean validateCoverageStartDateForEHolding(OleHoldings eHoldings) {
        boolean covStartDateFlag = true;
        List<Coverage> coverageList = new ArrayList<>();
        if (eHoldings.getExtentOfOwnership().size() > 0 && eHoldings.getExtentOfOwnership().get(0).getCoverages() != null
                && eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().size() > 0) {
            coverageList = eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage();
            for (Coverage coverage : coverageList) {
                if(StringUtils.isNotEmpty(coverage.getCoverageStartDateString())) {
                    coverage.setCoverageStartDate(coverage.getCoverageStartDateString());
                } else if(StringUtils.isNotEmpty(coverage.getCoverageStartDateFormat())) {
                    coverage.setCoverageStartDate(coverage.getCoverageStartDateFormat());
                }
                covStartDateFlag &= validateCoverageStartDates(coverage);
            }
            if (!covStartDateFlag) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OleHoldings.ERROR_MSG_COV_START_DATE);
                return covStartDateFlag;
            }
        }
        return covStartDateFlag;
    }

    public boolean validateCoverageEndDateForEHolding(OleHoldings eHoldings) {
        boolean covEndDateFlag = true;
        List<Coverage> coverageList = new ArrayList<>();
        if (eHoldings.getExtentOfOwnership().size() > 0 && eHoldings.getExtentOfOwnership().get(0).getCoverages() != null
                && eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage().size() > 0) {
            coverageList = eHoldings.getExtentOfOwnership().get(0).getCoverages().getCoverage();
            for (Coverage coverage : coverageList) {
                if(StringUtils.isNotEmpty(coverage.getCoverageEndDateString())) {
                    coverage.setCoverageEndDate(coverage.getCoverageEndDateString());
                } else if(StringUtils.isNotEmpty(coverage.getCoverageEndDateFormat())) {
                    coverage.setCoverageEndDate(coverage.getCoverageEndDateFormat());
                }
                covEndDateFlag &= validateCoverageEndDates(coverage);
            }
            if (!covEndDateFlag) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OleHoldings.ERROR_MSG_COV_END_DATE);
                return covEndDateFlag;
            }
        }
        return covEndDateFlag;
    }

    public boolean validatePerpetualAccStartDateForEHolding(OleHoldings eHoldings) {
        boolean perpetualAccStartDateFlag = true;
        List<PerpetualAccess> perpetualAccessList = new ArrayList<>();
        if (eHoldings.getExtentOfOwnership().size() > 0 && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses() != null
                && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().size() > 0) {
            perpetualAccessList = eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess();
            for (PerpetualAccess perpetualAccess : perpetualAccessList) {
                if(StringUtils.isNotEmpty(perpetualAccess.getPerpetualAccessStartDateFormat())) {
                    perpetualAccess.setPerpetualAccessStartDate(perpetualAccess.getPerpetualAccessStartDateFormat());
                } else if(StringUtils.isNotEmpty(perpetualAccess.getPerpetualAccessStartDateString())) {
                    perpetualAccess.setPerpetualAccessStartDate(perpetualAccess.getPerpetualAccessStartDateString());
                }
                perpetualAccStartDateFlag &= validatePerpetualAccessStartDates(perpetualAccess);
            }
            if (!perpetualAccStartDateFlag) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OleHoldings.ERROR_MSG_PER_ACC_START_DATE);
                return perpetualAccStartDateFlag;
            }
        }
        return perpetualAccStartDateFlag;
    }

    public boolean validatePerpetualAccEndDateForEHolding(OleHoldings eHoldings) {
        boolean perpetualAccEndDateFlag = true;
        List<PerpetualAccess> perpetualAccessList = new ArrayList<>();
        if (eHoldings.getExtentOfOwnership().size() > 0 && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses() != null
                && eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess().size() > 0) {
            perpetualAccessList = eHoldings.getExtentOfOwnership().get(0).getPerpetualAccesses().getPerpetualAccess();
            for (PerpetualAccess perpetualAccess : perpetualAccessList) {
                if(StringUtils.isNotEmpty(perpetualAccess.getPerpetualAccessEndDateString())) {
                    perpetualAccess.setPerpetualAccessEndDate(perpetualAccess.getPerpetualAccessEndDateString());
                } else if(StringUtils.isNotEmpty(perpetualAccess.getPerpetualAccessEndDateFormat())) {
                    perpetualAccess.setPerpetualAccessEndDate(perpetualAccess.getPerpetualAccessEndDateFormat());
                }
                perpetualAccEndDateFlag &= validatePerpetualAccessEndDates(perpetualAccess);
            }
            if (!perpetualAccEndDateFlag) {
                GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.OleHoldings.ERROR_MSG_PER_ACC_END_DATE);
                return perpetualAccEndDateFlag;
            }
        }
        return perpetualAccEndDateFlag;
    }

    private boolean validateCoverageStartDates(Coverage coverage) {
        boolean coverageStartFlag = true;
        String coverageStartDate = "";
        try {
            if (coverage != null) {
                coverageStartDate = coverage.getCoverageStartDate();
                if (StringUtils.isNotEmpty(coverageStartDate)) {
                    if (coverageStartDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(coverageStartDate);
                        String dateFormat = coverageStartDate;
                        coverage.setCoverageStartDate(dateFormat);
                    } else if (coverageStartDate.matches(calendarYearAgo)) {
                        String[] coverageStartYear = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartYear.length > 0 && coverageStartYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfCovDate = getYearFormat();
                            previousYearOfCovDate = getFirstDay(previousYearOfCovDate);*/
                            String previousYearOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousYearOfCovDate);
                        } else {
                            coverage.setCoverageStartDate(coverageStartDate);
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(calendarYearsAgo)) {
                        String[] coverageStartYears = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartYears.length > 0 && !coverageStartYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfCovDate = getYearsFormat(coverageStartYears);
                            previousYearsOfCovDate = getFirstDay(previousYearsOfCovDate);*/
                            String previousYearsOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousYearsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(monthAgo)) {
                        String[] coverageStartMonth = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartMonth.length > 0 && coverageStartMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfCovDate = getMonthFormat();
                            String previousMonthOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousMonthOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(monthsAgo)) {
                        String[] coverageStartMonths = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartMonths.length > 0 && !coverageStartMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfCovDate = getMonthsFormat(coverageStartMonths);
                            String previousMonthsOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousMonthsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(weekAgo)) {
                        String[] coverageStartWeek = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartWeek.length > 0 && coverageStartWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovDate = getWeekFormat();
                            String previousWeekOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousWeekOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(weeksAgo)) {
                        String[] coverageStartWeeks = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartWeeks.length > 0 && !coverageStartWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousYearsOfCovDate = getWeeksFormat(coverageStartWeeks);
                            String previousYearsOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousYearsOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(dayAgo)) {
                        String[] coverageStartDay = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartDay.length > 0 && coverageStartDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfCovDate = getDayFormat();
                            String previousDayOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousDayOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else if (coverageStartDate.matches(daysAgo)) {
                        String[] coverageStartDays = coverageStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageStartDays.length > 0 && !coverageStartDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfCovDate = getDaysFormat(coverageStartDays);
                            String previousDaysOfCovDate = coverageStartDate;
                            coverage.setCoverageStartDate(previousDaysOfCovDate);
                        } else {
                            coverageStartFlag = false;
                        }
                    } else {
                        coverage.setCoverageStartDate(coverageStartDate);
                        coverageStartFlag = false;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while validating the coverage start date format in EHoldings" + ex.getMessage());
            throw new RuntimeException();
        }
        return coverageStartFlag;
    }

    private boolean validateCoverageEndDates(Coverage coverage) {
        boolean coverageEndFlag = true;
        String coverageEndDate = "";
        try {
            if (coverage != null) {
                coverageEndDate = coverage.getCoverageEndDate();
                if (StringUtils.isNotEmpty(coverageEndDate)) {
                    if (coverageEndDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(coverageEndDate);
                        String dateFormat = coverageEndDate;
                        coverage.setCoverageEndDate(dateFormat);
                    } else if (coverageEndDate.matches(calendarYearAgo)) {
                        String[] coverageEndYear = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndYear.length > 0 && coverageEndYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfCovDate = getYearFormat();
                            previousYearOfCovDate = getLastDay(previousYearOfCovDate);*/
                            String previousYearOfCovDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousYearOfCovDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(calendarYearsAgo)) {
                        String[] coverageEndYears = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndYears.length > 0 && !coverageEndYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfCovDate = getYearsFormat(coverageEndYears);
                            previousYearsOfCovDate = getLastDay(previousYearsOfCovDate);*/
                            String previousYearsOfCovDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousYearsOfCovDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(monthAgo)) {
                        String[] coverageEndMonth = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndMonth.length > 0 && coverageEndMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfCovDate = getMonthFormat();
                            String previousMonthOfCovDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousMonthOfCovDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(monthsAgo)) {
                        String[] coverageEndMonths = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndMonths.length > 0 && !coverageEndMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfCovDate = getMonthsFormat(coverageEndMonths);
                            String previousMonthsOfCovDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousMonthsOfCovDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(weekAgo)) {
                        String[] coverageEndWeek = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndWeek.length > 0 && coverageEndWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovEndDate = getWeekFormat();
                            String previousWeekOfCovEndDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousWeekOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(weeksAgo)) {
                        String[] coverageEndWeeks = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndWeeks.length > 0 && !coverageEndWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfCovEndDate = getWeeksFormat(coverageEndWeeks);
                            String previousWeeksOfCovEndDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousWeeksOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(dayAgo)) {
                        String[] coverageEndDay = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndDay.length > 0 && coverageEndDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfCovDate = getDayFormat();
                            String previousDayOfCovDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousDayOfCovDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else if (coverageEndDate.matches(daysAgo)) {
                        String[] coverageEndDays = coverageEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (coverageEndDays.length > 0 && !coverageEndDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfCovEndDate = getDaysFormat(coverageEndDays);
                            String previousDaysOfCovEndDate = coverageEndDate;
                            coverage.setCoverageEndDate(previousDaysOfCovEndDate);
                        } else {
                            coverageEndFlag = false;
                        }
                    } else {
                        coverageEndFlag = false;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while validating the coverage end date format in EHoldings " + ex.getMessage());
            throw new RuntimeException();
        }
        return coverageEndFlag;
    }

    private boolean validatePerpetualAccessStartDates(PerpetualAccess perpetualAccess) {
        boolean perpetualAccessStartFlag = true;
        String perpetualAccessStartDate = "";
        try {
            if (perpetualAccess != null) {
                perpetualAccessStartDate = perpetualAccess.getPerpetualAccessStartDate();
                if (StringUtils.isNotEmpty(perpetualAccessStartDate)) {
                    if (perpetualAccessStartDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(perpetualAccessStartDate);
                        String dateFormat = perpetualAccessStartDate;
                        perpetualAccess.setPerpetualAccessStartDate(dateFormat);
                    } else if (perpetualAccessStartDate.matches(calendarYearAgo)) {
                        String[] perpetualAccessStartYear = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartYear.length > 0 && perpetualAccessStartYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfPerpetualAccDate = getYearFormat();
                            previousYearOfPerpetualAccDate = getFirstDay(previousYearOfPerpetualAccDate);*/
                            String previousYearOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousYearOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(calendarYearsAgo)) {
                        String[] perpetualAccessStartYears = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartYears.length > 0 && !perpetualAccessStartYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfPerpetualAccDate = getYearsFormat(perpetualAccessStartYears);
                            previousYearsOfPerpetualAccDate = getFirstDay(previousYearsOfPerpetualAccDate);*/
                            String previousYearsOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousYearsOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(monthAgo)) {
                        String[] perpetualAccessStartMonth = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartMonth.length > 0 && perpetualAccessStartMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfPerpetualAccDate = getMonthFormat();
                            String previousMonthOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousMonthOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(monthsAgo)) {
                        String[] perpetualAccessStartMonths = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartMonths.length > 0 && !perpetualAccessStartMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfPerpetualAccDate = getMonthsFormat(perpetualAccessStartMonths);
                            String previousMonthsOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousMonthsOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(weekAgo)) {
                        String[] perpetualAccessStartWeek = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartWeek.length > 0 && perpetualAccessStartWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfCovEndDate = getWeekFormat();
                            String previousWeekOfCovEndDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousWeekOfCovEndDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(weeksAgo)) {
                        String[] perpetualAccessStartWeeks = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartWeeks.length > 0 && !perpetualAccessStartWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfCovEndDate = getWeeksFormat(perpetualAccessStartWeeks);
                            String previousWeeksOfCovEndDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousWeeksOfCovEndDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(dayAgo)) {
                        String[] perpetualAccessStartDay = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartDay.length > 0 && perpetualAccessStartDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfPerpetualAccDate = getDayFormat();
                            String previousDayOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousDayOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else if (perpetualAccessStartDate.matches(daysAgo)) {
                        String[] perpetualAccessStartDays = perpetualAccessStartDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessStartDays.length > 0 && !perpetualAccessStartDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfPerpetualAccDate = getDaysFormat(perpetualAccessStartDays);
                            String previousDaysOfPerpetualAccDate = perpetualAccessStartDate;
                            perpetualAccess.setPerpetualAccessStartDate(previousDaysOfPerpetualAccDate);
                        } else {
                            perpetualAccessStartFlag = false;
                        }
                    } else {
                        perpetualAccessStartFlag = false;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while validating the Perpetual access start date format in EHoldings " + ex.getMessage());
            throw new RuntimeException();
        }
        return perpetualAccessStartFlag;
    }

    private boolean validatePerpetualAccessEndDates(PerpetualAccess perpetualAccess) {
        boolean perpetualAccessEndFlag = true;
        String perpetualAccessEndDate = "";
        try {
            if (perpetualAccess != null) {
                perpetualAccessEndDate = perpetualAccess.getPerpetualAccessEndDate();
                if (StringUtils.isNotEmpty(perpetualAccessEndDate)) {
                    if (perpetualAccessEndDate.matches(OLEConstants.OLEEResourceRecord.DATE_FORMAT_REGEX)) {
                        //String dateFormat = getDateFormat(perpetualAccessEndDate);
                        String dateFormat = perpetualAccessEndDate;
                        perpetualAccess.setPerpetualAccessEndDate(dateFormat);
                    } else if (perpetualAccessEndDate.matches(calendarYearAgo)) {
                        String[] perpetualAccessEndYear = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndYear.length > 0 && perpetualAccessEndYear[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearOfPerpetualAccDate = getYearFormat();
                            previousYearOfPerpetualAccDate = getLastDay(previousYearOfPerpetualAccDate);*/
                            String previousYearOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousYearOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(calendarYearsAgo)) {
                        String[] perpetualAccessEndYears = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndYears.length > 0 && !perpetualAccessEndYears[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            /*String previousYearsOfPerpetualAccDate = getYearsFormat(perpetualAccessEndYears);
                            previousYearsOfPerpetualAccDate = getLastDay(previousYearsOfPerpetualAccDate);*/
                            String previousYearsOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousYearsOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(monthAgo)) {
                        String[] perpetualAccessEndMonth = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndMonth.length > 0 && perpetualAccessEndMonth[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthOfPerpetualAccDate = getMonthFormat();
                            String previousMonthOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousMonthOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(monthsAgo)) {
                        String[] perpetualAccessEndMonths = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndMonths.length > 0 && !perpetualAccessEndMonths[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousMonthsOfPerpetualAccDate = getMonthsFormat(perpetualAccessEndMonths);
                            String previousMonthsOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousMonthsOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(weekAgo)) {
                        String[] perpetualAccessEndWeek = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndWeek.length > 0 && !perpetualAccessEndWeek[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeekOfPerpetualAccDate = getWeekFormat();
                            String previousWeekOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousWeekOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(weeksAgo)) {
                        String[] perpetualAccessEndWeeks = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndWeeks.length > 0 && !perpetualAccessEndWeeks[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousWeeksOfPerpetualAccDate = getWeeksFormat(perpetualAccessEndWeeks);
                            String previousWeeksOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousWeeksOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(dayAgo)) {
                        String[] perpetualAccessEndDay = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndDay.length > 0 && perpetualAccessEndDay[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDayOfPerpetualAccDate = getDayFormat();
                            String previousDayOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousDayOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else if (perpetualAccessEndDate.matches(daysAgo)) {
                        String[] perpetualAccessEndDays = perpetualAccessEndDate.split(OLEConstants.OLEEResourceRecord.SPACE);
                        if (perpetualAccessEndDays.length > 0 && !perpetualAccessEndDays[0].equals(OLEConstants.OLEEResourceRecord.ONE)) {
                            //String previousDaysOfPerpetualAccDate = getDaysFormat(perpetualAccessEndDays);
                            String previousDaysOfPerpetualAccDate = perpetualAccessEndDate;
                            perpetualAccess.setPerpetualAccessEndDate(previousDaysOfPerpetualAccDate);
                        } else {
                            perpetualAccessEndFlag = false;
                        }
                    } else {
                        perpetualAccessEndFlag = false;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception while validating the Perpetual access end date format in EHoldings " + ex.getMessage());
            throw new RuntimeException();
        }
        return perpetualAccessEndFlag;
    }

    private String getDateFormat(String perpetualAccessEndDate) {
        Date date = new Date(perpetualAccessEndDate);
        String dateFormat = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(date);
        return dateFormat;
    }

    private String getYearFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Date previousYear = calendar.getTime();
        String year = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousYear);
        return year;
    }

    private String getYearsFormat(String[] years) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -(Integer.parseInt(years[0])));
        Date previousYears = calendar.getTime();
        String numberOfYears = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousYears);
        return numberOfYears;
    }

    private String getMonthFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date previousMonth = calendar.getTime();
        String month = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousMonth);
        return month;
    }

    private String getMonthsFormat(String[] months) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -(Integer.parseInt(months[0])));
        Date previousMonths = calendar.getTime();
        String numberOfMonths = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousMonths);
        return numberOfMonths;
    }

    private String getWeekFormat() {
        Calendar calendar = Calendar.getInstance();
        int days = 7;
        calendar.add(Calendar.DATE, -(days));
        Date previousWeek = calendar.getTime();
        String week = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousWeek);
        return week;
    }

    private String getWeeksFormat(String[] weeks) {
        Calendar calendar = Calendar.getInstance();
        int days = Integer.parseInt(weeks[0]) * 7;
        calendar.add(Calendar.DATE, -(days));
        Date previousWeeks = calendar.getTime();
        String numberOfWeeks = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousWeeks);
        return numberOfWeeks;
    }

    private String getDayFormat() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date previousDay = calendar.getTime();
        String day = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousDay);
        return day;
    }

    private String getDaysFormat(String[] days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -(Integer.parseInt(days[0])));
        Date previousDays = calendar.getTime();
        String numberOfDays = new SimpleDateFormat(OLEConstants.OLEEResourceRecord.DATE_FORMAT).format(previousDays);
        return numberOfDays;
    }

    private String getFirstDay(String firstDay) {
        String[] date = firstDay.split(OLEConstants.SLASH);
        String yearAlone = "";
        if (date.length > 1) {
            yearAlone = date[2];
        }
        yearAlone = firstDayOfYear + yearAlone;
        return yearAlone;
    }

    private String getLastDay(String lastDay) {
        String[] date = lastDay.split(OLEConstants.SLASH);
        String yearAlone = "";
        if (date.length > 1) {
            yearAlone = date[2];
        }
        yearAlone = lastDayOfYear + yearAlone;
        return yearAlone;
    }

    public void getPOAndInvoiceItemsWithoutDuplicate(OLEEResourceRecordDocument oleERSDoc) {
        List<OLEEResourcePO> oleeResourcePOItems = oleERSDoc.getOleERSPOItems();
        List<OLEEResourceInvoices> oleERSInvoices = oleERSDoc.getOleERSInvoices();
        Map avoidingDuplicateMap = new HashMap<>();
        for (OLEEResourcePO oleeResourcePO : oleeResourcePOItems) {
            avoidingDuplicateMap.put(oleeResourcePO.getOlePOItemId(),oleeResourcePO);
        }
        oleERSDoc.getOleERSPOItems().clear();
        oleERSDoc.getOleERSPOItems().addAll((Collection<? extends OLEEResourcePO>) avoidingDuplicateMap.values());
        avoidingDuplicateMap.clear();
        for (OLEEResourceInvoices oleeResourceInvoice : oleERSInvoices) {
            avoidingDuplicateMap.put(oleeResourceInvoice.getInvoiceId(),oleeResourceInvoice);
        }
        oleERSDoc.getOleERSInvoices().clear();
        oleERSDoc.getOleERSInvoices().addAll((Collection<? extends OLEEResourceInvoices>) avoidingDuplicateMap.values());
    }

    public void getAcquisitionInfoFromPOAndInvoice(String holdingsId,WorkEInstanceOlemlForm workEInstanceOlemlForm) {
        Map map = new HashMap();
        map.put(OLEConstants.INSTANCE_ID, holdingsId);
        List<OleCopy> oleCopyList = (List) getBusinessObjectService().findMatching(OleCopy.class, map);
        StringBuffer linkedPos = new StringBuffer();
        StringBuffer vendor = new StringBuffer();
        StringBuffer orderType = new StringBuffer();
        StringBuffer orderFormat = new StringBuffer();
        StringBuffer fundCode = new StringBuffer();
        List fundCodeList = new ArrayList();
        List<PurApAccountingLine> accountingLines = new ArrayList<>();
        KualiDecimal currentFYCost=new KualiDecimal(0);
        for (OleCopy oleCopy : oleCopyList) {
            if (oleCopy.getPoItemId() != null) {
                map.clear();
                map.put(OLEConstants.OLEEResourceRecord.PO_ITEM_ID, oleCopy.getPoItemId().toString());
                OlePurchaseOrderItem olePurchaseOrderItem = getBusinessObjectService().findByPrimaryKey(OlePurchaseOrderItem.class, map);
                if (olePurchaseOrderItem != null) {
                    // vendor, current FY cost & order type
                    map.clear();
                    map.put(OLEConstants.DOC_NUM, olePurchaseOrderItem.getDocumentNumber());
                    OlePurchaseOrderDocument olePurchaseOrderDocument = getBusinessObjectService().findByPrimaryKey(OlePurchaseOrderDocument.class, map);
                    if (olePurchaseOrderDocument != null) {
                        // po
                        linkedPos.append(olePurchaseOrderDocument.getPurapDocumentIdentifier());
                        linkedPos.append(OLEConstants.COMMA);
                        linkedPos.append(' ');

                        Integer poCreatedYear = olePurchaseOrderDocument.getPostingYear();
                        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        if (currentYear.compareTo(poCreatedYear) == 0) {
                            currentFYCost = currentFYCost.add(olePurchaseOrderItem.getItemInvoicedTotalAmount());
                        }

                        vendor.append(olePurchaseOrderDocument.getVendorName());
                        vendor.append(OLEConstants.COMMA);
                        vendor.append(' ');

                        map.clear();
                        map.put(OLEConstants.PURCHASE_ORDER_TYPE_ID, olePurchaseOrderDocument.getPurchaseOrderTypeId());
                        Collection<PurchaseOrderType> purchaseOrderTypeDocumentList = getBusinessObjectService().findMatching(PurchaseOrderType.class, map);
                        if (purchaseOrderTypeDocumentList != null && purchaseOrderTypeDocumentList.size() > 0) {
                            PurchaseOrderType purchaseOrderTypeDoc = purchaseOrderTypeDocumentList.iterator().next();
                            orderType.append(purchaseOrderTypeDoc.getPurchaseOrderType());
                            orderType.append(OLEConstants.SEMI_COLON);
                            orderType.append(' ');
                        }
                    }
                    // payment status & Fund code
                    map.clear();
                    map.put(OLEConstants.OLEEResourceRecord.INV_PO_ITEM_ID, olePurchaseOrderItem.getItemIdentifier());
                    List<OleInvoiceItem> oleInvoiceItems = (List<OleInvoiceItem>) getBusinessObjectService().findMatching(OleInvoiceItem.class, map);
                    if (oleInvoiceItems != null && oleInvoiceItems.size() > 0) {
                        for (OleInvoiceItem oleInvoiceItem : oleInvoiceItems) {
                            map.put(OLEConstants.OLEEResourceRecord.INV_PO_ITEM_ID, oleInvoiceItem.getItemIdentifier());
                            OlePaymentRequestItem olePaymentRequestItem = getBusinessObjectService().findByPrimaryKey(OlePaymentRequestItem.class, map);
                            if (olePaymentRequestItem != null) {
                                workEInstanceOlemlForm.getExtendedEHoldingFields().setPaymentStatus(OLEConstants.PAID);
                                break;
                            }
                        }
                        for (OleInvoiceItem oleInvoiceItem : oleInvoiceItems) {
                            List purApAccountingLines = oleInvoiceItem.getSourceAccountingLines();
                            if (purApAccountingLines != null && purApAccountingLines.size() > 0) {
                                accountingLines.addAll(purApAccountingLines);
                            }
                        }
                    }
                    // order format
                    if (olePurchaseOrderItem.getFormatTypeId() != null) {
                        map.clear();
                        map.put(OLEConstants.FORMAT_TYPE_ID, olePurchaseOrderItem.getFormatTypeId());
                        OleFormatType oleFormatType = getBusinessObjectService().findByPrimaryKey(OleFormatType.class, map);
                        if (oleFormatType != null) {
                            orderFormat.append(oleFormatType.getFormatTypeName());
                            orderFormat.append(OLEConstants.COMMA);
                            orderFormat.append(' ');
                        }
                    }
                }
            }
        }
        if (linkedPos.length() > 0) {
            if (vendor.length() > 0) {
                vendor.deleteCharAt(vendor.length() - 2);
                workEInstanceOlemlForm.getExtendedEHoldingFields().setVendorName(vendor.toString());
            }
            if (orderType.length() > 0) {
                orderType.deleteCharAt(orderType.length() - 2);
                workEInstanceOlemlForm.getExtendedEHoldingFields().setOrderType(orderType.toString());
            }
            if (orderFormat.length() > 0) {
                orderFormat.deleteCharAt(orderFormat.length() - 2);
                workEInstanceOlemlForm.getExtendedEHoldingFields().setOrderFormat(orderFormat.toString());
            }
            String pos[] = linkedPos.toString().split(",");
            Set set = new HashSet();
            for (String po : pos) {
                set.add(po.trim());
            }
            pos = (String[]) set.toArray(new String[0]);
            StringBuffer poLink = new StringBuffer();
            for (String po : pos) {
                String link = null;
                if (StringUtils.isNotBlank(po)) {
                    Map poMap = new HashMap();
                    poMap.put(org.kuali.ole.sys.OLEConstants.PUR_DOC_IDENTIFIER, po.trim());
                    List<OlePurchaseOrderDocument> olePurchaseOrderDocumentList = (List) getBusinessObjectService().findMatching(OlePurchaseOrderDocument.class, poMap);
                    if (olePurchaseOrderDocumentList != null && olePurchaseOrderDocumentList.size() > 0) {
                        for (OlePurchaseOrderDocument olePurchaseOrderDocument : olePurchaseOrderDocumentList) {
                            boolean validPO = olePurchaseOrderDocumentList != null ? olePurchaseOrderDocument.getPurchaseOrderCurrentIndicatorForSearching() : false;
                            if (validPO) {
                                link = ConfigContext.getCurrentContextConfig().getProperty("kew.url") + org.kuali.ole.sys.OLEConstants.PO_LINE_ITEM_URL + olePurchaseOrderDocument.getDocumentNumber();
                                poLink.append("<a href=" + link + " target='_blank'>" + po.trim() + "</a>, ");
                            }
                        }
                    }
                }
            }
            if (poLink.length() > 0) {
                poLink.deleteCharAt(poLink.length() - 2);
                workEInstanceOlemlForm.getExtendedEHoldingFields().setPurchaseOrderId(poLink.toString());
            }
            workEInstanceOlemlForm.getExtendedEHoldingFields().setCurrentFYCost(currentFYCost.toString());
            if (org.apache.commons.lang.StringUtils.isBlank(workEInstanceOlemlForm.getExtendedEHoldingFields().getPaymentStatus())) {
                workEInstanceOlemlForm.getExtendedEHoldingFields().setPaymentStatus(OLEConstants.NOT_PAID);
            }
            if (accountingLines.size() > 0) {
                for (PurApAccountingLine accountingLine : accountingLines) {
                    map.clear();
                    map.put(OLEConstants.ACCOUNT_NUMBER, accountingLine.getAccountNumber());
                    map.put(OLEConstants.OBJECT_CODE, accountingLine.getFinancialObjectCode());
                    OleVendorAccountInfo oleVendorAccountInfo = getBusinessObjectService().findByPrimaryKey(OleVendorAccountInfo.class, map);
                    if (oleVendorAccountInfo != null && !fundCodeList.contains(oleVendorAccountInfo.getVendorRefNumber())) {
                        fundCodeList.add(oleVendorAccountInfo.getVendorRefNumber());
                        fundCode.append(oleVendorAccountInfo.getVendorRefNumber());
                        fundCode.append(OLEConstants.COMMA);
                        fundCode.append(' ');
                    }
                }
            }
            if (fundCode.length() > 0) {
                fundCode.deleteCharAt(fundCode.length() - 2);
                workEInstanceOlemlForm.getExtendedEHoldingFields().setFundCode(fundCode.toString());
            }
        }
    }

    private void updateEResInOleCopy(Holdings holdings, OLEEResourceRecordDocument oleERSDoc) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put(OLEConstants.INSTANCE_ID, holdings.getId());
        List<OleCopy> copies = (List<OleCopy>) getBusinessObjectService().findMatching(OleCopy.class,
                criteriaMap);
        if (copies.size() > 0) {
            oleERSDoc.getCopyList().addAll(copies);
        } else {
            List<OleCopy> newCopies = new ArrayList<OleCopy>();
            OleCopy oleCopy = new OleCopy();
            oleCopy.setBibId(holdings.getBib().getId());
            oleCopy.setOleERSIdentifier(oleERSDoc.getOleERSIdentifier() != null ? oleERSDoc.getOleERSIdentifier() : "");
            oleCopy.setInstanceId(holdings.getId());
            newCopies.add(oleCopy);
            oleERSDoc.getCopyList().addAll(newCopies);
        }
    }
}
