package com.server.api.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.api.domain.dto.accion.AccionCreateRequest;
import com.server.api.domain.dto.accion.AccionResponse;
import com.server.api.domain.dto.accion.AccionSummary;
import com.server.api.domain.dto.accion.AccionUpdateRequest;
import com.server.api.domain.entity.Accion;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.mapper.AccionMapper;
import com.server.api.domain.repository.AccionRepository;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.SeccionRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Servicio para gestión de acciones.
 * Implementa la lógica de negocio siguiendo principios SOLID.
 * Sigue el principio SRP al manejar únicamente operaciones de acciones.
 * Implementa el principio DIP al depender de abstracciones (repositorios).
 */
@Service
@Transactional
public class AccionService {

    private final AccionRepository accionRepository;
    private final AplicacionRepository aplicacionRepository;
    private final SeccionRepository seccionRepository;
    private final AccionMapper accionMapper;

    public AccionService(
            AccionRepository accionRepository,
            AplicacionRepository aplicacionRepository,
            SeccionRepository seccionRepository,
            AccionMapper accionMapper) {
        this.accionRepository = accionRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.seccionRepository = seccionRepository;
        this.accionMapper = accionMapper;
    }

    /**
     * Crea una nueva acción.
     * Implementa validaciones de negocio y relaciones.
     */
    public AccionResponse crear(AccionCreateRequest request) {
        // Validar que la aplicación existe y está activa
        Aplicacion aplicacion = aplicacionRepository.findById(request.aplicacionId())
                .orElseThrow(() -> new EntityNotFoundException("Aplicación no encontrada con ID: " + request.aplicacionId()));

        // Validar que la sección existe y está activa
        Seccion seccion = seccionRepository.findByIdAndActive(request.seccionId())
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada con ID: " + request.seccionId()));

        // Validar que no existe una acción con el mismo nombre en la misma aplicación y sección
        validarNombreUnico(request.nombre(), request.aplicacionId(), request.seccionId());
        
        Accion accion = accionMapper.toEntity(request);
        accion.setAplicacion(aplicacion);
        accion.setSeccion(seccion);
        
        Accion accionGuardada = accionRepository.save(accion);
        
        return accionMapper.toResponse(accionGuardada);
    }

    /**
     * Obtiene una acción por ID.
     */
    @Transactional(readOnly = true)
    public AccionResponse obtenerPorId(UUID id) {
        Accion accion = buscarAccionActiva(id);
        return accionMapper.toResponse(accion);
    }

    /**
     * Obtiene todas las acciones activas.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> obtenerTodas() {
        List<Accion> acciones = accionRepository.findAllActive();
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Obtiene todas las acciones con paginación.
     */
    @Transactional(readOnly = true)
    public Page<AccionSummary> obtenerTodas(Pageable pageable) {
        Page<Accion> accionesPage = accionRepository.findAllActive(pageable);
        List<AccionSummary> summaries = accionMapper.toSummaryList(accionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, accionesPage.getTotalElements());
    }

    /**
     * Busca acciones por nombre.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> buscarPorNombre(String nombre) {
        List<Accion> acciones = accionRepository.findByNombreContainingIgnoreCaseAndActive(nombre);
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Busca acciones por nombre con paginación.
     */
    @Transactional(readOnly = true)
    public Page<AccionSummary> buscarPorNombre(String nombre, Pageable pageable) {
        Page<Accion> accionesPage = accionRepository.findByNombreContainingIgnoreCaseAndActive(nombre, pageable);
        List<AccionSummary> summaries = accionMapper.toSummaryList(accionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, accionesPage.getTotalElements());
    }

    /**
     * Busca acciones por texto en nombre o descripción.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> buscarPorTexto(String texto) {
        List<Accion> acciones = accionRepository.findByTextoEnNombreOrDescripcion(texto);
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Busca acciones por aplicación.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> buscarPorAplicacion(UUID aplicacionId) {
        List<Accion> acciones = accionRepository.findByAplicacionIdAndActive(aplicacionId);
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Busca acciones por aplicación con paginación.
     */
    @Transactional(readOnly = true)
    public Page<AccionSummary> buscarPorAplicacion(UUID aplicacionId, Pageable pageable) {
        Page<Accion> accionesPage = accionRepository.findByAplicacionIdAndActive(aplicacionId, pageable);
        List<AccionSummary> summaries = accionMapper.toSummaryList(accionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, accionesPage.getTotalElements());
    }

    /**
     * Busca acciones por sección.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> buscarPorSeccion(UUID seccionId) {
        List<Accion> acciones = accionRepository.findBySeccionIdAndActive(seccionId);
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Busca acciones por sección con paginación.
     */
    @Transactional(readOnly = true)
    public Page<AccionSummary> buscarPorSeccion(UUID seccionId, Pageable pageable) {
        Page<Accion> accionesPage = accionRepository.findBySeccionIdAndActive(seccionId, pageable);
        List<AccionSummary> summaries = accionMapper.toSummaryList(accionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, accionesPage.getTotalElements());
    }

