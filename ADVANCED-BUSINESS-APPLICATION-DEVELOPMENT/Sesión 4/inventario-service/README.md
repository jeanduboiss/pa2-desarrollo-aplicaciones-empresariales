# Inventario Service — Jakarta EE 11 + SQL Server Express 2025

Proyecto académico complementario de `sistema-pedidos-jsp` para la Sesión 4: **Integración de aplicativos web con componentes empresariales**.

## Stack

- Java 21
- Maven
- Jakarta EE 11
- WildFly 41
- Jakarta REST
- Jakarta Enterprise Beans (EJB)
- Jakarta Persistence 3.2
- Jakarta Transactions (JTA)
- Bean Validation
- Microsoft JDBC Driver for SQL Server 13.6
- SQL Server Express 2025

## Arquitectura

```text
Sistema de Pedidos / Postman
          |
          | HTTP + JSON
          v
ProductoResource (Jakarta REST)
          |
          | @EJB
          v
InventarioService (@Stateless)
          |
          | @PersistenceContext
          v
EntityManager / InventarioPU
          |
          | java:app/jdbc/InventarioDS
          v
SQL Server Express 2025
```

## Preparación rápida

1. Habilitar TCP/IP en SQL Server Express y configurar un puerto fijo (la guía usa 1433).
2. Habilitar autenticación SQL Server/Mixed Mode para el usuario académico.
3. Ejecutar `database/01_crear_inventario.sql` en SSMS.
4. Verificar JDK 21 y Maven.
5. Ejecutar:

```bash
mvn clean test
mvn clean wildfly:run
```

El plugin usa `-Djboss.socket.binding.port-offset=2`, por lo que WildFly escucha en HTTP **8082** y Management **9992**. Esto permite ejecutar a la vez el `sistema-pedidos` existente (HTTP 8081, Management 9990).

Abrir:

```text
http://localhost:8082/inventario-service/
```

API principal:

```text
GET  /inventario-service/api/inventario/productos
GET  /inventario-service/api/inventario/productos/1/stock
POST /inventario-service/api/inventario/productos/1/reservas
```

Body de reserva:

```json
{
  "cantidad": 2
}
```

También existen endpoints por `codigo` (`LAP-001`, `MON-001`, `TEC-001`) para mostrar un contrato menos acoplado a las claves internas de base de datos.

## Material de clase

- `docs/GUIA_IMPLEMENTACION_PASO_A_PASO.md`
- `docs/INTEGRACION_CON_SISTEMA_PEDIDOS.md`
- `requests/inventario-service.http`
- `openapi/inventario-service.yaml`
- `database/02_reset_stock_demo.sql`

## Nota académica de seguridad

`@DataSourceDefinition` contiene usuario/contraseña para reducir pasos durante el laboratorio. En producción las credenciales no deben almacenarse en código; deben externalizarse en un DataSource administrado por WildFly, Credential Store o un mecanismo de secretos. `trustServerCertificate=true` también se usa solo para el laboratorio local.

---

## PA2 — API de devolución de stock

Ampliación para la Práctica de Aprendizaje 2: devolución de stock, usada por `sistema-pedidos-springboot` para anular pedidos.

### Endpoint nuevo

| Método | Ruta | Respuestas |
|--------|------|-----------|
| `POST` | `/inventario-service/api/inventario/productos/codigo/{codigo}/devoluciones` | `200` ok · `400` cantidad ≤ 0 o ausente · `404` código inexistente · `409` conflicto de `@Version` |

Solicitud:

```json
{ "cantidad": 2 }
```

Respuesta `200`:

```json
{ "codigo": "LAP-001", "cantidadDevuelta": 2, "stockRestante": 10, "version": 3 }
```

Ejemplo ejecutable (con WildFly en el puerto 8082):

```bash
curl -i -X POST http://localhost:8082/inventario-service/api/inventario/productos/codigo/LAP-001/devoluciones \
  -H 'Content-Type: application/json' -d '{"cantidad":2}'
```

También en `requests/inventario-service.http` (casos válido, cero, negativo y código inexistente).

### Capas implicadas

```text
ProductoResource.devolverPorCodigo
   -> InventarioService.devolverStockPorCodigo   (@Stateless, @TransactionAttribute(REQUIRED))
      -> ProductoInventario.devolverStock(int)    (regla de dominio)
         -> JPA / @Version -> SQL Server
```

- DTO propios: `DevolucionStockRequest` y `DevolucionStockResponse` (no se reutiliza `ReservaStockRequest` para que el contrato de devoluciones evolucione solo).
- Validación en dos niveles: Bean Validation en el DTO (`@NotNull @Positive`) y regla de dominio en `devolverStock`.
- Incremento dentro de una transacción local de Inventario; `flush()` traduce un `OptimisticLockException` a `409`.

### Pruebas unitarias nuevas (PA2)

Clase `src/test/java/pe/edu/isil/inventario/domain/ProductoInventarioDevolucionTest.java`:

| Prueba | Verifica |
|--------|----------|
| `devolucionValidaIncrementaElStockExactamenteEnLaCantidadDevuelta` | 8 + 2 = 10 |
| `devolucionConCantidadCeroONegativaSeRechazaYNoCambiaElStock` | 0 y -3 lanzan `IllegalArgumentException`; el stock sigue en 8 |
| `reservarYLuegoDevolverMismaCantidadRestauraElStockInicial` | prueba adicional: reservar 2 y devolver 2 restaura 10 |

```bash
mvn test        # no requiere WildFly ni SQL Server
```

### Orden de arranque y limitaciones

Orden recomendado: SQL Server → WildFly (`mvn clean wildfly:run`) → `sistema-pedidos-springboot`.

- La devolución no es idempotente: repetir la misma solicitud suma otra vez el stock. La protección contra duplicados vive en `sistema-pedidos-springboot` (estado + `UNIQUE`).
- No hay un tope máximo de stock; solo se valida que la cantidad sea positiva y que el total no desborde `int`.
- Configuración sin secretos reales: `config/database.properties` contiene credenciales de laboratorio; en otro entorno sustitúyalas por variables o un archivo no versionado.
