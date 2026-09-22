package com.pedidos360.bff.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Traduce los claims de Azure AD a GrantedAuthority de Spring Security:
 *  - claim "roles"  -> App Roles asignados al usuario/grupo (ROLE_xxx)
 *  - claim "scp"    -> scopes delegados expuestos por la API (SCOPE_xxx)
 *
 * Con esto @PreAuthorize("hasRole(Admin)") o hasAuthority("SCOPE_Pedidos.Write")
 * funcionan de forma nativa sobre el token emitido por Entra ID.
 */
public class AzureRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        String scopeClaim = jwt.getClaimAsString("scp");
        if (scopeClaim != null && !scopeClaim.isBlank()) {
            for (String scope : scopeClaim.split(" ")) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }
        }

        return authorities;
    }
}
