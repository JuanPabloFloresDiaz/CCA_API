package com.server.api.application.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.api.domain.mapper.TipoUsuarioMapper;
import com.server.api.domain.dto.tipousuario.TipoUsuarioCreateRequest;
import com.server.api.domain.dto.tipousuario.TipoUsuarioResponse;
import com.server.api.domain.dto.tipousuario.TipoUsuarioSummary;
import com.server.api.domain.dto.tipousuario.TipoUsuarioUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.TipoUsuario;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.TipoUsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para la gestión de tipos de usuario.
 * Implementa la lógica de negocio siguiendo principios SOLID.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TipoUsuarioService {

    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final AplicacionRepository aplicacionRepository;
    private final TipoUsuarioMapper tipoUsuarioMapper;

    /**
     * Crea un nuevo tipo de usuario
     */
    @Transactional
    public TipoUsuarioResponse crear(TipoUsuarioCreateRequest request) {
        log.info("Creando nuevo tipo de usuario con nombre: {}", request.nombre());

        // Validar que la aplicación existe
        Aplicacion aplicacion = aplicacionRepository.findById(request.aplicacionId())
            .orElseThrow(() -> new EntityNotFoundException("Aplicación no encontrada con ID: " + request.aplicacionId()));

        // Validar unicidad del nombre en la aplicación
        if (tipoUsuarioRepository.existsByNombreAndAplicacionIdAndDeletedAtIsNull(
                request.nombre(), request.aplicacionId(), null)) {
            throw new IllegalArgumentException("Ya existe un tipo de usuario con ese nombre en la aplicación");
        }

        TipoUsuario tipoUsuario = tipoUsuarioMapper.toEntity(request, aplicacion);
        TipoUsuario savedTipoUsuario = tipoUsuarioRepository.save(tipoUsuario);

        log.info("Tipo de usuario creado exitosamente con ID: {}", savedTipoUsuario.getId());
        return tipoUsuarioMapper.toResponse(savedTipoUsuario);
    }

    /**
     * Obtiene un tipo de usuario por ID
     */
    public TipoUsuarioResponse obtenerPorId(UUID id) {
        log.debug("Obteniendo tipo de usuario por ID: {}", id);

        TipoUsuario tipoUsuario = tipoUsuarioRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Tipo de usuario no encontrado con ID: " + id));

        return tipoUsuarioMapper.toResponse(tipoUsuario);
    }

    /**
     * Obtiene todos los tipos de usuario con paginación y filtros
     */
    public Page<TipoUsuarioSummary> obtenerTodos(Pageable pageable, String nombre, UUID aplicacionId, EstadoTipoUsuario estado) {
        log.debug("Obteniendo tipos de usuario paginados. Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<TipoUsuario> tiposUsuario = tipoUsuarioRepository.findByFilters(nombre, aplicacionId, estado, pageable);
        return tiposUsuario.map(tipoUsuarioMapper::toSummary);
    }

    /**
     * Actualiza un tipo de usuario existente
     */
    @Transactional
    public TipoUsuarioResponse actualizar(UUID id, TipoUsuarioUpdateRequest request) {
        log.info("Actualizando tipo de usuario con ID: {}", id);

        TipoUsuario tipoUsuario = tipoUsuarioRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Tipo de usuario no encontrado con ID: " + id));

        // Validar que la aplicación existe
        Aplicacion aplicacion = aplicacionRepository.findById(request.aplicacionId())
            .orElseThrow(() -> new EntityNotFoundException("Aplicación no encontrada con ID: " + request.aplicacionId()));

        // Validar unicidad del nombre en la aplicación (excluyendo el actual)
        if (tipoUsuarioRepository.existsByNombreAndAplicacionIdAndDeletedAtIsNull(
                request.nombre(), request.aplicacionId(), id)) {
            throw new IllegalArgumentException("Ya existe un tipo de usuario con ese nombre en la aplicación");
        }

        tipoUsuarioMapper.updateEntity(tipoUsuario, request, aplicacion);
        TipoUsuario updatedTipoUsuario = tipoUsuarioRepository.save(tipoUsuario);

        log.info("Tipo de usuario actualizado exitosamente con ID: {}", id);
        return tipoUsuarioMapper.toResponse(updatedTipoUsuario);
    }

    /**
     * Elimina un tipo de usuario (soft delete)
     */
    @Transactional
    public void eliminar(UUID id) {
        log.info("Eliminando tipo de usuario con ID: {}", id);

        TipoUsuario tipoUsuario = tipoUsuarioRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Tipo de usuario no encontrado con ID: " + id));

        tipoUsuario.setDeletedAt(OffsetDateTime.now());
        tipoUsuarioRepository.save(tipoUsuario);

        log.info("Tipo de usuario eliminado exitosamente con ID: {}", id);
    }

    /**
     * Cambia el estado de un tipo de usuario
     */
    @Transactional
    public TipoUsuarioResponse cambiarEstado(UUID id, EstadoTipoUsuario nuevoEstado) {
        log.info("Cambiando estado del tipo de usuario con ID: {} a {}", id, nuevoEstado);

        TipoUsuario tipoUsuario = tipoUsuarioRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Tipo de usuario no encontrado con ID: " + id));

        tipoUsuario.setEstado(nuevoEstado);
        TipoUsuario updatedTipoUsuario = tipoUsuarioRepository.save(tipoUsuario);

        log.info("Estado del tipo de usuario cambiado exitosamente");
        return tipoUsuarioMapper.toResponse(updatedTipoUsuario);
    }

    /**
     * Obtiene estadísticas de tipos de usuario
     */
    public Map<String, Long> obtenerEstadisticas(UUID aplicacionId, EstadoTipoUsuario estado) {
        log.debug("Obteniendo estadísticas de tipos de usuario");

        Map<String, Long> estadisticas = new HashMap<>();
        
        estadisticas.put("totalTiposUsuario", tipoUsuarioRepository.countByDeletedAtIsNull());
        
        if (aplicacionId != null) {
            estadisticas.put("tiposUsuarioPorAplicacion", 
                tipoUsuarioRepository.countByAplicacionIdAndDeletedAtIsNull(aplicacionId));
        } else {
            estadisticas.put("tiposUsuarioPorAplicacion", 0L);
        }
        
        if (estado != null) {
            estadisticas.put("tiposUsuarioPorEstado", 
                tipoUsuarioRepository.countByEstadoAndDeletedAtIsNull(estado));
        } else {
            estadisticas.put("tiposUsuarioPorEstado", 0L);
        }

        return estadisticas;
    }
}
