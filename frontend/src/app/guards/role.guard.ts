import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";

/**
 * Guard funcional (Angular 15+) que se ejecuta DESPUES de MsalGuard.
 * MsalGuard ya garantiza que hay sesion activa; este guard adicional
 * verifica el rol leido desde los claims del ID token y redirige a
 * /no-autorizado si el usuario no tiene el rol requerido para la ruta.
 *
 * Uso en app.routes.ts:
 *   canActivate: [MsalGuard, roleGuard("Admin")]
 */
export function roleGuard(requiredRole: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasRole(requiredRole)) {
      return true;
    }

    return router.parseUrl("/no-autorizado");
  };
}
