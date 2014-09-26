/*
 * Copyright 2011 The Kuali Foundation
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
package org.kuali.ole.sys.batch;

import java.util.List;

import org.kuali.ole.sys.FileUtil;

/**
 * Base class for InitiateDirectory implementations 
 */
public abstract class InitiateDirectoryBase implements InitiateDirectory{
    
    /**
     * Create the directories needed
     * 
     * @see org.kuali.ole.sys.batch.service.InitiateDirectory#prepareDirectories(java.util.List)
     */
    public void prepareDirectories(List<String> directoryPaths) {
        FileUtil.createDirectories(directoryPaths);
    }
    
    /**
     * @see org.kuali.ole.sys.batch.service.InitiateDirectory#getRequiredDirectoryNames()
     */
    public abstract List<String> getRequiredDirectoryNames();
}
