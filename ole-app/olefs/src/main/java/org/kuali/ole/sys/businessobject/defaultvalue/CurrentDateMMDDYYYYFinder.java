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

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.krad.valuefinder.ValueFinder;

import java.util.Calendar;
import java.util.Date;

public class CurrentDateMMDDYYYYFinder implements ValueFinder {

    public String getValue() {
        // get the current date from the service
        Date date = SpringContext.getBean(DateTimeService.class).getCurrentDate();

        // remove the time component
        date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);

        // format it as expected
        return DateFormatUtils.format(date, RiceConstants.SIMPLE_DATE_FORMAT_FOR_DATE);
    }

}
