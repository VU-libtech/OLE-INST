/*
 * Copyright 2005-2009 The Kuali Foundation
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
package org.kuali.ole.gl.dataaccess;


/**
 * DAO interface that declares common methods needed for the balancing process.
 */
public interface LedgerBalancingDao {
    /**
     * Populates LedgerEntryHistory with Entry (or LaborEntry) data based on the start fiscal year passed in
     * @param universityFiscalYear fiscal year the populate should start from
     * @return number of rows affected
     */
    public int populateLedgerEntryHistory(Integer universityFiscalYear);
    
    /**
     * Populates LedgerBalanceHistory with Balance (or LaborBalance) data based on the start fiscal year passed in
     * @param universityFiscalYear fiscal year the populate should start from
     * @return number of rows affected
     */
    public int populateLedgerBalanceHistory(Integer universityFiscalYear);
}
