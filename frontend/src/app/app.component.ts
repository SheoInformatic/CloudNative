import { Component, OnDestroy, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink, RouterOutlet } from "@angular/router";
import { MsalBroadcastService } from "@azure/msal-angular";
import { InteractionStatus } from "@azure/msal-browser";
import { Subject } from "rxjs";
import { filter, takeUntil } from "rxjs/operators";
import { AuthService } from "./services/auth.service";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: "./app.component.html",
  styleUrl: "./app.component.css",
})
export class AppComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  displayName = "";
  roles: string[] = [];

  private readonly destroying$ = new Subject<void>();

  constructor(
    private readonly authService: AuthService,
    private readonly msalBroadcastService: MsalBroadcastService
  ) {}

  ngOnInit(): void {
    // PASO CLAVE: procesa el "#code=..." que Azure AD agrega a la URL tras el login,
    // lo intercambia por tokens y activa la cuenta. Sin esto, MsalGuard/Interceptor
    // nunca ven una sesion valida aunque el login en Azure AD haya sido exitoso.
    this.authService
      .handleRedirect()
      .pipe(takeUntil(this.destroying$))
      .subscribe({
        next: () => this.refreshAuthState(),
        error: (err) => console.error("Error procesando el redirect de MSAL:", err),
      });

    // InteractionStatus.None significa que MSAL termino de procesar login/logout
    // (incluyendo el retorno del redirect de Azure AD). Recien ahi es seguro
    // leer las cuentas y sus claims.
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this.destroying$)
      )
      .subscribe(() => {
        this.refreshAuthState();
      });
  }

  ngOnDestroy(): void {
    this.destroying$.next();
    this.destroying$.complete();
  }

  login(): void {
    this.authService.login();
  }

  logout(): void {
    this.authService.logout();
  }

  private refreshAuthState(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
    this.displayName = this.authService.getDisplayName();
    this.roles = this.authService.getRoles();
  }
}