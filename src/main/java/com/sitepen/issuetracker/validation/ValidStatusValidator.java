
package com.sitepen.issuetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ValidStatusValidator implements ConstraintValidator<ValidStatus, String> {

    private static final Set<String> VALID_STATUSES = Set.of(
            "OPEN", "IN_PROGRESS", "CLOSED"
    );

    @Override
    public void initialize(ValidStatus constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull if required
        if (value == null) {
            return true;
        }

        return VALID_STATUSES.contains(value.toUpperCase());
    }
}
