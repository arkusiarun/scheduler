package com.airtel.scheduler.execution.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@Data
public class JobConfiguration {

    @Value(value = "${jobRunr.dashBoardPort}")
    private Integer jobRunrDashBoardPort;

    @Value(value = "${jobRunr.pollInterval}")
    private Integer pollInterval;

    @Value(value = "${jobRunr.defaultRetryCount}")
    private Integer defaultRetryCount;

    @Value(value = "${jobRunr.delete.successJobDuration}")
    private Integer successJobDuration;

    @Value(value = "${jobRunr.delete.jobDuration}")
    private Integer jobDuration;

    @Value(value = "${jobRunr.remove.timedOutServerDuration}")
    private Integer timedOutServerDuration;

    @Value(value = "${jobRunr.restart.duration}")
    private Integer restartBackgroundServerDuration;
}