    /**
     * Busca acciones por aplicación y sección.
     */
    @Transactional(readOnly = true)
    public List<AccionSummary> buscarPorAplicacionYSeccion(UUID aplicacionId, UUID seccionId) {
        List<Accion> acciones = accionRepository.findByAplicacionIdAndSeccionIdAndActive(aplicacionId, seccionId);
        return accionMapper.toSummaryList(acciones);
    }

    /**
     * Actualiza una acción existente.
     */
    public AccionResponse actualizar(UUID id, AccionUpdateRequest request) {
        Accion accion = buscarAccionActiva(id);
        
        // Validar que la aplicación existe
        Aplicacion aplicacion = aplicacionRepository.findById(request.aplicacionId())
                .orElseThrow(() -> new EntityNotFoundException("Aplicación no encontrada con ID: " + request.aplicacionId()));

        // Validar que la sección existe y está activa
        Seccion seccion = seccionRepository.findByIdAndActive(request.seccionId())
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada con ID: " + request.seccionId()));

        // Validar nombre único solo si cambió
        if (!accion.getNombre().equalsIgnoreCase(request.nombre()) ||
            !accion.getAplicacion().getId().equals(request.aplicacionId()) ||
            !accion.getSeccion().getId().equals(request.seccionId())) {
            validarNombreUnicoParaActualizacion(request.nombre(), request.aplicacionId(), request.seccionId(), id);
        }
        
        accionMapper.updateEntity(accion, request);
        accion.setAplicacion(aplicacion);
        accion.setSeccion(seccion);
        
        Accion accionActualizada = accionRepository.save(accion);
        
        return accionMapper.toResponse(accionActualizada);
    }

    /**
     * Elimina una acción (soft delete).
     */
    public void eliminar(UUID id) {
        Accion accion = buscarAccionActiva(id);
        accion.softDelete();
        accionRepository.save(accion);
    }

    /**
     * Restaura una acción eliminada.
     */
    public AccionResponse restaurar(UUID id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Acción no encontrada con ID: " + id));
        
        if (!accion.isDeleted()) {
            throw new IllegalStateException("La acción no está eliminada");
        }
        
        // Validar que el nombre no esté en uso por otra acción activa en la misma aplicación y sección
        validarNombreUnicoParaActualizacion(
            accion.getNombre(), 
            accion.getAplicacion().getId(), 
            accion.getSeccion().getId(), 
            id
        );
        
        accion.restore();
        Accion accionRestaurada = accionRepository.save(accion);
        
        return accionMapper.toResponse(accionRestaurada);
    }

    /**
     * Obtiene el total de acciones activas.
     */
    @Transactional(readOnly = true)
    public long contarAcciones() {
        return accionRepository.countActive();
    }

    /**
     * Cuenta acciones por aplicación.
     */
    @Transactional(readOnly = true)
    public long contarPorAplicacion(UUID aplicacionId) {
        return accionRepository.countByAplicacionIdAndActive(aplicacionId);
    }

    /**
     * Cuenta acciones por sección.
     */
    @Transactional(readOnly = true)
    public long contarPorSeccion(UUID seccionId) {
        return accionRepository.countBySeccionIdAndActive(seccionId);
    }

    /**
     * Verifica si existe una acción con el nombre dado en la aplicación y sección especificadas.
     */
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre, UUID aplicacionId, UUID seccionId) {
        return accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(nombre, aplicacionId, seccionId);
    }

    // Métodos de utilidad privados

    /**
     * Busca una acción activa por ID o lanza excepción.
     */
    private Accion buscarAccionActiva(UUID id) {
        return accionRepository.findByIdAndActive(id)
                .orElseThrow(() -> new EntityNotFoundException("Acción no encontrada con ID: " + id));
    }

    /**
     * Valida que el nombre sea único en la aplicación y sección.
     */
    private void validarNombreUnico(String nombre, UUID aplicacionId, UUID seccionId) {
        if (accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(nombre, aplicacionId, seccionId)) {
            throw new IllegalArgumentException("Ya existe una acción con el nombre '" + nombre + 
                "' en la aplicación y sección especificadas");
        }
    }

    /**
     * Valida que el nombre sea único excluyendo un ID específico.
     */
    private void validarNombreUnicoParaActualizacion(String nombre, UUID aplicacionId, UUID seccionId, UUID excludeId) {
        if (accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionIdAndIdNot(
                nombre, aplicacionId, seccionId, excludeId)) {
            throw new IllegalArgumentException("Ya existe una acción con el nombre '" + nombre + 
                "' en la aplicación y sección especificadas");
        }
    }
}
