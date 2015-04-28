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
package org.kuali.ole.sys.context;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.kuali.ole.batch.service.OLEBatchSchedulerService;
import org.kuali.ole.sys.MemoryMonitor;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.batch.Step;
import org.kuali.ole.sys.batch.service.SchedulerService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.framework.resourceloader.SpringResourceLoader;
import org.kuali.rice.coreservice.api.CoreServiceApiServiceLocator;
import org.kuali.rice.coreservice.api.component.Component;
import org.kuali.rice.krad.service.*;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;

@SuppressWarnings("deprecation")
public class SpringContext {
    private static final Logger LOG = Logger.getLogger(SpringContext.class);
    protected static final String MEMORY_MONITOR_THRESHOLD_KEY = "memory.monitor.threshold";
    protected static final String USE_QUARTZ_SCHEDULING_KEY = "use.quartz.scheduling";
    protected static final String OLE_BATCH_STEP_COMPONENT_SET_ID = "STEP:OLE";
    protected static ConfigurableApplicationContext applicationContext;
    protected static Set<Class<? extends Object>> SINGLETON_TYPES = new HashSet<Class<? extends Object>>();
    protected static Map<Class<? extends Object>, Object> SINGLETON_BEANS_BY_TYPE_CACHE = new HashMap<Class<? extends Object>, Object>();
    protected static Map<String, Object> SINGLETON_BEANS_BY_NAME_CACHE = new HashMap<String, Object>();
    @SuppressWarnings("rawtypes")
    protected static Map<Class<? extends Object>, Map> SINGLETON_BEANS_OF_TYPE_CACHE = new HashMap<Class<? extends Object>, Map>();
    protected static Thread processWatchThread = null;
    protected static MemoryMonitor memoryMonitor;
    /**
     * Use this method to retrieve a service which may or may not be implemented locally. (That is, defined in the main Spring
     * ApplicationContext created by Rice.
     */
    public static Object getService(String serviceName) {
        return GlobalResourceLoader.getService(serviceName);
    }

