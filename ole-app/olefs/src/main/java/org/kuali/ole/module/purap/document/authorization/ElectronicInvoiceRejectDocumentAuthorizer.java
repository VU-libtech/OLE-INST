/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.ole.module.purap.document.authorization;

import org.kuali.ole.module.purap.businessobject.SensitiveData;
import org.kuali.ole.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.ole.module.purap.identity.PurapKimAttributes;
import org.kuali.ole.module.purap.service.SensitiveDataService;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.document.authorization.TransactionalDocumentAuthorizerBase;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.KRADConstants;

import java.util.List;
import java.util.Map;

public class ElectronicInvoiceRejectDocumentAuthorizer extends TransactionalDocumentAuthorizerBase {

    @Override
    protected void addRoleQualification(Object businessObject, Map<String, String> attributes) {
        super.addRoleQualification(businessObject, attributes);
        attributes.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "false");
        ElectronicInvoiceRejectDocument purapDoc = (ElectronicInvoiceRejectDocument) businessObject;
        if (purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
            List<SensitiveData> sensitiveDataList = SpringContext.getBean(SensitiveDataService.class).getSensitiveDatasAssignedByRelatedDocId(purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier());
            StringBuffer sensitiveDataCodes = new StringBuffer();
            for (SensitiveData sensitiveData : sensitiveDataList) {
                sensitiveDataCodes.append(sensitiveData.getSensitiveDataCode()).append(";");
            }
            if (sensitiveDataCodes.length() > 0) {
                attributes.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "true");
                attributes.put(PurapKimAttributes.SENSITIVE_DATA_CODE, sensitiveDataCodes.toString().substring(0, sensitiveDataCodes.length() - 1));
                attributes.put(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER, purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier().toString());
            }
        }
    }

    @Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document,
                KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT,
                user.getPrincipalId());
    }

}
