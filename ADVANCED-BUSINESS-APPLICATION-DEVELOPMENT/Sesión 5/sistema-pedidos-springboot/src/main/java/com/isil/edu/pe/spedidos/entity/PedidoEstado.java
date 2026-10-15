package com.isil.edu.pe.spedidos.entity;

/**
 * Estado del ciclo de vida de un pedido. Un pedido anulado no se elimina de la base local.
 */
public enum PedidoEstado {
  REGISTRADO,
  ANULADO
}
