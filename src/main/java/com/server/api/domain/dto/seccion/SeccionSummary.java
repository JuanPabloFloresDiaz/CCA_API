package com.server.api.domain.dto.seccion;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO resumido para listar secciones.
 * Contiene solo la información esencial para listados y búsquedas.
 */
@Schema(description = "Información resumida de una sección para listados")
public record SeccionSummary(
        
        @Schema(description = "Identificador único de la sección", 
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Nombre de la sección", 
                example = "Gestión de Usuarios")
        String nombre,
        
        @Schema(description = "Descripción breve de la sección", 
                example = "Administración de usuarios y permisos")
        String descripcion,
        
        @Schema(description = "Indica si la sección está activa", 
                example = "true")
        Boolean activo
) {
        /**
         * Constructor compacto para asegurar valores por defecto
         */
        public SeccionSummary {
                if (activo == null) {
                        activo = true;
                }
                
                // Truncar descripción si es muy larga para el summary
                if (descripcion != null && descripcion.length() > 100) {
                        descripcion = descripcion.substring(0, 97) + "...";
                }
        }
}
