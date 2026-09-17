import { HttpClient } from "@angular/common/http";
import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
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
export class HomeComponent implements OnInit {
  me: MeResponse | null = null;
  error = "";

  constructor(private readonly http: HttpClient, public readonly authService: AuthService) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.cargarMe();
    }
  }

  private cargarMe(): void {
    this.http.get<MeResponse>(`${environment.apiBaseUrl}/api/me`).subscribe({
      next: (data) => (this.me = data),
      error: (err) => (this.error = `No se pudo consumir el BFF: ${err.status ?? ""} ${err.message ?? ""}`),
    });
  }
}
