package org.kuali.ole.deliver.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.OLEParameterConstants;
import org.kuali.ole.deliver.bo.OLEDeliverNotice;
import org.kuali.ole.deliver.bo.OleLoanDocument;
import org.kuali.ole.describe.bo.OleInstanceItemType;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by premkb on 3/30/15.
 */
public class OverdueNoticesExecutor extends NoticesExecutor {
    private static final Logger LOG = Logger.getLogger(OverdueNoticesExecutor.class);
    private NoticeMailContentFormatter noticeMailContentFormatter;

    public OverdueNoticesExecutor(List<OleLoanDocument> loanDocuments) {
        super(loanDocuments);
    }

    @Override
    protected void postProcess(List<OleLoanDocument> loanDocuments) {

    }

    @Override
    protected void preProcess(List<OleLoanDocument> loanDocuments) {

    }

    public List<OLEDeliverNotice> buildNoticesForDeletion() {
        List<OLEDeliverNotice> oleDeliverNotices = new ArrayList<>();
        for (OleLoanDocument loanDocument:loanDocuments) {
            if (loanDocument.getItemTypeName() != null) {
                loanDocument.setItemType(getItemTypeCodeByName(loanDocument.getItemTypeName()));
            }
            String overdueNoticeToDate = getParameterResolverInstance().getParameter(OLEConstants
                    .APPL_ID_OLE, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT, OLEConstants.OVERDUE_NOTICE_TO_DATE);
            Timestamp overdueNoticetoSendDate = new Timestamp(System.currentTimeMillis());
            if (!StringUtils.isEmpty(overdueNoticeToDate)) {
                overdueNoticetoSendDate = new Timestamp(new Date(overdueNoticeToDate).getTime());
            }
            for (OLEDeliverNotice oleDeliverNotice : loanDocument.getDeliverNotices()) {
                LOG.info("OverdueNoticesExecutor thread id---->"+Thread.currentThread().getId()+"current thread---->"+Thread.currentThread()+"Loan id-->"+loanDocument.getLoanId()+"notice id--->"+oleDeliverNotice.getId());
                Timestamp toBeSendDate = oleDeliverNotice.getNoticeToBeSendDate();
                if (oleDeliverNotice.getNoticeType().equals(OLEConstants.NOTICE_OVERDUE) && toBeSendDate.compareTo(overdueNoticetoSendDate) < 0) {
                    try {
                        int noOfOverdueNoticeSent = Integer.parseInt(loanDocument.getNumberOfOverdueNoticesSent() != null ? loanDocument.getNumberOfOverdueNoticesSent() : "0");
                        noOfOverdueNoticeSent = noOfOverdueNoticeSent + 1;
                        loanDocument.setNumberOfOverdueNoticesSent(Integer.toString(noOfOverdueNoticeSent));
                        loanDocument.setOverDueNoticeDate(new java.sql.Date(System.currentTimeMillis()));
                        //getBusinessObjectService().save(loanDocument);
                        oleDeliverNotices.add(oleDeliverNotice);
                    } catch (Exception e) {
                        Log.info(e.getStackTrace());
                    }

                }
            }
        }
        return oleDeliverNotices;
    }

    public String getItemTypeCodeByName(String itemTypeName) {
        String itemTypeCode = "";
        List<OleInstanceItemType> instanceItemTypeList = null;
        Map<String, String> instanceItemTypeMap = new HashMap<String, String>();
        instanceItemTypeMap.put("instanceItemTypeName", itemTypeName);
        instanceItemTypeList = (List<OleInstanceItemType>) getBusinessObjectService().findMatching(OleInstanceItemType.class, instanceItemTypeMap);
        if (instanceItemTypeList != null && instanceItemTypeList.size() > 0) {
            itemTypeCode = instanceItemTypeList.get(0).getInstanceItemTypeCode();
        }
        return itemTypeCode;
    }


    public String generateMailContent(List<OleLoanDocument> oleLoanDocuments) {
        String title = getParameterResolverInstance().getParameter(OLEConstants.APPL_ID, OLEConstants
                .DLVR_NMSPC, OLEConstants.DLVR_CMPNT,
                OLEParameterConstants
                        .OVERDUE_TITLE);
        String body = getParameterResolverInstance().getParameter(OLEConstants.APPL_ID_OLE, OLEConstants
                .DLVR_NMSPC, OLEConstants.DLVR_CMPNT, OLEConstants.OleDeliverRequest.OVERDUE_NOTICE_CONTENT);
        String mailContent = getNoticeMailContentFormatter().generateMailContentForPatron(oleLoanDocuments,title,body);

        return mailContent;
    }

    private NoticeMailContentFormatter getNoticeMailContentFormatter() {
        if (null == noticeMailContentFormatter) {
            noticeMailContentFormatter = new OverdueNoticeEmailContentFormatter();
        }
        return noticeMailContentFormatter;
    }

    public void setNoticeMailContentFormatter(NoticeMailContentFormatter noticeMailContentFormatter) {
        this.noticeMailContentFormatter = noticeMailContentFormatter;
    }
}
