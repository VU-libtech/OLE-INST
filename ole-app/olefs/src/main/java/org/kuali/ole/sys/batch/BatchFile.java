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
package org.kuali.ole.sys.batch;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.krad.bo.TransientBusinessObjectBase;

public class BatchFile extends TransientBusinessObjectBase {
    private File file;
    
    public BatchFile() {
    }

    public String getPath() {
        return BatchFileUtils.pathRelativeToRootDirectory(file.getAbsoluteFile().getParentFile().getAbsolutePath());
    }
    
    public String getFileName() {
        return file.getName();
    }
    
    public Date getLastModifiedDate() {
        return new Date(file.lastModified());
    }
    
    public long getFileSize() {
        return file.length();
    }
    
    
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        return null;
    }

    // purposely not creating a getter method, to prevent the file object from being unintentionally accessed via form parameters
    public File retrieveFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
}
