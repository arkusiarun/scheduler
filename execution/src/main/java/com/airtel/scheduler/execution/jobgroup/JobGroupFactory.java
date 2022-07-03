package com.airtel.scheduler.execution.jobgroup;

import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.exception.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobGroupFactory {

    private static Map<JobGroup, JobGroups> jobGroupsMap = new EnumMap<>(JobGroup.class);

    @Autowired
    private JobGroupFactory(List<JobGroups> jobGroups) {
        populateFactoryMap(jobGroups.stream().collect(Collectors.toMap(JobGroups::getJobGroup, Function.identity())));
    }

    public static JobGroups getJobGroup(JobGroup jobGroup) {
        return Optional.ofNullable(jobGroupsMap.get(jobGroup)).orElseThrow(() -> new SchedulerException("Job Group Not Found"));
    }

    public static void populateFactoryMap(Map<JobGroup, JobGroups> factoryMap) {
        jobGroupsMap = factoryMap;
    }
}