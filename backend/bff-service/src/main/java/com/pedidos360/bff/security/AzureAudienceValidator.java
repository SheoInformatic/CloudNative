package com.pedidos360.bff.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;

import java.util.List;

/**
 * Verifica explicitamente que el claim "aud" del token corresponda a la API de Pedidos360
 * y no a otra aplicacion registrada en el mismo tenant de Azure AD.
 *
 * Este validador se suma (no reemplaza) a los validadores por defecto de Spring, que ya
 * verifican firma, "iss" (issuer) y vigencia ("exp"/"nbf").
 */
public class AzureAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ERROR = new OAuth2Error(
            "invalid_token", "El token no contiene la audiencia esperada para esta API", null);

    private final String expectedAudience;

    public AzureAudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        if (audiences != null && audiences.contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(ERROR);
    }
}