    /**
     * Use this method to retrieve a spring bean when one of the following is the case. Pass in the type of the service interface,
     * NOT the service implementation. 1. there is only one bean of the specified type in our spring context 2. there is only one
     * bean of the specified type in our spring context, but you want the one whose bean id is the same as type.getSimpleName() with
     * the exception of the first letter being lower case in the former and upper case in the latter, For example, there are two
     * beans of type DateTimeService in our context dateTimeService and testDateTimeService. To retrieve the former, you should
     * specific DateTimeService.class as the type. To retrieve the latter, you should specify ConfigurableDateService.class as the
     * type. Unless you are writing a unit test and need to down cast to an implementation, you do not need to cast the result of
     * this method.
     *
     * @param <T>
     * @param type
     * @return an object that has been defined as a bean in our spring context and is of the specified type
     */
    public static <T> T getBean(Class<T> type) {
        verifyProperInitialization();
        T bean = null;
        if (SINGLETON_BEANS_BY_TYPE_CACHE.containsKey(type)) {
            bean = (T) SINGLETON_BEANS_BY_TYPE_CACHE.get(type);
        }
        else {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("Bean not already in cache: " + type + " - calling getBeansOfType() ");
            }
            Collection<T> beansOfType = getBeansOfType(type).values();
            if ( !beansOfType.isEmpty() ) {
                if (beansOfType.size() > 1) {
                    bean = getBean(type, StringUtils.uncapitalize(type.getSimpleName()) );
                }
                else {
                    bean = beansOfType.iterator().next();
                }
            }
            else {
                try {
                    bean = getBean(type, StringUtils.uncapitalize(type.getSimpleName()) );
                }
                catch (Exception ex) {
                    // do nothing, let fall through
                }
                if ( bean == null ) { // unable to find bean - check GRL
                    // this is needed in case no beans of the given type exist locally
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug("Bean not found in local context: " + type.getName() + " - calling GRL");
                    }
                    Object remoteServiceBean = getService( StringUtils.uncapitalize(type.getSimpleName()) );
                    if ( remoteServiceBean != null ) {
                        if ( type.isAssignableFrom( remoteServiceBean.getClass() ) ) {
                            bean = (T)remoteServiceBean;
                        }
                    }
                }
            }
            if ( bean != null ) {
                synchronized( SINGLETON_TYPES ) {
                    if (SINGLETON_TYPES.contains(type) || hasSingletonSuperType(type,SINGLETON_TYPES)) {
                        SINGLETON_TYPES.add(type);
                        SINGLETON_BEANS_BY_TYPE_CACHE.put(type, bean);
                    }
                }
            }
            else {
                throw new RuntimeException( "Request for non-existent bean.  Unable to find in local context or on the GRL: " + type.getName() );
            }
        }
        return bean;
    }

    /**
     * Use this method to retrieve all beans of a give type in our spring context. Pass in the type of the service interface, NOT
     * the service implementation.
     *
     * @param <T>
     * @param type
     * @return a map of the spring bean ids / beans that are of the specified type
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        verifyProperInitialization();
        Map<String, T> beansOfType = null;
        if (SINGLETON_BEANS_OF_TYPE_CACHE.containsKey(type)) {
            beansOfType = SINGLETON_BEANS_OF_TYPE_CACHE.get(type);
        }
        else {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("Bean not already in \"OF_TYPE\" cache: " + type + " - calling getBeansOfType() on Spring context");
            }
            boolean allOfTypeAreSingleton = true;
            beansOfType = applicationContext.getBeansOfType(type);
            for ( String key : beansOfType.keySet() ) {
                if ( !applicationContext.isSingleton(key) ) {
                    allOfTypeAreSingleton = false;
                }
            }
            if ( allOfTypeAreSingleton ) {
                synchronized( SINGLETON_TYPES ) {
                    SINGLETON_TYPES.add(type);
                    SINGLETON_BEANS_OF_TYPE_CACHE.put(type, beansOfType);
                }
            }
        }
        return beansOfType;
    }

    public static <T> T getBean(Class<T> type, String name) {
        T bean = null;
        if (SINGLETON_BEANS_BY_NAME_CACHE.containsKey(name)) {
            bean = (T) SINGLETON_BEANS_BY_NAME_CACHE.get(name);
        }
        else {
            try {
                bean = (T) applicationContext.getBean(name);
                if ( applicationContext.isSingleton(name) ) {
                    synchronized( SINGLETON_BEANS_BY_NAME_CACHE ) {
                        SINGLETON_BEANS_BY_NAME_CACHE.put(name, bean);
                    }
                }
            }
            catch (NoSuchBeanDefinitionException nsbde) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug("Bean with name and type not found in local context: " + name + "/" + type.getName() + " - calling GRL");
                }
                Object remoteServiceBean = getService( name );
                if ( remoteServiceBean != null ) {
                    if ( type.isAssignableFrom( AopUtils.getTargetClass(remoteServiceBean) ) ) {
                        bean = (T)remoteServiceBean;
                        // assume remote beans are services and thus singletons
                        synchronized( SINGLETON_BEANS_BY_NAME_CACHE ) {
                            SINGLETON_BEANS_BY_NAME_CACHE.put(name, bean);
                        }
                    }
                }
                if (bean == null) {
                throw new RuntimeException("No bean of this type and name exist in the application context or from the GRL: " + type.getName() + ", " + name);
            }
        }
        }
        return bean;
    }

    private static boolean hasSingletonSuperType(Class<? extends Object> type, Set<Class<? extends Object>> knownSingletonTypes ) {
        for (Class<? extends Object> singletonType : knownSingletonTypes) {
            if (singletonType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    public static Object getBean(String beanName) {
        return getBean(Object.class, beanName);
    }

    protected static String[] getBeanNames() {
        verifyProperInitialization();
        return applicationContext.getBeanDefinitionNames();
    }

    protected static void close() {
        if ( processWatchThread != null ) {
            LOG.info("Shutting down the ProcessWatch thread" );
            if ( processWatchThread.isAlive() ) {
                processWatchThread.stop();
            }
            processWatchThread = null;
        }

        try {
            if ( isInitialized() && getBean(Scheduler.class) != null ) {
                if ( getBean(Scheduler.class).isStarted() ) {
                    LOG.info( "Shutting Down scheduler" );
                    getBean(Scheduler.class).shutdown();
        }
        }
        } catch (SchedulerException ex) {
            LOG.error( "Exception while shutting down the scheduler", ex );
        }
        LOG.info( "Stopping the MemoryMonitor thread" );
        if ( memoryMonitor != null ) {
            memoryMonitor.stop();
        }
    }

    public static boolean isInitialized() {
        return applicationContext != null;
    }

    private static void verifyProperInitialization() {
        if (!isInitialized()) {
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "Spring not initialized properly.  Initialization has begun and the application context is null.  Probably spring loaded bean is trying to use SpringContext.getBean() before the application context is initialized.", new IllegalStateException() );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            LOG.fatal( "*****************************************************************" );
            throw new IllegalStateException("Spring not initialized properly.  Initialization has begun and the application context is null.  Probably spring loaded bean is trying to use SpringContext.getBean() before the application context is initialized.");
        }
    }

    static void initMemoryMonitor() {
        ConfigurationService configurationService = GlobalResourceLoader.getService("kualiConfigurationService");
        if ( NumberUtils.isNumber(configurationService.getPropertyValueAsString(MEMORY_MONITOR_THRESHOLD_KEY))) {
            if (Double.valueOf(configurationService.getPropertyValueAsString(MEMORY_MONITOR_THRESHOLD_KEY)) > 0) {
                LOG.info( "Starting up MemoryMonitor thread" );
                MemoryMonitor.setPercentageUsageThreshold(Double.valueOf(configurationService.getPropertyValueAsString(MEMORY_MONITOR_THRESHOLD_KEY)));
                memoryMonitor = new MemoryMonitor("OLE Memory Monitor: Over " + configurationService.getPropertyValueAsString(MEMORY_MONITOR_THRESHOLD_KEY) + "% Memory Used");
            memoryMonitor.addListener(new MemoryMonitor.Listener() {
                org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MemoryMonitor.class);

                    @Override
                public void memoryUsageLow(String springContextId, Map<String, String> memoryUsageStatistics, String deadlockedThreadIds) {
                        StringBuilder logStatement = new StringBuilder(springContextId).append("\n\tMemory Usage");
                        for (String memoryType : memoryUsageStatistics.keySet()) {
                            logStatement.append("\n\t\t").append(memoryType.toUpperCase()).append(": ").append(memoryUsageStatistics.get(memoryType));
                        }
                        logStatement.append("\n\tLocked Thread Ids: ").append(deadlockedThreadIds).append("\n\tThread Stacks");
                        for (Map.Entry<Thread, StackTraceElement[]> threadStackTrace : Thread.getAllStackTraces().entrySet()) {
                            logStatement.append("\n\t\tThread: name=").append(threadStackTrace.getKey().getName()).append(", id=").append(threadStackTrace.getKey().getId()).append(", priority=").append(threadStackTrace.getKey().getPriority()).append(", state=").append(threadStackTrace.getKey().getState());
                            for (StackTraceElement stackTraceElement : threadStackTrace.getValue()) {
                                logStatement.append("\n\t\t\t").append(stackTraceElement);
                            }
                        }
                        LOG.warn(logStatement);
                    MemoryMonitor.setPercentageUsageThreshold(0.95);
                }
            });
        }
        } else {
            LOG.warn( MEMORY_MONITOR_THRESHOLD_KEY + " is not a number: " + configurationService.getPropertyValueAsString(MEMORY_MONITOR_THRESHOLD_KEY) );
            }
        }

    static void initMonitoringThread() {
        ConfigurationService configurationService = GlobalResourceLoader.getService("kualiConfigurationService");
        if ( configurationService.getPropertyValueAsBoolean("periodic.thread.dump") ) {
            final long sleepPeriod = Long.parseLong(configurationService.getPropertyValueAsString("periodic.thread.dump.seconds") ) * 1000;
            final File logDir = new File(configurationService.getPropertyValueAsString("logs.directory") );
            final File monitoringLogDir = new File( logDir, "monitoring" );
            if ( !monitoringLogDir.exists() ) {
                monitoringLogDir.mkdir();
            }
            if ( LOG.isInfoEnabled() ) {
                LOG.info( "Starting the Periodic Thread Dump thread - dumping every " + (sleepPeriod/1000) + " seconds");
                LOG.info( "Periodic Thread Dump Logs: " + monitoringLogDir.getAbsolutePath() );
            }
            final DateFormat df = new SimpleDateFormat( "yyyyMMdd" );
            final DateFormat tf = new SimpleDateFormat( "HH-mm-ss" );
            Runnable processWatch = new Runnable() {
                @Override
                public void run() {
                    while ( true ) {
                        Date now = new Date();
                        File todaysLogDir = new File( monitoringLogDir, df.format(now) );
                        if ( !todaysLogDir.exists() ) {
                            todaysLogDir.mkdir();
                        }
                        File logFile = new File( todaysLogDir, "process-"+tf.format(now)+".log" );
                        try {
                            StringBuilder logStatement = new StringBuilder(10240);
                            logStatement.append("Threads Running at: " ).append( now ).append( "\n\n\n" );
                            Map<Thread,StackTraceElement[]> threads = Thread.getAllStackTraces();
                            List<Thread> sortedThreads = new ArrayList<Thread>( threads.keySet() );
                            Collections.sort( sortedThreads, new Comparator<Thread>() {
                                @Override
                                public int compare(Thread o1, Thread o2) {
                                    return o1.getName().compareTo( o2.getName() );
                                }
                            });
                            for ( Thread t : sortedThreads ) {
                                logStatement.append("\tThread: name=").append(t.getName()).append(", id=").append(t.getId()).append(", priority=").append(t.getPriority()).append(", state=").append(t.getState());
                                logStatement.append('\n');
                                for (StackTraceElement stackTraceElement : threads.get(t) ) {
                                    logStatement.append("\t\t" + stackTraceElement).append( '\n' );
                                }
                                logStatement.append('\n');
                            }
                            FileUtils.writeStringToFile(logFile, logStatement.toString(), "UTF-8");
                        }
                        catch (IOException ex) {
                            LOG.error( "Unable to write the ProcessWatch output file: " + logFile.getAbsolutePath(), ex );
                        }
                        try {
                            Thread.sleep( sleepPeriod );
                        }
                        catch (InterruptedException ex) {
                            LOG.error( "woken up during sleep of the ProcessWatch thread", ex );
                        }
                    }
                }
            };
            processWatchThread = new Thread( processWatch, "ProcessWatch thread" );
            processWatchThread.setDaemon(true);
            processWatchThread.start();
        }
    }

    static void initScheduler() {
        ConfigurationService configurationService = GlobalResourceLoader.getService("kualiConfigurationService");
        if (configurationService.getPropertyValueAsBoolean(USE_QUARTZ_SCHEDULING_KEY)) {
            try {
                LOG.info("Attempting to initialize the SchedulerService");
                getBean(SchedulerService.class).initialize();
                getBean(OLEBatchSchedulerService.class).initialize();
                LOG.info("Starting the Quartz scheduler");
                getBean(Scheduler.class).start();
            } catch (NoSuchBeanDefinitionException e) {
                LOG.warn("Not initializing the scheduler because there is no scheduler bean");
            } catch ( Exception ex ) {
                LOG.error("Caught Exception while starting the scheduler", ex);
            }
        }
    }

    public static void registerSingletonBean(String beanId, Object bean) {
        applicationContext.getBeanFactory().registerSingleton(beanId, bean);
        //Cleaning caches
        SINGLETON_BEANS_BY_NAME_CACHE.clear();
        SINGLETON_BEANS_BY_TYPE_CACHE.clear();
        SINGLETON_BEANS_OF_TYPE_CACHE.clear();
    }

    public static void finishInitializationAfterRiceStartup() {
        if ( LOG.isTraceEnabled() ) {
            GlobalResourceLoader.logAllContents();
        }
        DataDictionaryComponentPublisherService dataDictionaryComponentPublisherService= GlobalResourceLoader.getService("dataDictionaryComponentPublisherService");
        SpringResourceLoader mainKfsSpringResourceLoader = (SpringResourceLoader)GlobalResourceLoader.getResourceLoader( new QName("OLE", "OLE_RICE_SPRING_RESOURCE_LOADER_NAME") );
        SpringContext.applicationContext = mainKfsSpringResourceLoader.getContext();

        // KFS addition - republish all components now - until this point, the KFS DD has not been loaded
        dataDictionaryComponentPublisherService.publishAllComponents();

        // KFS addition - we also publish all our Step classes as components - and these are not in the
        // DD so are not published by the command above
        publishBatchStepComponents();
    }

    public static void publishBatchStepComponents() {
        Map<String,Step> steps = SpringContext.getBeansOfType(Step.class);
        List<Component> stepComponents = new ArrayList<Component>( steps.size() );
        for ( Step step : steps.values() ) {
            Step unproxiedStep = (Step) ProxyUtils.getTargetIfProxied(step);
            String namespaceCode = OLEConstants.CoreModuleNamespaces.OLE;
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Building component for step: " + unproxiedStep.getName() + "(" + unproxiedStep.getClass() + ")" );
            }
            ModuleService moduleService = SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService(unproxiedStep.getClass());
            if ( moduleService != null ) {
                namespaceCode = moduleService.getModuleConfiguration().getNamespaceCode();
            }
            Component.Builder component = Component.Builder.create(namespaceCode, unproxiedStep.getClass().getSimpleName(), unproxiedStep.getClass().getSimpleName());
            component.setComponentSetId(OLE_BATCH_STEP_COMPONENT_SET_ID);
            component.setActive(true);
            stepComponents.add(component.build());
        }

        CoreServiceApiServiceLocator.getComponentService().publishDerivedComponents(OLE_BATCH_STEP_COMPONENT_SET_ID, stepComponents);
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        SpringContext.applicationContext = applicationContext;
    }
}
