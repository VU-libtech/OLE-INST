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
package org.kuali.ole.sys.document.datadictionary;

import org.kuali.ole.sys.businessobject.AccountingLine;
import org.kuali.ole.sys.document.web.AccountingLineViewLineFillingElement;

/**
 * Contract for elements which can live within an AccountingLineViewLinesDefinition, as they promise they will fill at least one table row
 */
public interface AccountingLineViewLineFillingDefinition extends AccountingLineViewRenderableElementDefinition {
    /**
     * Has the definition create not just a layout element, but a line filling layout element
     * @param accountingLineClass the class of the accounting line being rendered
     * @return an AccountingLineViewLineFillingElement based off of this definition
     */
    public abstract AccountingLineViewLineFillingElement createLineFillingLayoutElement(Class<? extends AccountingLine> accountingLineClass);
}
