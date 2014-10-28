/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.vnd.batch;

import java.util.Date;

import org.kuali.ole.sys.batch.AbstractStep;
import org.kuali.ole.vnd.batch.service.VendorExcludeService;

public class VendorMatchStep extends AbstractStep {
    VendorExcludeService vendorExcludeService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return vendorExcludeService.matchVendors();
    }
    
    public void setVendorExcludeService(VendorExcludeService vendorExcludeService) {
        this.vendorExcludeService = vendorExcludeService;
    }

}
