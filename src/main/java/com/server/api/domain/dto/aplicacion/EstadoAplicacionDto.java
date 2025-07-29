package com.server.api.domain.dto.aplicacion;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que representa los estados posibles de una aplicación.
 * Usado en la capa de presentación para evitar dependencias directas con las entidades del dominio.
 */
@Schema(description = "Estados posibles de una aplicación")
public enum EstadoAplicacionDto {
    
    @Schema(description = "Aplicación activa y en funcionamiento")
    ACTIVO("ACTIVO", "Aplicación activa y en funcionamiento"),
    
    @Schema(description = "Aplicación inactiva temporalmente")
    INACTIVO("INACTIVO", "Aplicación inactiva temporalmente");
    
    private final String codigo;
    private final String descripcion;
    
    /**
     * Constructor del enum.
     *
     * @param codigo Código del estado
     * @param descripcion Descripción del estado
     */
    EstadoAplicacionDto(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene el código del estado.
     *
     * @return Código del estado
     */
    public String getCodigo() {
        return codigo;
    }
    
    /**
     * Obtiene la descripción del estado.
     *
     * @return Descripción del estado
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Convierte un string al enum correspondiente.
     *
     * @param estado String representando el estado
     * @return EstadoAplicacionDto correspondiente
     * @throws IllegalArgumentException si el estado no es válido
     */
    public static EstadoAplicacionDto fromString(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede ser nulo o vacío");
        }
        
        try {
            return EstadoAplicacionDto.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de aplicación inválido: " + estado + 
                ". Estados válidos: ACTIVO, INACTIVO");
        }
    }
}
