package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Incidente pendiente de conciliación manual: Inventario confirmó una reserva, el pedido local
 * no pudo guardarse y la devolución compensatoria también falló. El stock remoto quedó descontado
 * sin pedido que lo respalde.
 */
@Entity
@Table(name = "incidente_conciliacion")
public class IncidenteConciliacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(nullable = false)
  private int cantidad;

  @Column(nullable = false, length = 500)
  private String detalle;

  @Column(nullable = false)
  private boolean resuelto;

  @Column(nullable = false)
  private LocalDateTime fecha;

  public IncidenteConciliacion() {
  }

  public IncidenteConciliacion(String productoCodigo, int cantidad, String detalle) {
    this.productoCodigo = productoCodigo;
    this.cantidad = cantidad;
    this.detalle = detalle == null || detalle.length() <= 500 ? detalle : detalle.substring(0, 500);
    this.resuelto = false;
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public int getCantidad() {
    return cantidad;
  }

  public String getDetalle() {
    return detalle;
  }

  public boolean isResuelto() {
    return resuelto;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }
}
