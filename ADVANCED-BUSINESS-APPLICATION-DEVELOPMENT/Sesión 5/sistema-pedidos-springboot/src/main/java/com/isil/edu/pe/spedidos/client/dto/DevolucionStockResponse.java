package com.isil.edu.pe.spedidos.client.dto;

/**
 * Respuesta JSON de la devolución de stock publicada por {@code inventario-service}.
 */
public class DevolucionStockResponse {

  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;
  private Long version;

  public DevolucionStockResponse() {
  }

  public DevolucionStockResponse(String codigo, int cantidadDevuelta, int stockRestante) {
    this.codigo = codigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public void setCantidadDevuelta(int cantidadDevuelta) {
    this.cantidadDevuelta = cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setStockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
