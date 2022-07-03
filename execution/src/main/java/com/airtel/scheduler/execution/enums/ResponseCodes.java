package com.airtel.scheduler.execution.enums;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;

/**
 * @author Arun Singh
 */
public enum ResponseCodes {

    SUCCESS("SUCCESS", HttpStatus.OK),
    FAILED("FAILED", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_RECORD_FOUND("NO_RECORD_FOUND", HttpStatus.INTERNAL_SERVER_ERROR);

    private String message;
    private HttpStatus status;

    ResponseCodes(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}