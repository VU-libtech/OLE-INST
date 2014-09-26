/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.web.struts;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.ole.module.purap.PurapKeyConstants;
import org.kuali.ole.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.ole.module.purap.businessobject.RequisitionItem;
import org.kuali.ole.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.ole.module.purap.document.RequisitionDocument;
import org.kuali.ole.module.purap.document.service.PurapService;
import org.kuali.ole.select.document.OleRequisitionDocument;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.PersistenceService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Struts Action for Requisition document.
 */
public class RequisitionAction extends PurchasingActionBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RequisitionAction.class);

    /**
     * save the document without any validations.....
     *
     * @see org.kuali.ole.sys.web.struts.KualiAccountingDocumentActionBase#save(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //call the super save to save the document without validations...

        ActionForward actionForward = super.save(mapping, form, request, response);

        //we need to make "calculated" to false so that the "below lines"
        //can be edited until calculated button is clicked.
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingAccountsPayableDocument purapDocument = (PurchasingAccountsPayableDocument) kualiDocumentFormBase.getDocument();

        baseForm.setCalculated(false);
        purapDocument.setCalculated(false);

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    /**
     * Does initialization for a new requisition.
     *
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#createDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.createDocument(kualiDocumentFormBase);
        ((RequisitionDocument) kualiDocumentFormBase.getDocument()).initiateDocument();
    }

    public ActionForward setAsDefaultBuilding(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequisitionDocument req = (RequisitionDocument) ((RequisitionForm) form).getDocument();

        if (ObjectUtils.isNotNull(req.getDeliveryCampusCode()) && ObjectUtils.isNotNull(req.getDeliveryBuildingCode())) {
            DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(GlobalVariables.getUserSession().getPerson().getPrincipalId());
            Map addressKeys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(defaultPrincipalAddress);
            defaultPrincipalAddress = (DefaultPrincipalAddress) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(DefaultPrincipalAddress.class, addressKeys);

            if (ObjectUtils.isNull(defaultPrincipalAddress)) {
                defaultPrincipalAddress = new DefaultPrincipalAddress(GlobalVariables.getUserSession().getPerson().getPrincipalId());
            }

            defaultPrincipalAddress.setDefaultBuilding(req.getDeliveryCampusCode(), req.getDeliveryBuildingCode(), req.getDeliveryBuildingRoomNumber());
            SpringContext.getBean(BusinessObjectService.class).save(defaultPrincipalAddress);
            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.DEFAULT_BUILDING_SAVED);
        }

        return mapping.findForward(OLEConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#refresh(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = super.refresh(mapping, form, request, response);
        RequisitionForm rqForm = (RequisitionForm) form;
        RequisitionDocument document = (RequisitionDocument) rqForm.getDocument();

        // super.refresh() must occur before this line to get the correct APO limit
        document.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class).getApoLimit(document.getVendorContractGeneratedIdentifier(), document.getChartOfAccountsCode(), document.getOrganizationCode()));

        return forward;
    }

    /**
     * Adds a PurchasingItemCapitalAsset (a container for the Capital Asset Number) to the selected
     * item's list.
     *
     * @param mapping  An ActionMapping
     * @param form     The Form
     * @param request  An HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward addAsset(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequisitionForm rqForm = (RequisitionForm) form;
        RequisitionDocument document = (RequisitionDocument) rqForm.getDocument();
        RequisitionItem item = (RequisitionItem) document.getItemByLineNumber(getSelectedLine(request) + 1);
        //TODO: Add a new way to add assets to the system.
        //item.addAsset();
        return mapping.findForward(OLEConstants.MAPPING_BASIC);
    }

    public ActionForward displayB2BRequisition(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequisitionForm reqForm = (RequisitionForm) form;
        reqForm.setDocId((String) request.getSession().getAttribute("docId"));
        loadDocument(reqForm);
        String multipleB2BReqs = (String) request.getSession().getAttribute("multipleB2BRequisitions");
        if (StringUtils.isNotEmpty(multipleB2BReqs)) {
            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.B2B_MULTIPLE_REQUISITIONS);
        }
        request.getSession().removeAttribute("docId");
        request.getSession().removeAttribute("multipleB2BRequisitions");
        return mapping.findForward(OLEConstants.MAPPING_BASIC);
    }

    /**
     * Clears the vendor selection from the Requisition.  NOTE, this functionality is only available on Requisition and not PO.
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  A HttpServletRequest
     * @param response A HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward clearVendor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        OleRequisitionDocument document = (OleRequisitionDocument) baseForm.getDocument();

        document.setVendorHeaderGeneratedIdentifier(null);
        document.setVendorDetailAssignedIdentifier(null);
        document.setVendorDetail(null);
        document.setVendorName("");
        document.setVendorLine1Address("");
        document.setVendorLine2Address("");
        document.setVendorAddressInternationalProvinceName("");
        document.setVendorCityName("");
        document.setVendorStateCode("");
        document.setVendorPostalCode("");
        document.setVendorCountryCode("");
        document.setVendorContractGeneratedIdentifier(null);
        document.setVendorContract(null);
        document.setVendorFaxNumber("");
        document.setVendorCustomerNumber("");
        document.setVendorAttentionName("");
        document.setVendorAliasName("");
        return mapping.findForward(OLEConstants.MAPPING_BASIC);
    }

    /**
     * Set up blanket approve indicator which will be used to decide if need to run accounting line validation at the time of
     * blanket approve.
     *
     * @see org.kuali.ole.module.purap.document.web.struts.PurchasingActionBase#blanketApprove(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward blanketApprove(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RequisitionDocument document = (RequisitionDocument) ((PurchasingFormBase) form).getDocument();
        document.setBlanketApproveRequest(true);
        return super.blanketApprove(mapping, form, request, response);
    }
}
