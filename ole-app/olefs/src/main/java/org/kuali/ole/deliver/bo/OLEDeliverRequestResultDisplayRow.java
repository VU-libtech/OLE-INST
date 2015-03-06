package org.kuali.ole.deliver.bo;

import java.sql.Date;

/**
 * Created by chenchulakshmig on 1/20/15.
 */
public class OLEDeliverRequestResultDisplayRow {

    private Integer borrowerQueuePosition;
    private String requestId;
    private String requestTypeCode;
    private String borrowerFirstName;
    private String borrowerLastName;
    private String borrowerBarcode;
    private Date createDate;
    private Date expiryDate;

    public Integer getBorrowerQueuePosition() {
        return borrowerQueuePosition;
    }

    public void setBorrowerQueuePosition(Integer borrowerQueuePosition) {
        this.borrowerQueuePosition = borrowerQueuePosition;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestTypeCode() {
        return requestTypeCode;
    }

    public void setRequestTypeCode(String requestTypeCode) {
        this.requestTypeCode = requestTypeCode;
    }

    public String getBorrowerFirstName() {
        return borrowerFirstName;
    }

    public void setBorrowerFirstName(String borrowerFirstName) {
        this.borrowerFirstName = borrowerFirstName;
    }

    public String getBorrowerLastName() {
        return borrowerLastName;
    }

    public void setBorrowerLastName(String borrowerLastName) {
        this.borrowerLastName = borrowerLastName;
    }

    public String getBorrowerBarcode() {
        return borrowerBarcode;
    }

    public void setBorrowerBarcode(String borrowerBarcode) {
        this.borrowerBarcode = borrowerBarcode;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
