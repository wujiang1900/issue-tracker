package com.sitepen.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPriorityValidator.class)
public @interface ValidPriority {
    String message() default "Invalid priority. Must be one of: LOW, MEDIUM, HIGH, CRITICAL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
