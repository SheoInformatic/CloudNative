import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
  selector: "app-unauthorized",
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Acceso no autorizado</h1>
    <p>Tu cuenta esta autenticada correctamente, pero no tiene el rol necesario para ver esta seccion.</p>
    <a routerLink="/">Volver al inicio</a>
  `,
})
export class UnauthorizedComponent {}
