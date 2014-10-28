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
package org.kuali.ole.gl.batch.service;

import java.util.List;

/**
 * An exception that occurs during the loading and parsing of a Collector file
 */
public class CollectorLoadException extends RuntimeException {
    private List errors;

    /**
     * Constructs a CollectorLoadException instance
     * @param errors a List of errors encountered while loading and parsing the file
     */
    public CollectorLoadException(List errors) {
        this.errors = errors;
    }

    /**
     * Returns the specific load/parse errors encountered that caused this exception 
     * 
     * @return a List of errors
     */
    public List getErrors() {
        return errors;
    }
}
