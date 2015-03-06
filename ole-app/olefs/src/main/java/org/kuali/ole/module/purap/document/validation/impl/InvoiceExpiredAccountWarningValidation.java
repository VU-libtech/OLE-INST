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
package org.kuali.ole.module.purap.document.validation.impl;

import org.kuali.ole.gl.batch.ScrubberStep;
import org.kuali.ole.module.purap.businessobject.PurApAccountingLine;
import org.kuali.ole.module.purap.businessobject.PurApItem;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.document.validation.GenericValidation;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kns.util.KNSGlobalVariables;

import java.util.Date;
import java.util.List;

public class InvoiceExpiredAccountWarningValidation extends GenericValidation {

    private PurApItem itemForValidation;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        List<PurApAccountingLine> accountingLines = itemForValidation.getSourceAccountingLines();
        for (PurApAccountingLine accountingLine : accountingLines) {
            if (accountingLine.getAccount().isExpired()) {
                Date current = dateTimeService.getCurrentDate();
                Date accountExpirationDate = accountingLine.getAccount().getAccountExpirationDate();
                String expirationExtensionDays = parameterService.getParameterValueAsString(ScrubberStep.class, OLEConstants.SystemGroupParameterNames.GL_SCRUBBER_VALIDATION_DAYS_OFFSET);
                int expirationExtensionDaysInt = 3 * 30; // default to 90 days (approximately 3 months)

                if (expirationExtensionDays.trim().length() > 0) {

                    expirationExtensionDaysInt = new Integer(expirationExtensionDays).intValue();
                }

                if (!accountingLine.getAccount().isForContractsAndGrants() ||
                        dateTimeService.dateDiff(accountExpirationDate, current, false) < expirationExtensionDaysInt) {
                    KNSGlobalVariables.getMessageList().add(OLEKeyConstants.ERROR_ACCOUNT_EXPIRED);
                    valid &= false;
                    break;
                }
            }
        }
        return valid;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
