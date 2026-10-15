package com.isil.edu.pe.spedidos.exception;

/**
 * El pedido solicitado no existe. Se traduce a HTTP 404.
 */
public class PedidoNoEncontradoException extends PedidoException {

  public PedidoNoEncontradoException(Long pedidoId) {
    super("No existe un pedido con id " + pedidoId + ".");
  }
}
