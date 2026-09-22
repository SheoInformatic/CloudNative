# bff-service (Backend for Frontend)

Punto de entrada unico para el frontend Angular. Es el unico backend expuesto detras de
**Azure API Management**; a su vez enruta hacia `pedidos-service` y `productos-service`,
que viven en red interna y nunca reciben trafico directo de internet.

## Que valida este servicio (requisito de la etapa 1)

Igual que la politica `validate-jwt` que se configura en Azure API Management, este BFF
valida **de forma independiente** cada access token que llega en el header `Authorization: Bearer <token>`:

| Verificacion            | Donde ocurre en el codigo                                              |
|--------------------------|-------------------------------------------------------------------------|
| Firma del token           | `NimbusJwtDecoder.withIssuerLocation(...)` obtiene las claves publicas (JWKS) del tenant de Azure AD y verifica la firma RS256 |
| Issuer (`iss`)            | `JwtValidators.createDefaultWithIssuer(issuerUri)` en `SecurityConfig.jwtDecoder()` |
| Vigencia (`exp` / `nbf`) | Incluido en los validadores por defecto de Spring (`JwtValidators`)     |
| Audiencia (`aud`)         | `AzureAudienceValidator` (validador propio) — rechaza tokens emitidos para otra app |
| Autorizacion por rol      | `@PreAuthorize("hasRole(...)")` sobre los claims `roles` del token (`AzureRolesConverter`) |
| Codigos de error          | `RestAuthenticationEntryPoint` -> 401 (token invalido/ausente); `RestAccessDeniedHandler` -> 403 (rol insuficiente) |

Si cualquiera de estas verificaciones falla, el request **nunca llega** a los controllers:
Spring Security corta la cadena de filtros antes de invocar el metodo del controller.

## Variables de entorno

| Variable                | Descripcion                                                            | Ejemplo                                             |
|--------------------------|-------------------------------------------------------------------------|------------------------------------------------------|
| `AZURE_TENANT_ID`         | Directory (tenant) ID de Azure AD                                       | `11111111-2222-3333-4444-555555555555`               |
| `AZURE_API_AUDIENCE`      | Application ID URI o Client ID de la App Registration de la API         | `api://22222222-3333-4444-5555-666666666666`         |
| `PRODUCTOS_SERVICE_URL`   | URL interna de productos-service                                        | `http://productos-service.internal...azurecontainerapps.io` |
| `PEDIDOS_SERVICE_URL`     | URL interna de pedidos-service                                          | `http://pedidos-service.internal...azurecontainerapps.io`   |
| `ALLOWED_ORIGINS`         | Origen(es) del frontend Angular permitidos por CORS                    | `https://pedidos360.azurestaticapps.net`              |

## Endpoints expuestos

| Metodo | Ruta                        | Autorizacion requerida         |
|--------|-----------------------------|----------------------------------|
| GET    | /api/me                     | Cualquier token valido            |
| GET    | /api/productos               | Cualquier token valido            |
| GET    | /api/productos/{id}          | Cualquier token valido            |
| POST   | /api/productos               | Rol `Admin`                       |
| PUT    | /api/productos/{id}          | Rol `Admin`                       |
| DELETE | /api/productos/{id}          | Rol `Admin`                       |
| GET    | /api/pedidos                 | Rol `User` o `Admin`              |
| POST   | /api/pedidos                 | Rol `User` o `Admin`              |
| PATCH  | /api/pedidos/{id}/estado     | Rol `Admin`                       |
| GET    | /actuator/health              | Publico (health check)            |

## Probar con curl

```bash
# Sin token -> 401
curl -i http://localhost:8080/api/me

# Con token (obtenido desde el frontend Angular via MSAL) -> 200
curl -i http://localhost:8080/api/me -H "Authorization: Bearer $TOKEN"

# Usuario sin rol Admin intentando crear un producto -> 403
curl -i -X POST http://localhost:8080/api/productos -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{}"
```

## Ejecutar localmente

```bash
export AZURE_TENANT_ID=<tenant-id>
export AZURE_API_AUDIENCE=api://<client-id-api>
mvn spring-boot:run
```
