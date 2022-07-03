package com.airtel.scheduler.execution.internal;

import com.airtel.scheduler.execution.enums.InternalJobsEnum;

import java.util.Map;

public interface InternalJobService {

    InternalJobsEnum getInternalJob();

    void execute(Map<String, Object> meta);
}