package com.airtel.scheduler.execution.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JobRequestValidator.class)
public @interface JobRequestValidate {

    String message() default "Invalid Job Request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}