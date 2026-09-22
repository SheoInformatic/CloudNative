package com.pedidos360.bff.controller;

import com.pedidos360.bff.security.JwtUserContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de diagnostico: devuelve al frontend la identidad y roles que el BFF
 * extrajo del access token, util para validar en el navegador que el flujo MSAL
 * -> APIM -> BFF esta funcionando de punta a punta.
 */
@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "userId", JwtUserContext.userId(jwt),
                "email", JwtUserContext.email(jwt),
                "name", JwtUserContext.displayName(jwt),
                "roles", JwtUserContext.roles(jwt)
        );
    }
}
