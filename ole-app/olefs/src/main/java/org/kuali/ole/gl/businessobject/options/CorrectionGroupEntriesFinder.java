/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.ole.gl.businessobject.options;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kuali.ole.gl.service.OriginEntryGroupService;
import org.kuali.ole.gl.web.util.OriginEntryFileComparator;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

/**
 * Returns list of GL origin entry filenames
 */
public class CorrectionGroupEntriesFinder extends KeyValuesBase {

    /**
     * Returns a list of key/value pairs to display correction groups that can be used in a Correction Document
     * 
     * @return a List of key/value pairs for correction groups
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List activeLabels = new ArrayList();

        OriginEntryGroupService originEntryGroupService = SpringContext.getBean(OriginEntryGroupService.class);
        File[] fileList = originEntryGroupService.getAllFileInBatchDirectory();

        List<File> sortedFileList = Arrays.asList(fileList);
        Collections.sort(sortedFileList, new OriginEntryFileComparator());

        for (File file : sortedFileList) {
            String fileName = file.getName();

            // build display file name with date and size
            Date date = new Date(file.lastModified());
            String timeInfo = "(" + SpringContext.getBean(DateTimeService.class).toDateTimeString(date) + ")";
            String sizeInfo = "(" + (new Long(file.length())).toString() + ")";

            activeLabels.add(new ConcreteKeyValue(fileName, timeInfo + " " + fileName + " " + sizeInfo));
        }

        return activeLabels;
    }

}
