import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface Producto {
  id: number;
  sku: string;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
}

export interface ProductoRequest {
  sku: string;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
}

@Injectable({ providedIn: "root" })
export class ProductosService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/productos`;

  constructor(private readonly http: HttpClient) {}

  listar(q?: string): Observable<Producto[]> {
    let params = new HttpParams();
    if (q) {
      params = params.set("q", q);
    }
    return this.http.get<Producto[]>(this.baseUrl, { params });
  }

  crear(request: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
