package org.kuali.ole.docstore.model.rdbms.bo;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: mjagan
 * Date: 6/28/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticalSearchRecord extends PersistableBusinessObjectBase
        implements Serializable {

    private String statisticalSearchId;
    private String code;
    private String name;

    public String getStatisticalSearchId() {
        return statisticalSearchId;
    }

    public void setStatisticalSearchId(String statisticalSearchId) {
        this.statisticalSearchId = statisticalSearchId;
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
