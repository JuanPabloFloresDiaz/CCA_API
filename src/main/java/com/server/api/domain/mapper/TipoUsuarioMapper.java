package com.server.api.domain.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.server.api.domain.dto.tipousuario.TipoUsuarioCreateRequest;
import com.server.api.domain.dto.tipousuario.TipoUsuarioResponse;
import com.server.api.domain.dto.tipousuario.TipoUsuarioSummary;
import com.server.api.domain.dto.tipousuario.TipoUsuarioUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.TipoUsuario;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;

/**
 * Mapper para la conversión entre entidad TipoUsuario y DTOs.
 * Maneja la transformación de datos entre las capas de dominio y presentación.
 */
@Component
public class TipoUsuarioMapper {

    /**
     * Convierte una request de creación a entidad TipoUsuario
     */
    public TipoUsuario toEntity(TipoUsuarioCreateRequest request, Aplicacion aplicacion) {
        if (request == null) {
            return null;
        }

        TipoUsuario tipoUsuario = new TipoUsuario();
        tipoUsuario.setNombre(request.nombre());
        tipoUsuario.setDescripcion(request.descripcion());
        tipoUsuario.setAplicacion(aplicacion);
        tipoUsuario.setEstado(EstadoTipoUsuario.ACTIVO);
        tipoUsuario.setCreatedAt(OffsetDateTime.now());
        tipoUsuario.setUpdatedAt(OffsetDateTime.now());

        return tipoUsuario;
    }

    /**
     * Actualiza una entidad TipoUsuario existente con datos de la request de actualización
     */
    public void updateEntity(TipoUsuario tipoUsuario, TipoUsuarioUpdateRequest request, Aplicacion aplicacion) {
        if (tipoUsuario == null || request == null) {
            return;
        }

        tipoUsuario.setNombre(request.nombre());
        tipoUsuario.setDescripcion(request.descripcion());
        
        if (aplicacion != null) {
            tipoUsuario.setAplicacion(aplicacion);
        }
        
        if (request.estado() != null) {
            try {
                tipoUsuario.setEstado(EstadoTipoUsuario.valueOf(request.estado().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Estado inválido, mantener el estado actual
            }
        }
        
        tipoUsuario.setUpdatedAt(OffsetDateTime.now());
    }

    /**
     * Convierte una entidad TipoUsuario a response DTO
     */
    public TipoUsuarioResponse toResponse(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) {
            return null;
        }

        return new TipoUsuarioResponse(
            tipoUsuario.getId(),
            tipoUsuario.getNombre(),
            tipoUsuario.getDescripcion(),
            tipoUsuario.getAplicacion() != null ? tipoUsuario.getAplicacion().getId() : null,
            tipoUsuario.getAplicacion() != null ? tipoUsuario.getAplicacion().getNombre() : null,
            tipoUsuario.getEstado() != null ? tipoUsuario.getEstado().name() : null,
            tipoUsuario.getCreatedAt(),
            tipoUsuario.getUpdatedAt()
        );
    }

    /**
     * Convierte una entidad TipoUsuario a summary DTO
     */
    public TipoUsuarioSummary toSummary(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) {
            return null;
        }

        return new TipoUsuarioSummary(
            tipoUsuario.getId(),
            tipoUsuario.getNombre(),
            tipoUsuario.getDescripcion(),
            tipoUsuario.getAplicacion() != null ? tipoUsuario.getAplicacion().getNombre() : null,
            tipoUsuario.getEstado() != null ? tipoUsuario.getEstado().name() : null
        );
    }

    /**
     * Convierte una lista de entidades TipoUsuario a lista de response DTOs
     */
    public List<TipoUsuarioResponse> toResponseList(List<TipoUsuario> tiposUsuario) {
        if (tiposUsuario == null) {
            return null;
        }

        return tiposUsuario.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de entidades TipoUsuario a lista de summary DTOs
     */
    public List<TipoUsuarioSummary> toSummaryList(List<TipoUsuario> tiposUsuario) {
        if (tiposUsuario == null) {
            return null;
        }

        return tiposUsuario.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
