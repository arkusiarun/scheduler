package com.airtel.scheduler.execution.actions.impl;

import com.airtel.scheduler.execution.actions.Actions;
import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.TaskResponse;
import com.airtel.scheduler.execution.dto.WorkerDefinition;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.kafka.KafkaProducer;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WorkerAction implements Actions {

    private final Logger logger = LoggerFactory.getLogger(WorkerAction.class);

    private KafkaProducer kafkaProducer;

    @Autowired
    public WorkerAction(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public ActionType getType() {
        return ActionType.WORKER;
    }

    @Override
    public TaskResponse execute(Task task, Action action, Map<String, Object> transformationResult) {
        logger.info("Executing Worker Action For For Meta {}", transformationResult);
        try {
            Map<String, String> kafkaHeaders = this.buildKafkaHeaders(task.getId(), action.getActionDefinition().getWorkerDefinition());
            this.kafkaProducer.sendMessage(transformationResult, action.getActionDefinition().getWorkerDefinition().getTopicName(), kafkaHeaders);
        } catch (SchedulerException | KafkaException e) {
            logger.error("Exception Occured While Pushing Tasks to Kafka for Task :{}", task);
            throw new SchedulerException(e.getMessage());
        }
        return null;
    }

    private Map<String, String> buildKafkaHeaders(String taskId, WorkerDefinition workerDefinition) {
        Map<String, String> headers = new HashMap<>();
        headers.put(CommonConstants.TASK_ID, taskId);
        if (!CommonUtils.isNullOrEmptyMap(workerDefinition.getHeaders())) {
            headers.putAll(workerDefinition.getHeaders());
        }
        return headers;
    }
}