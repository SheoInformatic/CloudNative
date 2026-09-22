# Pedidos360 — Backend (microservicios Spring Boot)

Tres microservicios independientes, cada uno con su propio `pom.xml`, `Dockerfile` y ciclo
de vida, pensados para desplegarse por separado en Azure Container Apps:

| Servicio            | Puerto | Expuesto a internet | Rol                                                        |
|----------------------|--------|-----------------------|-------------------------------------------------------------|
| `bff-service`         | 8080   | Si (detras de APIM)   | Valida JWT de Azure AD, autoriza por rol, enruta al resto  |
| `pedidos-service`     | 8082   | No (red interna)      | Gestion de pedidos                                          |
| `productos-service`   | 8081   | No (red interna)      | Catalogo de productos                                       |

Ver `../docs/architecture.md` para el diagrama completo y `../docs/azure-setup-guide.md` / 
`../docs/deployment-guide.md` para la configuracion de Azure AD, API Management y el
despliegue en Container Apps.

## Probar los 3 servicios juntos en local

```bash
export AZURE_TENANT_ID=<tenant-id>
export AZURE_API_AUDIENCE=api://<client-id-api>
docker compose up --build
```

Esto valida la comunicacion **bff-service → pedidos-service → productos-service** de
punta a punta antes de tocar Azure. Nota: para probar el flujo completo necesitas un
access token real emitido por Azure AD (se obtiene iniciando sesion desde el frontend
Angular corriendo en paralelo con `npm start`).

## Estructura

```
backend/
├── bff-service/         # Backend for Frontend — valida JWT, autoriza, enruta
├── pedidos-service/      # Microservicio de pedidos
├── productos-service/    # Microservicio de productos
└── docker-compose.yml    # Orquestacion local de los 3 servicios

(../docs/ en la raiz del repo — arquitectura y guias de despliegue en Azure)
```
