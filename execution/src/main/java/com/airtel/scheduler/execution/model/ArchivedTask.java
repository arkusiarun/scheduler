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
@Document(collection = "archived_tasks")
public class ArchivedTask {

    @Id
    private String id;
    private String eventType;
    private JobGroup jobGroup;
    private String action;
    private LocalDateTime creationDate;
    private LocalDateTime scheduledTime;
    private LocalDateTime archivalTime;
    private LocalDateTime taskCreationDate;
    private Status status;
    private String comments;
    private String jobName;
    private JobType jobType;
    private String parentTaskId;
    private RetryDef retryDef;
    private Map<String, Object> meta;

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public ArchivedTask() {
        this.creationDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}