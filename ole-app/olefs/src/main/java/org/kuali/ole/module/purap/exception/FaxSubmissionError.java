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
 * @author aapotts
 */
public class FaxSubmissionError extends Error {

    /**
     *
     */
    public FaxSubmissionError() {
        super();
    }

    /**
     * @param arg0
     */
    public FaxSubmissionError(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public FaxSubmissionError(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public FaxSubmissionError(Throwable arg0) {
        super(arg0);
    }

}
