package com.airtel.scheduler.execution.internal;

import com.airtel.scheduler.execution.enums.InternalJobsEnum;
import com.airtel.scheduler.execution.exception.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InternalJobFactory {

    private static Map<InternalJobsEnum, InternalJobService> internalJobServiceMap = new EnumMap<>(InternalJobsEnum.class);

    @Autowired
    private InternalJobFactory(List<InternalJobService> jobServices) {
        for (InternalJobService service : jobServices) {
            register(service.getInternalJob(), service);
        }
    }

    public static void register(InternalJobsEnum internalJob, InternalJobService service) {
        internalJobServiceMap.put(internalJob, service);
    }

    public static InternalJobService getInernalJob(InternalJobsEnum internalJob) {
        return Optional.ofNullable(internalJobServiceMap.get(internalJob)).orElseThrow(() -> new SchedulerException("Internal Action Not Found"));
    }
}