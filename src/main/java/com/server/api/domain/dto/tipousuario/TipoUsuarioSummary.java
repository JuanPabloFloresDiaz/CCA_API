package com.server.api.domain.dto.tipousuario;

import java.util.UUID;

/**
 * DTO de resumen para tipos de usuario.
 * Contiene información básica de un tipo de usuario para listados y selecciones.
 */
public record TipoUsuarioSummary(
    UUID id,
    String nombre,
    String descripcion,
    String aplicacionNombre,
    String estado
) {
}
