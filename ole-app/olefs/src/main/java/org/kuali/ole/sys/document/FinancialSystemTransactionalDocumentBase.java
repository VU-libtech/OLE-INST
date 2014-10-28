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
package org.kuali.ole.sys.document;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.dataaccess.FinancialSystemDocumentHeaderDao;
import org.kuali.ole.sys.document.service.FinancialSystemDocumentService;
import org.kuali.ole.sys.service.impl.OleParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteLevelChange;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.document.TransactionalDocumentBase;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * This class is a OLE specific TransactionalDocumentBase class
 */
public class FinancialSystemTransactionalDocumentBase extends TransactionalDocumentBase implements FinancialSystemTransactionalDocument {
    private static final Logger LOG = Logger.getLogger(FinancialSystemTransactionalDocumentBase.class);

    protected static final String UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME = "UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_IND";

    private static transient BusinessObjectService businessObjectService;
    private static transient FinancialSystemDocumentService financialSystemDocumentService;
    private static transient ParameterService parameterService;

    private transient Map<String,Boolean> canEditCache;

    /**
     * Constructs a FinancialSystemTransactionalDocumentBase.java.
     */
    public FinancialSystemTransactionalDocumentBase() {
        super();
    }

    @Override
    public FinancialSystemDocumentHeader getFinancialSystemDocumentHeader() {
        return (FinancialSystemDocumentHeader) documentHeader;
    }

    /**
     * @see org.kuali.rice.krad.document.DocumentBase#setDocumentHeader(org.kuali.rice.krad.bo.DocumentHeader)
     */
    @Override
    public void setDocumentHeader(DocumentHeader documentHeader) {
        if ((documentHeader != null) && (!FinancialSystemDocumentHeader.class.isAssignableFrom(documentHeader.getClass()))) {
            throw new IllegalArgumentException("document header of class '" + documentHeader.getClass() + "' is not assignable from financial document header class '" + FinancialSystemDocumentHeader.class + "'");
        }
        this.documentHeader = documentHeader;
    }

    /**
     * If the document has a total amount, call method on document to get the total and set in doc header.
     *
     * @see org.kuali.rice.krad.document.Document#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        if (this instanceof AmountTotaling) {
            getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
        }
        super.prepareForSave();
    }

    /**
     * This is the default implementation which ensures that document note attachment references are loaded.
     *
     * @see org.kuali.rice.krad.document.Document#processAfterRetrieve()
     */
    @Override
    public void processAfterRetrieve() {
        // set correctedByDocumentId manually, since OJB doesn't maintain that relationship
        try {
            DocumentHeader correctingDocumentHeader = SpringContext.getBean(FinancialSystemDocumentHeaderDao.class).getCorrectingDocumentHeader(getFinancialSystemDocumentHeader().getDocumentNumber());
            if (correctingDocumentHeader != null) {
                getFinancialSystemDocumentHeader().setCorrectedByDocumentId(correctingDocumentHeader.getDocumentNumber());
            }
        } catch (Exception e) {
            LOG.error("Received WorkflowException trying to get route header id from workflow document.", e);
            throw new WorkflowRuntimeException(e);
        }
        // set the ad hoc route recipients too, since OJB doesn't maintain that relationship
        // TODO - see KULNRVSYS-1054

        super.processAfterRetrieve();
    }

