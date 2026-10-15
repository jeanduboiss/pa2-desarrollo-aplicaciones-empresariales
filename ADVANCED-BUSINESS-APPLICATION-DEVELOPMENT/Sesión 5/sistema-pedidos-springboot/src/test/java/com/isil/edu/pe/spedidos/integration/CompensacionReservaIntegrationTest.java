package com.isil.edu.pe.spedidos.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.PedidoService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * RETO PA2: fallo local posterior a una reserva remota confirmada.
 *
 * <p>Escenario: Inventario inicia con 10 unidades y confirma una reserva de 2 (stock 8). El INSERT
 * en H2 falla de verdad (nombre de cliente de 200 caracteres sobre una columna de 120), antes del
 * commit. La compensación debe restituir el stock a 10 y no debe quedar pedido en H2.</p>
 */
@SpringBootTest
@Import(IntegracionH2Base.class)
class CompensacionReservaIntegrationTest {

  private static final String CLIENTE_INVALIDO = "C".repeat(200);

  @Autowired
  private PedidoService pedidoService;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private IncidenteConciliacionRepository incidenteRepository;

  @Autowired
  private InventarioFalso inventario;

  @BeforeEach
  void limpiar() {
    pedidoRepository.deleteAll();
    incidenteRepository.deleteAll();
    inventario.reiniciar(10);
  }

  @Test
  void fallaLocalTrasReservaConfirmadaSeCompensaYElPedidoNoSeGuarda() {
    System.out.printf("ANTES    -> Inventario stock=%d | H2 pedidos=%d%n",
        inventario.stock(), pedidoRepository.count());

    assertThrows(RuntimeException.class,
        () -> pedidoService.registrarPedido(CLIENTE_INVALIDO, "LAP-001", 2));

    System.out.printf("DESPUÉS  -> Inventario stock=%d | H2 pedidos=%d | llamadas=%s%n",
        inventario.stock(), pedidoRepository.count(), inventario.llamadas());

    assertEquals(10, inventario.stock(), "la compensación debe restituir el stock a 10");
    assertEquals(0, pedidoRepository.count(), "no debe existir pedido confirmado en H2");
    assertEquals(List.of("RESERVAR LAP-001 2", "DEVOLVER LAP-001 2"), inventario.llamadas());
    assertEquals(0, incidenteRepository.count());
  }

  @Test
  void siLaCompensacionFallaSeRegistraIncidenteQueSobreviveAlRollback() {
    inventario.devolucionFalla(true);

    assertThrows(RuntimeException.class,
        () -> pedidoService.registrarPedido(CLIENTE_INVALIDO, "LAP-001", 2));

    System.out.printf("COMPENSACIÓN FALLIDA -> Inventario stock=%d | H2 pedidos=%d | incidentes=%d%n",
        inventario.stock(), pedidoRepository.count(), incidenteRepository.count());

    assertEquals(8, inventario.stock(), "el stock sigue descontado: requiere conciliación");
    assertEquals(0, pedidoRepository.count());
    assertEquals(1, incidenteRepository.count(), "incidente pendiente persistido (REQUIRES_NEW)");
    assertFalse(incidenteRepository.findAll().getFirst().isResuelto());
    // Un único intento de devolución: no hay reintento del POST.
    assertEquals(List.of("RESERVAR LAP-001 2", "DEVOLVER LAP-001 2"), inventario.llamadas());
  }

  @Test
  void pedidoValidoSeRegistraYNoDisparaCompensacion() {
    var pedido = pedidoService.registrarPedido("Ana", "LAP-001", 2);

    assertEquals(8, inventario.stock());
    assertEquals(1, pedidoRepository.count());
    assertEquals("REGISTRADO", pedido.getEstado().name());
    assertEquals(List.of("RESERVAR LAP-001 2"), inventario.llamadas());
  }
}
