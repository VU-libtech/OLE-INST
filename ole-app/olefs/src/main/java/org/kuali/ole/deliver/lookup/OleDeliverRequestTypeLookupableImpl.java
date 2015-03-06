package org.kuali.ole.deliver.lookup;

import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.bo.OleDeliverRequestType;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.form.LookupForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: asham
 * Date: 7/15/13
 * Time: 7:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleDeliverRequestTypeLookupableImpl extends LookupableImpl {
    @Override
    protected List<?> getSearchResults(LookupForm form, Map<String, String> searchCriteria, boolean unbounded) {
        List<?> searchResults;
        List<OleDeliverRequestType> finalSearchResult = new ArrayList<OleDeliverRequestType>();
        List<OleDeliverRequestType> oleDeliverRequestType = new ArrayList<OleDeliverRequestType>();
        oleDeliverRequestType = (List<OleDeliverRequestType>) super.getSearchResults(form, searchCriteria, unbounded);
        finalSearchResult.addAll(oleDeliverRequestType);
        searchResults = finalSearchResult;
        if (searchResults.size() == 0) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, OLEConstants.NO_RECORD_FOUND);
        }
        sortSearchResults(form, searchResults);
        return searchResults;
    }

}
