/*
 * Copyright 2005-2006 The Kuali Foundation
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
package org.kuali.ole.sys.dataaccess;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.kuali.ole.KualiTestBase;
import org.kuali.ole.sys.businessobject.UniversityDate;
import org.kuali.ole.sys.context.SpringContext;
import org.kuali.ole.sys.dataaccess.UniversityDateDao;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUniversityDateDao extends KualiTestBase {

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Test
    public void testGetFirstLastFiscalYearDates() throws Exception {
        UniversityDateDao dao = SpringContext.getBean(UniversityDateDao.class);
        assertNotNull("Dao shouldn't be null", dao);

        UniversityDate firstFiscalYearDate = dao.getFirstFiscalYearDate(new Integer(2007));
        assertEquals("07/01/2006", sdf.format(firstFiscalYearDate.getUniversityDate()));

        UniversityDate lastFiscalYearDate = dao.getLastFiscalYearDate(new Integer(2007));
        assertEquals("06/30/2007", sdf.format(lastFiscalYearDate.getUniversityDate()));
    }
}
