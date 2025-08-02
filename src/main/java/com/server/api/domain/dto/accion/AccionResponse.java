package com.server.api.domain.dto.accion;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para una acción.
 * Contiene toda la información de una acción que se retorna al cliente.
 * Implementa el principio de inmutabilidad con record.
 */
@Schema(description = "Información completa de una acción")
public record AccionResponse(
        
        @Schema(description = "Identificador único de la acción", 
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Nombre de la acción", 
                example = "Crear Usuario")
        String nombre,
        
        @Schema(description = "Descripción de la acción", 
                example = "Permite crear nuevos usuarios en el sistema")
        String descripcion,
        
        @Schema(description = "Información de la aplicación asociada")
        AplicacionBasicInfo aplicacion,
        
        @Schema(description = "Información de la sección asociada")
        SeccionBasicInfo seccion,
        
        @Schema(description = "Indica si la acción está activa", 
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
    public AccionResponse {
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
    
    /**
     * DTO para información básica de aplicación
     */
    @Schema(description = "Información básica de una aplicación")
    public record AplicacionBasicInfo(
            @Schema(description = "ID de la aplicación")
            UUID id,
            @Schema(description = "Nombre de la aplicación")
            String nombre,
            @Schema(description = "Llave identificadora de la aplicación")
            String llaveIdentificadora
    ) {}
    
    /**
     * DTO para información básica de sección
     */
    @Schema(description = "Información básica de una sección")
    public record SeccionBasicInfo(
            @Schema(description = "ID de la sección")
            UUID id,
            @Schema(description = "Nombre de la sección")
            String nombre
    ) {}
}
