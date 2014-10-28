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
package org.kuali.ole.module.purap.service;

import java.util.Date;

/**
 * Defines methods that must be implemented by a PdpExtractService implementation.
 */
public interface PdpExtractService {

    /**
     * Extract all payments marked immediate. This won't combine any payments with credit memos.
     */
    public void extractImmediatePaymentsOnly();

    /**
     * Extract all payments ready to be paid. This may combine payments with appropriate credit memos
     *
     * @param runDate the date to assume when the process (e.g. a batch Step or Job) was started to extract payments
     */
    public void extractPayments(Date runDate);

}
