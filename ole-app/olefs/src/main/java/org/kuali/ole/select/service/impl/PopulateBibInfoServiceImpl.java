/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.select.service.impl;

import org.kuali.ole.select.OleSelectConstant;
import org.kuali.ole.select.batch.service.OleRequisitionCreateDocumentService;
import org.kuali.ole.select.batch.service.impl.OleRequisitionCreateDocumentServiceImpl;
import org.kuali.ole.select.businessobject.BibInfoBean;
import org.kuali.ole.select.service.PopulateBibInfoService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.ErrorMessage;
import org.kuali.rice.krad.util.GlobalVariables;
import org.springframework.util.AutoPopulatingList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PopulateBibInfoServiceImpl implements PopulateBibInfoService {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PopulateBibInfoServiceImpl.class);
    protected ConfigurationService kualiConfigurationService;

    @Override
    public String processBibInfoForCitation(String citationString, BibInfoBean bibInfoBean) throws Exception {
        try {
            String user;
            if (GlobalVariables.getUserSession() != null) {
                user = GlobalVariables.getUserSession().getPrincipalName();
            } else {
                kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
                user = kualiConfigurationService.getPropertyValueAsString("userName");
            }
            GlobalVariables.setUserSession(new UserSession(user));
            BuildCitationBibInfoBean buildCitationBibInfoBean = SpringContext.getBean(BuildCitationBibInfoBean.class);
            bibInfoBean = buildCitationBibInfoBean.getBean(citationString, bibInfoBean);
            if (bibInfoBean.getTitle() == null || "".equalsIgnoreCase(bibInfoBean.getTitle())) {
                return OleSelectConstant.SOAP_CITATION_PARSER_UNREACHABLE;
            }
            bibInfoBean.setRequestSourceUrl(citationString);
            //bibInfoBean.setRequestSource(OleSelectConstant.REQUEST_SRC_TYPE_WEBFORM);
            bibInfoBean.setRequisitionSource(OleSelectConstant.REQUISITON_SRC_TYPE_WEBFORM);
            bibInfoBean.setDocStoreOperation(OleSelectConstant.DOCSTORE_OPERATION_WEBFORM);
            bibInfoBean = setBibInfoDefaultValues(bibInfoBean);
            OleRequisitionCreateDocumentService createDocument = SpringContext.getBean(OleRequisitionCreateDocumentServiceImpl.class);
            List<BibInfoBean> bibInfoBeanList = getBibInfoBeanList(bibInfoBean);
            String docNumber = createDocument.saveRequisitionDocument(bibInfoBeanList, true);
            return docNumber;
        } catch (Exception e) {
            LOG.error("Exception processing for SOAP citation document creation----" + e.getMessage(), e);
            String errorMessage = null;
            if (GlobalVariables.getMessageMap().hasErrors()) {
                Map<String, AutoPopulatingList<ErrorMessage>> map = GlobalVariables.getMessageMap().getErrorMessages();
                for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> entry : map.entrySet()) {
                    AutoPopulatingList<ErrorMessage> errors = entry.getValue();
                    ErrorMessage error = errors.get(0);
                    String[] params = error.getMessageParameters();
                    errorMessage = params[0];
                }
            }
            return OleSelectConstant.SOAP_EXCEPTION + " - " + errorMessage;
        }
    }

    /**
     * This method sets the default values to the bibInfoBean
     *
     * @param bibInfoBean
     * @return BibInfoBean
     */
    private BibInfoBean setBibInfoDefaultValues(BibInfoBean bibInfoBean) throws Exception {

        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        // bibInfoBean.setIsbn(kualiConfigurationService.getPropertyValueAsString("isbn"));
        bibInfoBean.setFinancialYear(kualiConfigurationService.getPropertyValueAsString("financialYear"));
        bibInfoBean.setChartOfAccountsCode(kualiConfigurationService.getPropertyValueAsString("chartOfAccountsCode"));
        bibInfoBean.setOrganizationCode(kualiConfigurationService.getPropertyValueAsString("organizationCode"));
        bibInfoBean.setDocumentFundingSourceCode(kualiConfigurationService.getPropertyValueAsString("documentFundingSourceCode"));
        bibInfoBean.setUseTaxIndicator(kualiConfigurationService.getPropertyValueAsString("useTaxIndicator") == "true" ? true : false);
        bibInfoBean.setDeliveryCampusCode(kualiConfigurationService.getPropertyValueAsString("deliveryCampusCode"));
        bibInfoBean.setDeliveryBuildingOtherIndicator(kualiConfigurationService.getPropertyValueAsString("deliveryBuildingOtherIndicator") == "true" ? true : false);
        bibInfoBean.setDeliveryBuildingCode(kualiConfigurationService.getPropertyValueAsString("deliveryBuildingCode"));
        bibInfoBean.setDeliveryBuildingLine1Address(kualiConfigurationService.getPropertyValueAsString("deliveryBuildingLine1Address"));
        bibInfoBean.setDeliveryBuildingRoomNumber(kualiConfigurationService.getPropertyValueAsString("deliveryBuildingRoomNumber"));
        bibInfoBean.setDeliveryCityName(kualiConfigurationService.getPropertyValueAsString("deliveryCityName"));
        bibInfoBean.setDeliveryStateCode(kualiConfigurationService.getPropertyValueAsString("deliveryStateCode"));
        bibInfoBean.setDeliveryPostalCode(kualiConfigurationService.getPropertyValueAsString("deliveryPostalCode"));
        bibInfoBean.setDeliveryCountryCode(kualiConfigurationService.getPropertyValueAsString("deliveryCountryCode"));
        bibInfoBean.setDeliveryToName(kualiConfigurationService.getPropertyValueAsString("deliveryToName"));
/*        bibInfoBean.setVendorCode(properties.getProperty("userName"));
        bibInfoBean.setVendorCustomerNumber(properties.getProperty("userName"));*/
        bibInfoBean.setUom(kualiConfigurationService.getPropertyValueAsString("uom"));
        bibInfoBean.setItemTypeCode(kualiConfigurationService.getPropertyValueAsString("itemTypeCode"));
        bibInfoBean.setListprice(new Double(kualiConfigurationService.getPropertyValueAsString("listprice")));
        bibInfoBean.setQuantity(new Long(kualiConfigurationService.getPropertyValueAsString("quantity")));
        bibInfoBean.setPurchaseOrderTransmissionMethodCode(kualiConfigurationService.getPropertyValueAsString("purchaseOrderTransmissionMethodCode"));
        bibInfoBean.setPurchaseOrderCostSourceCode(kualiConfigurationService.getPropertyValueAsString("purchaseOrderCostSourceCode"));
        bibInfoBean.setRequestorPersonName(kualiConfigurationService.getPropertyValueAsString("requestorPersonName"));
        bibInfoBean.setRequestorPersonPhoneNumber(kualiConfigurationService.getPropertyValueAsString("requestorPersonPhoneNumber"));
        bibInfoBean.setRequestorPersonEmailAddress(kualiConfigurationService.getPropertyValueAsString("requestorPersonEmailAddress"));
        bibInfoBean.setLocation(kualiConfigurationService.getPropertyValueAsString("location"));
        bibInfoBean.setOrganizationAutomaticPurchaseOrderLimit(kualiConfigurationService.getPropertyValueAsString("organizationAutomaticPurchaseOrderLimit"));
        bibInfoBean.setPurchaseOrderAutomaticIndicator(kualiConfigurationService.getPropertyValueAsString("purchaseOrderAutomaticIndicator") == "true" ? true : false);
        bibInfoBean.setReceivingDocumentRequiredIndicator(kualiConfigurationService.getPropertyValueAsString("receivingDocumentRequiredIndicator") == "true" ? true : false);
        bibInfoBean.setPaymentRequestPositiveApprovalIndicator(kualiConfigurationService.getPropertyValueAsString("paymentRequestPositiveApprovalIndicator") == "true" ? true : false);
        /*bibInfoBean.setChart(properties.getProperty("chart"));
        bibInfoBean.setAccountNumber(properties.getProperty("accountNumber"));
        bibInfoBean.setObjectCode(properties.getProperty("objectCode"));
        bibInfoBean.setPercent(new Long(properties.getProperty("percent")));*/
        if (LOG.isDebugEnabled()) {
            LOG.debug("---------------Billing Name from property--------->" + kualiConfigurationService.getPropertyValueAsString("billingName"));
        }
        bibInfoBean.setBillingName(kualiConfigurationService.getPropertyValueAsString("billingName"));
        bibInfoBean.setBillingCityName(kualiConfigurationService.getPropertyValueAsString("billingCityName"));
        bibInfoBean.setBillingCountryCode(kualiConfigurationService.getPropertyValueAsString("billingCountryCode"));
        bibInfoBean.setBillingLine1Address(kualiConfigurationService.getPropertyValueAsString("billingLine1Address"));
        bibInfoBean.setBillingPhoneNumber(kualiConfigurationService.getPropertyValueAsString("billingPhoneNumber"));
        bibInfoBean.setBillingPostalCode(kualiConfigurationService.getPropertyValueAsString("billingPostalCode"));
        bibInfoBean.setBillingStateCode(kualiConfigurationService.getPropertyValueAsString("billingStateCode"));
       /* bibInfoBean.setLicensingRequirementIndicator(kualiConfigurationService.getPropertyValueAsBoolean("licensingRequirementIndicator"));*/
        /*bibInfoBean.setLicensingRequirementCode(kualiConfigurationService.getPropertyValueAsString("licensingRequirementCode"));*/
        return bibInfoBean;
    }


    @Override
    public String processBibInfoForOperURL(String openUrlString, BibInfoBean bibInfoBean) throws Exception {
        try {
            String user;
            if (GlobalVariables.getUserSession() != null) {
                user = GlobalVariables.getUserSession().getPrincipalName();
            } else {
                kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
                user = kualiConfigurationService.getPropertyValueAsString("userName");
            }
            GlobalVariables.setUserSession(new UserSession(user));
            BuildOpenUrlBibInfoBean buildOpenUrlBibInfoBean = SpringContext.getBean(BuildOpenUrlBibInfoBean.class);
            bibInfoBean = buildOpenUrlBibInfoBean.getBean(bibInfoBean, openUrlString);
            if (bibInfoBean.getTitle() == null || "".equalsIgnoreCase(bibInfoBean.getTitle())) {
                return OleSelectConstant.SOAP_INVALID_OPENURL;
            }
            bibInfoBean.setRequestSourceUrl(openUrlString);
            //bibInfoBean.setRequestSource(OleSelectConstant.REQUEST_SRC_TYPE_WEBFORM);
            bibInfoBean.setRequisitionSource(OleSelectConstant.REQUISITON_SRC_TYPE_WEBFORM);
            bibInfoBean.setDocStoreOperation(OleSelectConstant.DOCSTORE_OPERATION_WEBFORM);
            bibInfoBean = setBibInfoDefaultValues(bibInfoBean);
            OleRequisitionCreateDocumentService createDocument = SpringContext.getBean(OleRequisitionCreateDocumentServiceImpl.class);
            List<BibInfoBean> bibInfoBeanList = getBibInfoBeanList(bibInfoBean);
            String docNumber = createDocument.saveRequisitionDocument(bibInfoBeanList, true);
            return docNumber;
        } catch (Exception e) {
            LOG.error("Exception processing for SOAP citation document creation----" + e.getMessage(), e);
            String errorMessage = null;
            if (GlobalVariables.getMessageMap().hasErrors()) {
                Map<String, AutoPopulatingList<ErrorMessage>> map = GlobalVariables.getMessageMap().getErrorMessages();
                for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> entry : map.entrySet()) {
                    AutoPopulatingList<ErrorMessage> errors = entry.getValue();
                    ErrorMessage error = errors.get(0);
                    String[] params = error.getMessageParameters();
                    errorMessage = params[0];
                }
            }
            return OleSelectConstant.SOAP_EXCEPTION + " - " + errorMessage;
        }
    }


    @Override
    public String processBibInfoForForm(BibInfoBean bibInfoBean, String title, String author, String edition, String series, String publisher, String placeOfPublication, String yearOfPublication, String standardNumber, String typeOfStandardNumber,
                                        String routeRequesterReceipt) throws Exception {
        try {
            String user;
/*            if(GlobalVariables.getUserSession()!=null){
                user = GlobalVariables.getUserSession().getPrincipalName();
            }else{*/
            kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
            user = kualiConfigurationService.getPropertyValueAsString("userName");
            // }
            if (LOG.isDebugEnabled()){
                LOG.debug("userName in processBibInfoForForm>>>>>>>>>>>>>>" + user);
            }
            GlobalVariables.setUserSession(new UserSession(user));
            BuildFormBibInfoBean buildFormBibInfoBean = SpringContext.getBean(BuildFormBibInfoBean.class);
            bibInfoBean = buildFormBibInfoBean.getBean(bibInfoBean, title, author, edition, series, publisher, placeOfPublication, yearOfPublication, standardNumber, typeOfStandardNumber, routeRequesterReceipt);
            bibInfoBean = setBibInfoDefaultValues(bibInfoBean);
            //bibInfoBean.setRequestSource(OleSelectConstant.REQUEST_SRC_TYPE_WEBFORM);
            bibInfoBean.setRequisitionSource(OleSelectConstant.REQUISITON_SRC_TYPE_WEBFORM);
            bibInfoBean.setDocStoreOperation(OleSelectConstant.DOCSTORE_OPERATION_WEBFORM);
            OleRequisitionCreateDocumentService createDocument = SpringContext.getBean(OleRequisitionCreateDocumentServiceImpl.class);
            List<BibInfoBean> bibInfoBeanList = getBibInfoBeanList(bibInfoBean);
            String docNumber = createDocument.saveRequisitionDocument(bibInfoBeanList, true);
            return docNumber;
        } catch (Exception e) {
            LOG.error("Exception processing for SOAP form document creation----" + e.getMessage(), e);
            String errorMessage = null;
            if (GlobalVariables.getMessageMap().hasErrors()) {
                Map<String, AutoPopulatingList<ErrorMessage>> map = GlobalVariables.getMessageMap().getErrorMessages();
                for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> entry : map.entrySet()) {
                    AutoPopulatingList<ErrorMessage> errors = entry.getValue();
                    ErrorMessage error = errors.get(0);
                    String[] params = error.getMessageParameters();
                    errorMessage = params[0];
                }
            }
            return OleSelectConstant.SOAP_EXCEPTION + " - " + errorMessage;
        }
    }

    private List<BibInfoBean> getBibInfoBeanList(BibInfoBean bibInfoBean) throws Exception {
        return Collections.singletonList(bibInfoBean);

    }

    public ConfigurationService getConfigurationService() {
        return kualiConfigurationService;
    }

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }


}
