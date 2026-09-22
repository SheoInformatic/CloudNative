# Guia de configuracion de Azure AD (Entra ID) y API Management

Todos los comandos usan Azure CLI (`az`). Ejecutalos con una cuenta que tenga permisos
de administrador de aplicaciones en el tenant.

## 1. Crear la App Registration de la API (representa a bff-service)

```bash
az login
az account set --subscription "<tu-suscripcion>"

# 1a. Crear el registro
API_APP_ID=$(az ad app create \
  --display-name "Pedidos360-API" \
  --sign-in-audience AzureADMyOrg \
  --query appId -o tsv)

echo "API_APP_ID=$API_APP_ID"

# 1b. Exponer un scope (Expose an API)
az ad app update --id $API_APP_ID \
  --identifier-uris "api://$API_APP_ID"
```

Para el scope `access_as_user` y los App Roles (`Admin`, `User`), el manifiesto se edita
mas facil desde **Azure Portal > App registrations > Pedidos360-API**:

- **Expose an API** → *Add a scope*:
  - Scope name: `access_as_user`
  - Who can consent: *Admins and users*
  - Admin/user consent display name y description: libres.
- **App roles** → *Create app role* (repetir para cada uno):
  - `Admin` — Allowed member types: *Users/Groups* — Value: `Admin`
  - `User` — Allowed member types: *Users/Groups* — Value: `User`

## 2. Asignar usuarios a los roles

**Azure Portal > Enterprise applications > Pedidos360-API > Users and groups > Add
user/group**, seleccionar el usuario y el rol (`Admin` o `User`). Sin esta asignacion,
el claim `roles` **no aparecera** en el token aunque el rol este definido en el manifiesto.

## 3. Crear la App Registration del frontend (SPA)

```bash
SPA_APP_ID=$(az ad app create \
  --display-name "Pedidos360-SPA" \
  --sign-in-audience AzureADMyOrg \
  --query appId -o tsv)

echo "SPA_APP_ID=$SPA_APP_ID"

# Redirect URIs de tipo SPA (no "Web"): local + produccion
az ad app update --id $SPA_APP_ID \
  --set spa={redirectUris:[http://localhost:4200,https://<tu-app>.azurestaticapps.net]}
```

### Otorgar el permiso delegado hacia la API

```bash
# API permission: Pedidos360-SPA -> Pedidos360-API -> access_as_user
az ad app permission add --id $SPA_APP_ID \
  --api $API_APP_ID \
  --api-permissions <scope-id-de-access_as_user>=Scope

az ad app permission grant --id $SPA_APP_ID --api $API_APP_ID
az ad app permission admin-consent --id $SPA_APP_ID
```

(El `<scope-id-de-access_as_user>` se obtiene de `az ad app show --id $API_APP_ID` en
`api.oauth2PermissionScopes[].id`, o simplemente se hace el consentimiento desde el
Portal en **API permissions > Add a permission > My APIs > Pedidos360-API**).

## 4. Variables resultantes para cada componente

| Variable                              | Valor                                              | Donde se usa |
|-----------------------------------------|------------------------------------------------------|----------------|
| `environment.azureAd.tenantId`           | Directory (tenant) ID                                 | Frontend       |
| `environment.azureAd.clientId`           | `$SPA_APP_ID`                                         | Frontend       |
| `environment.azureAd.apiScope`           | `api://$API_APP_ID/access_as_user`                    | Frontend       |
| `AZURE_TENANT_ID`                        | Directory (tenant) ID                                 | bff-service    |
| `AZURE_API_AUDIENCE`                     | `api://$API_APP_ID` (o `$API_APP_ID` a secas, segun como quede el `aud` del token — verificar con jwt.ms) | bff-service |

> Tip: pega un access token real en https://jwt.ms para confirmar el valor exacto de
> `aud` y de `roles`/`scp` antes de fijar `AZURE_API_AUDIENCE`.

## 5. Azure API Management — politica validate-jwt

Crear el API Management (tier Developer o Consumption sirven para esta etapa):

```bash
az apim create \
  --name pedidos360-apim \
  --resource-group <rg> \
  --publisher-email admin@tuempresa.com \
  --publisher-name "Pedidos360" \
  --sku-name Consumption
```

Crear una API que apunte al `bff-service` (Container Apps) como backend, y agregarle
esta politica en el nivel de la API (Design > Inbound processing > Policy code editor):

```xml
<policies>
  <inbound>
    <base />
    <validate-jwt header-name="Authorization" failed-validation-httpcode="401"
                  failed-validation-error-message="Token invalido o ausente"
                  require-expiration-time="true" require-signed-tokens="true">
      <openid-config url="https://login.microsoftonline.com/{tenant-id}/v2.0/.well-known/openid-configuration" />
      <audiences>
        <audience>api://{api-client-id}</audience>
      </audiences>
      <issuers>
        <issuer>https://login.microsoftonline.com/{tenant-id}/v2.0</issuer>
      </issuers>
    </validate-jwt>
    <set-backend-service base-url="https://<bff-service-fqdn>" />
  </inbound>
  <backend><base /></backend>
  <outbound><base /></outbound>
  <on-error><base /></on-error>
</policies>
```

Con esto, APIM rechaza con 401 cualquier request antes de que llegue al BFF si el token
no es valido — exactamente el mismo criterio (issuer, audience, firma, expiracion) que
el BFF vuelve a aplicar de forma independiente.
