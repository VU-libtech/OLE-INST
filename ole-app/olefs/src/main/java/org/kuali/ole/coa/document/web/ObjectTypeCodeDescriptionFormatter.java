/*
 * Copyright 2006-2008 The Kuali Foundation
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
package org.kuali.ole.coa.document.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kuali.ole.coa.businessobject.ObjectType;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.document.web.CodeDescriptionFormatterBase;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.service.BusinessObjectService;

public class ObjectTypeCodeDescriptionFormatter extends CodeDescriptionFormatterBase {

    public ObjectTypeCodeDescriptionFormatter() {
    }

    @Override
    protected String getDescriptionOfBO(PersistableBusinessObject bo) {
        return ((ObjectType) bo).getName();
    }

    @Override
    protected Map<String, PersistableBusinessObject> getValuesToBusinessObjectsMap(Set values) {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put(OLEConstants.GENERIC_CODE_PROPERTY_NAME, values);

        Map<String, PersistableBusinessObject> results = new HashMap<String, PersistableBusinessObject>();
        Collection<ObjectType> coll = SpringContext.getBean(BusinessObjectService.class).findMatchingOrderBy(ObjectType.class, criteria, "versionNumber", true);
        // by sorting on ver #, we can guarantee that the most recent value will remain in the map (assuming the iterator returns
        // BOs in order)
        for (ObjectType ot : coll) {
            results.put(ot.getCode(), ot);
        }
        return null;
    }

}
