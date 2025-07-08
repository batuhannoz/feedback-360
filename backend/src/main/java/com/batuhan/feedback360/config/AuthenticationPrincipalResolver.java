package com.batuhan.feedback360.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationPrincipalResolver {

    public CustomPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomPrincipal)) {
            throw new IllegalStateException("User is not authenticated or principal is not of expected type.");
        }

        return (CustomPrincipal) authentication.getPrincipal();
    }

    public Integer getCompanyId() {
        return getPrincipal().companyId();
    }

    public Integer getUserId() {
        return getPrincipal().userId();
    }
}
