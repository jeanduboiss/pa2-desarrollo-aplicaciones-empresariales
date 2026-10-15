package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.controller.dto.ApiErrorResponse;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce excepciones a respuestas HTTP coherentes solo para {@link AnulacionPedidoController}.
 *
 * <p>404 pedido inexistente, 409 anulación repetida, 400 solicitud inválida, 502 Inventario
 * respondió con error, 503 Inventario no disponible (conexión o timeout).</p>
 */
@RestControllerAdvice(assignableTypes = AnulacionPedidoController.class)
public class AnulacionPedidoExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(AnulacionPedidoExceptionHandler.class);

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ResponseEntity<ApiErrorResponse> pedidoNoEncontrado(PedidoNoEncontradoException e) {
    return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({AnulacionNoPermitidaException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ApiErrorResponse> conflicto(RuntimeException e) {
    // Una violación del UNIQUE de pedido_id equivale a una anulación repetida concurrente.
    String mensaje = e instanceof AnulacionNoPermitidaException
        ? e.getMessage()
        : "El pedido ya tiene una anulación registrada.";
    return respuesta(HttpStatus.CONFLICT, mensaje);
  }

  @ExceptionHandler(PedidoException.class)
  public ResponseEntity<ApiErrorResponse> solicitudInvalida(PedidoException e) {
    return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> validacion(MethodArgumentNotValidException e) {
    String mensaje = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getDefaultMessage())
        .sorted()
        .collect(Collectors.joining(" "));
    return respuesta(HttpStatus.BAD_REQUEST, mensaje);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> jsonInvalido(HttpMessageNotReadableException e) {
    return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo JSON de la solicitud es inválido.");
  }

  @ExceptionHandler(InventarioNoDisponibleException.class)
  public ResponseEntity<ApiErrorResponse> inventarioNoDisponible(InventarioNoDisponibleException e) {
    log.warn("Inventario no disponible durante la anulación: {}", e.getMessage());
    return respuesta(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
  }

  @ExceptionHandler(InventarioClientException.class)
  public ResponseEntity<ApiErrorResponse> inventarioRechazo(InventarioClientException e) {
    log.warn("Inventario rechazó la devolución (HTTP {}): {}", e.getStatus(), e.getMessage());
    return respuesta(HttpStatus.BAD_GATEWAY, "Inventario rechazó la devolución: " + e.getMessage());
  }

  private static ResponseEntity<ApiErrorResponse> respuesta(HttpStatus status, String mensaje) {
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), mensaje));
  }
}
