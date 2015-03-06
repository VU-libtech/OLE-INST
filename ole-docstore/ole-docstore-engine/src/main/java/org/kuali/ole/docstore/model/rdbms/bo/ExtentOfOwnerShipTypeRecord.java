package org.kuali.ole.docstore.model.rdbms.bo;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: mjagan
 * Date: 7/8/13
 * Time: 8:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtentOfOwnerShipTypeRecord extends PersistableBusinessObjectBase
        implements Serializable {

    private String extOfOwnerShipTypeId;
    private String code;
    private String name;


    public String getExtOfOwnerShipTypeId() {
        return extOfOwnerShipTypeId;
    }

    public void setExtOfOwnerShipTypeId(String extOfOwnerShipTypeId) {
        this.extOfOwnerShipTypeId = extOfOwnerShipTypeId;
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
