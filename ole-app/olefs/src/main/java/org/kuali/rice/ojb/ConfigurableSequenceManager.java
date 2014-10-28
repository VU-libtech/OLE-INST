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
package org.kuali.rice.ojb;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.log4j.Logger;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.JdbcAccess;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.util.sequence.SequenceManager;
import org.apache.ojb.broker.util.sequence.SequenceManagerException;
import org.kuali.rice.core.api.config.ConfigurationException;
import org.kuali.rice.core.api.config.property.ConfigContext;


/**
 * Overriding corresponding rice implementation that doesn't work for OLE.
 */
public class ConfigurableSequenceManager implements SequenceManager {
    private static final Logger LOG = Logger.getLogger(ConfigurableSequenceManager.class);
    private static final String SEQUENCE_MANAGER_CLASS_NAME_PROPERTY = "datasource.ojb.sequence.manager";
    private SequenceManager sequenceManager;

    public ConfigurableSequenceManager(PersistenceBroker broker) {
        this.sequenceManager = createSequenceManager(broker);
    }

    protected SequenceManager createSequenceManager(PersistenceBroker broker) {
        String sequenceManagerClassName = ConfigContext.getCurrentContextConfig().getProperty(SEQUENCE_MANAGER_CLASS_NAME_PROPERTY);
        try {
            Object sequenceManagerObject = ConstructorUtils.invokeConstructor(Class.forName(sequenceManagerClassName), broker);
            if (!(sequenceManagerObject instanceof SequenceManager)) {
                throw new ConfigurationException("The configured sequence manager ('" + sequenceManagerClassName + "') is not an instance of '" + SequenceManager.class.getName() + "'");
            }
            return (SequenceManager) sequenceManagerObject;
        }
        catch (Exception e) {
            String message = "Unable to configure SequenceManager specified by " + SEQUENCE_MANAGER_CLASS_NAME_PROPERTY + " KualiConfigurationService property";
            LOG.fatal(message, e);
            throw new RuntimeException(message, e);
        }
    }

    protected SequenceManager getConfiguredSequenceManager() {
        return this.sequenceManager;
    }

    @Override
    public void afterStore(JdbcAccess jdbcAccess, ClassDescriptor classDescriptor, Object object) throws SequenceManagerException {
        sequenceManager.afterStore(jdbcAccess, classDescriptor, object);
    }

    @Override
    public Object getUniqueValue(FieldDescriptor fieldDescriptor) throws SequenceManagerException {
        return sequenceManager.getUniqueValue(fieldDescriptor);
    }
}
