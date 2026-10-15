package com.isil.edu.pe.spedidos.controller.dto;

import java.time.OffsetDateTime;

/**
 * Cuerpo JSON de error de la API de anulaciones.
 */
public record ApiErrorResponse(int status, String error, String message, String timestamp) {

  public ApiErrorResponse(int status, String error, String message) {
    this(status, error, message, OffsetDateTime.now().toString());
  }
}
