package org.kuali.ole.batch.form;

import org.kuali.ole.batch.document.OLEBatchProcessDefinitionDocument;
import org.kuali.rice.krad.web.form.TransactionalDocumentFormBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: adityas
 * Date: 7/12/13
 * Time: 4:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEBatchProcessDefinitionForm extends TransactionalDocumentFormBase {

    private OLEBatchProcessDefinitionDocument oleBatchProcessDefinitionDocument;

    private String batchProcessId;
    private String batchProcessType;
    private boolean permissionFlag =true;
    private boolean marcOnly;
    private String navigationBatchProcessId;

    public String getBatchProcessId() {
        return batchProcessId;
    }

    public void setBatchProcessId(String batchProcessId) {
        this.batchProcessId = batchProcessId;
    }

    public String getBatchProcessType() {
        return batchProcessType;
    }

    public void setBatchProcessType(String batchProcessType) {
        this.batchProcessType = batchProcessType;
    }

    public OLEBatchProcessDefinitionForm() {
        super();
    }
    @Override
    protected String getDefaultDocumentTypeName() {
        return "OLE_BCH_PRCS_PRFL_DOC";
    }

    public OLEBatchProcessDefinitionDocument getOleBatchProcessDefinitionDocument() {
        return oleBatchProcessDefinitionDocument;
    }

    public void setOleBatchProcessDefinitionDocument(OLEBatchProcessDefinitionDocument oleBatchProcessDefinitionDocument) {
        this.oleBatchProcessDefinitionDocument = oleBatchProcessDefinitionDocument;
    }

    public boolean isPermissionFlag() {
        return permissionFlag;
    }

    public void setPermissionFlag(boolean permissionFlag) {
        this.permissionFlag = permissionFlag;
    }

    public boolean isMarcOnly() {
        OLEBatchProcessDefinitionDocument batchProcessDefinitionDocument = (OLEBatchProcessDefinitionDocument)getDocument();
        if(batchProcessDefinitionDocument.getMarcOnly() != null){
            return batchProcessDefinitionDocument.getMarcOnly();
        }
        return false;
    }

    public void setMarcOnly(boolean marcOnly) {
        this.marcOnly = marcOnly;
    }

    //private String cronOrSchedule;
    //private String scheduleType;
    //private String oneTimeOrRecurring;

    /*public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }*/

   /* public String getOneTimeOrRecurring() {
        return oneTimeOrRecurring;
    }

    public void setOneTimeOrRecurring(String oneTimeOrRecurring) {
        this.oneTimeOrRecurring = oneTimeOrRecurring;
    }*/

 /*   public String getCronOrSchedule() {
        return cronOrSchedule;
    }

    public void setCronOrSchedule(String cronOrSchedule) {
        this.cronOrSchedule = cronOrSchedule;
    }*/

    public String getNavigationBatchProcessId() {
        return navigationBatchProcessId;
    }

    public void setNavigationBatchProcessId(String navigationBatchProcessId) {
        this.navigationBatchProcessId = navigationBatchProcessId;
    }
}
