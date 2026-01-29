package com.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStatusValidator.class)
public @interface ValidStatus {
    String message() default "Invalid status. Must be one of: OPEN, IN_PROGRESS, CLOSED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
