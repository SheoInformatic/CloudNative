# pedidos360-frontend

Aplicacion Angular 18 (standalone components) que implementa el frontend de **Pedidos360**,
con autenticacion via Azure AD (Entra ID) usando MSAL.

## Que resuelve este proyecto (requisitos de la etapa 1)

- **Login / logout**: `AuthService` (sobre `MsalService`) con flujo de redireccion.
- **Guards**: `MsalGuard` de `@azure/msal-angular` protege rutas que requieren sesion
  (`/pedidos`, `/productos`); un `roleGuard` propio (`src/app/guards/role.guard.ts`)
  protege ademas por rol (ej. `/productos` exige rol `Admin`).
- **MsalInterceptor**: adjunta automaticamente el access token `Bearer` a toda peticion
  HTTP dirigida a `environment.apiBaseUrl` (Azure API Management), ver `auth-config.ts`
  -> `MSALInterceptorConfigFactory` y su registro en `app.config.ts`.
- **Lectura de roles/scopes desde el token**: `AuthService.getRoles()` lee el claim
  `roles` del ID token de la cuenta activa (los mismos roles viajan en el access token
  que valida el BFF).

## Configuracion previa (Azure AD / Entra ID)

Antes de correr la app hay que crear en Azure AD:

1. Una **App Registration tipo SPA** (el frontend), con:
   - Redirect URI de tipo *Single-page application*: `http://localhost:4200` (dev) y la URL
     de Azure Static Web Apps en produccion.
   - Permiso delegado (API permission) hacia la App Registration de la API, scope
     `access_as_user` (o el nombre que se le de), con consentimiento de administrador otorgado.
2. Una **App Registration tipo API** (representa a `bff-service`), con:
   - Un scope expuesto (`Expose an API`), p.ej. `api://<client-id-api>/access_as_user`.
   - App Roles definidos (p.ej. `Admin`, `User`) asignables a usuarios/grupos.
   - Usuarios de prueba asignados a esos roles en **Enterprise Applications > Users and groups**.

Ver `../docs/azure-setup-guide.md` para el paso a paso detallado con comandos de Azure CLI.

## Configurar el proyecto

Editar `src/environments/environment.ts` (y `environment.prod.ts`) con los valores reales:

```ts
export const environment = {
  production: false,
  azureAd: {
    clientId: "<client-id-de-la-app-registration-SPA>",
    tenantId: "<tenant-id>",
    redirectUri: "http://localhost:4200",
    postLogoutRedirectUri: "http://localhost:4200",
    apiScope: "api://<client-id-de-la-app-registration-API>/access_as_user",
  },
  apiBaseUrl: "http://localhost:8080", // BFF local; en Azure sera la URL de API Management
};
```

## Ejecutar en desarrollo

```bash
npm install
npm start
```

La app queda en `http://localhost:4200`. Con el BFF corriendo en `http://localhost:8080`
(repositorio `pedidos360-backend`, servicio `bff-service`), el flujo completo login ->
token -> llamada protegida funciona de punta a punta en local, antes de desplegar nada
en Azure.

## Build de produccion

```bash
npm run build:prod
```

Genera `dist/pedidos360-frontend/browser`, listo para publicar en Azure Static Web Apps
o en un contenedor Nginx (ver `../docs/deployment-guide.md`).
