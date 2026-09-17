import { APP_INITIALIZER, ApplicationConfig } from "@angular/core";
import { provideRouter } from "@angular/router";
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import { HTTP_INTERCEPTORS } from "@angular/common/http";
import {
  MSAL_GUARD_CONFIG,
  MSAL_INSTANCE,
  MSAL_INTERCEPTOR_CONFIG,
  MsalBroadcastService,
  MsalGuard,
  MsalInterceptor,
  MsalService,
} from "@azure/msal-angular";

/**
 * MSAL (msal-browser v3) exige llamar a instance.initialize() antes de usar
 * loginRedirect/loginPopup, guards o el interceptor. Usamos APP_INITIALIZER
 * para garantizar que esto ocurra antes de que arranque el resto de la app.
 */
export function initializeMsal(msalService: MsalService) {
  return () => msalService.instance.initialize();
}

import { routes } from "./app.routes";
import {
  MSALGuardConfigFactory,
  MSALInstanceFactory,
  MSALInterceptorConfigFactory,
} from "./auth-config";

/**
 * Registro central de MSAL para una app standalone de Angular 18.
 * Este es el equivalente standalone al MsalModule.forRoot(...) que se usa
 * en apps basadas en NgModule.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),

    { provide: MSAL_INSTANCE, useFactory: MSALInstanceFactory },
    { provide: MSAL_GUARD_CONFIG, useFactory: MSALGuardConfigFactory },
    { provide: MSAL_INTERCEPTOR_CONFIG, useFactory: MSALInterceptorConfigFactory },

    // Adjunta el access token a cada llamada HTTP que matchee protectedResourceMap.
    { provide: HTTP_INTERCEPTORS, useClass: MsalInterceptor, multi: true },

    MsalService,
    MsalGuard,
    MsalBroadcastService,

    {
      provide: APP_INITIALIZER,
      useFactory: initializeMsal,
      deps: [MsalService],
      multi: true,
    },
  ],
};
