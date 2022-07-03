package com.airtel.scheduler.execution.model;

import com.airtel.scheduler.execution.dto.RetryDef;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Arun Singh
 */

@Data
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;
    private String eventType;
    private JobGroup jobGroup;
    private String action;
    private LocalDateTime creationDate;
    private LocalDateTime scheduledTime;
    private LocalDateTime archivalTime;
    private Status status;
    private String comments;
    private String jobName;
    private Boolean active;
    private JobType jobType;
    private String parentTaskId;
    private String referenceKeysHash;
    private RetryDef retryDef;
    private Map<String, Object> meta;

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public Task() {
        this.creationDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}