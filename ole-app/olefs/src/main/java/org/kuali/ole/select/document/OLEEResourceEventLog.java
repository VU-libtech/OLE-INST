package org.kuali.ole.select.document;

import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.GlobalVariables;

import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: arjuns
 * Date: 6/26/13
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */


public class OLEEResourceEventLog extends PersistableBusinessObjectBase {
    private String oleEResEventLogID;
    private String oleERSIdentifier;
    private String eventType;
    private Timestamp eventDate;
    private String eventUser;
    private String eventNote;
    private OLEEResourceRecordDocument oleERSDocument;

    public String getOleEResEventLogID() {
        return oleEResEventLogID;
    }

    public void setOleEResEventLogID(String oleEResEventLogID) {
        this.oleEResEventLogID = oleEResEventLogID;
    }

    public String getOleERSIdentifier() {
        return oleERSIdentifier;
    }

    public void setOleERSIdentifier(String oleERSIdentifier) {
        this.oleERSIdentifier = oleERSIdentifier;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventUser() {
        if (eventUser == null){
            eventUser= GlobalVariables.getUserSession().getPrincipalName();
        }
        return eventUser;
    }

    public void setEventUser(String eventUser) {
        this.eventUser = eventUser;
    }

    public String getEventNote() {
        return eventNote;
    }

    public void setEventNote(String eventNote) {
        this.eventNote = eventNote;
    }

    public OLEEResourceRecordDocument getOleERSDocument() {
        return oleERSDocument;
    }

    public void setOleERSDocument(OLEEResourceRecordDocument oleERSDocument) {
        this.oleERSDocument = oleERSDocument;
    }

    /**
     * set the timestamp attribute value.
     */
    public void setCurrentTimeStamp() {
        final Timestamp now = CoreApiServiceLocator.getDateTimeService().getCurrentTimestamp();
        this.setEventDate(now);
    }
}
