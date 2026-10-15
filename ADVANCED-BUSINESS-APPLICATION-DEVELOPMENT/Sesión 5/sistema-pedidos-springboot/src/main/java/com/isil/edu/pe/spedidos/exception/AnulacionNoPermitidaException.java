package com.isil.edu.pe.spedidos.exception;

/**
 * La anulación entra en conflicto con el estado actual del pedido (por ejemplo, ya fue anulado).
 * Se traduce a HTTP 409.
 */
public class AnulacionNoPermitidaException extends PedidoException {

  public AnulacionNoPermitidaException(String message) {
    super(message);
  }
}
