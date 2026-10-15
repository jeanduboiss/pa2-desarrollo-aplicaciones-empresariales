package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionNoPermitidaException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del flujo de anulación de pedidos.
 *
 * <p>Las tres dependencias llegan por Constructor Injection. La transacción de Spring cubre
 * únicamente los cambios locales en H2: la devolución remota a Inventario NO forma parte de
 * una transacción distribuida.</p>
 *
 * <p>Orden deliberado: primero la llamada remota y después las escrituras locales. Si Inventario
 * falla, se lanza la excepción antes de modificar H2 y no queda rastro local. El riesgo inverso
 * (Inventario confirma y H2 falla) se documenta como limitación en el informe.</p>
 */
@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

  private static final int MOTIVO_MAX = 255;

  private final PedidoRepository pedidoRepository;
  private final AnulacionPedidoRepository anulacionPedidoRepository;
  private final DevolucionStockClient devolucionStockClient;

  public AnulacionPedidoServiceImpl(
      PedidoRepository pedidoRepository,
      AnulacionPedidoRepository anulacionPedidoRepository,
      DevolucionStockClient devolucionStockClient) {
    this.pedidoRepository = pedidoRepository;
    this.anulacionPedidoRepository = anulacionPedidoRepository;
    this.devolucionStockClient = devolucionStockClient;
  }

  @Override
  @Transactional
  public AnulacionPedido anularPedido(Long pedidoId, String motivo) {
    if (pedidoId == null) {
      throw new PedidoException("El pedidoId es obligatorio.");
    }
    if (motivo != null && motivo.length() > MOTIVO_MAX) {
      throw new PedidoException("El motivo no puede superar " + MOTIVO_MAX + " caracteres.");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));

    if (pedido.getEstado() != PedidoEstado.REGISTRADO
        || anulacionPedidoRepository.existsByPedidoId(pedidoId)) {
      throw new AnulacionNoPermitidaException("El pedido " + pedidoId + " ya fue anulado.");
    }

    // Si el cliente lanza una excepción, se propaga sin tocar H2: el pedido sigue REGISTRADO.
    devolucionStockClient.devolverStock(pedido.getProductoCodigo(), pedido.getCantidad());

    AnulacionPedido anulacion = new AnulacionPedido(
        pedido.getId(),
        pedido.getProductoCodigo(),
        pedido.getCantidad(),
        motivo == null || motivo.isBlank() ? null : motivo.trim()
    );
    AnulacionPedido guardada = anulacionPedidoRepository.save(anulacion);

    pedido.anular();
    pedidoRepository.save(pedido);

    return guardada;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnulacionPedido> listarAnulaciones() {
    return anulacionPedidoRepository.findAllByOrderByIdDesc();
  }
}
