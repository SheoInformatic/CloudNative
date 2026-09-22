import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Pedido, PedidosService } from "../../services/pedidos.service";

@Component({
  selector: "app-pedidos",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./pedidos.component.html",
})
export class PedidosComponent implements OnInit {
  pedidos: Pedido[] = [];
  cargando = false;
  error = "";

  nuevoProductoId = 1;
  nuevaCantidad = 1;

  constructor(private readonly pedidosService: PedidosService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.pedidosService.listar().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
      },
      error: (err) => {
        this.error = `Error ${err.status}: ${err.error?.message ?? err.message}`;
        this.cargando = false;
      },
    });
  }

  crearPedido(): void {
    this.pedidosService
      .crear({ items: [{ productoId: this.nuevoProductoId, cantidad: this.nuevaCantidad }] })
      .subscribe({
        next: () => this.cargar(),
        error: (err) => (this.error = `Error ${err.status}: ${err.error?.message ?? err.message}`),
      });
  }
}
