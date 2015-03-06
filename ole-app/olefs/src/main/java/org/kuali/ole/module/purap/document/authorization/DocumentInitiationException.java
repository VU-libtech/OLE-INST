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
package org.kuali.ole.module.purap.document.authorization;

import org.kuali.ole.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.exception.KualiException;

import java.text.MessageFormat;

public class DocumentInitiationException extends KualiException {
    public DocumentInitiationException(String messageKey, Object[] messageParameters) {
        super(MessageFormat.format(getConfigurationService().getPropertyValueAsString(messageKey), messageParameters));
    }

    public DocumentInitiationException(String messageKey, Object[] messageParameters, boolean hideIncidentReport) {
        super(MessageFormat.format(getConfigurationService().getPropertyValueAsString(messageKey), messageParameters), hideIncidentReport);
    }

    private static ConfigurationService configurationService;

    private static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }
}
