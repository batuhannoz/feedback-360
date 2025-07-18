package com.batuhan.feedback360.config;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record CustomPrincipal(
    Integer userId,
    Integer companyId,
    String email,
    Collection<? extends GrantedAuthority> authorities
) {
}
