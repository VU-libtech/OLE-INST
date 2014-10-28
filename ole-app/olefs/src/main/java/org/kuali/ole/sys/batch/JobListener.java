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
package org.kuali.ole.sys.batch;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.kuali.ole.batch.service.OLEBatchSchedulerService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.OLEKeyConstants;
import org.kuali.ole.sys.batch.service.SchedulerService;
import org.kuali.ole.sys.context.NDCFilter;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.krad.service.MailService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobListener implements org.quartz.JobListener {
    private static final Logger LOG = Logger.getLogger(JobListener.class);
    protected static final String NAME = "jobListener";
    public static final String REQUESTOR_EMAIL_ADDRESS_KEY = "requestorEmailAdress";
    public static final String DEFAULT_LOG4J_PATTERN = "%d [%t] u:%X{user}/d:%X{docId} %-5p %c - %m%n";
    protected SchedulerService schedulerService;
    protected ConfigurationService configurationService;
    protected MailService mailService;
    protected DateTimeService dateTimeService;
    private static final String SUCCEEDED = "Succeeded";

    /**
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException jobExecutionException) {
        if (jobExecutionContext.getJobInstance() instanceof Job) {
            try {
                if (!((Job) jobExecutionContext.getJobInstance()).isNotRunnable()) {
                    notify(jobExecutionContext, schedulerService.getStatus(jobExecutionContext.getJobDetail()));
                    if (schedulerService.getStatus(jobExecutionContext.getJobDetail()).equalsIgnoreCase(SUCCEEDED)) {
                        if (schedulerService instanceof OLEBatchSchedulerService) {
                            ((OLEBatchSchedulerService) schedulerService).unScheduleOneTimeJob(jobExecutionContext.getJobDetail().getName(), jobExecutionContext.getJobDetail().getGroup());
                        }
                    }
                }
            }
            finally {
                completeLogging(jobExecutionContext);
            }
        }
    }

    /**
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        if (jobExecutionContext.getJobInstance() instanceof Job) {
            schedulerService.initializeJob(jobExecutionContext.getJobDetail().getName(), (Job) jobExecutionContext.getJobInstance());
            initializeLogging(jobExecutionContext);
            // We only want to auto-cancel executions if they are part of a master scheduling job
            // Otherwise, this is a standalone job and should fire, regardless of prior status
            if ( jobExecutionContext.getMergedJobDataMap().containsKey(Job.MASTER_JOB_NAME) ) {
                if (schedulerService.shouldNotRun(jobExecutionContext.getJobDetail())) {
                    ((Job) jobExecutionContext.getJobInstance()).setNotRunnable(true);
                }
            }
            else {
                ((Job) jobExecutionContext.getJobInstance()).setNotRunnable(false);
            }
        }
    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        if (jobExecutionContext.getJobInstance() instanceof Job) {
            throw new UnsupportedOperationException("JobListener does not implement jobExecutionVetoed(JobExecutionContext jobExecutionContext)");
        }
    }

    protected void initializeLogging(JobExecutionContext jobExecutionContext) {
        String jobName = jobExecutionContext.getJobDetail().getName();
        try {
            String nestedDiagnosticContext = getNestedDiagnosticContext(jobName);
            String logFilename = getLogFileName(nestedDiagnosticContext);
            System.out.println("Log filename=" + logFilename);
            Job job = (Job) jobExecutionContext.getJobInstance();
            Logger rootLogger = Logger.getRootLogger();
            Appender appender = rootLogger.getAppender("LogFile");
            Layout layout = getLayout(appender);
            Appender newAppender = new FileAppender(layout, logFilename);
            Filter newFilter = new NDCFilter(nestedDiagnosticContext);
            newAppender.addFilter(newFilter);
            job.setNdcAppender(newAppender);
            rootLogger.addAppender(newAppender);
            NDC.push(nestedDiagnosticContext);
        }
        catch (IOException e) {
            LOG.warn("Could not initialize special custom logging for job: " + jobName, e);
        }
    }

    protected Layout getLayout(Appender appender) {
        if (appender == null) {
            return new PatternLayout(DEFAULT_LOG4J_PATTERN);
        }
        else {
            return appender.getLayout();
        }
    }

    protected String getNestedDiagnosticContext(String jobName) {
        Date now = dateTimeService.getCurrentDate();
        String dateString = dateTimeService.toDateStringForFilename(now);
        JobDescriptor jobDescriptor = BatchSpringContext.getJobDescriptor(jobName);
        if(jobDescriptor==null){
            jobDescriptor = new JobDescriptor();
            jobDescriptor.setNamespaceCode("JOB-OLEBATCH");
        }
        String namespaceCode = jobDescriptor.getNamespaceCode();
        String namespaceCodeToken = StringUtils.substringAfter(namespaceCode, "-").toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append(namespaceCodeToken);
        sb.append(File.separator);
        sb.append(jobName);
        sb.append("-");
        sb.append(dateString);
        return sb.toString();
    }

    private void completeLogging(JobExecutionContext jobExecutionContext) {
        ((Job) jobExecutionContext.getJobInstance()).getNdcAppender().close();
        Logger.getRootLogger().removeAppender(((Job) jobExecutionContext.getJobInstance()).getNdcAppender());
        NDC.pop();
    }

    protected String getLogFileName(String nestedDiagnosticContext) {
        return new StringBuilder(configurationService.getPropertyValueAsString(OLEConstants.REPORTS_DIRECTORY_KEY)).append(File.separator).append(nestedDiagnosticContext.toString()).append(".log").toString();
    }

    protected void notify(JobExecutionContext jobExecutionContext, String jobStatus) {
        try {
            StringBuilder mailMessageSubject = new StringBuilder(jobExecutionContext.getJobDetail().getGroup()).append(": ").append(jobExecutionContext.getJobDetail().getName());
            MailMessage mailMessage = new MailMessage();
            mailMessage.setFromAddress(mailService.getBatchMailingList());
            if (jobExecutionContext.getMergedJobDataMap().containsKey(REQUESTOR_EMAIL_ADDRESS_KEY) && !StringUtils.isBlank(jobExecutionContext.getMergedJobDataMap().getString(REQUESTOR_EMAIL_ADDRESS_KEY))) {
                mailMessage.addToAddress(jobExecutionContext.getMergedJobDataMap().getString(REQUESTOR_EMAIL_ADDRESS_KEY));
            }
            if (SchedulerService.FAILED_JOB_STATUS_CODE.equals(jobStatus) || SchedulerService.CANCELLED_JOB_STATUS_CODE.equals(jobStatus)) {
                mailMessage.addToAddress(mailService.getBatchMailingList());
            }
            mailMessageSubject.append(": ").append(jobStatus);
            String messageText = MessageFormat.format(configurationService.getPropertyValueAsString(OLEKeyConstants.MESSAGE_BATCH_FILE_LOG_EMAIL_BODY), getLogFileName(NDC.peek()));
            mailMessage.setMessage(messageText);
            if (mailMessage.getToAddresses().size() > 0) {
                mailMessage.setSubject(mailMessageSubject.toString());
                mailService.sendMessage(mailMessage);
            }
        }
        catch (Exception iae) {
            LOG.error("Caught exception while trying to send job completion notification e-mail for " + jobExecutionContext.getJobDetail().getName(), iae);
        }
    }

    /**
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Sets the schedulerService attribute value.
     * 
     * @param schedulerService The schedulerService to set.
     */
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * Sets the configurationService attribute value.
     * 
     * @param configurationService The configurationService to set.
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Sets the mailService attribute value.
     * 
     * @param mailService The mailService to set.
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
