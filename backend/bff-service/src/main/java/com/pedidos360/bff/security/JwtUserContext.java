package com.pedidos360.bff.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Helper para leer de forma consistente los claims de identidad desde el token
 * emitido por Azure AD (Entra ID), sin repetir logica en cada controller.
 */
public final class JwtUserContext {

    private JwtUserContext() {
    }

    public static String userId(Jwt jwt) {
        // "oid" es el identificador estable del usuario/objeto en el directorio de Azure AD.
        String oid = jwt.getClaimAsString("oid");
        return oid != null ? oid : jwt.getSubject();
    }

    public static String email(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername;
        }
        return jwt.getClaimAsString("email");
    }

    public static String displayName(Jwt jwt) {
        return jwt.getClaimAsString("name");
    }

    public static List<String> roles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null ? roles : List.of();
    }
}
