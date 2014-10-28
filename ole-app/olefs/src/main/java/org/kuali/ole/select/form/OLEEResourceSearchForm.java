package org.kuali.ole.select.form;

import org.kuali.ole.select.bo.OLESearchCondition;
import org.kuali.ole.select.bo.OLESearchParams;
import org.kuali.ole.select.document.OLEEResourceRecordDocument;
import org.kuali.rice.krad.web.form.UifFormBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenchulakshmig
 * Date: 6/26/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class OLEEResourceSearchForm extends UifFormBase {
    public OLESearchParams oleSearchParams = new OLESearchParams();
    public boolean statusDate;
    public Date beginDate;
    public Date endDate;
    public List<String> status;
    private List<OLEEResourceRecordDocument> eresourceDocumentList = new ArrayList<OLEEResourceRecordDocument>();

    public OLEEResourceSearchForm() {
        List<OLESearchCondition> searchConditions = new ArrayList<OLESearchCondition>();
        searchConditions = getOleSearchParams().getSearchFieldsList();
        searchConditions.add(new OLESearchCondition());
        searchConditions.add(new OLESearchCondition());
        searchConditions.add(new OLESearchCondition());
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public boolean isStatusDate() {
        return statusDate;
    }

    public void setStatusDate(boolean statusDate) {
        this.statusDate = statusDate;
    }

    public OLESearchParams getOleSearchParams() {
        return oleSearchParams;
    }

    public void setOleSearchParams(OLESearchParams oleSearchParams) {
        this.oleSearchParams = oleSearchParams;
    }

    public List<OLEEResourceRecordDocument> getEresourceDocumentList() {
        return eresourceDocumentList;
    }

    public void setEresourceDocumentList(List<OLEEResourceRecordDocument> eresourceDocumentList) {
        this.eresourceDocumentList = eresourceDocumentList;
    }
}
