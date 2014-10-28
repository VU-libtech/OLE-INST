/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.sys.document.web;

/**
 * Interface that declares extra methods needed for renderable elements that have headers
 */
public interface TableJoiningWithHeader extends TableJoining {
    /**
     * Returns the property of the accounting line business object that can be used to find the label for the given renderable element
     * @param renderingContext the context the header will be rendered to
     * @return the property to lookup the label in the data dictionary
     */
    public abstract HeaderLabel createHeaderLabel();
    
    /**
     * Will this table joining element actually end up hidden?  Then we best not create a header cell for it
     * @return true if the table joiner will be hidden, false otherwise - in which case a header cell will be rendered
     */
    public abstract boolean isHidden();
}
