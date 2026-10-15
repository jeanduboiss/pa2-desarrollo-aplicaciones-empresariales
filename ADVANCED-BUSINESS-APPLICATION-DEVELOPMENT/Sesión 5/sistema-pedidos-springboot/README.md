# sistema-pedidos-springboot — PA2: anulación de pedidos

Spring Boot 4 / Spring MVC / Spring Data JPA / H2 (puerto 8081, contexto `/sistema-pedidos`). Consume `inventario-service` (Jakarta EE, WildFly, SQL Server, puerto 8082).

## Requisitos

- JDK 21 y Maven 3.9+
- `inventario-service` desplegado (ver su README) con la API `POST .../devoluciones`

## Orden de arranque

1. SQL Server (base `InventarioDB`, script `database/01_crear_inventario.sql` del otro proyecto)
2. WildFly con `inventario-service`: `mvn clean wildfly:run`
3. Este proyecto: `mvn spring-boot:run`

## Configuración (sin secretos)

`src/main/resources/application.properties`:

```properties
inventario.api.base-url=http://localhost:8082/inventario-service/api/inventario
inventario.api.connect-timeout-ms=2000
inventario.api.read-timeout-ms=3000
```

La URL base está centralizada ahí y la usan `InventarioRestClient` y `DevolucionStockRestClient`. H2 es en memoria con credenciales de laboratorio; la base de Inventario nunca se toca desde este proyecto.

## Endpoints nuevos

| Método | Ruta | Resultado |
|--------|------|-----------|
| `POST` | `/sistema-pedidos/api/v1/anulaciones` | `201` anulación creada · `400` solicitud inválida · `404` pedido inexistente · `409` ya anulado · `502` Inventario respondió con error · `503` Inventario no disponible / timeout |
| `GET` | `/sistema-pedidos/api/v1/anulaciones` | `200` lista de anulaciones (más reciente primero) |
| `GET` | `/sistema-pedidos/api/v1/orders` | Existente; ahora incluye el campo `estado` |

Solicitud:

```json
{ "pedidoId": 1, "motivo": "Solicitud del cliente" }
```

Respuesta `201`:

```json
{ "id": 1, "pedidoId": 1, "productoCodigo": "LAP-001", "cantidadDevuelta": 2,
  "fecha": "2026-10-15T15:31:59.772719", "motivo": "Solicitud del cliente" }
```

Ejemplos listos para ejecutar en `requests/anulaciones.http` (IntelliJ / VS Code REST Client).

## Capas y clases nuevas

```text
AnulacionPedidoController   (@RestController, DTO in/out)
  -> AnulacionPedidoService / AnulacionPedidoServiceImpl
       -> PedidoRepository, AnulacionPedidoRepository -> H2
       -> DevolucionStockClient / DevolucionStockRestClient -> HTTP+JSON -> ProductoResource (WildFly)
```

- Entidades: `AnulacionPedido` (UNIQUE sobre `pedido_id`), `PedidoEstado` (`REGISTRADO`/`ANULADO`) y `Pedido.estado` (inicial `REGISTRADO`).
- Constructor Injection en todas las clases (sin `new` de colaboradores ni `@Autowired` de campo).
- `AnulacionPedidoExceptionHandler` traduce excepciones a JSON con el código HTTP correcto.
- Orden del servicio: validar → llamar a Inventario → escribir en H2. Si Inventario falla, no hay escritura local.

## Reto de compensación (+3)

`PedidoServiceImpl.registrarPedido` compensa una reserva ya confirmada si falla algo local antes del commit:

`reservar (stock 10→8)` → fallo local → `CompensacionStockService` llama a `devoluciones` (8→10) → se relanza la excepción → rollback de H2.

Si la compensación también falla, `CompensacionStockServiceImpl` (transacción `REQUIRES_NEW`) guarda un `IncidenteConciliacion` pendiente que sobrevive al rollback. Demostración reproducible: `CompensacionReservaIntegrationTest` (falla real de columna: cliente de 200 caracteres sobre `varchar(120)`; sin endpoints ni controles inseguros).

## Pruebas

```bash
mvn test
```

Pruebas unitarias PA2 (Mockito, sin arrancar Spring ni Inventario) — `AnulacionPedidoServiceImplTest`:

| Prueba | Verifica |
|--------|----------|
| `anulacionCorrectaInvocaUnaDevolucionMarcaAnuladoYGuardaElRegistro` | 1 sola devolución, estado `ANULADO`, registro guardado |
| `anulacionDuplicadaSeRechazaSinNuevaDevolucionNiRegistroAdicional` | `never()` devolver / guardar |
| `falloDeInventarioNoConfirmaCambiosLocalesNiRegistraAnulacion` | excepción del cliente → `never()` guardar, pedido sigue `REGISTRADO` |
| `pedidoInexistenteSeRechazaSinTocarInventarioNiRegistrarAnulacion` | adicional |

Otras pruebas nuevas: `CompensacionStockServiceImplTest` (2), `AnulacionPedidoIntegrationTest` (4, H2 y transacciones reales con Inventario simulado) y `CompensacionReservaIntegrationTest` (3). Total del proyecto: 18, todas en verde.

Las pruebas con mocks validan la lógica del servicio; no demuestran por sí solas un rollback real. Eso lo cubren las pruebas de integración y los 5 escenarios manuales contra el stack real.

## Limitaciones

- La devolución remota y la escritura local NO son una transacción distribuida. Si Inventario confirma y H2 falla justo al anular, el stock queda devuelto y el pedido en `REGISTRADO`; un reintento devolvería el stock dos veces.
- `UNIQUE` y el control de estado cubren el flujo secuencial, no solicitudes concurrentes entre sistemas.
- No hay reintentos automáticos de POST (no son idempotentes). Un timeout deja un resultado remoto incierto; no se concilia automáticamente.
- La compensación cubre fallos hasta el `flush`; un error exclusivo del commit no se intercepta dentro del método.
