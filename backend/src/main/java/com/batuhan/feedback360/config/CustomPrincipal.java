package com.batuhan.feedback360.config;

import com.batuhan.feedback360.model.enums.UserType;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record CustomPrincipal(
    Integer userId,
    Integer companyId,
    String email,
    UserType userType,
    Collection<? extends GrantedAuthority> authorities
) {
}
