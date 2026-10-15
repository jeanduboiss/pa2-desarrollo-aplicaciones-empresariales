package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de anulaciones. Recibe y retorna DTO y delega toda la lógica en el servicio.
 */
@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

  private final AnulacionPedidoService anulacionPedidoService;

  public AnulacionPedidoController(AnulacionPedidoService anulacionPedidoService) {
    this.anulacionPedidoService = anulacionPedidoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AnulacionPedidoResponse anular(@Valid @RequestBody AnulacionPedidoRequest request) {
    return AnulacionPedidoResponse.from(
        anulacionPedidoService.anularPedido(request.pedidoId(), request.motivo())
    );
  }

  @GetMapping
  public List<AnulacionPedidoResponse> listar() {
    return anulacionPedidoService.listarAnulaciones()
        .stream()
        .map(AnulacionPedidoResponse::from)
        .toList();
  }
}
