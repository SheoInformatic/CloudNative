export const environment = {
  production: true,
  azureAd: {
    clientId: "REEMPLAZAR-CLIENT-ID-SPA",
    tenantId: "REEMPLAZAR-TENANT-ID",
    redirectUri: "https://REEMPLAZAR.azurestaticapps.net",
    postLogoutRedirectUri: "https://REEMPLAZAR.azurestaticapps.net",
    apiScope: "api://REEMPLAZAR-CLIENT-ID-API/access_as_user",
  },
  apiBaseUrl: "https://REEMPLAZAR.azure-api.net/pedidos360",
};
