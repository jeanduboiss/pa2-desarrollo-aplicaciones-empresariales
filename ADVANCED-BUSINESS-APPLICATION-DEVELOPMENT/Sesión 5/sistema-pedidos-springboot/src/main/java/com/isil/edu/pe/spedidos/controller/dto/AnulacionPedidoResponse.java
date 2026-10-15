package com.isil.edu.pe.spedidos.controller.dto;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.time.LocalDateTime;

public record AnulacionPedidoResponse(
    Long id,
    Long pedidoId,
    String productoCodigo,
    int cantidadDevuelta,
    LocalDateTime fecha,
    String motivo
) {

  public static AnulacionPedidoResponse from(AnulacionPedido anulacion) {
    return new AnulacionPedidoResponse(
        anulacion.getId(),
        anulacion.getPedidoId(),
        anulacion.getProductoCodigo(),
        anulacion.getCantidadDevuelta(),
        anulacion.getFecha(),
        anulacion.getMotivo()
    );
  }
}
