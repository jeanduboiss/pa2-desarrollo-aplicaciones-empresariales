package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias PA2 de {@link AnulacionPedidoServiceImpl}.
 *
 * <p>Se usan mocks de {@link DevolucionStockClient}, {@link PedidoRepository} y
 * {@link AnulacionPedidoRepository}; no se inicia Spring ni inventario-service.</p>
 */
class AnulacionPedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private AnulacionPedidoRepository anulacionPedidoRepository;
  private DevolucionStockClient devolucionStockClient;
  private AnulacionPedidoServiceImpl service;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    anulacionPedidoRepository = mock(AnulacionPedidoRepository.class);
    devolucionStockClient = mock(DevolucionStockClient.class);
    service = new AnulacionPedidoServiceImpl(
        pedidoRepository,
        anulacionPedidoRepository,
        devolucionStockClient
    );
  }

  private static Pedido pedidoRegistrado() {
    return new Pedido(1L, "Ana", "LAP-001", "Laptop", new BigDecimal("2500.00"), 2,
        new BigDecimal("5000.00"), null);
  }

  @Test
  void anulacionCorrectaInvocaUnaDevolucionMarcaAnuladoYGuardaElRegistro() {
    // Preparación
    Pedido pedido = pedidoRegistrado();
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));
    when(anulacionPedidoRepository.save(any(AnulacionPedido.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Ejecución
    AnulacionPedido resultado = service.anularPedido(1L, "Solicitud del cliente");

    // Verificación
    verify(devolucionStockClient, times(1)).devolverStock("LAP-001", 2);
    verify(anulacionPedidoRepository).save(any(AnulacionPedido.class));
    verify(pedidoRepository).save(pedido);
    assertEquals(PedidoEstado.ANULADO, pedido.getEstado());
    assertEquals(1L, resultado.getPedidoId());
    assertEquals("LAP-001", resultado.getProductoCodigo());
    assertEquals(2, resultado.getCantidadDevuelta());
    assertEquals("Solicitud del cliente", resultado.getMotivo());
  }

  @Test
  void anulacionDuplicadaSeRechazaSinNuevaDevolucionNiRegistroAdicional() {
    // Preparación: el pedido ya fue anulado.
    Pedido pedido = pedidoRegistrado();
    pedido.anular();
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(true);

    // Ejecución + verificación
    assertThrows(AnulacionNoPermitidaException.class, () -> service.anularPedido(1L, null));

    verify(devolucionStockClient, never()).devolverStock(anyString(), anyInt());
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  @Test
  void falloDeInventarioNoConfirmaCambiosLocalesNiRegistraAnulacion() {
    // Preparación
    Pedido pedido = pedidoRegistrado();
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(anulacionPedidoRepository.existsByPedidoId(1L)).thenReturn(false);
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("Inventario caído"));

    // Ejecución
    assertThrows(InventarioNoDisponibleException.class, () -> service.anularPedido(1L, null));

    // Verificación: sin guardado local posterior al error y el pedido sigue REGISTRADO.
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
    verify(pedidoRepository, never()).save(any(Pedido.class));
    assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());
  }

  @Test
  void pedidoInexistenteSeRechazaSinTocarInventarioNiRegistrarAnulacion() {
    when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(PedidoNoEncontradoException.class, () -> service.anularPedido(99L, null));

    verifyNoInteractions(devolucionStockClient);
    verify(anulacionPedidoRepository, never()).save(any(AnulacionPedido.class));
  }
}
