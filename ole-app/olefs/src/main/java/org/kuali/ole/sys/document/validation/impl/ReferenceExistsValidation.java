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
package org.kuali.ole.sys.document.validation.impl;

import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Validation to check if a reference of a validation 
 */
public class ReferenceExistsValidation extends GenericValidation {
    private PersistableBusinessObject businessObjectToValidate;
    private DictionaryValidationService dictionaryValidationService;
    private DataDictionaryService dataDictionaryService;
    private String referenceName;
    private String responsibleProperty;

    /**
     * 
     * @see org.kuali.ole.sys.document.validation.Validation#validate(org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean result = true;
        if (dictionaryValidationService.validateReferenceExists(businessObjectToValidate, referenceName)) {
            GlobalVariables.getMessageMap().putError(responsibleProperty, OLEKeyConstants.ERROR_EXISTENCE, new String[] { dataDictionaryService.getAttributeLabel(businessObjectToValidate.getClass(), responsibleProperty) });
            result = false;
        }
        return result;
    }

    /**
     * Gets the businessObjectToValidate attribute. 
     * @return Returns the businessObjectToValidate.
     */
    public PersistableBusinessObject getBusinessObjectToValidate() {
        return businessObjectToValidate;
    }

    /**
     * Sets the businessObjectToValidate attribute value.
     * @param businessObjectToValidate The businessObjectToValidate to set.
     */
    public void setBusinessObjectToValidate(PersistableBusinessObject businessObjectToValidate) {
        this.businessObjectToValidate = businessObjectToValidate;
    }

    /**
     * Gets the referenceName attribute. 
     * @return Returns the referenceName.
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * Sets the referenceName attribute value.
     * @param referenceName The referenceName to set.
     */
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    /**
     * Gets the responsibleProperty attribute. 
     * @return Returns the responsibleProperty.
     */
    public String getResponsibleProperty() {
        return responsibleProperty;
    }

    /**
     * Sets the responsibleProperty attribute value.
     * @param responsibleProperty The responsibleProperty to set.
     */
    public void setResponsibleProperty(String responsibleProperty) {
        this.responsibleProperty = responsibleProperty;
    }

    /**
     * Sets the dictionaryValidationService attribute value.
     * @param dictionaryValidationService The dictionaryValidationService to set.
     */
    public void setDictionaryValidationService(DictionaryValidationService dictionaryValidationService) {
        this.dictionaryValidationService = dictionaryValidationService;
    }

    /**
     * Sets the dataDictionaryService attribute value.
     * @param dataDictionaryService The dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
}
