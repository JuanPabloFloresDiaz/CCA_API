package com.server.api.domain.dto.tipousuario;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación de tipos de usuario.
 * Contiene los datos requeridos para crear un nuevo tipo de usuario.
 */
public record TipoUsuarioCreateRequest(
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,
    
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    String descripcion,
    
    @NotNull(message = "La aplicación es obligatoria")
    UUID aplicacionId
) {
}
