package com.airtel.scheduler.execution.exception;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ControllerAdvice(basePackages = "com.airtel.scheduler")
public class SchedulerExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {SchedulerException.class})
    public ResponseEntity<Response<Object>> schedulerException(SchedulerException ex) {
        List<Error> errorList = new ArrayList<>();
        if (CommonUtils.isEmptyList(ex.getErrorList())) {
            errorList.add(ex.getError());
        } else {
            errorList.addAll(ex.getErrorList());
        }
        return new ResponseEntity<>(Response.failureResponse(errorList), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Response<Object>> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<Error> errorList = new ArrayList<>();
        List<ObjectError> allErrors = result.getAllErrors();
        for (ObjectError allError : allErrors) {
            if (allError instanceof FieldError) {
                FieldError fieldError = (FieldError) allError;
                errorList.add(new Error(fieldError.getCode(), fieldError.getDefaultMessage()));
            } else {
                errorList.add(new Error(allError.getCode(), allError.getDefaultMessage()));
            }
        }
        return new ResponseEntity<>(Response.failureResponse(errorList), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Response<Object>> constraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        List<Error> errors = new ArrayList<>();
        for (ConstraintViolation constraintViolation : constraintViolations) {
            errors.add(new Error(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()));
        }
        return new ResponseEntity<>(Response.failureResponseWithBody(errors), HttpStatus.BAD_REQUEST);
    }
}