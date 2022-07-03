package com.airtel.scheduler.execution.exception;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.enums.ResponseCodes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Arun Singh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper=true)
@Data
public class SchedulerException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final ResponseCodes responseCodes;

    private final String message;

    private final List<Error> errorList;

    private final Error error;

    public SchedulerException() {
        this.message = null;
        this.responseCodes = null;
        this.errorList = null;
        this.error = null;
    }

    public SchedulerException(ResponseCodes responseCodes) {
        super(responseCodes.getMessage());
        this.responseCodes = responseCodes;
        this.message = null;
        this.errorList = null;
        this.error = null;
    }

    public SchedulerException(ResponseCodes responseCodes, String message) {
        super(responseCodes.getMessage());
        this.responseCodes = responseCodes;
        this.message = message;
        this.errorList = null;
        this.error = null;
    }

    public SchedulerException(String errorMessage) {
        this.message = errorMessage;
        this.responseCodes = null;
        this.errorList = null;
        this.error = null;
    }

    public SchedulerException(Error error) {
        this.error = error;
        this.message = null;
        this.responseCodes = null;
        this.errorList = null;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}