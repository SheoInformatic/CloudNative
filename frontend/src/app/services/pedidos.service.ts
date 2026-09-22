import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface ItemPedidoRequest {
  productoId: number;
  cantidad: number;
}

export interface PedidoRequest {
  items: ItemPedidoRequest[];
}

export interface Pedido {
  id: number;
  clienteId: string;
  clienteEmail: string;
  items: Array<{ productoId: number; nombreProducto: string; cantidad: number; precioUnitario: number }>;
  estado: string;
  creadoEn: string;
  total?: number;
}

/**
 * Todas las llamadas van a environment.apiBaseUrl (Azure API Management -> BFF).
 * El MsalInterceptor (registrado en app.config.ts) adjunta el Bearer token
 * automaticamente porque la URL matchea el protectedResourceMap.
 */
@Injectable({ providedIn: "root" })
export class PedidosService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/pedidos`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseUrl);
  }

  obtener(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.baseUrl}/${id}`);
  }

  crear(request: PedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.baseUrl, request);
  }

  cambiarEstado(id: number, nuevoEstado: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.baseUrl}/${id}/estado`, JSON.stringify(nuevoEstado));
  }
}
