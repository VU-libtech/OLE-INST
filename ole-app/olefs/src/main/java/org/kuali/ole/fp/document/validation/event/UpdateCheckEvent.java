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
package org.kuali.ole.fp.document.validation.event;

import org.kuali.ole.fp.businessobject.Check;
import org.kuali.ole.sys.document.validation.event.AttributedDocumentEventBase;
import org.kuali.rice.krad.document.Document;

/**
 * An event which is fired when a member of the Cash Receipt family of documents updates a pre-existing check on a document.
 */ 
public class UpdateCheckEvent extends AttributedDocumentEventBase implements CheckEvent {
    private final Check check;
    
    /**
     * Initializes fields common to all subclasses
     * 
     * @param description
     * @param errorPathPrefix
     * @param document
     * @param check
     */
    public UpdateCheckEvent(String description, String errorPathPrefix, Document document, Check check) {
        super(description, errorPathPrefix, document);

        this.check = check;
    }
    
    /**
     * Constructs a UpdateCheckEvent with a blank description
     * @param errorPathPrefix
     * @param document
     * @param check
     */
    public UpdateCheckEvent(String errorPathPrefix, Document document, Check check) {
        this("", errorPathPrefix, document, check);
    }
    
    /**
     * @see org.kuali.rice.krad.rule.event.CheckEvent#getCheck()
     */
    public Check getCheck() {
        return check;
    }
}
