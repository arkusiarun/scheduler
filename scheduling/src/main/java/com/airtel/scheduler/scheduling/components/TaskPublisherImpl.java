package com.airtel.scheduler.scheduling.components;

import com.airtel.scheduler.scheduling.dto.TaskRequest;
import com.airtel.scheduler.scheduling.enums.ExceptionEnum;
import com.airtel.scheduler.scheduling.exception.SchedulingException;
import com.airtel.scheduler.scheduling.utilities.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("schedulerTaskPublisher")
public class TaskPublisherImpl implements TaskPublisher {

    @Autowired
    @Qualifier("schedulerKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.scheduler.topic:scheduler}")
    private String topicName;

    private final Logger logger = LoggerFactory.getLogger(TaskPublisherImpl.class);

    @Override
    public void publishTask(Object message, Map<String, String> kafkaHeaders) {
        logger.info("Publishing Message to Topic : {} with Content :{}", topicName, message);
        try {
            ObjectMapper objectMapper = Utils.getObjectMapper();
            Map<String, Object> requestObject = objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {
            });
            ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, requestObject);
            if (!(kafkaHeaders == null || kafkaHeaders.isEmpty())) {
                for (Map.Entry<String, String> entry : kafkaHeaders.entrySet()) {
                    record.headers().add(new RecordHeader(entry.getKey(), entry.getValue().getBytes()));
                }
            }
            kafkaTemplate.send(record);
        } catch (Exception e) {
            logger.error("Exception Occurred while Pushing Message to Kafka Topic : {} ", this.topicName, e);
            throw new SchedulingException(ExceptionEnum.KAFKA_ERROR);
        }
    }

    @Override
    public void createAndPublishSchedulerTask(String eventType, String jobName, long referenceTime, Object meta, Map<String, String> kafkaHeaders) {
        logger.info("Creating Task Request with EventType : {}, JobName : {}, ReferenceTime :{}, with Content :{}", eventType, jobName, referenceTime, meta);
        try {
            TaskRequest taskRequest = Utils.createTaskRequest(eventType, jobName, referenceTime, meta);
            this.publishTask(taskRequest, kafkaHeaders);
        } catch (Exception e) {
            logger.error("Invalid Task Request", e);
            throw new SchedulingException(ExceptionEnum.TASK_CREATION_ERROR);
        }
    }
}