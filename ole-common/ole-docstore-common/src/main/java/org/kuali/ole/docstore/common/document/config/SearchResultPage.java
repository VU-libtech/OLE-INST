package org.kuali.ole.docstore.common.document.config;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: chandrasekharag
 * Date: 4/3/14
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultPage extends PersistableBusinessObjectBase implements Serializable {

    private Integer id;
    private Integer size;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
