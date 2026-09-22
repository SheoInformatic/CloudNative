# Arquitectura Pedidos360 — Etapa 1

## Diagrama de componentes

```
┌─────────────────────┐        (1) login / token         ┌───────────────────────┐
│   Angular + MSAL     │ ────────────────────────────────▶│   Azure AD (Entra ID) │
│  (Static Web App)    │◀──────────────────────────────── │   Tenant unico         │
└──────────┬───────────┘        id_token + access_token   └───────────────────────┘
           │ (2) HTTPS + Bearer access_token
           ▼
┌───────────────────────────┐
│   Azure API Management     │  (3) valida JWT: issuer, audience, firma (JWKS), exp
│   (policy validate-jwt)     │      -> 401 si falla, en la puerta de entrada misma
└──────────────┬─────────────┘
               │ (4) reenvia con el token intacto
               ▼
┌───────────────────────────┐
│      bff-service            │  (5) VUELVE A validar el JWT (issuer/audience/firma/exp)
│  Spring Boot + Resource      │      + autorizacion por rol (@PreAuthorize)
│  Server (Azure AD)           │      -> 401 / 403 segun corresponda
└──────┬────────────┬─────────┘
       │            │  (6) agrega X-User-Id / X-User-Email (red interna, sin JWT)
       ▼            ▼
┌───────────────┐ ┌────────────────────┐
│ pedidos-service │ │ productos-service    │   Azure Container Apps
│ Spring Boot     │ │ Spring Boot          │   (ingress interno, no publico)
└────────┬────────┘ └──────────┬─────────┘
         └──────────(WebClient)┘
```

## Por que se valida el token dos veces (APIM y BFF)

Es intencional, no redundante por error: son dos perimetros de confianza distintos.

- **Azure API Management** es la puerta de entrada publica de todo el sistema. Si el
  token esta vencido, mal firmado, o no corresponde a esta API, el request se rechaza
  ahi mismo, **antes** de gastar computo en el backend.
- **bff-service** no puede asumir ciegamente que "si paso por APIM, es valido": en un
  escenario real puede haber rutas de red alternativas, entornos de prueba sin APIM
  delante, o simplemente conviene no depender de un unico punto de verificacion.
  Por eso el BFF vuelve a hacer la validacion completa de forma independiente.

Los microservicios de negocio (`pedidos-service`, `productos-service`) **no** validan
JWT: viven en una red interna de Azure Container Apps con *ingress* interno (no accesible
desde internet), y solo el BFF puede llamarlos. La identidad ya resuelta y autorizada
por el BFF se les pasa por cabeceras internas (`X-User-Id`, `X-User-Email`).

## Flujo de un login exitoso

1. El usuario hace clic en "Iniciar sesion" en Angular → `AuthService.login()` →
   `MsalService.loginRedirect()` con el scope `api://<api-client-id>/access_as_user`.
2. El navegador redirige a Azure AD, el usuario se autentica (y si corresponde, MFA).
3. Azure AD redirige de vuelta a la SPA con un `id_token` (identidad del usuario) y
   permite luego adquirir un `access_token` para el scope de la API, ambos como JWT
   firmados por Azure AD y con expiracion corta.
4. `MsalGuard` deja pasar a las rutas protegidas porque ya hay una cuenta activa.
5. `MsalInterceptor` adjunta el `access_token` como `Authorization: Bearer ...` a toda
   llamada HTTP hacia `environment.apiBaseUrl` (definido en `protectedResourceMap`).
6. Azure API Management valida el token (politica `validate-jwt`) y lo reenvia al BFF.
7. El BFF vuelve a validar el token, resuelve el usuario y sus roles, aplica
   `@PreAuthorize` segun el endpoint, y si todo esta en regla, llama al microservicio
   correspondiente.

## Roles y scopes usados en esta etapa

| Claim en el token | Origen                                              | Uso                                   |
|--------------------|------------------------------------------------------|------------------------------------------|
| `roles`             | App Roles definidos en la App Registration de la API, asignados a usuarios/grupos en Enterprise Applications | Autorizacion gruesa: `Admin`, `User`   |
| `scp`               | Scope delegado consentido por el usuario al iniciar sesion (`access_as_user`) | Confirma que el token se emitio para esta API con consentimiento del usuario |
| `aud`               | Client ID (o Application ID URI) de la App Registration de la API | Verificado por `AzureAudienceValidator` en el BFF y por `validate-jwt` en APIM |
| `iss`               | `https://login.microsoftonline.com/<tenant-id>/v2.0` | Verificado por los validadores por defecto de Spring Security |
