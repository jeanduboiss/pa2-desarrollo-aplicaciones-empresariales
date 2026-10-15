package com.isil.edu.pe.spedidos.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/v1/anulaciones}.
 */
public record AnulacionPedidoRequest(
    @NotNull(message = "El pedidoId es obligatorio.")
    Long pedidoId,

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres.")
    String motivo
) {
}
