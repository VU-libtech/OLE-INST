package org.kuali.ole.docstore.model.rdbms.bo;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: mjagan
 * Date: 7/15/13
 * Time: 10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocationsCheckinCountRecord extends PersistableBusinessObjectBase
        implements Serializable {

    private String locationCountId;
    private String locationName;
    private Integer locationCount;
    private Integer locationInhouseCount;
    private String itemId;

    public String getLocationCountId() {
        return locationCountId;
    }

    public void setLocationCountId(String locationCountId) {
        this.locationCountId = locationCountId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(Integer locationCount) {
        this.locationCount = locationCount;
    }

    public Integer getLocationInhouseCount() {
        return locationInhouseCount;
    }

    public void setLocationInhouseCount(Integer locationInhouseCount) {
        this.locationInhouseCount = locationInhouseCount;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
