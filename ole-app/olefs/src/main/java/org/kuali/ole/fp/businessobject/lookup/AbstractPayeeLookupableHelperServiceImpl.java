/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.fp.businessobject.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.ole.fp.businessobject.DisbursementPayee;
import org.kuali.ole.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEPropertyConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.vnd.VendorConstants;
import org.kuali.ole.vnd.VendorPropertyConstants;
import org.kuali.ole.vnd.businessobject.VendorDetail;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.rice.kns.lookup.Lookupable;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.datadictionary.AttributeSecurity;
import org.kuali.rice.krad.util.ObjectUtils;

public class AbstractPayeeLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    protected Lookupable vendorLookupable;
    protected DisbursementVoucherPayeeService disbursementVoucherPayeeService;

    // get the label for the given attribute of the current business object
    protected String getAttributeLabel(String attributeName) {
        return this.getDataDictionaryService().getAttributeLabel(getBusinessObjectClass(), attributeName);
    }

    // perform vendor search
    protected List<DisbursementPayee> getVendorsAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();

        Map<String, String> fieldsForLookup = this.getVendorFieldValues(fieldValues);
        vendorLookupable.setBusinessObjectClass(VendorDetail.class);
        vendorLookupable.validateSearchParameters(fieldsForLookup);

        List<? extends BusinessObject> vendorList = vendorLookupable.getSearchResults(fieldsForLookup);
        for (BusinessObject vendor : vendorList) {
            VendorDetail vendorDetail = (VendorDetail) vendor;
            DisbursementPayee payee = getPayeeFromVendor(vendorDetail, fieldValues);
            payeeList.add(payee);
        }

        return payeeList;
    }

    // get the search criteria valid for vendor lookup
    protected Map<String, String> getVendorFieldValues(Map<String, String> fieldValues) {
        Map<String, String> vendorFieldValues = new HashMap<String, String>();
        vendorFieldValues.put(OLEPropertyConstants.TAX_NUMBER, fieldValues.get(OLEPropertyConstants.TAX_NUMBER));
        vendorFieldValues.put(OLEPropertyConstants.VENDOR_NAME, fieldValues.get(OLEPropertyConstants.VENDOR_NAME));
        vendorFieldValues.put(OLEPropertyConstants.VENDOR_NUMBER, fieldValues.get(OLEPropertyConstants.VENDOR_NUMBER));
        vendorFieldValues.put(OLEPropertyConstants.PERSON_FIRST_NAME, fieldValues.get(OLEPropertyConstants.PERSON_FIRST_NAME));
        vendorFieldValues.put(OLEPropertyConstants.PERSON_LAST_NAME, fieldValues.get(OLEPropertyConstants.PERSON_LAST_NAME));
        vendorFieldValues.put(OLEPropertyConstants.ACTIVE, fieldValues.get(OLEPropertyConstants.ACTIVE));

        Map<String, String> fieldConversionMap = disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndVendor();
        this.replaceFieldKeys(vendorFieldValues, fieldConversionMap);

        String vendorName = this.getVendorName(vendorFieldValues);
        if (StringUtils.isNotBlank(vendorName)) {
            vendorFieldValues.put(OLEPropertyConstants.VENDOR_NAME, vendorName);
        }

        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_FIRST_NAME);
        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_LAST_NAME);

        return vendorFieldValues;
    }

    // get the vendor name from the given field value map
    protected String getVendorName(Map<String, String> vendorFieldValues) {
        String firstName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_FIRST_NAME);
        String lastName = vendorFieldValues.get(VendorPropertyConstants.VENDOR_LAST_NAME);

        // TODO The following code has a bug: it doesn't handle blank first name right.
        if (StringUtils.isNotBlank(lastName)) {
            return lastName + VendorConstants.NAME_DELIM + firstName;
        }
        else if (StringUtils.isNotBlank(firstName)) {
            return OLEConstants.WILDCARD_CHARACTER + VendorConstants.NAME_DELIM + firstName;
        }

        return StringUtils.EMPTY;
    }

    protected DisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail, Map<String, String> fieldValues) {
        DisbursementPayee payee = disbursementVoucherPayeeService.getPayeeFromVendor(vendorDetail);
        payee.setPaymentReasonCode(fieldValues.get(OLEPropertyConstants.PAYMENT_REASON_CODE));

        //KFSMI-5497
        //get the attributeSecurity property and mask the field so that on results screen will be shown masked.
        DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        AttributeSecurity attributeSecurity =  dataDictionaryService.getAttributeSecurity(DisbursementPayee.class.getName(), OLEPropertyConstants.TAX_NUMBER);
        if (ObjectUtils.isNotNull(attributeSecurity)) {
            attributeSecurity.setMask(true);
        }

        return payee;
    }

    // perform person search
    protected List<DisbursementPayee> getPersonAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();

        Map<String, String> fieldsForLookup = this.getPersonFieldValues(fieldValues);
        List<Person> persons = SpringContext.getBean(PersonService.class).findPeople(fieldsForLookup);

        for (Person personDetail : persons) {
            DisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues);
            payeeList.add(payee);
        }

        return payeeList;
    }

    // get the search criteria valid for person lookup
    protected Map<String, String> getPersonFieldValues(Map<String, String> fieldValues) {
        Map<String, String> personFieldValues = new HashMap<String, String>();
        personFieldValues.put(OLEPropertyConstants.PERSON_FIRST_NAME, fieldValues.get(OLEPropertyConstants.PERSON_FIRST_NAME));
        personFieldValues.put(OLEPropertyConstants.PERSON_LAST_NAME, fieldValues.get(OLEPropertyConstants.PERSON_LAST_NAME));
        personFieldValues.put(OLEPropertyConstants.EMPLOYEE_ID, fieldValues.get(OLEPropertyConstants.EMPLOYEE_ID));
        personFieldValues.put(OLEPropertyConstants.ACTIVE, fieldValues.get(OLEPropertyConstants.ACTIVE));
        Map<String, String> fieldConversionMap = disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndPerson();
        this.replaceFieldKeys(personFieldValues, fieldConversionMap);

        personFieldValues.put(KIMPropertyConstants.Person.EMPLOYEE_STATUS_CODE, OLEConstants.EMPLOYEE_ACTIVE_STATUS);

        return personFieldValues;
    }

    protected DisbursementPayee getPayeeFromPerson(Person personDetail, Map<String, String> fieldValues) {
        DisbursementPayee payee = disbursementVoucherPayeeService.getPayeeFromPerson(personDetail);
        payee.setPaymentReasonCode(fieldValues.get(OLEPropertyConstants.PAYMENT_REASON_CODE));

        //OLEMI-5497
        //get the attributeSecurity property and unmask the field so that on results screen, it will set as blank.
        DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        AttributeSecurity attributeSecurity =  dataDictionaryService.getAttributeSecurity(DisbursementPayee.class.getName(), OLEPropertyConstants.TAX_NUMBER);
        if (ObjectUtils.isNotNull(attributeSecurity)) {
            attributeSecurity.setMask(false);
        }

        return payee;
    }

    // replace the keys in fieldValues with the corresponding values defined in fieldConversionMap
    protected void replaceFieldKeys(Map<String, String> fieldValues, Map<String, String> fieldConversionMap) {
        for (String key : fieldConversionMap.keySet()) {
            if (fieldValues.containsKey(key)) {
                String value = fieldValues.get(key);
                String newKey = fieldConversionMap.get(key);
                fieldValues.remove(key);
                fieldValues.put(newKey, value);
            }
        }
    }

    /**
     * Sets the vendorLookupable attribute value.
     *
     * @param vendorLookupable The vendorLookupable to set.
     */
    public void setVendorLookupable(Lookupable vendorLookupable) {
        this.vendorLookupable = vendorLookupable;
    }

    /**
     * Sets the disbursementVoucherPayeeService attribute value.
     *
     * @param disbursementVoucherPayeeService The disbursementVoucherPayeeService to set.
     */
    public void setDisbursementVoucherPayeeService(DisbursementVoucherPayeeService disbursementVoucherPayeeService) {
        this.disbursementVoucherPayeeService = disbursementVoucherPayeeService;
    }

}
