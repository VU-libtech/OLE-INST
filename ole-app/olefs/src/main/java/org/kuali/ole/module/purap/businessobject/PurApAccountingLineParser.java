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
package org.kuali.ole.module.purap.businessobject;

import org.kuali.ole.module.purap.document.PurchaseOrderDocument;
import org.kuali.ole.module.purap.document.RequisitionDocument;
import org.kuali.ole.sys.businessobject.AccountingLineParserBase;
import org.kuali.ole.sys.document.AccountingDocument;

import static org.kuali.ole.sys.OLEPropertyConstants.*;

/**
 * This class is used to parse an PurApItem accounting line.
 */
public class PurApAccountingLineParser extends AccountingLineParserBase {
    protected static final String[] PURAP_FORMAT = {CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, PERCENT};

    /**
     * Constructs a AuxiliaryVoucherAccountingLineParser.java.
     */
    public PurApAccountingLineParser() {
        super();
    }

    /**
     * @see org.kuali.rice.krad.bo.AccountingLineParserBase#getSourceAccountingLineFormat()
     */
    @Override
    public String[] getSourceAccountingLineFormat() {
        return removeChartFromFormatIfNeeded(PURAP_FORMAT);
    }

    /**
     * @see org.kuali.rice.krad.bo.AccountingLineParser#getSourceAccountingLineClass(org.kuali.rice.krad.document.AccountingDocument)
     */
    @Override
    protected Class getSourceAccountingLineClass(final AccountingDocument accountingDocument) {
        if (accountingDocument instanceof RequisitionDocument)
            return RequisitionAccount.class;
        else if (accountingDocument instanceof PurchaseOrderDocument)
            return PurchaseOrderAccount.class;
        else return super.getSourceAccountingLineClass(accountingDocument);
    }

}
