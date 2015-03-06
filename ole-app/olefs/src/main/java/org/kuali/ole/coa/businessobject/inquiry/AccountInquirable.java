/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.coa.businessobject.inquiry;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.coa.businessobject.IndirectCostRecoveryRate;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.businessobject.inquiry.KfsInquirableImpl;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.util.UrlFactory;

/**
 * Inquirable class for {@link Account}
 */
public class AccountInquirable extends KfsInquirableImpl {

    /**
     * @see org.kuali.ole.sys.businessobject.inquiry.KfsInquirableImpl#getInquiryUrl(org.kuali.rice.krad.bo.BusinessObject,
     *      java.lang.String, boolean)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String attributeName, boolean forceInquiry) {
        if (OLEPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER.equals(attributeName)) {
            String baseUrl = KRADConstants.INQUIRY_ACTION;

            Properties parameters = new Properties();
            parameters.put(OLEConstants.DISPATCH_REQUEST_PARAMETER, OLEConstants.START_METHOD);
            parameters.put(OLEConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, IndirectCostRecoveryRate.class.getName());
            parameters.put(OLEConstants.DOC_FORM_KEY, "88888888");

            Map<String, String> inquiryFields = new HashMap<String, String>();
            String icrIdentifier = (String) ObjectUtils.getPropertyValue(businessObject, attributeName);
            if (StringUtils.isBlank(icrIdentifier)) {
                return new AnchorHtmlData();
            }
            inquiryFields.put(OLEPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, icrIdentifier);
            parameters.put(OLEPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, icrIdentifier);

            Integer fiscalYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
            parameters.put(OLEPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear.toString());

            return getHyperLink(IndirectCostRecoveryRate.class, inquiryFields, UrlFactory.parameterizeUrl(baseUrl, parameters));
        }

        return super.getInquiryUrl(businessObject, attributeName, forceInquiry);
    }

}
