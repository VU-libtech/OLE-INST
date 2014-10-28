/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.ole.sys.businessobject.defaultvalue;

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.service.UniversityDateService;
import org.kuali.rice.krad.valuefinder.ValueFinder;

/**
 * Returns the current fiscal year.
 */
public class FiscalYearFinder implements ValueFinder {

    /**
     * @see org.kuali.rice.krad.valuefinder.ValueFinder#getValue()
     */
    public String getValue() {
        return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().toString();
    }

}
