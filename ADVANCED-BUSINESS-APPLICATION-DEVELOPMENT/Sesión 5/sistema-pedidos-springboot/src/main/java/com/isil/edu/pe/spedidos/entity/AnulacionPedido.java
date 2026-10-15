package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Registro de auditoría de la anulación de un pedido.
 *
 * <p>La restricción UNIQUE sobre {@code pedido_id} impide en base de datos que un mismo pedido
 * se anule dos veces.</p>
 */
@Entity
@Table(
    name = "anulacion_pedido",
    uniqueConstraints = @UniqueConstraint(name = "uk_anulacion_pedido_pedido_id", columnNames = "pedido_id")
)
public class AnulacionPedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pedido_id", nullable = false)
  private Long pedidoId;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(name = "cantidad_devuelta", nullable = false)
  private int cantidadDevuelta;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @Column(length = 255)
  private String motivo;

  public AnulacionPedido() {
  }

  public AnulacionPedido(Long pedidoId, String productoCodigo, int cantidadDevuelta, String motivo) {
    this.pedidoId = pedidoId;
    this.productoCodigo = productoCodigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.motivo = motivo;
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getPedidoId() {
    return pedidoId;
  }

  public void setPedidoId(Long pedidoId) {
    this.pedidoId = pedidoId;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public void setProductoCodigo(String productoCodigo) {
    this.productoCodigo = productoCodigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setCantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }
}
