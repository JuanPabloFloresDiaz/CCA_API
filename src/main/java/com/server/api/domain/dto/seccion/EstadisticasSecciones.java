package com.server.api.domain.dto.seccion;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para responder estadísticas de secciones.
 * Contiene información resumida sobre el estado de las secciones en el sistema.
 */
@Schema(description = "Estadísticas de secciones del sistema")
public record EstadisticasSecciones(
        @Schema(description = "Total de secciones activas en el sistema", example = "25")
        long totalSecciones
) {
    /**
     * Constructor que valida que el total no sea negativo.
     *
     * @param totalSecciones Número total de secciones activas
     * @throws IllegalArgumentException si el total es negativo
     */
    public EstadisticasSecciones {
        if (totalSecciones < 0) {
            throw new IllegalArgumentException("El total de secciones no puede ser negativo");
        }
    }
}
