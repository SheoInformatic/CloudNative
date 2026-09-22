import { Routes } from "@angular/router";
import { MsalGuard } from "@azure/msal-angular";
import { roleGuard } from "./guards/role.guard";

export const routes: Routes = [
  {
    path: "",
    loadComponent: () => import("./pages/home/home.component").then((m) => m.HomeComponent),
  },
  {
    path: "pedidos",
    canActivate: [MsalGuard],
    loadComponent: () => import("./pages/pedidos/pedidos.component").then((m) => m.PedidosComponent),
  },
  {
    path: "productos",
    // MsalGuard primero exige sesion activa; roleGuard exige ademas el rol Admin.
    canActivate: [MsalGuard, roleGuard("Admin")],
    loadComponent: () => import("./pages/productos/productos.component").then((m) => m.ProductosComponent),
  },
  {
    path: "no-autorizado",
    loadComponent: () =>
      import("./pages/unauthorized/unauthorized.component").then((m) => m.UnauthorizedComponent),
  },
  { path: "**", redirectTo: "" },
];
