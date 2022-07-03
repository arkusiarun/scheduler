package com.airtel.scheduler.scheduling.utilities;

import com.airtel.scheduler.scheduling.configurations.CustomMapperModule;
import com.airtel.scheduler.scheduling.dto.SchedulerTask;
import com.airtel.scheduler.scheduling.dto.TaskRequest;
import com.airtel.scheduler.scheduling.enums.ExceptionEnum;
import com.airtel.scheduler.scheduling.exception.SchedulingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.util.StringUtils;

import java.util.Map;

public class Utils {

    private Utils() { }

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        objectMapper.registerModules(new JavaTimeModule(), new JsonOrgModule(), new JodaModule(), new CustomMapperModule());
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        return objectMapper;
    }

    public static TaskRequest getBasicTaskRequest(String eventType, String jobName, long referenceTime) {
        TaskRequest taskRequest = new TaskRequest();
        if (StringUtils.isEmpty(eventType) || Long.valueOf(referenceTime).equals(Long.valueOf(0))) {
            throw new SchedulingException(ExceptionEnum.TASK_CREATION_ERROR, "Event Type and Reference Time are Required");
        }
        if (!StringUtils.isEmpty(jobName)) {
            taskRequest.setJobName(jobName);
        }
        taskRequest.setEventType(eventType);
        taskRequest.setReferenceTime(referenceTime);
        return taskRequest;
    }

    public static void createTaskMeta(Object meta, TaskRequest taskRequest) {
        if (meta instanceof SchedulerTask) {
            convertToTaskMeta((SchedulerTask) meta, taskRequest);
        }
    }

    public static void convertToTaskMeta(SchedulerTask schedulerTask, TaskRequest taskRequest) {
        taskRequest.setMeta(getObjectMapper().convertValue(schedulerTask, new TypeReference<Map<String, Object>>() {
        }));
    }

    public static TaskRequest createTaskRequest(String eventType, String jobName, long referenceTime, Object meta) {
        TaskRequest taskRequest = getBasicTaskRequest(eventType, jobName, referenceTime);
        createTaskMeta(meta, taskRequest);
        return taskRequest;
    }
}