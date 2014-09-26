package org.kuali.ole.select.form;

import org.kuali.ole.select.document.OLEEResourceRecordDocument;
import org.kuali.rice.krad.web.form.TransactionalDocumentFormBase;

/**
 * Created with IntelliJ IDEA.
 * User: srinivasane
 * Date: 6/21/13
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEEResourceRecordForm extends TransactionalDocumentFormBase {

    private boolean selectFlag;
    private String statusDate;
    private String documentDescription;

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    private String instanceId;
    private String bibId;
    private boolean linkInstance;
    private boolean createInstance;
    private String selectInstance;
    private boolean selectLinkInstance;
    private boolean selectCreateInstance;
    private boolean coverageFlag;
    private boolean perpetualAccessFlag;
    private boolean removeInstanceFlag;
    private boolean defaultDatesFlag;
    private String defaultCovStartDateErrorMessage;
    private String defaultCovEndDateErrorMessage;
    private String defaultPerAccStartDateErrorMessage;
    private String defaultPerAccEndDateErrorMessage;

    public OLEEResourceRecordForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "OLE_ERS_DOC";
    }

    private OLEEResourceRecordDocument oleeResourceRecordDocument;

    public OLEEResourceRecordDocument getOleeResourceRecordDocument() {
        return oleeResourceRecordDocument;
    }

    public void setOleeResourceRecordDocument(OLEEResourceRecordDocument oleeResourceRecordDocument) {
        this.oleeResourceRecordDocument = oleeResourceRecordDocument;
    }

    public boolean isSelectFlag() {
        return selectFlag;
    }

    public void setSelectFlag(boolean selectFlag) {
        this.selectFlag = selectFlag;
    }

    public String getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }

    public boolean isLinkInstance() {
        return linkInstance;
    }

    public void setLinkInstance(boolean linkInstance) {
        this.linkInstance = linkInstance;
    }

    public String getSelectInstance() {
        return selectInstance;
    }

    public void setSelectInstance(String selectInstance) {
        this.selectInstance = selectInstance;
    }

    public boolean isSelectLinkInstance() {
        return selectLinkInstance;
    }

    public void setSelectLinkInstance(boolean selectLinkInstance) {
        this.selectLinkInstance = selectLinkInstance;
    }

    public boolean isSelectCreateInstance() {
        return selectCreateInstance;
    }

    public void setSelectCreateInstance(boolean selectCreateInstance) {
        this.selectCreateInstance = selectCreateInstance;
    }

    public boolean isCreateInstance() {
        return createInstance;
    }

    public void setCreateInstance(boolean createInstance) {
        this.createInstance = createInstance;
    }

    public boolean isCoverageFlag() {
        return coverageFlag;
    }

    public void setCoverageFlag(boolean coverageFlag) {
        this.coverageFlag = coverageFlag;
    }

    public boolean isPerpetualAccessFlag() {
        return perpetualAccessFlag;
    }

    public void setPerpetualAccessFlag(boolean perpetualAccessFlag) {
        this.perpetualAccessFlag = perpetualAccessFlag;
    }

    public boolean isRemoveInstanceFlag() {
        return removeInstanceFlag;
    }

    public void setRemoveInstanceFlag(boolean removeInstanceFlag) {
        this.removeInstanceFlag = removeInstanceFlag;
    }

    public boolean isDefaultDatesFlag() {
        return defaultDatesFlag;
    }

    public void setDefaultDatesFlag(boolean defaultDatesFlag) {
        this.defaultDatesFlag = defaultDatesFlag;
    }

    public String getDefaultCovStartDateErrorMessage() {
        return defaultCovStartDateErrorMessage;
    }

    public void setDefaultCovStartDateErrorMessage(String defaultCovStartDateErrorMessage) {
        this.defaultCovStartDateErrorMessage = defaultCovStartDateErrorMessage;
    }

    public String getDefaultCovEndDateErrorMessage() {
        return defaultCovEndDateErrorMessage;
    }

    public void setDefaultCovEndDateErrorMessage(String defaultCovEndDateErrorMessage) {
        this.defaultCovEndDateErrorMessage = defaultCovEndDateErrorMessage;
    }

    public String getDefaultPerAccStartDateErrorMessage() {
        return defaultPerAccStartDateErrorMessage;
    }

    public void setDefaultPerAccStartDateErrorMessage(String defaultPerAccStartDateErrorMessage) {
        this.defaultPerAccStartDateErrorMessage = defaultPerAccStartDateErrorMessage;
    }

    public String getDefaultPerAccEndDateErrorMessage() {
        return defaultPerAccEndDateErrorMessage;
    }

    public void setDefaultPerAccEndDateErrorMessage(String defaultPerAccEndDateErrorMessage) {
        this.defaultPerAccEndDateErrorMessage = defaultPerAccEndDateErrorMessage;
    }
}
