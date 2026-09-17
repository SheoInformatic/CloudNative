package com.pedidos360.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Datos del inquilino (tenant) y de la App Registration de la API en Azure AD (Entra ID).
 * Se cargan desde application.yml / variables de entorno, nunca hardcodeados.
 */
@ConfigurationProperties(prefix = "azure.ad")
public class AzureAdProperties {

    /** Identificador del tenant de Azure AD (Directory (tenant) ID). */
    private String tenantId;

    /**
     * Identificador de audiencia esperado en el token (Application ID URI o Client ID
     * de la App Registration que representa esta API), p.ej. "api://<client-id>" o el
     * Client ID en formato GUID, segun como se haya expuesto la API.
     */
    private String audience;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getIssuerUri() {
        return "https://login.microsoftonline.com/" + tenantId + "/v2.0";
    }
}