    /**
     * This is the default implementation which checks for a different workflow statuses, and updates the Kuali status accordingly.
     *
     * @see org.kuali.rice.krad.document.Document#doRouteStatusChange()
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        if (getDocumentHeader().getWorkflowDocument().isCanceled()) {
            getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(OLEConstants.DocumentStatusCodes.CANCELLED);
        }
        else if (getDocumentHeader().getWorkflowDocument().isEnroute()) {
            getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(OLEConstants.DocumentStatusCodes.ENROUTE);
        }
        if (getDocumentHeader().getWorkflowDocument().isDisapproved()) {
            getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(OLEConstants.DocumentStatusCodes.DISAPPROVED);
        }
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(OLEConstants.DocumentStatusCodes.APPROVED);
        }
        if ( LOG.isInfoEnabled() ) {
            LOG.info("Document: " + statusChangeEvent.getDocumentId() + " -- Status is: " + getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode());
        }

        super.doRouteStatusChange(statusChangeEvent);
    }

    /**
     * This is the default implementation which, if parameter KFS-SYS / Document / UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_IND is on, updates the document
     * and resaves if needed
     * @see org.kuali.rice.kns.document.DocumentBase#doRouteLevelChange(org.kuali.rice.kew.dto.DocumentRouteLevelChangeDTO)
     */
    @Override
    public void doRouteLevelChange(DocumentRouteLevelChange levelChangeEvent) {
        if (this instanceof AmountTotaling
                && getDocumentHeader() != null
                && getParameterService() != null
                && getBusinessObjectService() != null
                && getParameterService().parameterExists(OleParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
                && getParameterService().getParameterValueAsBoolean(OleParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
            final KualiDecimal currentTotal = ((AmountTotaling)this).getTotalDollarAmount();
            if (!currentTotal.equals(getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount())) {
                getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(currentTotal);
                getBusinessObjectService().save(getFinancialSystemDocumentHeader());
            }
        }
        super.doRouteLevelChange(levelChangeEvent);
    }

    /**
     * @see org.kuali.ole.sys.document.Correctable#toErrorCorrection()
     */
    public void toErrorCorrection() throws WorkflowException, IllegalStateException {
        DocumentHelperService documentHelperService = SpringContext.getBean(DocumentHelperService.class);
        final Set<String> documentActionsFromPresentationController = documentHelperService.getDocumentPresentationController(this).getDocumentActions(this);
        final Set<String> documentActionsFromAuthorizer = documentHelperService.getDocumentAuthorizer(this).getDocumentActions(this, GlobalVariables.getUserSession().getPerson(), documentActionsFromPresentationController);
        if (!documentActionsFromAuthorizer.contains(OLEConstants.KFS_ACTION_CAN_ERROR_CORRECT)) {
            throw new IllegalStateException(this.getClass().getName() + " does not support document-level error correction");
        }

        String sourceDocumentHeaderId = getDocumentNumber();
        setNewDocumentHeader();
        getFinancialSystemDocumentHeader().setFinancialDocumentInErrorNumber(sourceDocumentHeaderId);
        addCopyErrorDocumentNote("error-correction for document " + sourceDocumentHeaderId);
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("No split node logic defined for split node "+nodeName+" on " + this.getClass().getSimpleName());
    }

    /**
     * @return the default implementation of the ParameterService
     */
    protected ParameterService getParameterService() {
       if (parameterService == null) {
           parameterService = SpringContext.getBean(ParameterService.class);
       }
       return parameterService;
    }

    /**
     * @return the default implementation of the BusinessObjectService
     */
    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    protected FinancialSystemDocumentService getFinancialSystemDocumentService() {
        if (financialSystemDocumentService == null) {
            financialSystemDocumentService = SpringContext.getBean(FinancialSystemDocumentService.class);
        }
        return financialSystemDocumentService;
    }

    @Override
    public void toCopy() throws WorkflowException, IllegalStateException {
        FinancialSystemDocumentHeader oldDocumentHeader = getFinancialSystemDocumentHeader();
        super.toCopy();

        getFinancialSystemDocumentService().prepareToCopy(oldDocumentHeader, this);
    }

    /**
     * Updates status of this document and saves the workflow data
     *
     * @param applicationDocumentStatus is the app doc status to save
     * @throws WorkflowException
     */
    public void updateAndSaveAppDocStatus(String applicationDocumentStatus) throws WorkflowException {
        getFinancialSystemDocumentHeader().updateAndSaveAppDocStatus(applicationDocumentStatus);
    }

    /**
     * Gets the applicationDocumentStatus attribute.
     *
     * @return Returns the applicationDocumentStatus
     */

    public String getApplicationDocumentStatus() {
        return getFinancialSystemDocumentHeader().getApplicationDocumentStatus();
    }

    /**
     * Sets the applicationDocumentStatus attribute.
     *
     * @param applicationDocumentStatus The applicationDocumentStatus to set.
     */
    public void setApplicationDocumentStatus(String applicationDocumentStatus) {
        getFinancialSystemDocumentHeader().setApplicationDocumentStatus(applicationDocumentStatus);
    }
}
