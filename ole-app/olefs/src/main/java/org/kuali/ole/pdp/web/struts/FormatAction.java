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
package org.kuali.ole.pdp.web.struts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.ole.pdp.PdpConstants;
import org.kuali.ole.pdp.PdpKeyConstants;
import org.kuali.ole.pdp.PdpParameterConstants;
import org.kuali.ole.pdp.PdpPropertyConstants;
import org.kuali.ole.pdp.businessobject.CustomerProfile;
import org.kuali.ole.pdp.businessobject.FormatProcessSummary;
import org.kuali.ole.pdp.businessobject.FormatSelection;
import org.kuali.ole.pdp.businessobject.ProcessSummary;
import org.kuali.ole.pdp.service.FormatService;
import org.kuali.ole.pdp.service.PdpAuthorizationService;
import org.kuali.ole.pdp.service.impl.exception.FormatException;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.kns.web.struts.action.KualiAction;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

/**
 * This class provides actions for the format process
 */
public class FormatAction extends KualiAction {

    private FormatService formatService;

    /**
     * Constructs a FormatAction.java.
     */
    public FormatAction() {
        formatService = SpringContext.getBean(FormatService.class);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PdpAuthorizationService authorizationService = SpringContext.getBean(PdpAuthorizationService.class);
        
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        String methodToCall = findMethodToCall(form, request);

        if (!authorizationService.hasFormatPermission(kualiUser.getPrincipalId())) {
            throw new AuthorizationException(kualiUser.getPrincipalName(), methodToCall, kualiUser.getCampusCode());
        }

        return super.execute(mapping, form, request, response);
    }

    /**
     * This method prepares the data for the format process
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;
        
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        FormatSelection formatSelection = formatService.getDataForFormat(kualiUser);
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

        formatForm.setCampus(kualiUser.getCampusCode());

        // no data for format because another format process is already running
        if (formatSelection.getStartDate() != null) {
            GlobalVariables.getMessageMap().putError(OLEConstants.GLOBAL_ERRORS, PdpKeyConstants.Format.ERROR_PDP_FORMAT_PROCESS_ALREADY_RUNNING, dateTimeService.toDateTimeString(formatSelection.getStartDate()));
        }
        else {
            List<CustomerProfile> customers = formatSelection.getCustomerList();

            for (CustomerProfile element : customers) {

                if (formatSelection.getCampus().equals(element.getDefaultPhysicalCampusProcessingCode())) {
                    element.setSelectedForFormat(Boolean.TRUE);
                }
                else {
                    element.setSelectedForFormat(Boolean.FALSE);
                }
            }

            formatForm.setPaymentDate(dateTimeService.toDateString(dateTimeService.getCurrentTimestamp()));
            formatForm.setPaymentTypes(PdpConstants.PaymentTypes.ALL);
            formatForm.setCustomers(customers);
            formatForm.setRanges(formatSelection.getRangeList());
        }
        
        return mapping.findForward(PdpConstants.MAPPING_SELECTION);
    }

    /**
     * This method marks the payments for format
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward prepare(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

        if (formatForm.getCampus() == null) {
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }

        // Figure out which ones they have selected
        List selectedCustomers = new ArrayList();

        for (CustomerProfile customer : formatForm.getCustomers()) {
            if (customer.isSelectedForFormat()) {
                selectedCustomers.add(customer);
            }
        }

        Date paymentDate = dateTimeService.convertToSqlDate(formatForm.getPaymentDate());
        Person kualiUser = GlobalVariables.getUserSession().getPerson();

        FormatProcessSummary formatProcessSummary = formatService.startFormatProcess(kualiUser, formatForm.getCampus(), selectedCustomers, paymentDate, formatForm.getPaymentTypes());
        if (formatProcessSummary.getProcessSummaryList().size() == 0) {
            KNSGlobalVariables.getMessageList().add(PdpKeyConstants.Format.ERROR_PDP_NO_MATCHING_PAYMENT_FOR_FORMAT);
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }

        formatForm.setFormatProcessSummary(formatProcessSummary);

        return mapping.findForward(PdpConstants.MAPPING_CONTINUE);
    }

    /**
     * This method performs the format process.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueFormat(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;
        KualiInteger processId = formatForm.getFormatProcessSummary().getProcessId();

        try {
            formatService.performFormat(processId.intValue());
        }
        catch (FormatException e) {
            // errors added to global message map
            return mapping.findForward(PdpConstants.MAPPING_CONTINUE);
        }

        String lookupUrl = buildUrl(String.valueOf(processId.intValue()));
        return new ActionForward(lookupUrl, true);
    }

    /**
     * This method clears all the customer checkboxes.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        List<CustomerProfile> customers = formatForm.getCustomers();
        for (CustomerProfile customerProfile : customers) {
            customerProfile.setSelectedForFormat(false);
        }
        formatForm.setCustomers(customers);
        
        return mapping.findForward(PdpConstants.MAPPING_SELECTION);

    }

    /**
     * This method cancels the format process
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;
        
        KualiInteger processId = formatForm.getFormatProcessSummary().getProcessId();

        if (processId != null) {
            formatService.clearUnfinishedFormat(processId.intValue());
        }
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);

    }

    /**
     * This method clears the unfinished format process and is called from the FormatProcess lookup page.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward clearUnfinishedFormat(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String processIdParam = request.getParameter(PdpParameterConstants.FormatProcess.PROCESS_ID_PARAM);
        Integer processId = Integer.parseInt(processIdParam);

        if (processId != null) {
            formatService.resetFormatPayments(processId);
        }

        return mapping.findForward(KRADConstants.MAPPING_PORTAL);

    }

    /**
     * This method builds the forward url for the format summary lookup page.
     * 
     * @param processId the batch id
     * @return the built url
     */
    private String buildUrl(String processId) {
        String basePath = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(OLEConstants.APPLICATION_URL_KEY);

        Properties parameters = new Properties();
        parameters.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, OLEConstants.SEARCH_METHOD);
        parameters.put(OLEConstants.BACK_LOCATION, basePath + "/" + OLEConstants.MAPPING_PORTAL + ".do");
        parameters.put(KRADConstants.DOC_FORM_KEY, "88888888");
        parameters.put(OLEConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, ProcessSummary.class.getName());
        parameters.put(OLEConstants.HIDE_LOOKUP_RETURN_LINK, "true");
        parameters.put(OLEConstants.SUPPRESS_ACTIONS, "false");
        parameters.put(PdpPropertyConstants.ProcessSummary.PROCESS_SUMMARY_PROCESS_ID, processId);

        String lookupUrl = UrlFactory.parameterizeUrl(basePath + "/" + OLEConstants.LOOKUP_ACTION, parameters);

        return lookupUrl;
    }
}
