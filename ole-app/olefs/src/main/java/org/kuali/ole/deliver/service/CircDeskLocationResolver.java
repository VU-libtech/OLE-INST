package org.kuali.ole.deliver.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.bo.OleCirculationDesk;
import org.kuali.ole.deliver.bo.OleCirculationDeskLocation;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.service.OLEEResourceSearchService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 4/8/15.
 */
public class CircDeskLocationResolver {

    private static final Logger LOG = Logger.getLogger(CircDeskLocationResolver.class);

    private BusinessObjectService businessObjectService;

    public String getReplyToEmail(String itemLocation) {
        OleCirculationDesk oleCirculationDesk = getCirculationDesk(itemLocation);
        if (oleCirculationDesk != null && StringUtils.isNotBlank(oleCirculationDesk.getReplyToEmail())) {
            return oleCirculationDesk.getReplyToEmail();
        }
        return null;
    }


    public OleCirculationDesk getCirculationDesk(String itemLocation) {
        OleLocation oleLocation = null;
        try {
            if (StringUtils.isNotBlank(itemLocation)) {
                oleLocation = getLocationByLocationCode(itemLocation);
            }
        } catch (Exception e) {
            LOG.error("Exception " + e);
        }
        if (oleLocation != null) {
            OleCirculationDesk oleCirculationDesk = getCirculationDeskByLocationId(oleLocation.getLocationId());
            return oleCirculationDesk;
        }
        return null;
    }



    /**
     * This method returns location using location code.
     *
     * @param locationCode
     * @return
     * @throws Exception
     */
    public OleLocation getLocationByLocationCode(String locationCode) throws Exception {
        LOG.debug("Inside the getLocationByLocationCode method");
        Map barMap = new HashMap();
        barMap.put(OLEConstants.LOC_CD, locationCode);
        List<OleLocation> matchingLocation = (List<OleLocation>) getBusinessObjectService().findMatching(OleLocation.class, barMap);
        return matchingLocation != null && matchingLocation.size() > 0 ? matchingLocation.get(0) : null;
    }

    public OleCirculationDesk getCirculationDeskByLocationId(String locationId) {
        OleCirculationDeskLocation oleCirculationDeskLocation = getOleCirculationDeskLocationByLocationId(locationId);
        if (oleCirculationDeskLocation != null) {
            Map<String, String> userMap = new HashMap<String, String>();
            userMap.put("circulationDeskId", oleCirculationDeskLocation.getCirculationDeskId());
            List<OleCirculationDesk> oleCirculationDesks = (List<OleCirculationDesk>) getBusinessObjectService().findMatching(OleCirculationDesk.class, userMap);
            return oleCirculationDesks != null && oleCirculationDesks.size() > 0 ? oleCirculationDesks.get(0) : null;
        }
        return null;
    }

    public OleCirculationDeskLocation getOleCirculationDeskLocationByLocationId(String locationId) {
        Map<String, String> locationMap = new HashMap<String, String>();
        locationMap.put("circulationDeskLocation", locationId);
        List<OleCirculationDeskLocation> oleCirculationDeskLocations = (List<OleCirculationDeskLocation>) getBusinessObjectService().findMatching(OleCirculationDeskLocation.class, locationMap);
        return oleCirculationDeskLocations != null && oleCirculationDeskLocations.size() > 0 ? oleCirculationDeskLocations.get(0) : null;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }
}
