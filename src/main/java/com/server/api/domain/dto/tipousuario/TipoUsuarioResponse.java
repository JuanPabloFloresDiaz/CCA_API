package com.server.api.domain.dto.tipousuario;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta completa para tipos de usuario.
 * Contiene toda la información de un tipo de usuario incluyendo metadatos.
 */
public record TipoUsuarioResponse(
    UUID id,
    String nombre,
    String descripcion,
    UUID aplicacionId,
    String aplicacionNombre,
    String estado,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
