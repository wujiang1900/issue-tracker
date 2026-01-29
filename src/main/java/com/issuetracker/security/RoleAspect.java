package com.issuetracker.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
public class RoleAspect {
    @Before("@annotation(role)")
    public void checkRole(JoinPoint joinPoint, Role role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("Invalid principal");
        }
        if (!((UserPrincipal) authentication.getPrincipal()).getUser().getRole().name().equals(role.value())) {
            throw new IllegalStateException("User does not have required role: " + role.value());
        }
    }
}
