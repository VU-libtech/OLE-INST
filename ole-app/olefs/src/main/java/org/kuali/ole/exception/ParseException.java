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
package org.kuali.ole.exception;

/**
 * Exception thrown when an error occurs parsing contents
 */
public class ParseException extends RuntimeException {

    /**
     * This method parse the exception and displays the message.
     * @param message
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * This method parse the exception and displays the message.
     * @param message
     */
    public ParseException(String message, Throwable t) {
        super(message, t);
    }

}
