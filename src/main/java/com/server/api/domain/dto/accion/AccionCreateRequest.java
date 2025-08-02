package com.server.api.domain.dto.accion;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear una nueva acción.
 * Implementa el principio de separación de responsabilidades al mantener
 * solo los datos necesarios para crear una acción.
 * Sigue las validaciones de Bean Validation para garantizar datos consistentes.
 */
@Schema(description = "Datos para crear una nueva acción")
public record AccionCreateRequest(
        
        @Schema(description = "Nombre de la acción", 
                example = "Crear Usuario",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,
        
        @Schema(description = "Descripción detallada de la acción", 
                example = "Permite crear nuevos usuarios en el sistema")
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String descripcion,
        
        @Schema(description = "ID de la aplicación a la que pertenece la acción",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de la aplicación es requerido")
        UUID aplicacionId,
        
        @Schema(description = "ID de la sección a la que pertenece la acción",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de la sección es requerido")
        UUID seccionId
) {
    
    /**
     * Constructor compacto para validaciones adicionales
     */
    public AccionCreateRequest {
        // Limpiar espacios en blanco
        if (nombre != null) {
            nombre = nombre.trim();
        }
        if (descripcion != null) {
            descripcion = descripcion.trim();
            // Si está vacío después del trim, establecer como null
            if (descripcion.isEmpty()) {
                descripcion = null;
            }
        }
    }
}
