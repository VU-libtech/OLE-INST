/*
 * Copyright 2009 The Kuali Foundation
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.ole.gl.batch.service.impl.exception.FatalErrorException;
import org.kuali.ole.gl.businessobject.Balance;
import org.kuali.ole.gl.businessobject.OriginEntryFull;
import org.kuali.ole.sys.service.ReportWriterService;

public interface OrganizationReversionProcess {
    /**
     * Runs the organization reversion process.
     * @param jobParameters the parameters used in the process
     * @param organizationReversionCounts a Map of named statistics generated by running the process
     */
    public abstract void organizationReversionProcess(Map jobParameters, Map<String, Integer> organizationReversionCounts);
    
    /**
     * This method initializes several properties needed for the process to run correctly
     */
    public abstract void initializeProcess();
    
    /**
     * Given a list of balances, this method generates the origin entries for the organization reversion/carry forward process, and saves those
     * to an initialized origin entry group
     * 
     * @param balances an iterator of balances to process; each balance returned by the iterator will be processed by this method
     */
    public abstract void processBalances(Iterator<Balance> balances);
    
    /**
     * This method determines which origin entries (reversion, cash reversion, or carry forward) need to be generated for the current unit of work,
     * and then delegates to the origin entry generation methods to create those entries
     * 
     * @return a list of OriginEntries which need to be written
     * @throws FatalErrorException thrown if object codes are missing in any of the generation methods
     */
    public abstract List<OriginEntryFull> generateOutputOriginEntries() throws FatalErrorException;
    
    /**
     * This method generates cash reversion origin entries for the current organization reversion, and adds them to the given list
     * 
     * @param originEntriesToWrite a list of OriginEntryFulls to stick generated origin entries into
     * @throws FatalErrorException thrown if an origin entry's object code can't be found
     */
    public abstract void generateCashReversions(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException;
    
    /**
     * Generates carry forward origin entries on a category by category basis (if the organization reversion record asks for that), assuming carry
     * forwards are required for the current unit of work
     * 
     * @param originEntriesToWrite a list of origin entries to write, which any generated origin entries should be added to
     * @throws FatalErrorException thrown if an object code cannot be found
     */
    public abstract void generateMany(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException;
    
    /**
     * If carry forwards need to be generated for this unit of work, this method will generate the origin entries to accomplish those object codes.
     * Note: this will only be called if the organization reversion record tells the process to munge all carry forwards for all categories
     * together; if the organization reversion record does not call for such a thing, then generateMany will be called
     * 
     * @param originEntriesToWrite a list of origin entries to write, that any generated origin entries should be added to
     * @throws FatalErrorException thrown if the current object code can't be found in the database
     */
    public abstract void generateCarryForwards(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException;
    
    /**
     * If reversions are necessary, this will generate the origin entries for those reversions
     * 
     * @param originEntriesToWrite the list of origin entries to add reversions into
     * @throws FatalErrorException thrown if object code if the entry can't be found
     */
    public abstract void generateReversions(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException;
    
    /**
     * This method calculates the totals for a given unit of work's reversion
     * 
     * @throws FatalErrorException
     */
    public abstract void calculateTotals() throws FatalErrorException;
    
    /**
     * Writes out the encapsulated origin entry ledger report to the given reportWriterService
     * @param reportWriterService the report to write the ledger summary report to
     */
    public abstract void writeLedgerSummaryReport(ReportWriterService reportWriterService);
    
    /**
     * Sets the holdGeneratedOriginEntries attribute value.
     * 
     * @param holdGeneratedOriginEntries The holdGeneratedOriginEntries to set.
     */
    public abstract void setHoldGeneratedOriginEntries(boolean holdGeneratedOriginEntries);
    
    /**
     * Gets the generatedOriginEntries attribute.
     * 
     * @return Returns the generatedOriginEntries.
     */
    public abstract List<OriginEntryFull> getGeneratedOriginEntries();
    
    /**
     * Returns the total number of balances for the previous fiscal year
     * 
     * @return the total number of balances for the previous fiscal year
     */
    public abstract int getBalancesRead();
    
    /**
     * Returns the total number of balances selected for inclusion in this process
     * 
     * @return the total number of balances selected for inclusion in this process
     */
    public abstract int getBalancesSelected();
}
