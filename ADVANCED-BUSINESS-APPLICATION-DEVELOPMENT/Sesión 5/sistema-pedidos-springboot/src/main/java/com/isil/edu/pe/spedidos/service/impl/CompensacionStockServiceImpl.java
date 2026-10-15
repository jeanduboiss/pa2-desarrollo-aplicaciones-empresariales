package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.IncidenteConciliacion;
import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import com.isil.edu.pe.spedidos.service.CompensacionStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa la compensación de reservas con la API REST de devolución de stock.
 *
 * <p>Se ejecuta en una transacción nueva ({@code REQUIRES_NEW}): la transacción del pedido se
 * revertirá, y el incidente de conciliación debe sobrevivir a ese rollback.</p>
 *
 * <p>La devolución se intenta una sola vez. No se reintenta automáticamente porque el POST no es
 * idempotente: si la respuesta se perdió, un segundo intento podría devolver el stock dos veces.</p>
 */
@Service
public class CompensacionStockServiceImpl implements CompensacionStockService {

  private static final Logger log = LoggerFactory.getLogger(CompensacionStockServiceImpl.class);

  private final DevolucionStockClient devolucionStockClient;
  private final IncidenteConciliacionRepository incidenteRepository;

  public CompensacionStockServiceImpl(
      DevolucionStockClient devolucionStockClient,
      IncidenteConciliacionRepository incidenteRepository) {
    this.devolucionStockClient = devolucionStockClient;
    this.incidenteRepository = incidenteRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean compensarReserva(String productoCodigo, int cantidad, String causa) {
    try {
      DevolucionStockResponse respuesta = devolucionStockClient.devolverStock(productoCodigo, cantidad);
      log.info("Compensación aplicada: {} +{} (stock restante {}). Causa: {}",
          productoCodigo, cantidad, respuesta.getStockRestante(), causa);
      return true;
    } catch (RuntimeException e) {
      log.error("La compensación falló para {} x{}. Se registra incidente pendiente.",
          productoCodigo, cantidad, e);
      incidenteRepository.save(new IncidenteConciliacion(
          productoCodigo,
          cantidad,
          "Reserva confirmada sin pedido local. Causa: " + causa
              + ". Fallo de compensación: " + e.getMessage()
      ));
      return false;
    }
  }
}
