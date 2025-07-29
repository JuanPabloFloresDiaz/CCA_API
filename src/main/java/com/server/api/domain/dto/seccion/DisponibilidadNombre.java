package com.server.api.domain.dto.seccion;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para verificar la disponibilidad de un nombre de sección.
 * Indica si un nombre específico está disponible para uso.
 */
@Schema(description = "Información sobre la disponibilidad de un nombre de sección")
public record DisponibilidadNombre(
        @Schema(description = "Nombre verificado", example = "Nueva Sección")
        String nombre,
        
        @Schema(description = "Indica si el nombre está disponible para uso", example = "true")
        boolean disponible,
        
        @Schema(description = "Indica si el nombre ya existe en el sistema", example = "false")
        boolean existe
) {
    /**
     * Constructor que valida la consistencia de los datos.
     *
     * @param nombre Nombre a verificar
     * @param disponible Si el nombre está disponible
     * @param existe Si el nombre ya existe
     * @throws IllegalArgumentException si el nombre es nulo o vacío, o si los booleanos son inconsistentes
     */
    public DisponibilidadNombre {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
        
        // Validar consistencia lógica: si existe debe no estar disponible, y viceversa
        if (existe == disponible) {
            throw new IllegalArgumentException("Inconsistencia en disponibilidad: si existe debe no estar disponible");
        }
    }
}
