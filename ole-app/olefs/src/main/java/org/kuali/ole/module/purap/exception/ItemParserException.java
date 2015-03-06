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
package org.kuali.ole.module.purap.exception;

/**
 * Exception that is thrown when a piece of the <code>ItemParser</code> fails.
 *
 * @see org.kuali.ole.module.purap.util.ItemParser
 */
public class ItemParserException extends RuntimeException {

    private String errorKey;
    private String[] errorParameters;

    /**
     * Constructs an ItemParserException instance.
     *
     * @param message error message
     */
    public ItemParserException(String message) {
        super(message);
    }

    /**
     * Constructs an ItemParserException instance.
     *
     * @param message         error message
     * @param errorKey        key to an error message
     * @param errorParameters error message parameters
     */
    public ItemParserException(String message, String errorKey, String... errorParameters) {
        super(message);
        this.errorKey = errorKey;
        this.errorParameters = errorParameters;
    }

    /**
     * Gets the errorKey attribute.
     *
     * @return Returns the errorKey.
     */
    public String getErrorKey() {
        return errorKey;
    }

    /**
     * Gets the errorParameters attribute.
     *
     * @return Returns the errorParameters.
     */
    public String[] getErrorParameters() {
        return errorParameters;
    }

}
