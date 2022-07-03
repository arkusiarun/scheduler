package com.airtel.scheduler.execution.repository;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.enums.*;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomRepository {

    private static final String JOB_GROUP = "jobGroup";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_TYPE = "jobType";
    private static final String ACTIVE = "active";
    private static final String SCHEDULED_TIME = "scheduledTime";
    private static final String ARCHIVAL_TIME = "archivalTime";
    private static final String STATUS = "status";
    private static final String EVENT_TYPE = "eventType";
    private static final String REFERENCE_KEYS_HASH = "referenceKeysHash";

    @Autowired
    @Qualifier("secondaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    public List<Task> findTasksForBatchExecution(String jobName, Integer offset, JobExecutionType executionType, long maxFetchDuration, Duration duration) {
        Query query = new Query();
        LocalDate localDate = this.getDateForExecution(executionType, maxFetchDuration, duration).toLocalDate();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where(JOB_GROUP).is(JobGroup.BATCH.getDescription()),
                Criteria.where(JOB_NAME).is(jobName),
                Criteria.where(STATUS).is(Status.SUBMITTED),
                Criteria.where(SCHEDULED_TIME).lt(localDate.plusDays(1)),
                Criteria.where(ACTIVE).ne(false)));
        query.limit(offset);
        return mongoTemplate.find(query, Task.class);
    }

    public List<Task> findTasksForGenericExecution(String jobName, Integer offset, JobExecutionType executionType, long maxFetchDuration, Duration duration) {
        Query query = new Query();
        LocalDateTime localDateTime = this.getDateForExecution(executionType, maxFetchDuration, duration);
        query.addCriteria(new Criteria().andOperator(
                Criteria.where(JOB_GROUP).is(JobGroup.GENERIC.getDescription()),
                Criteria.where(JOB_NAME).is(jobName),
                Criteria.where(STATUS).is(Status.SUBMITTED),
                Criteria.where(SCHEDULED_TIME).lte(localDateTime),
                Criteria.where(ACTIVE).is(true)));
        query.limit(offset);
        return mongoTemplate.find(query, Task.class);
    }

    public ScheduledJob findScheduledJobByFilter(String jobName, String jobGroup, String jobType) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where(JOB_GROUP).is(jobGroup),
                Criteria.where(JOB_NAME).is(jobName),
                Criteria.where(JOB_TYPE).is(jobType),
                Criteria.where(ACTIVE).is(true)));
        return mongoTemplate.findOne(query, ScheduledJob.class);
    }

    public List<Task> fetchExistingTasks(String eventType, String referenceKeysHash) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where(REFERENCE_KEYS_HASH).exists(Boolean.TRUE),
                Criteria.where(REFERENCE_KEYS_HASH).is(referenceKeysHash),
                Criteria.where(EVENT_TYPE).is(eventType),
                Criteria.where(STATUS).is(Status.SUBMITTED),
                Criteria.where(ACTIVE).is(true)));
        return mongoTemplate.find(query, Task.class);
    }

    public List<Task> fetchTasksForArchival(List<Status> statuses, Integer limit) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where(ARCHIVAL_TIME).lte(CommonUtils.getCurrentDateTime()),
                Criteria.where(STATUS).nin(statuses),
                Criteria.where(ACTIVE).ne(true)));
        query.with(Sort.by(Sort.Direction.ASC, SCHEDULED_TIME)).limit(limit);
        return mongoTemplate.find(query, Task.class);
    }

    private LocalDateTime getDateForExecution(JobExecutionType executionType, long maxFetchDuration, Duration duration) {
        boolean isBackLog = false;
        if(JobExecutionType.BACKLOG.equals(executionType)) {
            isBackLog = true;
        }
        return this.handleTimeDuration(duration, maxFetchDuration, isBackLog);
    }

    private LocalDateTime handleTimeDuration(Duration duration, long maxFetchDuration, boolean isBacklog) {
        LocalDateTime localDateTime;
        switch (duration) {
            case YEAR:
                localDateTime = this.handleYears(isBacklog, maxFetchDuration);
                break;
            case MONTH:
                localDateTime = this.handleMonths(isBacklog, maxFetchDuration);
                break;
            case DAYS:
                localDateTime = this.handleDays(isBacklog, maxFetchDuration);
                break;
            case HOUR:
                localDateTime = this.handleHours(isBacklog, maxFetchDuration);
                break;
            case MINUTES:
                localDateTime = this.handleMinutes(isBacklog, maxFetchDuration);
                break;
            case SECONDS:
                localDateTime = this.handleSeconds(isBacklog, maxFetchDuration);
                break;
            default:
                throw new SchedulerException(new Error(ValidationErrorCodes.SCHEDULED_TIME_ERROR.toString(), ValidationErrorCodes.SCHEDULED_TIME_ERROR.getErrorMessage()));
        }
        return localDateTime;
    }

    private LocalDateTime handleYears(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusYears(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusYears(maxFetchDuration);
        }
        return localDateTime;
    }

    private LocalDateTime handleMonths(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusMonths(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusMonths(maxFetchDuration);
        }
        return localDateTime;
    }

    private LocalDateTime handleDays(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusDays(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusDays(maxFetchDuration);
        }
        return localDateTime;
    }

    private LocalDateTime handleHours(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusHours(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusHours(maxFetchDuration);
        }
        return localDateTime;
    }

    private LocalDateTime handleMinutes(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusMinutes(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusMinutes(maxFetchDuration);
        }
        return localDateTime;
    }

    private LocalDateTime handleSeconds(boolean isBacklog, long maxFetchDuration) {
        LocalDateTime localDateTime;
        if (isBacklog) {
            localDateTime = CommonUtils.getCurrentDateTime().minusSeconds(maxFetchDuration);
        } else {
            localDateTime = CommonUtils.getCurrentDateTime().plusSeconds(maxFetchDuration);
        }
        return localDateTime;
    }
}