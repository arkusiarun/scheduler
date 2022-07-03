package com.airtel.scheduler.execution.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Arun Singh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@Data
public class GenericException extends RuntimeException {

    private final String message;

    private static final long serialVersionUID = 1L;

    public GenericException(String errorMessage) {
        this.message = errorMessage;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
