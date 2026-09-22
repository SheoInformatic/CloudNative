import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Producto, ProductosService } from "../../services/productos.service";

/**
 * Ruta protegida por MsalGuard + roleGuard("Admin") en app.routes.ts.
 * Aun asi, el BFF vuelve a exigir el rol Admin para crear/eliminar (defensa en profundidad):
 * si alguien llega aqui con un token manipulado o sin el rol, el BFF respondera 403.
 */
@Component({
  selector: "app-productos",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./productos.component.html",
})
export class ProductosComponent implements OnInit {
  productos: Producto[] = [];
  cargando = false;
  error = "";

  nuevoSku = "";
  nuevoNombre = "";
  nuevoPrecio = 0;
  nuevoStock = 0;

  constructor(private readonly productosService: ProductosService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.productosService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
      },
      error: (err) => {
        this.error = `Error ${err.status}: ${err.error?.message ?? err.message}`;
        this.cargando = false;
      },
    });
  }

  crear(): void {
    this.productosService
      .crear({
        sku: this.nuevoSku,
        nombre: this.nuevoNombre,
        descripcion: "",
        precio: this.nuevoPrecio,
        stock: this.nuevoStock,
      })
      .subscribe({
        next: () => {
          this.nuevoSku = "";
          this.nuevoNombre = "";
          this.nuevoPrecio = 0;
          this.nuevoStock = 0;
          this.cargar();
        },
        error: (err) => (this.error = `Error ${err.status}: ${err.error?.message ?? err.message}`),
      });
  }

  eliminar(id: number): void {
    this.productosService.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => (this.error = `Error ${err.status}: ${err.error?.message ?? err.message}`),
    });
  }
}
