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
package org.kuali.ole.sys.service;

import java.util.List;

/**
 * This interface defines methods that an AccountingLine service implementation must provide.
 */
public interface AccountingLineService {
    /**
     * Retrieves a list of accounting lines for a given group (i.e. Target/Source) given the associated document id.
     * 
     * @param clazz
     * @param documentHeaderId
     * @return A list of AccountingLines... to be casted to the appropriate class.
     * @throws Exception
     */
    public List getByDocumentHeaderId(Class clazz, String documentHeaderId);

}
