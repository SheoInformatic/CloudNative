// Entorno de desarrollo local.
// Reemplazar los valores de Azure AD por los de la App Registration real (SPA).
export const environment = {
  production: false,
  azureAd: {
    clientId: "REEMPLAZAR-CLIENT-ID-SPA",
    tenantId: "REEMPLAZAR-TENANT-ID",
    redirectUri: "http://localhost:4200",
    postLogoutRedirectUri: "http://localhost:4200",
    // Scope expuesto por la App Registration de la API (bff-service / APIM).
    apiScope: "api://REEMPLAZAR-CLIENT-ID-API/access_as_user",
  },
  apiBaseUrl: "http://localhost:8080", // en produccion: URL publica de Azure API Management
};
