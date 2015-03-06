/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.ole.docstore.discovery.solr.work.bib.dublin.unqualified;

import org.kuali.ole.docstore.discovery.solr.work.bib.WorkBibCommonFields;

/**
 * Class to WorkBibDublinUnqualifiedFields.
 *
 * @author Rajesh Chowdary K
 */
public interface WorkBibDublinUnqualifiedFields
        extends WorkBibCommonFields {
    public static final String ISBN_DISPLAY = "ISBN_display";
    public static final String ISBN_SEARCH = "ISBN_search";
    public static final String ISSN_DISPLAY = "ISSN_display";
    public static final String ISSN_SEARCH = "ISSN_search";
}
