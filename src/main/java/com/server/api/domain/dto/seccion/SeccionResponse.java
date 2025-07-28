package com.server.api.domain.dto.seccion;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para una sección.
 * Utiliza Records de Java para simplificar la creación de DTOs inmutables.
 */
@Schema(description = "Información completa de una sección")
public record SeccionResponse(
        
        @Schema(description = "Identificador único de la sección", 
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Nombre de la sección", 
                example = "Gestión de Usuarios")
        String nombre,
        
        @Schema(description = "Descripción de la sección", 
                example = "Sección para administrar usuarios, roles y permisos del sistema")
        String descripcion,
        
        @Schema(description = "Indica si la sección está activa", 
                example = "true")
        Boolean activo,
        
        @Schema(description = "Fecha y hora de creación", 
                example = "2025-01-15T10:30:00Z")
        OffsetDateTime createdAt,
        
        @Schema(description = "Fecha y hora de última actualización", 
                example = "2025-01-15T14:45:00Z")
        OffsetDateTime updatedAt,
        
        @Schema(description = "Fecha y hora de eliminación (si aplica)", 
                example = "null")
        OffsetDateTime deletedAt
) {
        /**
         * Constructor compacto para formatear datos si es necesario
         */
        public SeccionResponse {
                // Asegurar que activo no sea null
                if (activo == null) {
                        activo = true;
                }
        }
        
        /**
         * Método de conveniencia para verificar si está eliminada
         */
        public boolean estaEliminada() {
                return deletedAt != null;
        }
        
        /**
         * Método de conveniencia para verificar si está activa
         */
        public boolean estaActiva() {
                return activo && !estaEliminada();
        }
}
