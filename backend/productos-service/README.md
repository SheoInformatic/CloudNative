# productos-service

Microservicio Spring Boot (Java 21) que expone el catalogo de productos de **Pedidos360**.

Este servicio **no valida tokens JWT directamente**: en la arquitectura del proyecto solo es
alcanzable desde el `bff-service` a traves de red interna (Azure Container Apps environment),
por lo que la validacion de identidad ocurre una unica vez, en el BFF y en Azure API Management.

## Endpoints

| Metodo | Ruta                     | Descripcion                    |
|--------|--------------------------|---------------------------------|
| GET    | /api/v1/productos        | Lista productos (filtro `?q=`) |
| GET    | /api/v1/productos/{id}   | Obtiene un producto             |
| POST   | /api/v1/productos        | Crea un producto                |
| PUT    | /api/v1/productos/{id}   | Actualiza un producto           |
| DELETE | /api/v1/productos/{id}   | Elimina un producto             |
| GET    | /actuator/health         | Health check (Container Apps)   |

## Ejecutar localmente

```bash
mvn spring-boot:run
```

El servicio queda disponible en `http://localhost:8081`.

## Docker

```bash
docker build -t pedidos360/productos-service:1.0.0 .
docker run -p 8081:8081 pedidos360/productos-service:1.0.0
```
