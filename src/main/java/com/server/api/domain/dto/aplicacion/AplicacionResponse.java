package com.server.api.domain.dto.aplicacion;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para aplicaciones.
 * Contiene toda la información de una aplicación que se retorna al cliente.
 * Implementa el principio de inmutabilidad con record.
 */
public record AplicacionResponse(
        UUID id,
        String nombre,
        String descripcion,
        String url,
        String llaveIdentificadora,
        String estado,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
