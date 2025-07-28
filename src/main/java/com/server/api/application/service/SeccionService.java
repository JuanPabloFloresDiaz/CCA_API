package com.server.api.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.api.domain.dto.seccion.SeccionCreateRequest;
import com.server.api.domain.dto.seccion.SeccionResponse;
import com.server.api.domain.dto.seccion.SeccionSummary;
import com.server.api.domain.dto.seccion.SeccionUpdateRequest;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.mapper.SeccionMapper;
import com.server.api.domain.repository.SeccionRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Servicio para gestión de secciones.
 * Implementa la lógica de negocio siguiendo principios SOLID.
 * Sigue el principio SRP al manejar únicamente operaciones de secciones.
 */
@Service
@Transactional
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final SeccionMapper seccionMapper;

    public SeccionService(SeccionRepository seccionRepository, SeccionMapper seccionMapper) {
        this.seccionRepository = seccionRepository;
        this.seccionMapper = seccionMapper;
    }

    /**
     * Crea una nueva sección.
     */
    public SeccionResponse crear(SeccionCreateRequest request) {
        validarNombreUnico(request.nombre());
        
        Seccion seccion = seccionMapper.toEntity(request);
        Seccion seccionGuardada = seccionRepository.save(seccion);
        
        return seccionMapper.toResponse(seccionGuardada);
    }

    /**
     * Obtiene una sección por ID.
     */
    @Transactional(readOnly = true)
    public SeccionResponse obtenerPorId(UUID id) {
        Seccion seccion = buscarSeccionActiva(id);
        return seccionMapper.toResponse(seccion);
    }

    /**
     * Obtiene todas las secciones activas.
     */
    @Transactional(readOnly = true)
    public List<SeccionSummary> obtenerTodas() {
        List<Seccion> secciones = seccionRepository.findAllActive();
        return seccionMapper.toSummaryList(secciones);
    }

    /**
     * Obtiene todas las secciones con paginación.
     */
    @Transactional(readOnly = true)
    public Page<SeccionSummary> obtenerTodas(Pageable pageable) {
        Page<Seccion> seccionesPage = seccionRepository.findAllActive(pageable);
        List<SeccionSummary> summaries = seccionMapper.toSummaryList(seccionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, seccionesPage.getTotalElements());
    }

    /**
     * Busca secciones por nombre.
     */
    @Transactional(readOnly = true)
    public List<SeccionSummary> buscarPorNombre(String nombre) {
        List<Seccion> secciones = seccionRepository.findByNombreContainingIgnoreCaseAndActive(nombre);
        return seccionMapper.toSummaryList(secciones);
    }

    /**
     * Busca secciones por nombre con paginación.
     */
    @Transactional(readOnly = true)
    public Page<SeccionSummary> buscarPorNombre(String nombre, Pageable pageable) {
        Page<Seccion> seccionesPage = seccionRepository.findByNombreContainingIgnoreCaseAndActive(nombre, pageable);
        List<SeccionSummary> summaries = seccionMapper.toSummaryList(seccionesPage.getContent());
        
        return new PageImpl<>(summaries, pageable, seccionesPage.getTotalElements());
    }

    /**
     * Busca secciones por texto en nombre o descripción.
     */
    @Transactional(readOnly = true)
    public List<SeccionSummary> buscarPorTexto(String texto) {
        List<Seccion> secciones = seccionRepository.findByTextoEnNombreOrDescripcion(texto);
        return seccionMapper.toSummaryList(secciones);
    }

    /**
     * Actualiza una sección existente.
     */
    public SeccionResponse actualizar(UUID id, SeccionUpdateRequest request) {
        Seccion seccion = buscarSeccionActiva(id);
        
        // Validar nombre único solo si cambió
        if (!seccion.getNombre().equalsIgnoreCase(request.nombre())) {
            validarNombreUnicoParaActualizacion(request.nombre(), id);
        }
        
        seccionMapper.updateEntity(seccion, request);
        Seccion seccionActualizada = seccionRepository.save(seccion);
        
        return seccionMapper.toResponse(seccionActualizada);
    }

    /**
     * Elimina una sección (soft delete).
     */
    public void eliminar(UUID id) {
        Seccion seccion = buscarSeccionActiva(id);
        seccion.softDelete();
        seccionRepository.save(seccion);
    }

    /**
     * Restaura una sección eliminada.
     */
    public SeccionResponse restaurar(UUID id) {
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada con ID: " + id));
        
        if (!seccion.isDeleted()) {
            throw new IllegalStateException("La sección no está eliminada");
        }
        
        // Validar que el nombre no esté en uso por otra sección activa
        validarNombreUnicoParaActualizacion(seccion.getNombre(), id);
        
        seccion.restore();
        Seccion seccionRestaurada = seccionRepository.save(seccion);
        
        return seccionMapper.toResponse(seccionRestaurada);
    }

    /**
     * Obtiene el total de secciones activas.
     */
    @Transactional(readOnly = true)
    public long contarSecciones() {
        return seccionRepository.countActive();
    }

    /**
     * Verifica si existe una sección con el nombre dado.
     */
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return seccionRepository.existsByNombreIgnoreCase(nombre);
    }

    // Métodos de utilidad privados

    /**
     * Busca una sección activa por ID o lanza excepción.
     */
    private Seccion buscarSeccionActiva(UUID id) {
        return seccionRepository.findByIdAndActive(id)
                .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada con ID: " + id));
    }

    /**
     * Valida que el nombre sea único en el sistema.
     */
    private void validarNombreUnico(String nombre) {
        if (seccionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una sección con el nombre: " + nombre);
        }
    }

    /**
     * Valida que el nombre sea único excluyendo un ID específico.
     */
    private void validarNombreUnicoParaActualizacion(String nombre, UUID excludeId) {
        if (seccionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, excludeId)) {
            throw new IllegalArgumentException("Ya existe una sección con el nombre: " + nombre);
        }
    }
}
