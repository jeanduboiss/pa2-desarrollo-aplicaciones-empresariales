package com.isil.edu.pe.spedidos.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import com.isil.edu.pe.spedidos.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Flujo de anulación con H2 y transacciones reales y un Inventario remoto simulado con estado.
 * Complementa (no sustituye) las pruebas manuales contra inventario-service real.
 */
@SpringBootTest
@Import(IntegracionH2Base.class)
class AnulacionPedidoIntegrationTest {

  @Autowired
  private PedidoService pedidoService;

  @Autowired
  private AnulacionPedidoService anulacionService;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private AnulacionPedidoRepository anulacionRepository;

  @Autowired
  private InventarioFalso inventario;

  @BeforeEach
  void limpiar() {
    anulacionRepository.deleteAll();
    pedidoRepository.deleteAll();
    inventario.reiniciar(10);
  }

  @Test
  void anulacionCorrectaDevuelveStockRegistraAnulacionYMarcaAnulado() {
    Pedido pedido = pedidoService.registrarPedido("Ana", "LAP-001", 2);
    assertEquals(8, inventario.stock());

    anulacionService.anularPedido(pedido.getId(), "Solicitud del cliente");

    assertEquals(10, inventario.stock());
    assertEquals(1, anulacionRepository.count());
    assertEquals(PedidoEstado.ANULADO, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
    assertEquals(1, pedidoRepository.count(), "el pedido no se elimina físicamente");
  }

  @Test
  void anulacionDuplicadaSeRechazaYNoDevuelveStockOtraVez() {
    Pedido pedido = pedidoService.registrarPedido("Ana", "LAP-001", 2);
    anulacionService.anularPedido(pedido.getId(), null);

    assertThrows(AnulacionNoPermitidaException.class,
        () -> anulacionService.anularPedido(pedido.getId(), null));

    assertEquals(10, inventario.stock());
    assertEquals(1, anulacionRepository.count());
  }

  @Test
  void pedidoInexistenteNoModificaInventarioNiRegistraAnulacion() {
    assertThrows(PedidoNoEncontradoException.class, () -> anulacionService.anularPedido(999L, null));

    assertEquals(10, inventario.stock());
    assertEquals(0, anulacionRepository.count());
    assertTrue(inventario.llamadas().isEmpty());
  }

  @Test
  void inventarioCaidoDejaElPedidoRegistradoYSinAnulacion() {
    Pedido pedido = pedidoService.registrarPedido("Ana", "LAP-001", 2);
    inventario.caido(true);

    assertThrows(InventarioNoDisponibleException.class,
        () -> anulacionService.anularPedido(pedido.getId(), null));

    assertEquals(PedidoEstado.REGISTRADO, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
    assertEquals(0, anulacionRepository.count());
  }
}
