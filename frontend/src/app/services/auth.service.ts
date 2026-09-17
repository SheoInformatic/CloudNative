import { Injectable } from "@angular/core";
import { MsalService } from "@azure/msal-angular";
import { AccountInfo, AuthenticationResult } from "@azure/msal-browser";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

/**
 * Envoltorio delgado sobre MsalService: centraliza login/logout y la lectura
 * de claims (nombre, correo, roles) para que los componentes no dependan
 * directamente de la forma exacta del token de Azure AD.
 */
@Injectable({ providedIn: "root" })
export class AuthService {
  constructor(private readonly msalService: MsalService) {}

  /**
   * Procesa la respuesta de Azure AD tras el redirect (el "#code=..." en la URL):
   * intercambia el codigo por los tokens y activa la cuenta. SIN esto, MSAL nunca
   * termina el login aunque Azure AD haya autenticado correctamente al usuario.
   * Debe llamarse una vez al arrancar la app (ver AppComponent.ngOnInit).
   */
  handleRedirect(): Observable<AuthenticationResult | null> {
    return this.msalService.handleRedirectObservable();
  }

  login(): void {
    this.msalService.loginRedirect({
      scopes: [environment.azureAd.apiScope],
    });
  }

  logout(): void {
    this.msalService.logoutRedirect({
      postLogoutRedirectUri: environment.azureAd.postLogoutRedirectUri,
    });
  }

  isAuthenticated(): boolean {
    return this.msalService.instance.getAllAccounts().length > 0;
  }

  getActiveAccount(): AccountInfo | null {
    const active = this.msalService.instance.getActiveAccount();
    if (active) {
      return active;
    }
    const accounts = this.msalService.instance.getAllAccounts();
    return accounts.length > 0 ? accounts[0] : null;
  }

  getDisplayName(): string {
    return this.getActiveAccount()?.name ?? this.getActiveAccount()?.username ?? "";
  }

  /**
   * Lee el claim "roles" (App Roles de Azure AD) desde el ID token de la cuenta activa.
   * Estos mismos roles tambien viajan en el access token que consume el BFF,
   * que es donde realmente se aplica la autorizacion.
   */
  getRoles(): string[] {
    const claims = this.getActiveAccount()?.idTokenClaims as Record<string, unknown> | undefined;
    const roles = claims?.["roles"];
    return Array.isArray(roles) ? (roles as string[]) : [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }
}