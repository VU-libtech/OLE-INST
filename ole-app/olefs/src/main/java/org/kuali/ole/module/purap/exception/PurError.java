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
package org.kuali.ole.module.purap.exception;

/**
 * Represents the error that is thrown when there is abnormal condition
 * within Purchasing Accounts Payable module.
 */
public class PurError extends Error {
    public PurError() {
        super();
    }

    public PurError(String message) {
        super(message);
    }

    public PurError(String message, Throwable arg1) {
        super(message, arg1);
    }

    public PurError(Throwable arg0) {
        super(arg0);
    }
}
