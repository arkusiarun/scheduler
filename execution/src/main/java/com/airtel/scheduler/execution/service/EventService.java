package com.airtel.scheduler.execution.service;

import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.model.EventConfig;

public interface EventService {

    Response reload();

    EventConfig getConfigsForEventType(String eventType);

    Response createNewEvent(EventConfig eventConfig);
}