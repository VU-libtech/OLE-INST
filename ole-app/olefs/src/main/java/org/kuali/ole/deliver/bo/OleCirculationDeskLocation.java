package org.kuali.ole.deliver.bo;

import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ?
 * Date: 12/17/12
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleCirculationDeskLocation extends PersistableBusinessObjectBase {
    private String circulationDeskLocationId;
    private String circulationDeskId;
    private String circulationDeskLocation;
    private String circulationLocationCode;
    private String circulationFullLocationCode;
    private OleCirculationDesk oleCirculationDesk = new OleCirculationDesk();
    private OleLocation location = new OleLocation();

    public String getCirculationDeskLocationId() {
        return circulationDeskLocationId;
    }

    public void setCirculationDeskLocationId(String circulationDeskLocationId) {
        this.circulationDeskLocationId = circulationDeskLocationId;
    }

    public String getCirculationDeskId() {
        return circulationDeskId;
    }

    public void setCirculationDeskId(String circulationDeskId) {
        this.circulationDeskId = circulationDeskId;
    }

    public String getCirculationDeskLocation() {
        return circulationDeskLocation;
    }

    public void setCirculationDeskLocation(String circulationDeskLocation) {
        this.circulationDeskLocation = circulationDeskLocation;
    }

    public OleCirculationDesk getOleCirculationDesk() {
        return oleCirculationDesk;
    }

    public void setOleCirculationDesk(OleCirculationDesk oleCirculationDesk) {
        this.oleCirculationDesk = oleCirculationDesk;
    }

    public OleLocation getLocation() {
        return location;
    }

    public void setLocation(OleLocation location) {
        this.location = location;
    }

    public String getCirculationLocationCode() {
        if ((circulationLocationCode == null && location != null)) {
            return location.getLocationCode();
        }
        return circulationLocationCode;
    }

    public void setCirculationLocationCode(String circulationLocationCode) {
        this.circulationLocationCode = circulationLocationCode;
    }

    public String getCirculationFullLocationCode() {
        String fullLocationCode = this.getCirculationLocationCode();
        if (circulationDeskLocation != null) {
            Map<String, String> locationMap = new HashMap<String, String>();
            locationMap.put("locationId", circulationDeskLocation);
            List<OleLocation> oleLocationList = (List<OleLocation>) KRADServiceLocator.getBusinessObjectService().findMatching(OleLocation.class, locationMap);
            if (oleLocationList.size() > 0) {

                OleLocation deskLocation = oleLocationList.get(0);
                if (deskLocation.getParentLocationId() != null) {
                    deskLocation = deskLocation.getOleLocation();
                } else {
                    return fullLocationCode;
                }
                while (deskLocation != null) {
                    fullLocationCode = deskLocation.getLocationCode() + "/" + fullLocationCode;
                    if (deskLocation.getParentLocationId() != null) {
                        deskLocation = deskLocation.getOleLocation();
                    } else
                        deskLocation = null;
                }
                return fullLocationCode;
            }
        } else {
            fullLocationCode = this.circulationFullLocationCode;
        }
        return fullLocationCode;
    }

    public void setCirculationFullLocationCode(String circulationFullLocationCode) {
        this.circulationFullLocationCode = circulationFullLocationCode;
    }
}
