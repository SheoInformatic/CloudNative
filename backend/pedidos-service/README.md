# pedidos-service

Microservicio Spring Boot (Java 21) que gestiona los pedidos de **Pedidos360**.
Al crear un pedido consulta a `productos-service` (via `WebClient`) para validar
existencia, precio y stock de cada item.

Al igual que `productos-service`, no valida JWT: solo es alcanzable desde `bff-service`
por red interna. Confia en las cabeceras `X-User-Id` / `X-User-Email` que el BFF agrega
una vez que valido y autorizo la peticion.

## Variables de entorno

| Variable                | Descripcion                                   | Default                  |
|--------------------------|-----------------------------------------------|---------------------------|
| `PRODUCTOS_SERVICE_URL`  | URL interna de productos-service              | `http://localhost:8081`  |

## Endpoints

| Metodo | Ruta                          | Descripcion                          |
|--------|-------------------------------|----------------------------------------|
| GET    | /api/v1/pedidos                | Lista pedidos (propios si hay X-User-Id) |
| GET    | /api/v1/pedidos/{id}           | Obtiene un pedido                     |
| POST   | /api/v1/pedidos                | Crea un pedido                        |
| PATCH  | /api/v1/pedidos/{id}/estado    | Cambia el estado de un pedido         |
| GET    | /actuator/health                | Health check                          |

## Ejecutar localmente

```bash
mvn spring-boot:run
```
