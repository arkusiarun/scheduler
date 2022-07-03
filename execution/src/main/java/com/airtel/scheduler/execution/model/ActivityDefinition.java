package com.airtel.scheduler.execution.model;

import com.airtel.scheduler.execution.dto.ExecutionData;
import com.airtel.scheduler.execution.dto.JobDef;
import com.airtel.scheduler.execution.dto.RetryDef;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Data
@Document(collection = "activity")
public class ActivityDefinition {

    @Id
    private String id;
    @NotNull
    private String event;
    @NotNull
    private String action;
    private Boolean active;
    private RetryDef retryDef;
    private ExecutionData executionData;
    @NotNull
    private JobDef jobDef;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
