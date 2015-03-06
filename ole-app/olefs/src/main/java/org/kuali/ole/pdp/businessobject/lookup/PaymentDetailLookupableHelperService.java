/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.pdp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.pdp.PdpConstants;
import org.kuali.ole.pdp.PdpKeyConstants;
import org.kuali.ole.pdp.PdpParameterConstants;
import org.kuali.ole.pdp.PdpPropertyConstants;
import org.kuali.ole.pdp.businessobject.PaymentDetail;
import org.kuali.ole.pdp.businessobject.PaymentGroupHistory;
import org.kuali.ole.pdp.service.PdpAuthorizationService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.web.format.BooleanFormatter;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

public class PaymentDetailLookupableHelperService extends KualiLookupableHelperServiceImpl {
    public static final String PDP_PAYMENTDETAIL_KEY = "PDPHOLDKEY";
    private ConfigurationService kualiConfigurationService;
    private PdpAuthorizationService pdpAuthorizationService;

    /**
     * @see org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        Map parameters = super.getParameters();
        String errorList;

        if (parameters.containsKey(PdpParameterConstants.ACTION_SUCCESSFUL_PARAM)) {
            String[] actionSuccessRequestParm = (String[]) parameters.get(PdpParameterConstants.ACTION_SUCCESSFUL_PARAM);
            Boolean actionSuccess = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(actionSuccessRequestParm[0]);

            if (actionSuccess != null) {

                if (!actionSuccess) {

                    // if the action performed on payment was not successful we get the error message list and add them to
                    // GlobalVariables errorMap
                    if (parameters.containsKey(PdpParameterConstants.ERROR_KEY_LIST_PARAM)) {
                        String[] errorListParam = (String[]) parameters.get(PdpParameterConstants.ERROR_KEY_LIST_PARAM);
                        errorList = errorListParam[0];
                        if (StringUtils.isNotEmpty(errorList)) {
                            String[] errorMsgs = StringUtils.split(errorList, PdpParameterConstants.ERROR_KEY_LIST_SEPARATOR);
                            for (String error : errorMsgs) {
                                if (StringUtils.isNotEmpty(error)) {
                                    GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, error);
                                }
                            }
                        }
                    }
                }
                else {
                    if (parameters.containsKey(PdpParameterConstants.MESSAGE_PARAM)) {
                        String[] messageRequestParm = (String[]) parameters.get(PdpParameterConstants.MESSAGE_PARAM);
                        String message = messageRequestParm[0];
                        KNSGlobalVariables.getMessageList().add(message);
                    }
                }
            }
        }

        List paymentDetailsFromPaymentGroupHistoryList = new ArrayList();
        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER)) {
            String disbursementNumberValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER);
            if (!StringUtils.isEmpty(disbursementNumberValue)) {
                List resultsForPaymentGroupHistory = searchForPaymentGroupHistory(fieldValues);
                paymentDetailsFromPaymentGroupHistoryList = getPaymentDetailsFromPaymentGroupHistoryList(resultsForPaymentGroupHistory);
            }
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE)) {
            String paymentStatusCodeValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE);
            if (!StringUtils.isEmpty(paymentStatusCodeValue) && paymentStatusCodeValue.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_ALL)) {
                paymentStatusCodeValue = PdpConstants.PaymentStatusCodes.HELD_TAX_ALL_FOR_SEARCH;
                fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE, paymentStatusCodeValue);
            }
        }

        List searchResults = super.getSearchResults(fieldValues);

        searchResults.addAll(paymentDetailsFromPaymentGroupHistoryList);

        sortResultListByPayeeName(searchResults);

        return searchResults;
    }

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#validateSearchParameters(java.util.Map)
     */
    @Override
    public void validateSearchParameters(Map fieldValues) {
        super.validateSearchParameters(fieldValues);

        // get field values
        String custPaymentDocNbrValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_DOC_NUMBER);
        String invoiceNbrValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_INVOICE_NUMBER);
        String purchaseOrderNbrValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PURCHASE_ORDER_NUMBER);
        String processIdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_ID);
        String paymentIdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_ID);
        String payeeNameValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_NAME);
        String payeeIdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID);
        String payeeIdTypeCdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID_TYPE_CODE);
        String disbursementTypeCodeValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_TYPE_CODE);
        String paymentStatusCodeValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE);
        String netPaymentAmountValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_NET_AMOUNT);
        String disbursementDateValueLower = (String) fieldValues.get(KRADConstants.LOOKUP_RANGE_LOWER_BOUND_PROPERTY_PREFIX + PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_DATE);
        String disbursementDateValueUpper = (String) fieldValues.get(KRADConstants.LOOKUP_DEFAULT_RANGE_SEARCH_UPPER_BOUND_LABEL + PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_DATE);
        String paymentDateValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DATE);
        String disbursementNbrValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER);
        String chartCodeValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CHART_CODE);
        String orgCodeValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_UNIT_CODE);
        String subUnitCodeValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_SUBUNIT_CODE);
        String requisitionNbrValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_REQUISITION_NUMBER);
        String customerInstitutionNumberValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_INSTITUTION_NUMBER);
        String pymtAttachmentValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_ATTACHMENT);
        String processImmediateValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_INSTITUTION_NUMBER);
        String pymtSpecialHandlingValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_SPECIAL_HANDLING);
        String batchIdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP_BATCH_ID);
        String paymentGroupIdValue = (String) fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DETAIL_PAYMENT_GROUP_ID);

        if ((StringUtils.isEmpty(custPaymentDocNbrValue)) && (StringUtils.isEmpty(invoiceNbrValue)) && (StringUtils.isEmpty(purchaseOrderNbrValue)) && (StringUtils.isEmpty(processIdValue)) && (StringUtils.isEmpty(paymentIdValue)) && (StringUtils.isEmpty(payeeNameValue)) && (StringUtils.isEmpty(payeeIdValue)) && (StringUtils.isEmpty(payeeIdTypeCdValue)) && (StringUtils.isEmpty(disbursementTypeCodeValue)) && (StringUtils.isEmpty(paymentStatusCodeValue)) && (StringUtils.isEmpty(netPaymentAmountValue)) &&
                (StringUtils.isEmpty(disbursementDateValueLower)) && (StringUtils.isEmpty(disbursementDateValueUpper)) && (StringUtils.isEmpty(paymentDateValue)) && (StringUtils.isEmpty(disbursementNbrValue)) && (StringUtils.isEmpty(chartCodeValue)) && (StringUtils.isEmpty(orgCodeValue)) && (StringUtils.isEmpty(subUnitCodeValue)) && (StringUtils.isEmpty(requisitionNbrValue)) && (StringUtils.isEmpty(customerInstitutionNumberValue)) && (StringUtils.isEmpty(pymtAttachmentValue)) && (StringUtils.isEmpty(processImmediateValue))
                && (StringUtils.isEmpty(pymtSpecialHandlingValue)) && (StringUtils.isEmpty(batchIdValue)) && (StringUtils.isEmpty(paymentGroupIdValue))) {

            GlobalVariables.getMessageMap().putError(OLEConstants.DOCUMENT_HEADER_ERRORS, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_DETAIL_CRITERIA_NOT_ENTERED);
        }
        else {
            if ((StringUtils.isNotEmpty(payeeIdValue)) && (StringUtils.isEmpty(payeeIdTypeCdValue))) {
                GlobalVariables.getMessageMap().putError(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID_TYPE_CODE, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_DETAIL_PAYEE_ID_TYPE_CODE_NULL_WITH_PAYEE_ID);
            }
            if ((StringUtils.isEmpty(payeeIdValue)) && (StringUtils.isNotEmpty(payeeIdTypeCdValue))) {
                GlobalVariables.getMessageMap().putError(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID, PdpKeyConstants.PaymentDetail.ErrorMessages.ERROR_PAYMENT_DETAIL_PAYEE_ID_NULL_WITH_PAYEE_ID_TYPE_CODE);
            }
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException("errors in search criteria");
        }
        
        buildAndStoreReturnUrl(fieldValues);     
     }

    /** 
     * Create a return URL for hold, cancel,set as and immediate keys to return back.  - the return url is stored and then retreived in PaymentDetailAction buildURL
     * 
     * @param fieldValues
     */
    protected void buildAndStoreReturnUrl(Map<String, String> fieldValues) {       
        String basePath = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(OLEConstants.APPLICATION_URL_KEY);
        Properties parameters = new Properties();
        parameters.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, OLEConstants.SEARCH_METHOD);
        parameters.put(OLEConstants.BACK_LOCATION, basePath + "/" + OLEConstants.MAPPING_PORTAL + ".do");
        parameters.put(KRADConstants.DOC_FORM_KEY, "88888888");
        parameters.put(OLEConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, PaymentDetail.class.getName());
        parameters.put(OLEConstants.HIDE_LOOKUP_RETURN_LINK, "true");
        parameters.put(OLEConstants.SUPPRESS_ACTIONS, "false");
        parameters.putAll(fieldValues);
        Object lookupUrl = UrlFactory.parameterizeUrl(basePath + "/" + OLEConstants.LOOKUP_ACTION, parameters);
        GlobalVariables.getUserSession().addObject(PDP_PAYMENTDETAIL_KEY,  lookupUrl);
    }
        
    
    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getCustomActionUrls(org.kuali.rice.krad.bo.BusinessObject,
     *      java.util.List)
     */
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        if (businessObject instanceof PaymentDetail) {
            Person person = GlobalVariables.getUserSession().getPerson();
            PaymentDetail paymentDetail = (PaymentDetail) businessObject;
            Integer paymentDetailId = paymentDetail.getId().intValue();
            String paymentDetailStatus = paymentDetail.getPaymentGroup().getPaymentStatusCode();
            List<HtmlData> anchorHtmlDataList = new ArrayList<HtmlData>();
            String linkText = OLEConstants.EMPTY_STRING;
            String url = OLEConstants.EMPTY_STRING;
            String basePath = kualiConfigurationService.getPropertyValueAsString(OLEConstants.APPLICATION_URL_KEY) + "/" + PdpConstants.Actions.PAYMENT_DETAIL_ACTION;

            boolean showCancel = paymentDetailStatus != null && ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.OPEN) && pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId())) || (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_CD) && pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId())) || ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_EMPLOYEE_CD) || paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_NRA_CD) || paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_NRA_EMPL_CD)) && pdpAuthorizationService.hasRemovePaymentTaxHoldPermission(person.getPrincipalId())));

            if (showCancel) {
                Properties params = new Properties();

                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_CANCEL_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));

                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.CANCEL_PAYMENT);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_CANCEL_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);
            }

            boolean showHold = paymentDetailStatus != null && (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.OPEN) && pdpAuthorizationService.hasHoldPaymentPermission(person.getPrincipalId()));
            if (showHold) {
                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_HOLD_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.HOLD_PAYMENT);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_HOLD_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }
            boolean showRemoveTaxHold = paymentDetailStatus != null && (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_NRA_CD) && pdpAuthorizationService.hasRemovePaymentTaxHoldPermission(person.getPrincipalId()));
            if (showRemoveTaxHold) {
                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_REMOVE_HOLD_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.REMOVE_HTXN_HOLD);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_REMOVE_HOLD_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);
            }
            
            boolean showRemoveImmediatePrint = paymentDetailStatus != null && (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.OPEN) && pdpAuthorizationService.hasSetAsImmediatePayPermission(person.getPrincipalId()) && paymentDetail.getPaymentGroup().getProcessImmediate());

            if (showRemoveImmediatePrint) {

                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_REMOVE_IMMEDIATE_PRINT_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.REMOVE_IMMEDIATE_PRINT);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_REMOVE_IMMEDIATE_PRINT_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }

            boolean showSetImmediatePrint = paymentDetailStatus != null && (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.OPEN) && pdpAuthorizationService.hasSetAsImmediatePayPermission(person.getPrincipalId()) && !paymentDetail.getPaymentGroup().getProcessImmediate());

            if (showSetImmediatePrint) {

                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_SET_IMMEDIATE_PRINT_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.SET_IMMEDIATE_PRINT);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_SET_IMMEDIATE_PRINT_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }

            boolean showRemoveHold = paymentDetailStatus != null && ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_CD) && pdpAuthorizationService.hasHoldPaymentPermission(person.getPrincipalId())) || ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_EMPLOYEE_CD) || paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.HELD_TAX_NRA_EMPL_CD)) && pdpAuthorizationService.hasRemovePaymentTaxHoldPermission(person.getPrincipalId())));

            if (showRemoveHold) {

                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_REMOVE_HOLD_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.REMOVE_PAYMENT_HOLD);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_REMOVE_HOLD_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }

            boolean showDisbursementCancel = paymentDetailStatus != null && ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.PENDING_ACH) && (pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId()))) || (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.EXTRACTED) && pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId()) && paymentDetail.getPaymentGroup().getDisbursementDate() != null && paymentDetail.isDisbursementActionAllowed()));

            if (showDisbursementCancel) {

                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_DISBURSEMENT_CANCEL_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.CANCEL_DISBURSEMENT);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_DISBURSEMENT_CANCEL_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }
            
            boolean showReissue = paymentDetailStatus != null && (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT) && (pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId()) && paymentDetail.isDisbursementActionAllowed()));
            if (showReissue) {
                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_REISSUE_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.REISSUE);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_REISSUE_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);                
            }

            boolean showReissueCancel = paymentDetailStatus != null && ((paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.PENDING_ACH) && (pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId()))) || (paymentDetailStatus.equalsIgnoreCase(PdpConstants.PaymentStatusCodes.EXTRACTED) && pdpAuthorizationService.hasCancelPaymentPermission(person.getPrincipalId()) && paymentDetail.getPaymentGroup().getDisbursementDate() != null && paymentDetail.isDisbursementActionAllowed()));

            if (showReissueCancel) {

                Properties params = new Properties();
                params.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, PdpConstants.ActionMethods.CONFIRM_REISSUE_CANCEL_ACTION);
                params.put(PdpParameterConstants.PaymentDetail.DETAIL_ID_PARAM, UrlFactory.encode(String.valueOf(paymentDetailId)));
                url = UrlFactory.parameterizeUrl(basePath, params);

                linkText = kualiConfigurationService.getPropertyValueAsString(PdpKeyConstants.PaymentDetail.LinkText.REISSUE_CANCEL);

                AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, PdpConstants.ActionMethods.CONFIRM_REISSUE_CANCEL_ACTION, linkText);
                anchorHtmlDataList.add(anchorHtmlData);

            }

            if (anchorHtmlDataList.isEmpty()) {
                AnchorHtmlData anchorHtmlData = new AnchorHtmlData("&nbsp;", "", "");
                anchorHtmlDataList.add(anchorHtmlData);
            }
            return anchorHtmlDataList;
        }
        return super.getEmptyActionUrls();
    }

    /**
     * This method builds the search fields for PaymentGroupHistory.
     * 
     * @param fieldValues entry fields from PaymentDetail
     * @return the fields map
     */
    private Map<String, String> buildSearchFieldsMapForPaymentGroupHistory(Map<String, String> fieldValues) {
        Map resultMap = new Properties();
        String fieldValue = OLEConstants.EMPTY_STRING;

        //TODO removed
//        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_INSTITUTION_NUMBER)) {
//            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_INSTITUTION_NUMBER);
//            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_CUSTOMER_INSTITUTION_NUMBER, fieldValue);
//        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_NAME)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_NAME);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_PAYEE_NAME, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_PAYEE_ID, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_ATTACHMENT)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_ATTACHMENT);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_PAYMENT_ATTACHMENT, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_SPECIAL_HANDLING)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_SPECIAL_HANDLING);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_PAYMENT_SPECIAL_HANDLING, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMEDIATE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMEDIATE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_PROCESS_IMMEDIATE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_DISBURSEMENT_NUMBER, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_ID)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_ID);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_PAYMENT_PROCESS_ID, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_NET_AMOUNT)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_NET_AMOUNT);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_PAYMENT_DETAILS_NET_AMOUNT, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_DATE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_DATE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_DISBURSE_DATE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_DATE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DATE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_PAYMENT_DATE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_STATUS_CODE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORIGIN_PAYMENT_STATUS_CODE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_TYPE_CODE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_TYPE_CODE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_CHART_CODE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_CHART_CODE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_CHART_CODE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_UNIT_CODE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_UNIT_CODE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_ORG_CODE, fieldValue);
        }

        if (fieldValues.containsKey(PdpPropertyConstants.PaymentDetail.PAYMENT_SUBUNIT_CODE)) {
            fieldValue = fieldValues.get(PdpPropertyConstants.PaymentDetail.PAYMENT_SUBUNIT_CODE);
            resultMap.put(PdpPropertyConstants.PaymentGroupHistory.PAYMENT_GROUP_SUB_UNIT_CODE, fieldValue);
        }

        return resultMap;

    }

    /**
     * This method searches for PaymentGroupHistory
     * 
     * @param fieldValues search field values
     * @return the list of PaymentGroupHistory
     */
    private List searchForPaymentGroupHistory(Map<String, String> fieldValues) {
        List resultsForPaymentGroupHistory = new ArrayList();
        Map fieldsForPaymentGroupHistory = buildSearchFieldsMapForPaymentGroupHistory(fieldValues);
        resultsForPaymentGroupHistory = (List) getLookupService().findCollectionBySearchHelper(PaymentGroupHistory.class, fieldsForPaymentGroupHistory, false);
        return resultsForPaymentGroupHistory;
    }

    /**
     * This method gets the PaymentDetails for the given list og PaymentGroupHistory
     * 
     * @param resultsForPaymentGroupHistory the list of PaymentGoupHistory objects
     * @return the list of PaymentDetails
     */
    private List getPaymentDetailsFromPaymentGroupHistoryList(List resultsForPaymentGroupHistory) {
        List finalResults = new ArrayList();
        for (Iterator iter = resultsForPaymentGroupHistory.iterator(); iter.hasNext();) {
            PaymentGroupHistory pgh = (PaymentGroupHistory) iter.next();
            finalResults.addAll(pgh.getPaymentGroup().getPaymentDetails());
        }
        return finalResults;
    }

    /**
     * This method sorts the given list by payee name
     * 
     * @param searchResults the list to be sorted
     */
    protected void sortResultListByPayeeName(List searchResults) {
        Collections.sort(searchResults, new Comparator() {
            public int compare(Object o1, Object o2) {
                PaymentDetail detail1 = (org.kuali.ole.pdp.businessobject.PaymentDetail) o1;
                PaymentDetail detail2 = (org.kuali.ole.pdp.businessobject.PaymentDetail) o2;
                
                if (detail1 == null || detail1.getPaymentGroup() == null || detail1.getPaymentGroup().getPayeeName() == null) {
                    return -1;
                }
                
                if (detail2 == null || detail2.getPaymentGroup() == null || detail2.getPaymentGroup().getPayeeName() == null) {
                    return 1;
                }                
                
                return detail1.getPaymentGroup().getPayeeName().compareTo(detail2.getPaymentGroup().getPayeeName());
            }
        });
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * 
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * This method sets the pdpAuthorizationService attribute valuse
     * 
     * @param pdpAuthorizationService The pdpAuthorizationService to set.
     */
    public void setPdpAuthorizationService(PdpAuthorizationService pdpAuthorizationService) {
        this.pdpAuthorizationService = pdpAuthorizationService;
    }

}
