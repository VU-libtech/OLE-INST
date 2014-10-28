/*
 * Copyright 2005-2006 The Kuali Foundation
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
package org.kuali.ole.gl.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.ole.gl.businessobject.LedgerEntryHolder;
import org.kuali.ole.gl.businessobject.OriginEntryFull;
import org.kuali.ole.gl.businessobject.PosterOutputSummaryEntry;

/**
 * An interface of methods to interact with Origin Entries
 */
public interface OriginEntryService {
    
    public void createEntry(OriginEntryFull originEntry, PrintStream ps);

    /**
     * writes out a list of origin entries to an output stream.
     * 
     * @param entries an Iterator of entries to save as text
     * @param bw the output stream to write origin entries to
     */
    public void flatFile(Iterator<OriginEntryFull> entries, BufferedOutputStream bw);

    /**
     * get the summarized information of the entries that belong to the entry groups with the given group id list
     * 
     * @param groupIdList the origin entry groups
     * @return a set of summarized information of the entries within the specified group
     */
    public LedgerEntryHolder getSummaryByGroupId(Collection groupIdList);
    
    /**
     * get the summarized information of poster input entries that belong to the entry groups with the given group id list
     * 
     * @param groupIdList the origin entry groups
     * @return a map of summarized information of poster input entries within the specified groups
     */
    public Map<String, PosterOutputSummaryEntry> getPosterOutputSummaryByGroupId(Collection groupIdList);

    public Integer getGroupCount(String groupId);
    
    public Map getEntriesByBufferedReader(BufferedReader inputBufferedReader, List<OriginEntryFull> originEntryList);
    
    public  Map getEntriesByGroupIdWithPath(String fileNameWithPath, List<OriginEntryFull> originEntryList);
}
