package com.isil.edu.pe.spedidos.service;

/**
 * Compensación de una reserva remota confirmada cuando la escritura local posterior falla.
 */
public interface CompensacionStockService {

  /**
   * Devuelve a Inventario el stock reservado. Si la propia devolución falla, registra un incidente
   * pendiente de conciliación.
   *
   * @return {@code true} si el stock fue restituido; {@code false} si quedó un incidente pendiente
   */
  boolean compensarReserva(String productoCodigo, int cantidad, String causa);

}
