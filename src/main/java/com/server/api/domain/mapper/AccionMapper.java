package com.server.api.domain.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.server.api.domain.dto.accion.AccionCreateRequest;
import com.server.api.domain.dto.accion.AccionResponse;
import com.server.api.domain.dto.accion.AccionResponse.AplicacionBasicInfo;
import com.server.api.domain.dto.accion.AccionResponse.SeccionBasicInfo;
import com.server.api.domain.dto.accion.AccionSummary;
import com.server.api.domain.dto.accion.AccionUpdateRequest;
import com.server.api.domain.entity.Accion;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Seccion;

/**
 * Mapper para convertir entre entidad Accion y sus DTOs.
 * Implementa el principio SRP al manejar únicamente conversiones.
 * Sigue el principio DRY centralizando la lógica de conversión.
 */
@Component
public class AccionMapper {

    /**
     * Convierte un CreateRequest a entidad Accion.
     * Nota: Las relaciones (aplicacion y seccion) deben ser establecidas por el servicio.
     */
    public Accion toEntity(AccionCreateRequest request) {
        if (request == null) {
            return null;
        }

        Accion accion = new Accion();
        accion.setNombre(request.nombre());
        accion.setDescripcion(request.descripcion());
        // Las relaciones se establecen en el servicio después de validar que existen
        
        return accion;
    }

    /**
     * Actualiza una entidad existente con datos del UpdateRequest.
     * Las relaciones son actualizadas por el servicio después de validar.
     */
    public void updateEntity(Accion accion, AccionUpdateRequest request) {
        if (accion == null || request == null) {
            return;
        }

        accion.setNombre(request.nombre());
        accion.setDescripcion(request.descripcion());
        // Las relaciones se actualizan en el servicio después de validar
    }

    /**
     * Convierte una entidad Accion a AccionResponse.
     */
    public AccionResponse toResponse(Accion accion) {
        if (accion == null) {
            return null;
        }

        return new AccionResponse(
                accion.getId(),
                accion.getNombre(),
                accion.getDescripcion(),
                toAplicacionBasicInfo(accion.getAplicacion()),
                toSeccionBasicInfo(accion.getSeccion()),
                !accion.isDeleted(), // activo = no eliminado
                accion.getCreatedAt(),
                accion.getUpdatedAt(),
                accion.getDeletedAt()
        );
    }

    /**
     * Convierte una entidad Accion a AccionSummary.
     */
    public AccionSummary toSummary(Accion accion) {
        if (accion == null) {
            return null;
        }

        return new AccionSummary(
                accion.getId(),
                accion.getNombre(),
                accion.getDescripcion(),
                accion.getAplicacion() != null ? accion.getAplicacion().getNombre() : null,
                accion.getSeccion() != null ? accion.getSeccion().getNombre() : null,
                !accion.isDeleted() // activo = no eliminado
        );
    }

    /**
     * Convierte una lista de entidades a lista de responses.
     */
    public List<AccionResponse> toResponseList(List<Accion> acciones) {
        if (acciones == null) {
            return null;
        }
        return acciones.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convierte una lista de entidades a lista de summaries.
     */
    public List<AccionSummary> toSummaryList(List<Accion> acciones) {
        if (acciones == null) {
            return null;
        }
        return acciones.stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Convierte Aplicacion a información básica.
     */
    private AplicacionBasicInfo toAplicacionBasicInfo(Aplicacion aplicacion) {
        if (aplicacion == null) {
            return null;
        }
        
        return new AplicacionBasicInfo(
                aplicacion.getId(),
                aplicacion.getNombre(),
                aplicacion.getLlaveIdentificadora()
        );
    }

    /**
     * Convierte Seccion a información básica.
     */
    private SeccionBasicInfo toSeccionBasicInfo(Seccion seccion) {
        if (seccion == null) {
            return null;
        }
        
        return new SeccionBasicInfo(
                seccion.getId(),
                seccion.getNombre()
        );
    }
}
