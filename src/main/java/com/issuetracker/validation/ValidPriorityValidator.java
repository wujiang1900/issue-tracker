package com.issuetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ValidPriorityValidator implements ConstraintValidator<ValidPriority, String> {

    private static final Set<String> VALID_PRIORITIES = Set.of(
            "LOW", "MEDIUM", "HIGH", "CRITICAL"
    );

    @Override
    public void initialize(ValidPriority constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull if required
        if (value == null) {
            return true;
        }

        return VALID_PRIORITIES.contains(value.toUpperCase());
    }
}
