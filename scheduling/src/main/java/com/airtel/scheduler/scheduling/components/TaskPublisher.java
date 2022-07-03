package com.airtel.scheduler.scheduling.components;

import java.util.Map;

public interface TaskPublisher {

    void publishTask(Object message, Map<String, String> kafkaHeaders);

    void createAndPublishSchedulerTask(String eventType, String jobName, long referenceTime, Object meta, Map<String, String> kafkaHeaders);
}