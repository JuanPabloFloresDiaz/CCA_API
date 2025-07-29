package com.server.api.domain.dto.aplicacion;

import java.util.UUID;

/**
 * DTO resumido para aplicaciones.
 * Contiene solo la información básica necesaria para listados y referencias.
 * Implementa el principio YAGNI incluyendo solo lo necesario.
 */
public record AplicacionSummary(
        UUID id,
        String nombre,
        String url,
        String llaveIdentificadora,
        String estado
) {}
