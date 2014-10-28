package org.kuali.ole.select.document;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * Created with IntelliJ IDEA.
 * User: srinivasane
 * Date: 6/27/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class OLEEResourceInstance extends PersistableBusinessObjectBase {

    private String oleEResourceInstanceId;
    private String oleERSIdentifier;
    private String instanceId;
    private String holdingsId;
    private String instanceFlag;
    private String bibId;
    private String instanceTitle;
    private String isbn;
    private String instanceHoldings;
    private String publicDisplayNote;
    private String instancePublisher;
    private String platForm;
    private String status;
    private String subscriptionStatus;
    private String autoAddTitlesFromGOKb;
    private String autoRemoveTitlesFromGOKb;
    private String autoUpdateMetaDataFromGOKb;
    private String autoArcInstanceRec;
    private String covStartDate;
    private String covStartVolume;
    private String covStartIssue;
    private String covEndDate;
    private String covEndVolume;
    private String covEndIssue;
    private String perpetualAccStartDate;
    private String perpetualAccStartVolume;
    private String perpetualAccStartIssue;
    private String perpetualAccEndDate;
    private String perpetualAccEndVolume;
    private String perpetualAccEndIssue;
    private String url;
    private OLEEResourceRecordDocument oleERSDocument;

    // private List<OLEEResourceCopy> oleCopyList = new ArrayList<OLEEResourceCopy>();
    public String getOleEResourceInstanceId() {
        return oleEResourceInstanceId;
    }

    public void setOleEResourceInstanceId(String oleEResourceInstanceId) {
        this.oleEResourceInstanceId = oleEResourceInstanceId;
    }

    public String getOleERSIdentifier() {
        return oleERSIdentifier;
    }

    public void setOleERSIdentifier(String oleERSIdentifier) {
        this.oleERSIdentifier = oleERSIdentifier;
    }

    public String getHoldingsId() {
        return holdingsId;
    }

    public void setHoldingsId(String holdingsId) {
        this.holdingsId = holdingsId;
    }

    public String getInstanceFlag() {
        return instanceFlag;
    }

    public void setInstanceFlag(String instanceFlag) {
        this.instanceFlag = instanceFlag;
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

    public String getInstanceTitle() {
        return instanceTitle;
    }

    public void setInstanceTitle(String instanceTitle) {
        this.instanceTitle = instanceTitle;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getInstanceHoldings() {
        return instanceHoldings;
    }

    public void setInstanceHoldings(String instanceHoldings) {
        this.instanceHoldings = instanceHoldings;
    }

    public String getPublicDisplayNote() {
        return publicDisplayNote;
    }

    public void setPublicDisplayNote(String publicDisplayNote) {
        this.publicDisplayNote = publicDisplayNote;
    }

    public String getInstancePublisher() {
        return instancePublisher;
    }

    public void setInstancePublisher(String instancePublisher) {
        this.instancePublisher = instancePublisher;
    }

    public String getPlatForm() {
        return platForm;
    }

    public void setPlatForm(String platForm) {
        this.platForm = platForm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getAutoAddTitlesFromGOKb() {
        return autoAddTitlesFromGOKb;
    }

    public void setAutoAddTitlesFromGOKb(String autoAddTitlesFromGOKb) {
        this.autoAddTitlesFromGOKb = autoAddTitlesFromGOKb;
    }

    public String getAutoRemoveTitlesFromGOKb() {
        return autoRemoveTitlesFromGOKb;
    }

    public void setAutoRemoveTitlesFromGOKb(String autoRemoveTitlesFromGOKb) {
        this.autoRemoveTitlesFromGOKb = autoRemoveTitlesFromGOKb;
    }

    public String getAutoUpdateMetaDataFromGOKb() {
        return autoUpdateMetaDataFromGOKb;
    }

    public void setAutoUpdateMetaDataFromGOKb(String autoUpdateMetaDataFromGOKb) {
        this.autoUpdateMetaDataFromGOKb = autoUpdateMetaDataFromGOKb;
    }

    public String getAutoArcInstanceRec() {
        return autoArcInstanceRec;
    }

    public void setAutoArcInstanceRec(String autoArcInstanceRec) {
        this.autoArcInstanceRec = autoArcInstanceRec;
    }

    public String getCovStartDate() {
        return covStartDate;
    }

    public void setCovStartDate(String covStartDate) {
        this.covStartDate = covStartDate;
    }

    public String getCovStartVolume() {
        return covStartVolume;
    }

    public void setCovStartVolume(String covStartVolume) {
        this.covStartVolume = covStartVolume;
    }

    public String getCovStartIssue() {
        return covStartIssue;
    }

    public void setCovStartIssue(String covStartIssue) {
        this.covStartIssue = covStartIssue;
    }

    public String getCovEndDate() {
        return covEndDate;
    }

    public void setCovEndDate(String covEndDate) {
        this.covEndDate = covEndDate;
    }

    public String getCovEndVolume() {
        return covEndVolume;
    }

    public void setCovEndVolume(String covEndVolume) {
        this.covEndVolume = covEndVolume;
    }

    public String getCovEndIssue() {
        return covEndIssue;
    }

    public void setCovEndIssue(String covEndIssue) {
        this.covEndIssue = covEndIssue;
    }

    public String getPerpetualAccStartDate() {
        return perpetualAccStartDate;
    }

    public void setPerpetualAccStartDate(String perpetualAccStartDate) {
        this.perpetualAccStartDate = perpetualAccStartDate;
    }

    public String getPerpetualAccStartVolume() {
        return perpetualAccStartVolume;
    }

    public void setPerpetualAccStartVolume(String perpetualAccStartVolume) {
        this.perpetualAccStartVolume = perpetualAccStartVolume;
    }

    public String getPerpetualAccStartIssue() {
        return perpetualAccStartIssue;
    }

    public void setPerpetualAccStartIssue(String perpetualAccStartIssue) {
        this.perpetualAccStartIssue = perpetualAccStartIssue;
    }

    public String getPerpetualAccEndDate() {
        return perpetualAccEndDate;
    }

    public void setPerpetualAccEndDate(String perpetualAccEndDate) {
        this.perpetualAccEndDate = perpetualAccEndDate;
    }

    public String getPerpetualAccEndVolume() {
        return perpetualAccEndVolume;
    }

    public void setPerpetualAccEndVolume(String perpetualAccEndVolume) {
        this.perpetualAccEndVolume = perpetualAccEndVolume;
    }

    public String getPerpetualAccEndIssue() {
        return perpetualAccEndIssue;
    }

    public void setPerpetualAccEndIssue(String perpetualAccEndIssue) {
        this.perpetualAccEndIssue = perpetualAccEndIssue;
    }

    public OLEEResourceRecordDocument getOleERSDocument() {
        return oleERSDocument;
    }

    public void setOleERSDocument(OLEEResourceRecordDocument oleERSDocument) {
        this.oleERSDocument = oleERSDocument;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    /*
    public List<OLEEResourceCopy> getOleCopyList() {
        return oleCopyList;
    }

    public void setOleCopyList(List<OLEEResourceCopy> oleCopyList) {
        this.oleCopyList = oleCopyList;
    }*/
}
