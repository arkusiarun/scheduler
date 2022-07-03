package com.airtel.scheduler.execution.kafka;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.dto.SchedulerResponse;
import com.airtel.scheduler.execution.dto.TaskRequest;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.service.EventService;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class KafkaListner {

    private final Logger logger = LoggerFactory.getLogger(KafkaListner.class);

    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    @Qualifier("schedulerObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @KafkaListener(topics = "${kafka.scheduler.topic}", containerFactory = "schedulerKafkaConsumerFactory")
    public void consume(ConsumerRecord message) {
        logger.info("Message recieved : {}", message);
        this.processMessage(message);
    }

    @Async("taskCreationThreadPoolExecutor")
    void processMessage(ConsumerRecord message) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(message.value(), TaskRequest.class);
            Response<List<SchedulerResponse>> schedulerResponse = this.taskExecutionService.pushTasksForExecution(taskRequest);
            this.pushResponseToProducer(message.headers(), taskRequest, schedulerResponse);
            if (schedulerResponse.isSuccess() && CommonUtils.isEmptyList(schedulerResponse.getBody())) {
                throw new SchedulerException(new Error(ValidationErrorCodes.ACTIVITY_NOT_FOUND.toString(), ValidationErrorCodes.ACTIVITY_NOT_FOUND.getErrorMessage()));
            }
        } catch (Exception e) {
            logger.error("Exception While processing Message", e);
        }
    }

    private void pushResponseToProducer(Headers headers, TaskRequest taskRequest, Response<List<SchedulerResponse>> schedulerResponse) {
        Optional.ofNullable(this.eventService.getConfigsForEventType(taskRequest.getEventType()))
                .filter(eventConfig -> !StringUtils.isEmpty(eventConfig.getNotificationTopic()))
                .ifPresent(eventConfig -> {
                    Map<String, String> kafkaHeaders = new HashMap<>();
                    for (Header header : headers) {
                        kafkaHeaders.put(header.key(), new String(header.value()));
                    }
                    Map<String, Object> response = this.objectMapper.convertValue(schedulerResponse, new TypeReference<Map<String, Object>>() {
                    });
                    logger.info("Pushing Result Fot Topic Creation to Topic: {}", eventConfig.getNotificationTopic());
                    this.kafkaProducer.sendMessage(response, eventConfig.getNotificationTopic(), kafkaHeaders);
                });
    }
}