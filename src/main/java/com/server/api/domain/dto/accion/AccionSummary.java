package com.server.api.domain.dto.accion;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO resumido de una acción para listados.
 * Contiene solo la información esencial para listados y búsquedas.
 * Implementa el principio de inmutabilidad con record.
 */
@Schema(description = "Información resumida de una acción")
public record AccionSummary(
        
        @Schema(description = "Identificador único de la acción", 
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Nombre de la acción", 
                example = "Crear Usuario")
        String nombre,
        
        @Schema(description = "Descripción resumida de la acción", 
                example = "Permite crear nuevos usuarios...")
        String descripcion,
        
        @Schema(description = "Nombre de la aplicación asociada",
                example = "Sistema de Gestión")
        String aplicacionNombre,
        
        @Schema(description = "Nombre de la sección asociada",
                example = "Gestión de Usuarios")
        String seccionNombre,
        
        @Schema(description = "Indica si la acción está activa", 
                example = "true")
        Boolean activo
) {
    
    /**
     * Constructor compacto con validaciones y formateo
     */
    public AccionSummary {
        // Asegurar que activo no sea null
        if (activo == null) {
            activo = true;
        }
        
        // Truncar descripción si es muy larga
        if (descripcion != null && descripcion.length() > 100) {
            descripcion = descripcion.substring(0, 97) + "...";
        }
    }
    
    /**
     * Método de conveniencia para verificar si está activa
     */
    public boolean estaActiva() {
        return Boolean.TRUE.equals(activo);
    }
}
