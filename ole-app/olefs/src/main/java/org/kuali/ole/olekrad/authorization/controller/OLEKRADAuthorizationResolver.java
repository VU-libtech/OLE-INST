package org.kuali.ole.olekrad.authorization.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.olekrad.authorization.form.OLEKRADAuthorizationForm;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.view.HistoryEntry;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.controller.UifControllerHelper;
import org.kuali.rice.krad.web.controller.UifHandlerExceptionResolver;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: sheiksalahudeenm
 * Date: 8/22/13
 * Time: 9:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEKRADAuthorizationResolver extends UifHandlerExceptionResolver {
    private static final Logger LOG = Logger.getLogger(OLEKRADAuthorizationResolver.class);
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        //LOG.error("The following error was caught by the UifHandlerExceptionResolver : ", ex);

        // log exception
        //LOG.error(ex.getMessage(), ex);
        if(ex!=null && ex.getMessage()!=null && ex.getMessage().contains("is not authorized")){

            String incidentDocId = request.getParameter(KRADConstants.DOCUMENT_DOCUMENT_NUMBER);
            String incidentViewId = "";

            UifFormBase form = (UifFormBase)request.getAttribute(UifConstants.REQUEST_FORM);
            if (form instanceof DocumentFormBase) {
                if (((DocumentFormBase) form).getDocument() != null) {
                    incidentDocId = ((DocumentFormBase) form).getDocument().getDocumentNumber();
                }
                incidentViewId = ((DocumentFormBase) form).getViewId();
            }
            GlobalVariables.getUifFormManager().removeSessionForm(form);

            UserSession userSession = (UserSession) request.getSession().getAttribute(KRADConstants.USER_SESSION_KEY);

            OLEKRADAuthorizationForm oleKradAuthorizationForm = new OLEKRADAuthorizationForm();
            oleKradAuthorizationForm.setDocId(incidentDocId);
            oleKradAuthorizationForm.setPrincipalId(userSession.getPrincipalId());
            oleKradAuthorizationForm.setError("Error Message      :");
            if(form instanceof LookupForm){
                oleKradAuthorizationForm.setInformation("You are not authorized to view \" "+((LookupForm) form).getView().getHeaderText()+" \"");

            }
            else if(form instanceof UifFormBase){
                oleKradAuthorizationForm.setInformation("You are not authorized to view \" "+((UifFormBase) form).getView().getHeaderText()+" \"");
            }
            else{
                oleKradAuthorizationForm.setInformation("You are not authorized to view this Document");
            }



            // Set the post url map to the incident report controller and not
            // the one the exception occurred on
            String postUrl = request.getRequestURL().toString();
            postUrl = postUrl.substring(0, postUrl.lastIndexOf("/")) + "/incidentReport";
            oleKradAuthorizationForm.setFormPostUrl(postUrl);

            oleKradAuthorizationForm.setViewId("OLEKRADAuthorizationView");

            if (form != null) {
                oleKradAuthorizationForm.setAjaxRequest(form.isAjaxRequest());
            } else {
                String ajaxRequestParm = request.getParameter(UifParameters.AJAX_REQUEST);
                if (StringUtils.isNotBlank(ajaxRequestParm)) {
                    oleKradAuthorizationForm.setAjaxRequest(Boolean.parseBoolean(ajaxRequestParm));
                }
            }

            // Set the view object
            oleKradAuthorizationForm.setView(getViewService().getViewById("OLEKRADAuthorizationView"));

            // Add a new History entry to avoid errors in the postHandle

            HistoryEntry entry = new HistoryEntry("", "", "Incident Report", "", "");

            // oleKradAuthorizationForm.setFormHistory(history);

            // Set the ajax return type
            oleKradAuthorizationForm.setAjaxReturnType(UifConstants.AjaxReturnTypes.UPDATEVIEW.getKey());

            ModelAndView modelAndView = UifControllerHelper.getUIFModelAndView(oleKradAuthorizationForm, "");
            try {
                UifControllerHelper.postControllerHandle(request, response, handler, modelAndView);
            } catch (Exception e) {
                LOG.error("An error stopped the incident form from loading", e);
            }

            return modelAndView;
        }
        else {
            return super.resolveException(request,response,handler,ex);

        }




    }
}