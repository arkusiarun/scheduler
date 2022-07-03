package com.airtel.scheduler.scheduling.exception;

import com.airtel.scheduler.scheduling.enums.ExceptionEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * @author Arun Singh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class SchedulingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ExceptionEnum exceptionEnum;

    private final String errorMessage;

    public SchedulingException() {
        this.errorMessage = null;
        this.exceptionEnum = null;
    }

    public SchedulingException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getErrorMessage());
        this.exceptionEnum = exceptionEnum;
        this.errorMessage = null;
    }

    public SchedulingException(ExceptionEnum exceptionEnum, String errorMessage) {
        super(errorMessage);
        this.exceptionEnum = exceptionEnum;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        String message = !StringUtils.isEmpty(errorMessage) ? errorMessage : exceptionEnum.getErrorMessage();
        return "Error Creating Task on Scheduler with [errorCode=" + exceptionEnum.getErrorCode() + "  errorMessage=" + message + "]";
    }
}
