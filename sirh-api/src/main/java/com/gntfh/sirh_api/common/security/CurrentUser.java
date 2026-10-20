package com.gntfh.sirh_api.common.security;

import com.gntfh.sirh_api.auth.enums.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public record CurrentUser(Long id, String email, Role role) {

    public static CurrentUser from(Jwt jwt) {
        String email = jwt.getSubject();
        Long id = jwt.getClaim("uid") != null ? Long.valueOf(jwt.getClaim("uid").toString()) : null;
        Role role = Role.valueOf(jwt.getClaim("role"));
        return new CurrentUser(id, email, role);
    }
}
