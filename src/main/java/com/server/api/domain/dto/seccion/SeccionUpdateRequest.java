package com.server.api.domain.dto.seccion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar una sección existente.
 * Utiliza Records de Java para simplificar la creación de DTOs inmutables.
 */
@Schema(description = "Datos para actualizar una sección existente")
public record SeccionUpdateRequest(
        
        @Schema(description = "Nombre de la sección", 
                example = "Gestión de Usuarios Actualizada",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,
        
        @Schema(description = "Descripción detallada de la sección", 
                example = "Sección actualizada para administrar usuarios, roles y permisos del sistema")
        @Size(max = 1000, message = "La descripción no debe exceder los 1000 caracteres")
        String descripcion
) {
        /**
         * Constructor compacto para validaciones adicionales
         */
        public SeccionUpdateRequest {
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
