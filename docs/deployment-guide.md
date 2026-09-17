# Guia de despliegue en Azure

Todo el sistema se despliega en Azure (sin AWS): Angular en **Azure Static Web Apps**,
los tres servicios Spring Boot en **Azure Container Apps**, y **Azure API Management**
como puerta de entrada unica.

## 1. Azure Container Apps — backend

```bash
RG=pedidos360-rg
LOCATION=eastus2

az group create --name $RG --location $LOCATION

az containerapp env create \
  --name pedidos360-env \
  --resource-group $RG \
  --location $LOCATION
```

Para cada microservicio (`productos-service`, `pedidos-service`, `bff-service`):

```bash
# 1. Build & push de la imagen a Azure Container Registry
az acr create --name pedidos360acr --resource-group $RG --sku Basic
az acr build --registry pedidos360acr --image productos-service:1.0.0 ./backend/productos-service

# 2. Deploy del contenedor
az containerapp create \
  --name productos-service \
  --resource-group $RG \
  --environment pedidos360-env \
  --image pedidos360acr.azurecr.io/productos-service:1.0.0 \
  --target-port 8081 \
  --ingress internal \
  --registry-server pedidos360acr.azurecr.io \
  --min-replicas 1 --max-replicas 3
```

Repetir para `pedidos-service` (`--target-port 8082`, variable de entorno
`PRODUCTOS_SERVICE_URL` apuntando al FQDN interno de `productos-service`) y para
`bff-service` (`--target-port 8080`, **`--ingress external`** porque este si debe ser
alcanzable por Azure API Management, con las variables `AZURE_TENANT_ID`,
`AZURE_API_AUDIENCE`, `PRODUCTOS_SERVICE_URL`, `PEDIDOS_SERVICE_URL`, `ALLOWED_ORIGINS`).

```bash
az containerapp create \
  --name bff-service \
  --resource-group $RG \
  --environment pedidos360-env \
  --image pedidos360acr.azurecr.io/bff-service:1.0.0 \
  --target-port 8080 \
  --ingress external \
  --registry-server pedidos360acr.azurecr.io \
  --env-vars \
    AZURE_TENANT_ID=secretref:tenant-id \
    AZURE_API_AUDIENCE=secretref:api-audience \
    PRODUCTOS_SERVICE_URL=https://productos-service.internal.<env-domain> \
    PEDIDOS_SERVICE_URL=https://pedidos-service.internal.<env-domain> \
    ALLOWED_ORIGINS=https://<tu-app>.azurestaticapps.net
```

`--ingress internal` en `productos-service` y `pedidos-service` es la garantia de que
estos dos microservicios **no** son alcanzables desde internet: solo otros recursos
dentro del mismo Container Apps Environment (como `bff-service`) pueden llamarlos.

## 2. Azure API Management delante de bff-service

Ver `azure-setup-guide.md` seccion 5 para la creacion de APIM y la politica `validate-jwt`.
El backend de la API en APIM debe apuntar al FQDN publico que entrega
`az containerapp show --name bff-service --query properties.configuration.ingress.fqdn`.

## 3. Azure Static Web Apps — frontend

```bash
az staticwebapp create \
  --name pedidos360-frontend \
  --resource-group $RG \
  --location $LOCATION \
  --sku Free
```

Build local y despliegue del artefacto (o conectar el repo de GitHub para CI/CD
automatico con el flujo estandar de Static Web Apps / GitHub Actions):

```bash
cd frontend
npm install
npm run build:prod
npx @azure/static-web-apps-cli deploy ./dist/pedidos360-frontend/browser \
  --deployment-token <token-del-recurso-static-web-app>
```

Antes de este paso, actualizar `src/environments/environment.prod.ts` con:
- `redirectUri` / `postLogoutRedirectUri`: la URL real de `*.azurestaticapps.net`
- `apiBaseUrl`: la URL publica de Azure API Management (no la del BFF directamente)

Y volver a la App Registration del SPA en Azure AD para agregar esa URL como Redirect URI.

## 4. Orden recomendado de despliegue

1. Azure Container Registry + build de las 3 imagenes.
2. `productos-service` y `pedidos-service` (ingress interno).
3. `bff-service` (ingress externo), con las URLs internas de los dos anteriores.
4. Azure API Management apuntando al FQDN de `bff-service`.
5. App Registrations en Azure AD (SPA + API) con las URLs ya conocidas de APIM y de
   Static Web Apps.
6. Frontend Angular con `environment.prod.ts` apuntando a APIM, build y deploy a
   Static Web Apps.
7. Prueba end-to-end: login en la SPA -> `GET /api/me` a traves de APIM -> respuesta
   200 con los claims resueltos por el BFF.
