import { HttpClient } from "@angular/common/http";
import { CommonModule } from "@angular/common";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { MsalBroadcastService } from "@azure/msal-angular";
import { InteractionStatus } from "@azure/msal-browser";
import { Subject } from "rxjs";
import { filter, takeUntil } from "rxjs/operators";
import { environment } from "../../../environments/environment";
import { AuthService } from "../../services/auth.service";

interface MeResponse {
  userId: string;
  email: string;
  name: string;
  roles: string[];
}

@Component({
  selector: "app-home",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./home.component.html",
})
export class HomeComponent implements OnInit, OnDestroy {
  me: MeResponse | null = null;
  error = "";

  private readonly destroying$ = new Subject<void>();

  constructor(
    private readonly http: HttpClient,
    public readonly authService: AuthService,
    private readonly msalBroadcastService: MsalBroadcastService
  ) {}

  ngOnInit(): void {
    // No basta con revisar isAuthenticated() una sola vez aqui: este componente
    // (ruta "/", sin MsalGuard) puede montarse ANTES de que MSAL termine de
    // procesar el redirect de Azure AD. Por eso esperamos a que inProgress$
    // llegue a "None" (login/logout terminado) antes de decidir si pedimos /api/me.
    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this.destroying$)
      )
      .subscribe(() => {
        if (this.authService.isAuthenticated() && !this.me) {
          this.cargarMe();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroying$.next();
    this.destroying$.complete();
  }

  private cargarMe(): void {
    this.http.get<MeResponse>(`${environment.apiBaseUrl}/api/me`).subscribe({
      next: (data) => (this.me = data),
      error: (err) => (this.error = `No se pudo consumir el BFF: ${err.status ?? ""} ${err.message ?? ""}`),
    });
  }
}