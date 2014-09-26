/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.module.purap.PurapParameterConstants;
import org.kuali.ole.module.purap.businessobject.PurchaseOrderVendorChoice;
import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.kns.service.DataDictionaryService;

import java.util.ArrayList;
import java.util.Collection;

/* 
 * 
*/
public class PurchaseOrderVendorChoiceRule extends MaintenanceDocumentRuleBase {

    protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
        LOG.info("processCustomApproveDocumentBusinessRules called");
        this.setupConvenienceObjects();
        boolean success = this.checkForSystemParametersExistence();
        return success && super.processCustomApproveDocumentBusinessRules(document);
    }

    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        LOG.info("processCustomRouteDocumentBusinessRules called");
        this.setupConvenienceObjects();
        boolean success = this.checkForSystemParametersExistence();
        return success && super.processCustomRouteDocumentBusinessRules(document);
    }

    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        LOG.info("processCustomSaveDocumentBusinessRules called");
        this.setupConvenienceObjects();
        boolean success = this.checkForSystemParametersExistence();
        return success && super.processCustomSaveDocumentBusinessRules(document);
    }

    protected boolean checkForSystemParametersExistence() {
        LOG.info("checkForSystemParametersExistence called");
        boolean success = true;
        Collection<String> apoParameterValues = new ArrayList<String>(SpringContext.getBean(ParameterService.class).getParameterValuesAsString(PurchaseOrderDocument.class, PurapParameterConstants.DEFAULT_APO_VENDOR_CHOICE));
        Collection<String> b2bParameterValues = new ArrayList<String>(SpringContext.getBean(ParameterService.class).getParameterValuesAsString(PurchaseOrderDocument.class, PurapParameterConstants.DEFAULT_B2B_VENDOR_CHOICE));
        PurchaseOrderVendorChoice newVendorChoice = (PurchaseOrderVendorChoice) getNewBo();
        PurchaseOrderVendorChoice oldVendorChoice = (PurchaseOrderVendorChoice) getOldBo();
        //If the new funding source code matches with the funding source in the DEFAULT_FUNDING_SOURCE 
        //system parameters then we can't inactivate this funding source, so return false.
        if ((apoParameterValues.contains(newVendorChoice.getPurchaseOrderVendorChoiceCode()) || b2bParameterValues.contains(newVendorChoice.getPurchaseOrderVendorChoiceCode())) && !newVendorChoice.isActive() && oldVendorChoice.isActive()) {
            success = false;
            String documentLabel = SpringContext.getBean(DataDictionaryService.class).getDocumentLabelByClass(newVendorChoice.getClass());
            putGlobalError(OLEKeyConstants.ERROR_CANNOT_INACTIVATE_USED_IN_SYSTEM_PARAMETERS, documentLabel);
        }
        return success;
    }
}
