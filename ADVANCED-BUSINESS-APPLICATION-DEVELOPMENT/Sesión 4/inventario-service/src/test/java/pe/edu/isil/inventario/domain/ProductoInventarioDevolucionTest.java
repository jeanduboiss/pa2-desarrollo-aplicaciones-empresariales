package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Pruebas PA2 de la operación de dominio {@link ProductoInventario#devolverStock(int)}.
 * No requieren WildFly ni SQL Server.
 */
class ProductoInventarioDevolucionTest {

  private static ProductoInventario productoConStock(int stock) {
    return new ProductoInventario("LAP-001", "Laptop", new BigDecimal("2500.00"), stock);
  }

  @Test
  void devolucionValidaIncrementaElStockExactamenteEnLaCantidadDevuelta() {
    // Preparación
    ProductoInventario producto = productoConStock(8);

    // Ejecución
    producto.devolverStock(2);

    // Verificación: 8 + 2 = 10
    assertEquals(10, producto.getStock());
  }

  @Test
  void devolucionConCantidadCeroONegativaSeRechazaYNoCambiaElStock() {
    // Preparación
    ProductoInventario producto = productoConStock(8);

    // Ejecución + verificación
    assertThrows(IllegalArgumentException.class, () -> producto.devolverStock(0));
    assertThrows(IllegalArgumentException.class, () -> producto.devolverStock(-3));
    assertEquals(8, producto.getStock());
  }

  @Test
  void reservarYLuegoDevolverMismaCantidadRestauraElStockInicial() {
    ProductoInventario producto = productoConStock(10);

    producto.reservarStock(2);
    assertEquals(8, producto.getStock());
    producto.devolverStock(2);

    assertEquals(10, producto.getStock());
  }
}
