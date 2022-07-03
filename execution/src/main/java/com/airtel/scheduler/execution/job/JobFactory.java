package com.airtel.scheduler.execution.job;

import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.exception.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Arun Singh
 * <p>
 * Job Factory.
 * Abstract That that will Categorise Jobs into 3 Types.
 * Job to be Scheduled is One Time Only.
 * Job to be Scheduled is Recursive i.e on regulat Intervals.
 * To be executed one time But at a Precise Time.
 */

@Component
public class JobFactory {

    private static Map<JobType, Jobs> jobsMap = new EnumMap<>(JobType.class);

    @Autowired
    private JobFactory(List<Jobs> jobs) {
        populateFactoryMap(jobs.stream().collect(Collectors.toMap(Jobs::getJobType, Function.identity())));
    }

    public static Jobs getJob(JobType jobType) {
        return Optional.ofNullable(jobsMap.get(jobType)).orElseThrow(() -> new SchedulerException("Job Not Found"));
    }

    public static void populateFactoryMap(Map<JobType, Jobs> factoryMap) {
        jobsMap = factoryMap;
    }
}