/*
 * Copyright 2006-2009 The Kuali Foundation
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
/*
 * Created on Feb 16, 2006
 *
 */
package org.kuali.ole.module.purap.util.cxml;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CxmlExtrinsic {

    private String name;
    private String value;

    public CxmlExtrinsic() {
    }

    public CxmlExtrinsic(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        ToStringBuilder toString = new ToStringBuilder(this);
        toString.append("Name", getName());
        toString.append("Value", getValue());
        return toString.toString();
    }
}
