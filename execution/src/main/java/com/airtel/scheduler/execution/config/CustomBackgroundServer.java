package com.airtel.scheduler.execution.config;

import com.airtel.scheduler.execution.properties.JobConfiguration;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.server.BackgroundJobServer;
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.utils.mapper.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomBackgroundServer extends BackgroundJobServer {

    private final Logger logger = LoggerFactory.getLogger(CustomBackgroundServer.class);
    private final StorageProvider storageProvider;
    private final JobActivator jobActivator;
    private final BackgroundJobServerConfiguration jobServerConfiguration;
    private final JobConfiguration jobConfiguration;
    private final JsonMapper jsonMapper;

    public CustomBackgroundServer(StorageProvider storageProvider, JsonMapper jsonMapper, JobActivator jobActivator, BackgroundJobServerConfiguration jobServerConfiguration, JobConfiguration jobConfiguration) {
        super(storageProvider, jsonMapper, jobActivator, jobServerConfiguration);
        this.storageProvider = storageProvider;
        this.jobActivator = jobActivator;
        this.jobServerConfiguration = jobServerConfiguration;
        this.jobConfiguration = jobConfiguration;
        this.jsonMapper = jsonMapper;
    }

    public void start(BackgroundJobServer backgroundJobServer) {
        backgroundJobServer.start();
        logger.error("Removing timed Out BackGround Servers..");
        Instant instant = Instant.now().minus(this.jobConfiguration.getTimedOutServerDuration(), ChronoUnit.SECONDS);
        backgroundJobServer.getStorageProvider().removeTimedOutBackgroundJobServers(instant);
    }

    @Override
    public void stop() {
        super.stop();
        restartServer();
    }

    private void restartServer() {
        ScheduledExecutorService invoker = Executors.newSingleThreadScheduledExecutor();
        invoker.schedule(restartServer, this.jobConfiguration.getRestartBackgroundServerDuration(), TimeUnit.SECONDS);
        invoker.shutdown();
    }

    private BackgroundJobServer createNewBackgroundServer() {
        logger.error("Poller Stopped, Attempt to Restart Background Server in : {} seconds", this.jobConfiguration.getRestartBackgroundServerDuration());
        CustomBackgroundServer backgroundJobServer = new CustomBackgroundServer(storageProvider, jsonMapper, jobActivator, jobServerConfiguration, jobConfiguration);
        backgroundJobServer.setJobFilters(Arrays.asList(new RetryFilter(this.jobConfiguration.getDefaultRetryCount())));
        return backgroundJobServer;
    }

    Runnable restartServer = () -> {
        try {
            logger.info("Attempting to Restart Background Server After Failure");
            if (!JobRunr.getBackgroundJobServer().isRunning()) {
                start(this.createNewBackgroundServer());
            } else {
                logger.info("BackGround Server Already Running...Restart Not required");
            }
        } catch (Exception e) {
            logger.error("Failed While Restarting Server, Retrying Again");
        }
    };
}