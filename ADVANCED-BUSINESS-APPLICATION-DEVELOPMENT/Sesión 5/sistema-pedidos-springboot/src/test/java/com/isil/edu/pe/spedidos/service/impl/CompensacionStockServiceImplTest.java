package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.IncidenteConciliacion;
import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del reto de compensación: devolución efectiva e incidente si la compensación falla.
 */
class CompensacionStockServiceImplTest {

  private DevolucionStockClient devolucionStockClient;
  private IncidenteConciliacionRepository incidenteRepository;
  private CompensacionStockServiceImpl service;

  @BeforeEach
  void setUp() {
    devolucionStockClient = mock(DevolucionStockClient.class);
    incidenteRepository = mock(IncidenteConciliacionRepository.class);
    service = new CompensacionStockServiceImpl(devolucionStockClient, incidenteRepository);
  }

  @Test
  void compensacionExitosaDevuelveElStockSinRegistrarIncidente() {
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));

    boolean compensado = service.compensarReserva("LAP-001", 2, "fallo local");

    assertTrue(compensado);
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    verify(incidenteRepository, never()).save(any(IncidenteConciliacion.class));
  }

  @Test
  void siLaCompensacionFallaSeRegistraIncidentePendienteYNoSeReintenta() {
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("caído"));

    boolean compensado = service.compensarReserva("LAP-001", 2, "fallo local");

    assertFalse(compensado);
    // Exactamente un intento: un POST sin idempotencia no se reintenta.
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    verify(incidenteRepository).save(any(IncidenteConciliacion.class));
  }
}
