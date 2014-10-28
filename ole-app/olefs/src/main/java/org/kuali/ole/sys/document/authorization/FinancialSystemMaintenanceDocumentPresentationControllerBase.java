/*
 * Copyright 2008-2009 The Kuali Foundation
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
package org.kuali.ole.sys.document.authorization;

import java.util.Set;

import org.kuali.ole.coa.service.AccountPersistenceStructureService;
import org.kuali.ole.coa.service.AccountService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.MaintenanceDocumentPresentationControllerBase;
import org.kuali.rice.krad.bo.PersistableBusinessObject;

public class FinancialSystemMaintenanceDocumentPresentationControllerBase extends MaintenanceDocumentPresentationControllerBase {

    private static ParameterEvaluatorService parameterEvaluatorService;

    /**
     * the following three methods still accept the deprecated class as argument in order to bridge the gap between old and new maintenance API
     *
     * This is just workaround solution. The better solution would be to replace old API with new one.
     */
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> readOnlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);

        // if accounts can't cross charts, then all chartOfAccountsCode fields shall be displayed readOnly
        if (!SpringContext.getBean(AccountService.class).accountsCanCrossCharts()) {
            AccountPersistenceStructureService apsService = SpringContext.getBean(AccountPersistenceStructureService.class);
            PersistableBusinessObject bo = document.getNewMaintainableObject().getBusinessObject();

            // non-collection reference accounts
            Set<String> coaCodeNames = apsService.listChartOfAccountsCodeNames(bo);
            readOnlyPropertyNames.addAll(coaCodeNames);

            // collection reference accounts
            coaCodeNames = apsService.listCollectionChartOfAccountsCodeNames(bo);
            readOnlyPropertyNames.addAll(coaCodeNames);
        }

        return readOnlyPropertyNames;
    }

    protected ParameterEvaluatorService getParameterEvaluatorService() {
        if (parameterEvaluatorService == null) {
            parameterEvaluatorService = SpringContext.getBean(ParameterEvaluatorService.class);
        }
        return parameterEvaluatorService;
    }
}
