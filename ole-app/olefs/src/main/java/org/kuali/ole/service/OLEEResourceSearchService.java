package org.kuali.ole.service;

import org.kuali.ole.describe.form.WorkEInstanceOlemlForm;
import org.kuali.ole.docstore.common.document.content.instance.OleHoldings;
import org.kuali.ole.docstore.model.bo.WorkBibDocument;
import org.kuali.ole.select.bo.OLESearchCondition;
import org.kuali.ole.select.document.OLEEResourceRecordDocument;
import org.kuali.ole.select.form.OLEEResourceRecordForm;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenchulakshmig
 * Date: 7/10/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OLEEResourceSearchService {

    public List<OLEEResourceRecordDocument> statusNotNull(List<OLEEResourceRecordDocument> eresourceList, List<String> status);

    public List<OLEEResourceRecordDocument> performSearch(List<OLESearchCondition> oleSearchConditionsList)throws Exception;

    public List<OLEEResourceRecordDocument> findMatching(Map<String, List<String>> map,DocumentSearchCriteria.Builder docSearchCriteria);

    public void getEResourcesFields(String eResourceId, OleHoldings OleHoldings, WorkEInstanceOlemlForm eInstanceOlemlForm);

    public void getEResourcesLicenseFields(String eResourceId, WorkEInstanceOlemlForm eInstanceOlemlForm);

    public OLEEResourceRecordDocument getNewOleERSDoc(OLEEResourceRecordDocument oleERSDoc);

   // public List<WorkBibDocument> getWorkBibDocuments(List<String> instanceIdsList, String docType);

    public String getParameter(String parameterName);

    public void getDefaultCovergeDate(OLEEResourceRecordDocument oleERSDoc);

    public void getDefaultPerpetualAccessDate(OLEEResourceRecordDocument oleERSDoc);

    public OLEEResourceRecordDocument saveDefaultCoverageDate(OLEEResourceRecordDocument oleeResourceRecordDocument);

    public OLEEResourceRecordDocument saveDefaultPerpetualAccessDate(OLEEResourceRecordDocument oleeResourceRecordDocument);

    public void getNewInstance(OLEEResourceRecordDocument oleERSDoc, String documentNumber) throws Exception;

    public void getAccessLocationFromEInstance(OleHoldings OleHoldings, WorkEInstanceOlemlForm workEInstanceOlemlForm);

    public void getDefaultCovDatesToPopup(OLEEResourceRecordDocument oleeResourceRecordDocument, String defaultCov);

    public void getDefaultPerAccDatesToPopup(OLEEResourceRecordDocument oleeResourceRecordDocument, String defaultPerpetualAcc);

    public boolean validateEResourceDocument(OLEEResourceRecordDocument oleeResourceRecordDocument);

    public void saveEResourceInstanceToDocstore(OLEEResourceRecordDocument oleeResourceRecordDocument)throws Exception;

    public boolean validateCoverageStartDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm);

    public boolean validateCoverageEndDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm);

    public boolean validatePerpetualAccessStartDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm);

    public boolean validatePerpetualAccessEndDates(OLEEResourceRecordDocument oleeResourceRecordDocument, OLEEResourceRecordForm oleERSForm);

    public boolean validateDates(OleHoldings eHoldings);

    public void getPOAndInvoiceItemsWithoutDuplicate(OLEEResourceRecordDocument oleERSDoc);

    public void getAcquisitionInfoFromPOAndInvoice(String holdingsId, WorkEInstanceOlemlForm workEInstanceOlemlForm);

    public void getPOInvoiceForERS(OLEEResourceRecordDocument oleERSDoc);

}
