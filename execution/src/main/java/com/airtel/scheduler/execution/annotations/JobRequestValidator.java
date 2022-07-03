package com.airtel.scheduler.execution.annotations;

import com.airtel.scheduler.execution.dto.ScheduledJobRequest;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.service.ActivityService;
import org.apache.commons.lang3.StringUtils;
import org.jobrunr.scheduling.cron.CronExpression;
import org.jobrunr.scheduling.cron.InvalidCronExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JobRequestValidator implements ConstraintValidator<JobRequestValidate, ScheduledJobRequest> {

    private final Logger logger = LoggerFactory.getLogger(JobRequestValidator.class);

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private ActivityService activityService;

    private boolean anyViolations = false;

    @Override
    public boolean isValid(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        logger.info("Validating Job : {}", scheduledJobRequest.getJobName());
        anyViolations = Boolean.FALSE;

        logger.info("Checking for Duplicate Job");
        checkForDuplication(scheduledJobRequest, context);

        logger.info("Validating Request Based On Job Type");
        validateRequestForJobType(scheduledJobRequest, context);

        logger.info("Validating Request Based on Job Group");
        validateRequestForJobGroup(scheduledJobRequest, context);

        return !anyViolations;
    }

    private void checkForDuplication(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        ScheduledJob scheduledJob = this.customRepository.findScheduledJobByFilter(scheduledJobRequest.getJobName(), scheduledJobRequest.getJobGroup().getDescription(), scheduledJobRequest.getJobType().getDescription());
        if (scheduledJob != null) {
            logger.info("Job already exists with Above Criteria :: {}", scheduledJobRequest.getJobName());
            anyViolations = true;
            context.buildConstraintViolationWithTemplate(ValidationErrorCodes.DUPLICATION_ERROR.getErrorMessage()).addConstraintViolation();
            context.disableDefaultConstraintViolation();
        }
    }

    private void validateRequestForJobType(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        switch (scheduledJobRequest.getJobType()) {
            case ONDEMAND:
                if (scheduledJobRequest.getScheduledTime() != 0 || !StringUtils.isEmpty(scheduledJobRequest.getCronExpression())) {
                    anyViolations = true;
                    context.buildConstraintViolationWithTemplate(ValidationErrorCodes.TIME_VALIDATION_FAILED.getErrorMessage()).addConstraintViolation();
                    context.disableDefaultConstraintViolation();
                }
                break;
            case SCHEDULED:
                if (scheduledJobRequest.getScheduledTime() == 0) {
                    anyViolations = true;
                    context.buildConstraintViolationWithTemplate(ValidationErrorCodes.SCHEDULED_TIME_ERROR.getErrorMessage()).addConstraintViolation();
                    context.disableDefaultConstraintViolation();
                }
                if (!StringUtils.isEmpty(scheduledJobRequest.getCronExpression())) {
                    anyViolations = true;
                    context.buildConstraintViolationWithTemplate(ValidationErrorCodes.CRON_ERROR.getErrorMessage()).addConstraintViolation();
                    context.disableDefaultConstraintViolation();
                }
                break;
            case REOCCURING:
                if (scheduledJobRequest.getScheduledTime() != 0) {
                    anyViolations = true;
                    context.buildConstraintViolationWithTemplate(ValidationErrorCodes.SCHEDULED_TIME_ERROR.getErrorMessage()).addConstraintViolation();
                    context.disableDefaultConstraintViolation();
                }
                if (StringUtils.isEmpty(scheduledJobRequest.getCronExpression())) {
                    anyViolations = true;
                    context.buildConstraintViolationWithTemplate(ValidationErrorCodes.CRON_ERROR.getErrorMessage()).addConstraintViolation();
                    context.disableDefaultConstraintViolation();
                } else {
                    this.validateCronExpression(scheduledJobRequest, context);
                }
                break;
            default:
                anyViolations = true;
                context.buildConstraintViolationWithTemplate(ValidationErrorCodes.GENERIC_ERROR.getErrorMessage()).addConstraintViolation();
                context.disableDefaultConstraintViolation();
        }
    }

    private void validateCronExpression(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        try {
            CronExpression.create(scheduledJobRequest.getCronExpression());
        } catch (InvalidCronExpressionException cronExpressionException) {
            anyViolations = true;
            logger.error("Invalid Cron Expression : {}", scheduledJobRequest.getCronExpression(), cronExpressionException);
            context.buildConstraintViolationWithTemplate(ValidationErrorCodes.CRON_ERROR.getErrorMessage()).addConstraintViolation();
            context.disableDefaultConstraintViolation();
        }
    }

    private void validateRequestForJobGroup(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        if (JobGroup.CUSTOM.equals(scheduledJobRequest.getJobGroup())) {
            this.validateActivity(scheduledJobRequest, context);
            if (scheduledJobRequest.getSchedulingInfo() != null) {
                anyViolations = true;
                context.buildConstraintViolationWithTemplate(ValidationErrorCodes.SCHEDULING_INFO_VALIDATION_FAILED.getErrorMessage()).addConstraintViolation();
                context.disableDefaultConstraintViolation();
            }
        } else {
            if (scheduledJobRequest.getSchedulingInfo() == null) {
                anyViolations = true;
                context.buildConstraintViolationWithTemplate(ValidationErrorCodes.SCHEDULING_INFO_VALIDATION_FAILED.getErrorMessage()).addConstraintViolation();
                context.disableDefaultConstraintViolation();
            }
        }
    }

    private void validateActivity(ScheduledJobRequest scheduledJobRequest, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(scheduledJobRequest.getEventType())) {
            anyViolations = true;
            context.buildConstraintViolationWithTemplate(ValidationErrorCodes.ACTIVITY_NOT_FOUND.getErrorMessage()).addConstraintViolation();
            context.disableDefaultConstraintViolation();
        }
    }
}