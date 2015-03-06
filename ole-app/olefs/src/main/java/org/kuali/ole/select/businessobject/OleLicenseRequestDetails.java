/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.select.businessobject;


import org.kuali.ole.select.document.OleRequisitionDocument;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.math.BigDecimal;

/**
 * OLE OleLicenseRequestDetails Base Business Object.
 */
public class OleLicenseRequestDetails extends PersistableBusinessObjectBase {

    private String documentNumber;
    private BigDecimal oleLicenseRequestDetailsId;
    private String licenseRequestDocNumber;

    private OleRequisitionDocument oleRequisitionDocument;

    /**
     * Constructs a OleRequisitionNotesBase.java.
     */
    public OleLicenseRequestDetails() {

    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public BigDecimal getOleLicenseRequestDetailsId() {
        return oleLicenseRequestDetailsId;
    }

    public void setOleLicenseRequestDetailsId(BigDecimal oleLicenseRequestDetailsId) {
        this.oleLicenseRequestDetailsId = oleLicenseRequestDetailsId;
    }

    public String getLicenseRequestDocNumber() {
        return licenseRequestDocNumber;
    }

    public void setLicenseRequestDocNumber(String licenseRequestDocNumber) {
        this.licenseRequestDocNumber = licenseRequestDocNumber;
    }

    public OleRequisitionDocument getOleRequisitionDocument() {
        return oleRequisitionDocument;
    }

    public void setOleRequisitionDocument(OleRequisitionDocument oleRequisitionDocument) {
        this.oleRequisitionDocument = oleRequisitionDocument;
    }


}