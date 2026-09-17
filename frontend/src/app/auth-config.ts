import {
  IPublicClientApplication,
  PublicClientApplication,
  InteractionType,
  BrowserCacheLocation,
  LogLevel,
} from "@azure/msal-browser";
import {
  MsalGuardConfiguration,
  MsalInterceptorConfiguration,
} from "@azure/msal-angular";
import { environment } from "../environments/environment";

const { clientId, tenantId, redirectUri, postLogoutRedirectUri, apiScope } = environment.azureAd;

/**
 * Instancia de MSAL. authority apunta al tenant especifico de Azure AD (Entra ID)
 * de la organizacion (flujo single-tenant); cacheLocation en sessionStorage evita
 * que los tokens sobrevivan entre pestañas/cierres del navegador.
 */
export function MSALInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId,
      authority: `https://login.microsoftonline.com/${tenantId}`,
      redirectUri,
      postLogoutRedirectUri,
      navigateToLoginRequestUrl: true,
    },
    cache: {
      cacheLocation: BrowserCacheLocation.SessionStorage,
      storeAuthStateInCookie: false,
    },
    system: {
      loggerOptions: {
        loggerCallback: (level: LogLevel, message: string, containsPii: boolean) => {
          if (containsPii) {
            return;
          }
          switch (level) {
            case LogLevel.Error:
              console.error(message);
              return;
            case LogLevel.Warning:
              console.warn(message);
              return;
            default:
              return; // silenciar Info/Verbose en produccion
          }
        },
      },
    },
  });
}

/**
 * Configuracion del MsalGuard: las rutas protegidas usan flujo de redireccion
 * (mas robusto que popup, evita bloqueadores de pop-ups) y piden de entrada
 * el scope de la API para poder consumir el BFF apenas el usuario inicia sesion.
 */
export function MSALGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: [apiScope],
    },
  };
}

/**
 * Configuracion del MsalInterceptor: mapea cada URL que empieza con apiBaseUrl
 * al scope de la API. El interceptor adjunta automaticamente el access token
 * (Bearer) a toda peticion HTTP hacia esas URLs, y renueva el token en silencio
 * (via refresh token / iframe oculto) cuando esta por expirar.
 */
export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string> | null>();
  protectedResourceMap.set(`${environment.apiBaseUrl}/*`, [apiScope]);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap,
  };
}
