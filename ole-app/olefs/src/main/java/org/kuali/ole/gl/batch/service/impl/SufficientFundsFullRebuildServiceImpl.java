/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.ole.gl.batch.service.impl;

import org.kuali.ole.gl.batch.service.SufficientFundsFullRebuildService;
import org.kuali.ole.gl.dataaccess.SufficientFundRebuildDao;
import org.springframework.transaction.annotation.Transactional;

/**
 * The base implementation of SufficientFundsFullRebuildService
 */
@Transactional
public class SufficientFundsFullRebuildServiceImpl implements SufficientFundsFullRebuildService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SufficientFundsFullRebuildServiceImpl.class);

    private SufficientFundRebuildDao sufficientFundRebuildDao;

    /**
     * Goes through all accounts in the database, and generates a sufficient fund rebuild record for each one!
     * @see org.kuali.ole.gl.batch.service.SufficientFundsFullRebuildService#syncSufficientFunds()
     */
    public void syncSufficientFunds() {
        LOG.debug("syncSufficientFunds() started");

        sufficientFundRebuildDao.purgeSufficientFundRebuild();
        
        sufficientFundRebuildDao.populateSufficientFundRebuild();
        
    }

    public void setSufficientFundRebuildDao(SufficientFundRebuildDao sfd) {
        sufficientFundRebuildDao = sfd;
    }

}
