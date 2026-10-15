package com.isil.edu.pe.spedidos.integration;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventario remoto simulado con estado (una "base" de stock en memoria) para observar el antes y el
 * después del stock mientras H2 es real. Solo se usa en pruebas.
 */
class InventarioFalso implements InventarioClient, DevolucionStockClient {

  private int stock = 10;
  private boolean caido;
  private boolean devolucionFalla;
  private final List<String> llamadas = new ArrayList<>();

  void reiniciar(int stockInicial) {
    stock = stockInicial;
    caido = false;
    devolucionFalla = false;
    llamadas.clear();
  }

  int stock() {
    return stock;
  }

  List<String> llamadas() {
    return llamadas;
  }

  void caido(boolean caido) {
    this.caido = caido;
  }

  void devolucionFalla(boolean devolucionFalla) {
    this.devolucionFalla = devolucionFalla;
  }

  @Override
  public List<ProductoInventarioResponse> listarProductos() {
    return List.of();
  }

  @Override
  public ReservaStockResponse reservarStock(String codigo, int cantidad) {
    llamadas.add("RESERVAR " + codigo + " " + cantidad);
    if (caido) {
      throw new InventarioNoDisponibleException("Inventario caído (simulado).");
    }
    if (stock < cantidad) {
      throw new InventarioClientException(409, "Stock insuficiente.");
    }
    stock -= cantidad;
    ReservaStockResponse respuesta = new ReservaStockResponse();
    respuesta.setCodigo(codigo);
    respuesta.setNombre("Laptop");
    respuesta.setPrecio(new BigDecimal("2500.00"));
    respuesta.setCantidadReservada(cantidad);
    respuesta.setStockRestante(stock);
    return respuesta;
  }

  @Override
  public DevolucionStockResponse devolverStock(String codigo, int cantidad) {
    llamadas.add("DEVOLVER " + codigo + " " + cantidad);
    if (caido || devolucionFalla) {
      throw new InventarioNoDisponibleException("Inventario caído (simulado).");
    }
    stock += cantidad;
    return new DevolucionStockResponse(codigo, cantidad, stock);
  }
}
