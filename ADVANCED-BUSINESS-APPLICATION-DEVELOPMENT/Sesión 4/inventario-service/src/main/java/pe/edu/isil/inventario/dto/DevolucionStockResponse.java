package pe.edu.isil.inventario.dto;

/**
 * Respuesta de una devolución de stock.
 */
public class DevolucionStockResponse {

  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;
  private Long version;

  public DevolucionStockResponse() {
  }

  public DevolucionStockResponse(String codigo, int cantidadDevuelta, int stockRestante, Long version) {
    this.codigo = codigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
    this.version = version;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  /**
   * Stock disponible después de aplicar la devolución.
   */
  public int getStockRestante() {
    return stockRestante;
  }

  public Long getVersion() {
    return version;
  }
}
