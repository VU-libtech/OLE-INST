package org.kuali.ole.vnd.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.vnd.VendorKeyConstants;
import org.kuali.ole.vnd.VendorPropertyConstants;
import org.kuali.ole.vnd.businessobject.VendorAlias;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.ole.vnd.businessobject.VendorTransmissionFormatDetail;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: arjuns
 * Date: 7/15/13
 * Time: 6:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class OleVendorRule extends VendorRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = processValidation(document);
        VendorDetail vendorDetail =  (VendorDetail)document.getNewMaintainableObject().getDataObject();
        List<VendorTransmissionFormatDetail> vendorTransmissionFormatDetailList = vendorDetail.getVendorTransmissionFormat();
        List formatId = new ArrayList();
        for(VendorTransmissionFormatDetail vendorTransmissionFormatDetail : vendorTransmissionFormatDetailList) {
            if (formatId.contains(vendorTransmissionFormatDetail.getVendorTransmissionFormatId())) {
                putFieldError(VendorPropertyConstants.VENDOR_TRANSMISSION_FORMAT, VendorKeyConstants.OLE_VENDOR_DUPLICATE_TRANS_FORMAT);
                valid &= false;
            }
            formatId.add(vendorTransmissionFormatDetail.getVendorTransmissionFormatId());
        }
        return valid & super.processCustomRouteDocumentBusinessRules(document);
    }

    private boolean processValidation(MaintenanceDocument document) {
        boolean valid = true;
        VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getDataObject();
        valid &= processExternalVendorCode(vendorDetail);

        return valid;
    }

    protected boolean processExternalVendorCode(VendorDetail vendorDetail) {
        boolean valid = true;
        List<VendorAlias> vendorAliases = vendorDetail.getVendorAliases();
        List<String> aliasList=new ArrayList<>();
        if(vendorAliases.size()>0) {
            for(VendorAlias vendorAlias : vendorAliases) {
                if(!aliasList.contains(vendorAlias.getVendorAliasType().getAliasType()))
                {
                    aliasList.add(vendorAlias.getVendorAliasType().getAliasType());
                } else{
                    putFieldError(VendorPropertyConstants.VENDOR_SEARCH_ALIASES, VendorKeyConstants.OLE_VENDOR_DUPLICATE_ALIAS_NAME);
                    valid &= false;
                }
            }
        }
        if (StringUtils.isBlank(vendorDetail.getVendorName())) {
            // At least one of the three vendor name fields must be filled in.

            if (StringUtils.isBlank(vendorDetail.getVendorFirstName()) && StringUtils.isBlank(vendorDetail.getVendorLastName())) {
//
//                GlobalVariables.getMessageMap().putErrorForSectionId("OleCirculationDesk-Locations", OLEConstants.OleCirculationDesk.OLE_VENDOR_DUPLICATE_ALIAS_NAME);
//                return getUIFModelAndView(form);
                putFieldError(VendorPropertyConstants.VENDOR_ALIAS_NAME, VendorKeyConstants.OLE_VENDOR_EMPTY_NAME);
                valid &= false;
            }
            // If either the vendor first name or the vendor last name have been entered, the other must be entered.

        }

        return valid;
    }
}
