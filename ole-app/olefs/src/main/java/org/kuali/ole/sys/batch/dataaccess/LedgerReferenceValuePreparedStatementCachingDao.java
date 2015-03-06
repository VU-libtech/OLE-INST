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
package org.kuali.ole.sys.batch.dataaccess;

import java.sql.Date;

import org.kuali.ole.sys.businessobject.OriginationCode;
import org.kuali.ole.sys.businessobject.SystemOptions;
import org.kuali.ole.sys.businessobject.UniversityDate;

public interface LedgerReferenceValuePreparedStatementCachingDao extends PreparedStatementCachingDao {
    public UniversityDate getUniversityDate(Date date);

    public OriginationCode getOriginationCode(String financialSystemOriginationCode);

    public SystemOptions getSystemOptions(Integer fiscalYear);
}
