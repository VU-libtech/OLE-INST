package org.kuali.ole.docstore.engine.service.storage.rdbms.pojo;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created by sambasivam on 17/10/14.
 */

public class BibInfoRecord  extends PersistableBusinessObjectBase
        implements Serializable {

    private String bibId;

    private String title;
    private String author;
    private String publisher;
    private String isxn;

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIsxn() {
        return isxn;
    }

    public void setIsxn(String isxn) {
        this.isxn = isxn;
    }
}
