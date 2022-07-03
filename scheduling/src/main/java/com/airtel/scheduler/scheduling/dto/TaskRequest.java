package com.airtel.scheduler.scheduling.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TaskRequest {
    private long referenceTime;
    private String eventType;
    private String jobName;
    private Map<String, Object> meta;
}