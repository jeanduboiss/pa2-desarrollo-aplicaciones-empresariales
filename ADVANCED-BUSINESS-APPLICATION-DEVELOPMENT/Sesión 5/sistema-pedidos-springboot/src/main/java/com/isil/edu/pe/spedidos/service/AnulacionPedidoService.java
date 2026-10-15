package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;

/**
 * Casos de uso de anulación de pedidos.
 */
public interface AnulacionPedidoService {

  /**
   * Anula un pedido: devuelve el stock a Inventario, registra la anulación y marca el pedido
   * como ANULADO.
   */
  AnulacionPedido anularPedido(Long pedidoId, String motivo);

  List<AnulacionPedido> listarAnulaciones();

}
