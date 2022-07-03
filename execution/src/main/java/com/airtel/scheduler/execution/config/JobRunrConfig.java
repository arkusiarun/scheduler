package com.airtel.scheduler.execution.config;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.properties.JobConfiguration;
import com.mongodb.client.MongoClient;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.jobs.AbstractJob;
import org.jobrunr.jobs.filters.DisplayNameFilter;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider;
import org.jobrunr.utils.mapper.gson.GsonJsonMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author Arun Singh
 */

@Configuration
public class JobRunrConfig {

    @Bean
    @Primary
    @Qualifier("mongoStorageProvider")
    public StorageProvider storageProvider(@Qualifier("primaryMongoClient") MongoClient mongoClient, MongoDBConfig mongoDBConfig) {
        return new MongoDBStorageProvider(mongoClient, mongoDBConfig.getDatabaseName());
    }

    @Bean
    public JobActivator jobActivator(ApplicationContext applicationContext) {
        return applicationContext::getBean;
    }

    @Bean
    public BackgroundJobServerConfiguration backgroundJobServerConfiguration(JobConfiguration jobConfiguration) {
        BackgroundJobServerConfiguration backgroundJobServerConfiguration = BackgroundJobServerConfiguration.usingStandardBackgroundJobServerConfiguration();
        backgroundJobServerConfiguration.andPollIntervalInSeconds(jobConfiguration.getPollInterval());
        backgroundJobServerConfiguration.andDeleteSucceededJobsAfter(Duration.ofHours(jobConfiguration.getSuccessJobDuration()));
        backgroundJobServerConfiguration.andPermanentlyDeleteDeletedJobsAfter(Duration.ofHours(jobConfiguration.getJobDuration()));
        return backgroundJobServerConfiguration;
    }

    @Bean("jobSchedulerBean")
    public JobScheduler initJobScheduler(@Qualifier("mongoStorageProvider") StorageProvider storageProvider, JobConfiguration jobConfiguration, BackgroundJobServerConfiguration backgroundJobServerConfiguration) {
        return JobRunr.configure()
                .useStorageProvider(storageProvider)
                .withJobFilter(new DisplayNameFilter() {
                    @Override
                    public void onCreating(AbstractJob job) {
                        if (!CollectionUtils.isEmpty(job.getJobDetails().getJobParameters())) {
                            Object jobObject = job.getJobDetails().getJobParameters().stream().iterator().next().getObject();
                            String jobName = null;
                            if (jobObject instanceof JobData) {
                                jobName = ((JobData) jobObject).getJobName();
                            }
                            if (jobObject instanceof Task) {
                                jobName = ((Task) jobObject).getEventType();
                            }
                            job.setJobName(jobName);
                        }
                    }
                })
                .useDashboard(jobConfiguration.getJobRunrDashBoardPort())
                .useBackgroundJobServer(backgroundJobServerConfiguration, Boolean.FALSE)
                .initialize().getJobScheduler();
    }

    @Bean
    public BackgroundJobServer backgroundJobServer(@Qualifier("mongoStorageProvider") StorageProvider storageProvider, JobActivator jobActivator, BackgroundJobServerConfiguration backgroundJobServerConfiguration, JobConfiguration jobConfiguration) {
        CustomBackgroundServer backgroundJobServer = new CustomBackgroundServer(storageProvider, new GsonJsonMapper(), jobActivator, backgroundJobServerConfiguration, jobConfiguration);
        backgroundJobServer.setJobFilters(Arrays.asList(new RetryFilter(jobConfiguration.getDefaultRetryCount())));
        backgroundJobServer.start(backgroundJobServer);
        return backgroundJobServer;
    }
}