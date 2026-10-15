package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

/**
 * Contrato para devolver stock a {@code inventario-service}.
 *
 * <p>La capa de negocio depende de esta interfaz y no de los detalles HTTP, lo que permite
 * sustituir la implementación por un mock en las pruebas unitarias.</p>
 */
public interface DevolucionStockClient {

  /**
   * Solicita la devolución de {@code cantidad} unidades del producto {@code codigo}.
   *
   * @throws InventarioClientException si Inventario responde 4xx o 5xx
   * @throws InventarioNoDisponibleException si no hay conexión o se supera el timeout
   */
  DevolucionStockResponse devolverStock(String codigo, int cantidad);

}
