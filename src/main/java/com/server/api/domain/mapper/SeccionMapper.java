package com.server.api.domain.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.server.api.domain.dto.seccion.SeccionCreateRequest;
import com.server.api.domain.dto.seccion.SeccionResponse;
import com.server.api.domain.dto.seccion.SeccionSummary;
import com.server.api.domain.dto.seccion.SeccionUpdateRequest;
import com.server.api.domain.entity.Seccion;

/**
 * Mapper para convertir entre entidad Seccion y sus DTOs.
 * Sigue el principio SRP al manejar únicamente conversiones.
 * Sigue el principio DRY centralizando toda la lógica de mapeo.
 */
@Component
public class SeccionMapper {

    /**
     * Convierte un CreateRequest a entidad Seccion.
     */
    public Seccion toEntity(SeccionCreateRequest request) {
        if (request == null) {
            return null;
        }

        Seccion seccion = new Seccion();
        seccion.setNombre(request.nombre());
        seccion.setDescripcion(request.descripcion());
        // Los campos de BaseEntity se establecen automáticamente
        
        return seccion;
    }

    /**
     * Actualiza una entidad existente con datos del UpdateRequest.
     */
    public void updateEntity(Seccion seccion, SeccionUpdateRequest request) {
        if (seccion == null || request == null) {
            return;
        }

        seccion.setNombre(request.nombre());
        seccion.setDescripcion(request.descripcion());
        // updatedAt se actualiza automáticamente en BaseEntity
    }

    /**
     * Convierte una entidad Seccion a SeccionResponse.
     */
    public SeccionResponse toResponse(Seccion seccion) {
        if (seccion == null) {
            return null;
        }

        return new SeccionResponse(
                seccion.getId(),
                seccion.getNombre(),
                seccion.getDescripcion(),
                !seccion.isDeleted(), // activo = no eliminado
                seccion.getCreatedAt(),
                seccion.getUpdatedAt(),
                seccion.getDeletedAt()
        );
    }

    /**
     * Convierte una entidad Seccion a SeccionSummary.
     */
    public SeccionSummary toSummary(Seccion seccion) {
        if (seccion == null) {
            return null;
        }

        return new SeccionSummary(
                seccion.getId(),
                seccion.getNombre(),
                seccion.getDescripcion(),
                !seccion.isDeleted() // activo = no eliminado
        );
    }

    /**
     * Convierte una lista de entidades a lista de responses.
     */
    public List<SeccionResponse> toResponseList(List<Seccion> secciones) {
        if (secciones == null) {
            return null;
        }

        return secciones.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convierte una lista de entidades a lista de summaries.
     */
    public List<SeccionSummary> toSummaryList(List<Seccion> secciones) {
        if (secciones == null) {
            return null;
        }

        return secciones.stream()
                .map(this::toSummary)
                .toList();
    }
}
