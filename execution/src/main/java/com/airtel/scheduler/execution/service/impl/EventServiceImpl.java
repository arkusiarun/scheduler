package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.EventConfig;
import com.airtel.scheduler.execution.repository.EventConfigRepository;
import com.airtel.scheduler.execution.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventServiceImpl implements EventService {

    private final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private Map<String, EventConfig> eventConfigMap = new ConcurrentHashMap<>();

    @Autowired
    private EventConfigRepository eventConfigRepository;

    @PostConstruct
    private void loadEventConfigs() {
        Map<String, EventConfig> eventConfigMapTemp = new HashMap<>();
        Optional.of(this.eventConfigRepository.findAll()).ifPresent(eventConfigList -> eventConfigList.forEach(eventConfig -> {
            logger.info("Loading Event Configuration :{}", eventConfig.getId());
            if (Boolean.TRUE.equals(eventConfig.getActive())) {
                String key = eventConfig.getId();
                eventConfigMapTemp.put(key, eventConfig);
            }
        }));
        eventConfigMap = eventConfigMapTemp;
    }

    @Override
    public Response reload() {
        try {
            loadEventConfigs();
            logger.debug("Events reloaded Successfully.");
        } catch (Exception e) {
            logger.error("Failure Occurred while Reloading Events.");
            throw new SchedulerException(new Error(ValidationErrorCodes.RELOAD_ERROR.toString(), e.getMessage()));
        }
        return Response.successResponse();
    }

    @Override
    public EventConfig getConfigsForEventType(String eventType) {
        return Optional.ofNullable(eventConfigMap.get(eventType)).orElse(null);
    }

    @Override
    public Response createNewEvent(EventConfig eventConfig) {
        try {
            this.eventConfigRepository.save(eventConfig);
        } catch (Exception e) {
            logger.error("Failure Occurred while Creating New Activity. : {}", eventConfig);
            throw new SchedulerException(new Error(ValidationErrorCodes.CREATION_FAILED.toString(), e.getMessage()));
        }
        return Response.successResponse();
    }
}