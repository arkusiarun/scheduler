package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper class
 *
 * @author Arun Singh
 *
 * @param <T>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private boolean success;
    private T body;
    private List<Error> errors;

    private Response() {
    }

    public static <T> Response<T> successResponse(T body) {
        Response<T> response = new Response<>();
        response.success = true;
        response.body = body;
        return response;
    }

    public static Response<Object> successResponse() {
        Response<Object> response = new Response<>();
        response.success = true;
        return response;
    }

    public static <T> Response<T> failureResponse(List<Error> errors) {
        Response<T> response = new Response<>();
        response.success = false;
        response.errors = errors;
        return response;
    }

    public static <T> Response<T> failureResponseWithBody(T body) {
        Response<T> response = new Response<>();
        response.success = false;
        response.body = body;
        return response;
    }

    public static <T> Response<T> internalServerError() {
        Response<T> response = new Response<>();
        response.success = false;
        response.errors = new ArrayList<>();
        Error error = new Error();
        error.setCode(ValidationErrorCodes.GENERIC_ERROR.toString());
        error.setErrorMessage(ValidationErrorCodes.GENERIC_ERROR.getErrorMessage());
        response.errors.add(error);
        return response;
    }

    public static <T> Response<T> failureResponseWithError(Error error) {
        Response<T> response = new Response<>();
        response.success = false;
        response.errors = new ArrayList<>();
        response.errors.add(error);
        return response;
    }

    @Override
    public String toString() {
        return CommonUtils.getGson().toJson(this);
    }
}
