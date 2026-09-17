# CloudNative — Pedidos360 (Etapa 1)

Arquitectura base del sistema **Pedidos360**: frontend Angular con autenticación Azure AD
(MSAL), backend en microservicios Spring Boot, y un BFF que valida cada token de forma
independiente antes de dejarlo pasar. Todo pensado para desplegarse en Azure.

## Estructura del repositorio

```
CloudNative/
├── backend/         # 3 microservicios Spring Boot (ver backend/README.md)
│   ├── bff-service/       # Valida JWT de Azure AD + autorizacion por rol
│   ├── pedidos-service/    # Gestion de pedidos
│   └── productos-service/  # Catalogo de productos
├── frontend/         # App Angular 18 + MSAL (ver frontend/README.md)
└── docs/             # Arquitectura, setup de Azure AD y guia de despliegue
    ├── architecture.md
    ├── azure-setup-guide.md
    └── deployment-guide.md
```

## Por dónde empezar

1. **Arquitectura completa y por qué se valida el token dos veces (APIM + BFF):**
   [`docs/architecture.md`](docs/architecture.md)
2. **Crear las App Registrations en Azure AD (SPA + API, roles, scopes):**
   [`docs/azure-setup-guide.md`](docs/azure-setup-guide.md)
3. **Desplegar en Azure Container Apps, API Management y Static Web Apps:**
   [`docs/deployment-guide.md`](docs/deployment-guide.md)
4. **Correr el backend en local:** [`backend/README.md`](backend/README.md)
5. **Correr el frontend en local:** [`frontend/README.md`](frontend/README.md)

## Checklist frente a los requisitos formales

| Requisito                                                       | Estado |
|--------------------------------------------------------------------|--------|
| Backend en varios microservicios Java/Spring Boot                  | ✅ |
| Frontend como componente Angular                                   | ✅ |
| MSAL integrado y operativo (login/logout, guards, interceptor)     | ✅ |
| Tokens obtenidos para consumir el API Gateway                      | ✅ |
| Roles y scopes leídos desde los claims del token                   | ✅ |
| BFF valida issuer, audience, firma y vigencia del token             | ✅ |
| BFF aplica autorización por rol y responde 401/403 según corresponda | ✅ |
| Despliegue en Azure (Container Apps, API Management, Static Web Apps) | ⏳ pendiente de aprovisionar en tu suscripción — guía lista en `docs/deployment-guide.md` |

## Verificación

El frontend fue compilado con `ng build --configuration production` sin errores antes de
subir este código. El backend no pudo compilarse con Maven en el entorno donde se generó
(sin salida a Maven Central), así que se recomienda correr `mvn clean verify` en cada
módulo de `backend/` antes de desplegar.
