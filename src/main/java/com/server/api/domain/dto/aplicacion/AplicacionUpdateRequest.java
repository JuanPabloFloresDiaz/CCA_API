package com.server.api.domain.dto.aplicacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de aplicaciones.
 * Implementa el principio de separación de responsabilidades al mantener
 * solo los datos necesarios para actualizar una aplicación.
 */
public record AplicacionUpdateRequest(
        
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,
        
        @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
        String descripcion,
        
        @NotBlank(message = "La URL es requerida")
        @Size(max = 255, message = "La URL no puede exceder 255 caracteres")
        @Pattern(regexp = "^https?://.*", message = "La URL debe comenzar con http:// o https://")
        String url,
        
        @NotBlank(message = "La llave identificadora es requerida")
        @Size(min = 5, max = 100, message = "La llave identificadora debe tener entre 5 y 100 caracteres")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "La llave identificadora solo puede contener letras mayúsculas, números y guiones bajos")
        String llaveIdentificadora,
        
        @Pattern(regexp = "^(ACTIVO|INACTIVO)$", message = "El estado debe ser ACTIVO o INACTIVO")
        String estado
) {}
