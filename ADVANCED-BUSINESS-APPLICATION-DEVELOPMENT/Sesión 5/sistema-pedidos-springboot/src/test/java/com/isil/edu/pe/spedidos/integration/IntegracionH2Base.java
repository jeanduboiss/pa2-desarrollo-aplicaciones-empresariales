package com.isil.edu.pe.spedidos.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de pruebas: un único bean {@link InventarioFalso} marcado como primario sustituye
 * a los clientes HTTP (InventarioClient y DevolucionStockClient). H2, JPA y las transacciones de
 * Spring son los reales.
 */
@TestConfiguration
class IntegracionH2Base {

  @Bean
  @Primary
  InventarioFalso inventarioFalso() {
    return new InventarioFalso();
  }
}
