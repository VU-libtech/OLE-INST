/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.ole.module.purap.batch;

import org.apache.log4j.Logger;
import org.kuali.ole.module.purap.businessobject.ElectronicInvoiceLoad;
import org.kuali.ole.module.purap.service.ElectronicInvoiceHelperService;
import org.kuali.ole.sys.batch.AbstractStep;

import java.util.Date;
import java.util.List;

public class ElectronicInvoiceStep extends AbstractStep {

    private static Logger LOG = Logger.getLogger(ElectronicInvoiceStep.class);

    private ElectronicInvoiceHelperService electronicInvoiceHelperService;

    public boolean execute(String jobName, Date jobRunDate) {
        ElectronicInvoiceLoad load = electronicInvoiceHelperService.loadElectronicInvoices();
        return load.containsRejects();
    }

    public void setElectronicInvoiceHelperService(ElectronicInvoiceHelperService electronicInvoiceHelperService) {
        this.electronicInvoiceHelperService = electronicInvoiceHelperService;
    }

    /**
     * Override the default implementation to pull the directory path list from the service
     *
     * @see org.kuali.ole.sys.batch.AbstractStep#getRequiredDirectoryNames()
     */
    @Override
    public List<String> getRequiredDirectoryNames() {
        return electronicInvoiceHelperService.getRequiredDirectoryNames();
    }
}
