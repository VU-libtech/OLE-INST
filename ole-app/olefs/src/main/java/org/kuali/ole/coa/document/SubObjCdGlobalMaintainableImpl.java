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
package org.kuali.ole.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.ole.coa.businessobject.AccountGlobalDetail;
import org.kuali.ole.coa.businessobject.SubObjectCode;
import org.kuali.ole.coa.businessobject.SubObjectCodeGlobal;
import org.kuali.ole.coa.businessobject.SubObjectCodeGlobalDetail;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.document.FinancialSystemGlobalMaintainable;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.maintenance.MaintenanceLock;

/**
 * This class provides some specific functionality for the {@link SubObjCdGlobal} maintenance document generateMaintenanceLocks -
 * generates maintenance locks on {@link SubObjCd}
 */
public class SubObjCdGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

    /**
     * This generates maintenance locks on {@link SubObjCd}
     * 
     * @see org.kuali.rice.kns.maintenance.Maintainable#generateMaintenanceLocks()
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        // create locking rep for each combination of account and object code
        List<MaintenanceLock> maintenanceLocks = new ArrayList();
        SubObjectCodeGlobal subObjCdGlobal = (SubObjectCodeGlobal) getBusinessObject();

        for (AccountGlobalDetail accountGlobalDetail : subObjCdGlobal.getAccountGlobalDetails()) {
            for (SubObjectCodeGlobalDetail subObjCdGlobalDetail : subObjCdGlobal.getSubObjCdGlobalDetails()) {
                MaintenanceLock maintenanceLock = new MaintenanceLock();
                maintenanceLock.setDocumentNumber(subObjCdGlobal.getDocumentNumber());

                StringBuffer lockrep = new StringBuffer();
                lockrep.append(SubObjectCode.class.getName() + OLEConstants.Maintenance.AFTER_CLASS_DELIM);
                lockrep.append("fiscalYear" + OLEConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockrep.append(subObjCdGlobalDetail.getUniversityFiscalYear() + OLEConstants.Maintenance.AFTER_VALUE_DELIM);
                lockrep.append("chartOfAccountsCode" + OLEConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockrep.append(accountGlobalDetail.getChartOfAccountsCode() + OLEConstants.Maintenance.AFTER_VALUE_DELIM);
                lockrep.append("accountNumber" + OLEConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockrep.append(accountGlobalDetail.getAccountNumber() + OLEConstants.Maintenance.AFTER_VALUE_DELIM);
                lockrep.append("financialObjectCode" + OLEConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockrep.append(subObjCdGlobalDetail.getFinancialObjectCode() + OLEConstants.Maintenance.AFTER_VALUE_DELIM);
                lockrep.append("financialSubObjectCode" + OLEConstants.Maintenance.AFTER_FIELDNAME_DELIM);
                lockrep.append(subObjCdGlobal.getFinancialSubObjectCode());

                maintenanceLock.setLockingRepresentation(lockrep.toString());
                maintenanceLocks.add(maintenanceLock);
            }
        }
        return maintenanceLocks;
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return SubObjectCode.class;
    }
}
