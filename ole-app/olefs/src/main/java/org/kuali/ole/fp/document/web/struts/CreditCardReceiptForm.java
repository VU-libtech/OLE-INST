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
package org.kuali.ole.fp.document.web.struts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.kuali.ole.fp.businessobject.CapitalAssetInformation;
import org.kuali.ole.fp.businessobject.CreditCardDetail;
import org.kuali.ole.fp.document.CapitalAssetEditable;
import org.kuali.ole.fp.document.CreditCardReceiptDocument;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.kns.service.BusinessObjectDictionaryService;

/**
 * This class is the struts form for Credit Card Receipt document.
 */
public class CreditCardReceiptForm extends CapitalAccountingLinesFormBase implements CapitalAssetEditable {
    protected CreditCardDetail newCreditCardReceipt;
    protected List<CapitalAssetInformation> capitalAssetInformation;

    /**
     * Constructs a CreditCardReceiptForm.java.
     */
    public CreditCardReceiptForm() {
        super();
        
        setNewCreditCardReceipt(new CreditCardDetail());
        
        capitalAssetInformation = new ArrayList<CapitalAssetInformation>();
        this.capitalAccountingLine.setCanCreateAsset(false); //This document can only edit asset information
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "OLE_CCR";
    }
    
    /**
     * @return CreditCardReceiptDocument
     */
    public CreditCardReceiptDocument getCreditCardReceiptDocument() {
        return (CreditCardReceiptDocument) getDocument();
    }

    /**
     * @return CreditCardDetail
     */
    public CreditCardDetail getNewCreditCardReceipt() {
        return newCreditCardReceipt;
    }

    /**
     * @param newCreditCardReceipt
     */
    public void setNewCreditCardReceipt(CreditCardDetail newCreditCardReceipt) {
        this.newCreditCardReceipt = newCreditCardReceipt;
    }

    /**
     * Overrides the parent to call super.populate and then tells each line to check the associated data dictionary and modify the
     * values entered to follow all the attributes set for the values of the accounting line.
     * 
     * @see org.kuali.rice.kns.web.struts.form.KualiTransactionalDocumentFormBase#populate(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public void populate(HttpServletRequest request) {
        super.populate(request);

        //
        // now run through all of the accounting lines and make sure they've been uppercased and populated appropriately
        SpringContext.getBean(BusinessObjectDictionaryService.class).performForceUppercase(getNewCreditCardReceipt());

        List<CreditCardDetail> creditCardReceipts = getCreditCardReceiptDocument().getCreditCardReceipts();
        for (CreditCardDetail detail : creditCardReceipts) {
            SpringContext.getBean(BusinessObjectDictionaryService.class).performForceUppercase(detail);
        }

    }

    /**
     * @see org.kuali.ole.fp.document.CapitalAssetEditable#getCapitalAssetInformation()
     */
    public List<CapitalAssetInformation> getCapitalAssetInformation() {
        return this.capitalAssetInformation;
    }

    /**
     * @see org.kuali.ole.fp.document.CapitalAssetEditable#setCapitalAssetInformation(org.kuali.ole.fp.businessobject.CapitalAssetInformation)
     */
    public void setCapitalAssetInformation(List<CapitalAssetInformation> capitalAssetInformation) {
        this.capitalAssetInformation = capitalAssetInformation;        
    }
}
