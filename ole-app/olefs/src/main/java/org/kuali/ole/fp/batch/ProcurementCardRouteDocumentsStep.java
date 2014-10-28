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
package org.kuali.ole.fp.batch;

import java.util.Date;

import org.kuali.ole.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.ole.sys.batch.AbstractStep;

/**
 * This step will call a service method to route pcdo documents that are in 'I' status.
 */
public class ProcurementCardRouteDocumentsStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardRouteDocumentsStep.class);
    private ProcurementCardCreateDocumentService procurementCardDocumentService;

    /**
     * @see org.kuali.ole.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {
        // TODO: put a temporary delay in here to workaround locking exception happening with Pcard approve and indexing
        try {
            Thread.sleep(300000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return procurementCardDocumentService.routeProcurementCardDocuments();
    }

    /**
     * @param procurementCardDocumentService The procurementCardDocumentService to set.
     */
    public void setProcurementCardCreateDocumentService(ProcurementCardCreateDocumentService procurementCardDocumentService) {
        this.procurementCardDocumentService = procurementCardDocumentService;
    }
}
