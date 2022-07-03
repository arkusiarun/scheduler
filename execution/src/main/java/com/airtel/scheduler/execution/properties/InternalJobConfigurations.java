package com.airtel.scheduler.execution.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@Data
public class InternalJobConfigurations {

    @Value(value = "${internal.jobs.archival.exclusionStatusKey}")
    private String excludeStatusKey;

    @Value(value = "${internal.jobs.archival.limitKey}")
    private String archivalLimitKey;
}