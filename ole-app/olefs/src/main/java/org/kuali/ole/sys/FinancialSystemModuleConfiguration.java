/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.ole.sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kuali.ole.sys.batch.dataaccess.FiscalYearMaker;
import org.kuali.rice.krad.bo.ModuleConfiguration;

/**
 * Slim subclass to enforce class hierarchy not enforced by the parent class' contract.
 */
public class FinancialSystemModuleConfiguration extends ModuleConfiguration {
    protected List<FiscalYearMaker> fiscalYearMakers;
    protected List<String> batchFileDirectories;
    
    /**
     * Constructs a FinancialSystemModuleConfiguration.java.
     */
    public FinancialSystemModuleConfiguration() {
        super();
        
        fiscalYearMakers = new ArrayList<FiscalYearMaker>();
        batchFileDirectories = new ArrayList<String>();
    }

    /**
     * Gets the fiscalYearMakers attribute.
     * 
     * @return Returns the fiscalYearMakers.
     */
    public List<FiscalYearMaker> getFiscalYearMakers() {
        return fiscalYearMakers;
    }

    /**
     * Sets the fiscalYearMakers attribute value.
     * 
     * @param fiscalYearMakers The fiscalYearMakers to set.
     */
    public void setFiscalYearMakers(List<FiscalYearMaker> fiscalYearMakers) {
        this.fiscalYearMakers = fiscalYearMakers;
    }
    
    public List<String> getBatchFileDirectories() {
        return batchFileDirectories;
    }
    
    public void setBatchFileDirectories(List<String> batchFileDirectories) {
        if (batchFileDirectories == null) {
            this.batchFileDirectories = new ArrayList<String>();
        } else {
            this.batchFileDirectories = batchFileDirectories;
            for (String batchFileDirectory : this.batchFileDirectories) {
                File directory = new File(batchFileDirectory);
                if ( !directory.exists() ) {
                    if ( !directory.mkdirs() ) {
                        throw new RuntimeException( batchFileDirectory + " does not exist and the server was unable to create it." );
                    }
                } else {
                    if (!directory.isDirectory()) {
                        throw new RuntimeException(batchFileDirectory + " exists but is not a directory.");
                    }
                }
            }
        }
    }
}
