package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnulacionPedidoRepository extends JpaRepository<AnulacionPedido, Long> {

  Optional<AnulacionPedido> findByPedidoId(Long pedidoId);

  boolean existsByPedidoId(Long pedidoId);

  List<AnulacionPedido> findAllByOrderByIdDesc();

}
