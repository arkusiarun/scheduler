package com.airtel.scheduler.execution.model;

import com.airtel.scheduler.execution.dto.SchedulingInfo;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Arun Singh
 */

@Data
@Document(collection = "scheduled_jobs")
public class ScheduledJob {

    @Id
    private String id;
    private String jobName;
    private String cronExpression;
    private LocalDateTime dateTime;
    private JobGroup jobGroup;
    private JobType jobType;
    private Boolean active;
    private String eventType;
    @NotNull
    private SchedulingInfo schedulingInfo;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
