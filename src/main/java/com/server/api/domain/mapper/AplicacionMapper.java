package com.server.api.domain.mapper;

import org.springframework.stereotype.Component;

import com.server.api.domain.dto.aplicacion.AplicacionCreateRequest;
import com.server.api.domain.dto.aplicacion.AplicacionResponse;
import com.server.api.domain.dto.aplicacion.AplicacionSummary;
import com.server.api.domain.dto.aplicacion.AplicacionUpdateRequest;
import com.server.api.domain.dto.aplicacion.EstadoAplicacionDto;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;

/**
 * Mapper para convertir entre Aplicacion y sus DTOs.
 * Implementa el principio SRP al tener una sola responsabilidad: mapeo de datos.
 * Sigue el principio DRY centralizando la lógica de conversión.
 */
@Component
public class AplicacionMapper {

    /**
     * Convierte un EstadoAplicacionDto a EstadoAplicacion de dominio.
     * 
     * @param estadoDto el DTO del estado
     * @return el enum de dominio
     */
    public EstadoAplicacion toEntityEstado(EstadoAplicacionDto estadoDto) {
        if (estadoDto == null) {
            return EstadoAplicacion.ACTIVO;
        }
        
        return switch (estadoDto) {
            case ACTIVO -> EstadoAplicacion.ACTIVO;
            case INACTIVO -> EstadoAplicacion.INACTIVO;
        };
    }
    
    /**
     * Convierte un EstadoAplicacion de dominio a EstadoAplicacionDto.
     * 
     * @param estado el enum de dominio
     * @return el DTO del estado
     */
    public EstadoAplicacionDto toDtoEstado(EstadoAplicacion estado) {
        if (estado == null) {
            return EstadoAplicacionDto.ACTIVO;
        }
        
        return switch (estado) {
            case ACTIVO -> EstadoAplicacionDto.ACTIVO;
            case INACTIVO -> EstadoAplicacionDto.INACTIVO;
        };
    }

    /**
     * Convierte un AplicacionCreateRequest a entidad Aplicacion.
     * 
     * @param request el DTO de creación
     * @return la entidad Aplicacion
     */
    public Aplicacion toEntity(AplicacionCreateRequest request) {
        if (request == null) {
            return null;
        }

        Aplicacion aplicacion = new Aplicacion();
        aplicacion.setNombre(request.nombre());
        aplicacion.setDescripcion(request.descripcion());
        aplicacion.setUrl(request.url());
        aplicacion.setLlaveIdentificadora(request.llaveIdentificadora());
        
        // Convertir string a enum
        if (request.estado() != null) {
            aplicacion.setEstado(EstadoAplicacion.valueOf(request.estado()));
        } else {
            aplicacion.setEstado(EstadoAplicacion.ACTIVO);
        }

        return aplicacion;
    }

    /**
     * Convierte una entidad Aplicacion a AplicacionResponse.
     * 
     * @param aplicacion la entidad a convertir
     * @return el DTO de respuesta
     */
    public AplicacionResponse toResponse(Aplicacion aplicacion) {
        if (aplicacion == null) {
            return null;
        }

        return new AplicacionResponse(
                aplicacion.getId(),
                aplicacion.getNombre(),
                aplicacion.getDescripcion(),
                aplicacion.getUrl(),
                aplicacion.getLlaveIdentificadora(),
                aplicacion.getEstado() != null ? aplicacion.getEstado().name() : EstadoAplicacion.ACTIVO.name(),
                aplicacion.getCreatedAt(),
                aplicacion.getUpdatedAt()
        );
    }

    /**
     * Convierte una entidad Aplicacion a AplicacionSummary.
     * 
     * @param aplicacion la entidad a convertir
     * @return el DTO resumido
     */
    public AplicacionSummary toSummary(Aplicacion aplicacion) {
        if (aplicacion == null) {
            return null;
        }

        return new AplicacionSummary(
                aplicacion.getId(),
                aplicacion.getNombre(),
                aplicacion.getUrl(),
                aplicacion.getLlaveIdentificadora(),
                aplicacion.getEstado() != null ? aplicacion.getEstado().name() : EstadoAplicacion.ACTIVO.name()
        );
    }

    /**
     * Actualiza una entidad existente con datos del DTO de actualización.
     * 
     * @param aplicacion la entidad a actualizar
     * @param request el DTO con los nuevos datos
     */
    public void updateEntityFromRequest(Aplicacion aplicacion, AplicacionUpdateRequest request) {
        if (aplicacion == null || request == null) {
            return;
        }

        aplicacion.setNombre(request.nombre());
        aplicacion.setDescripcion(request.descripcion());
        aplicacion.setUrl(request.url());
        aplicacion.setLlaveIdentificadora(request.llaveIdentificadora());
        
        if (request.estado() != null) {
            aplicacion.setEstado(EstadoAplicacion.valueOf(request.estado()));
        }
    }
}
