package ru.gentleman.order.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Object rolesClaim = source.getClaims().get("roles");

        if (!(rolesClaim instanceof Collection<?> rolesCollection)) {
            return Collections.emptyList();
        }

        return rolesCollection
                .stream()
                .map(roleName -> "ROLE_" + roleName) //в Spring Security роли должны быть вида ROLE_USER
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }
}
