/*
 * Copyright 2011 The Regents of the University of California.
 */
package org.kuali.ole.sys.businessobject;

import org.kuali.rice.krad.bo.PersistableBusinessObject;

public interface FiscalYearBasedBusinessObject extends PersistableBusinessObject {

    Integer getUniversityFiscalYear();
    void setUniversityFiscalYear( Integer fiscalYear );
}
