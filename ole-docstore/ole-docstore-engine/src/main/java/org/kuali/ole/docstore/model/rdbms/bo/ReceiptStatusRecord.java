package org.kuali.ole.docstore.model.rdbms.bo;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: mjagan
 * Date: 7/8/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReceiptStatusRecord extends PersistableBusinessObjectBase
        implements Serializable {

    private String receiptStatusId;
    private String code;
    private String name;

    public String getReceiptStatusId() {
        return receiptStatusId;
    }

    public void setReceiptStatusId(String receiptStatusId) {
        this.receiptStatusId = receiptStatusId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
