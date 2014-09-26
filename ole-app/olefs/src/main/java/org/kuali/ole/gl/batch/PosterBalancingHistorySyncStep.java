/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.ole.gl.batch;

import org.kuali.ole.gl.batch.service.BalancingService;
import org.kuali.ole.sys.batch.AbstractWrappedBatchStep;
import org.kuali.ole.sys.batch.service.WrappedBatchExecutorService.CustomBatchExecutor;

/**
 * Clears the BalanceHistory data.
 */
public class PosterBalancingHistorySyncStep extends AbstractWrappedBatchStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PosterBalancingHistorySyncStep.class);
    
    private BalancingService balancingService;
    
    /**
     * @see org.kuali.ole.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
    @Override
    protected CustomBatchExecutor getCustomBatchExecutor() {
        return new CustomBatchExecutor() {
            public boolean execute() {
                balancingService.clearHistories();
                return true;
            }
        };
    }

    /**
     * Sets the BalancingService
     * 
     * @param balancingService The BalancingService to set.
     */
    public void setBalancingService(BalancingService balancingService) {
        this.balancingService = balancingService;
    }
}
