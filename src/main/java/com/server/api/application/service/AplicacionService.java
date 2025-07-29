package com.server.api.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.api.domain.dto.aplicacion.AplicacionCreateRequest;
import com.server.api.domain.dto.aplicacion.AplicacionUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;
import com.server.api.domain.mapper.AplicacionMapper;
import com.server.api.domain.repository.AplicacionRepository;

/**
 * Servicio para gestión de aplicaciones del sistema.
 * Implementa el principio SRP al manejar únicamente la lógica de negocio de aplicaciones.
 * Sigue el patrón de Clean Architecture separando la lógica de negocio de los detalles de infraestructura.
 */
@Service
@Transactional
public class AplicacionService {

    private final AplicacionRepository aplicacionRepository;
    private final AplicacionMapper aplicacionMapper;

    public AplicacionService(AplicacionRepository aplicacionRepository, AplicacionMapper aplicacionMapper) {
        this.aplicacionRepository = aplicacionRepository;
        this.aplicacionMapper = aplicacionMapper;
    }

    /**
     * Crea una nueva aplicación en el sistema.
     * Implementa validaciones de negocio y el principio ACID.
     * 
     * @param request datos de la aplicación a crear
     * @return la aplicación creada
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Aplicacion crear(AplicacionCreateRequest request) {
        validarDatosCreacion(request);
        
        Aplicacion aplicacion = aplicacionMapper.toEntity(request);
        return aplicacionRepository.save(aplicacion);
    }

    /**
     * Obtiene una aplicación por su ID.
     * Implementa el principio KISS manteniendo la lógica simple.
     * 
     * @param id identificador único de la aplicación
     * @return la aplicación encontrada
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public Aplicacion obtenerPorId(UUID id) {
        return aplicacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aplicación no encontrada con ID: " + id));
    }

    /**
     * Obtiene una aplicación por su llave identificadora.
     * 
     * @param llaveIdentificadora llave única de la aplicación
     * @return la aplicación encontrada
     * @throws IllegalArgumentException si no existe
     */
    @Transactional(readOnly = true)
    public Aplicacion obtenerPorLlaveIdentificadora(String llaveIdentificadora) {
        return aplicacionRepository.findByLlaveIdentificadora(llaveIdentificadora)
                .orElseThrow(() -> new IllegalArgumentException("Aplicación no encontrada con llave: " + llaveIdentificadora));
    }

    /**
     * Obtiene todas las aplicaciones activas del sistema.
     * 
     * @return lista de todas las aplicaciones activas
     */
    @Transactional(readOnly = true)
    public List<Aplicacion> obtenerTodas() {
        return aplicacionRepository.findByEstado(EstadoAplicacion.ACTIVO);
    }

    /**
     * Obtiene aplicaciones con paginación.
     * Implementa el principio de paginación para manejar grandes volúmenes de datos.
     * 
     * @param pageable configuración de paginación
     * @return página de aplicaciones
     */
    @Transactional(readOnly = true)
    public Page<Aplicacion> obtenerTodas(Pageable pageable) {
        return aplicacionRepository.findByEstado(EstadoAplicacion.ACTIVO, pageable);
    }

    /**
     * Busca aplicaciones por nombre.
     * Implementa búsqueda insensible a mayúsculas para mejor UX.
     * 
     * @param nombre nombre o parte del nombre a buscar
     * @return lista de aplicaciones que coinciden
     */
    @Transactional(readOnly = true)
    public List<Aplicacion> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerTodas();
        }
        return aplicacionRepository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    /**
     * Actualiza una aplicación existente.
     * Implementa validaciones y mantiene integridad de datos.
     * 
     * @param id identificador de la aplicación
     * @param request nuevos datos
     * @return aplicación actualizada
     * @throws IllegalArgumentException si no existe o datos inválidos
     */
    public Aplicacion actualizar(UUID id, AplicacionUpdateRequest request) {
        Aplicacion aplicacionExistente = obtenerPorId(id);
        validarDatosActualizacion(request, aplicacionExistente);
        
        aplicacionMapper.updateEntityFromRequest(aplicacionExistente, request);
        return aplicacionRepository.save(aplicacionExistente);
    }

    /**
     * Elimina una aplicación de forma lógica (soft delete).
     * Implementa el principio de preservación de datos históricos.
     * 
     * @param id identificador de la aplicación
     * @throws IllegalArgumentException si no existe
     */
    public void eliminar(UUID id) {
        Aplicacion aplicacion = obtenerPorId(id);
        aplicacion.softDelete();
        aplicacionRepository.save(aplicacion);
    }

    /**
     * Restaura una aplicación eliminada lógicamente.
     * 
     * @param id identificador de la aplicación
     * @throws IllegalArgumentException si no existe
     */
    public Aplicacion restaurar(UUID id) {
        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aplicación no encontrada con ID: " + id));
        
        aplicacion.restore();
        return aplicacionRepository.save(aplicacion);
    }

    /**
     * Cuenta el número total de aplicaciones activas.
     * 
     * @return número de aplicaciones activas
     */
    @Transactional(readOnly = true)
    public long contarAplicacionesActivas() {
        return aplicacionRepository.countAplicacionesActivas();
    }

    /**
     * Verifica si existe una aplicación con la llave identificadora dada.
     * 
     * @param llaveIdentificadora llave a verificar
     * @return true si existe, false si no
     */
    @Transactional(readOnly = true)
    public boolean existePorLlaveIdentificadora(String llaveIdentificadora) {
        return aplicacionRepository.existsByLlaveIdentificadora(llaveIdentificadora);
    }

    /**
     * Cambia el estado de una aplicación.
     * 
     * @param id identificador de la aplicación
     * @param nuevoEstado nuevo estado a establecer
     * @return aplicación actualizada
     */
    public Aplicacion cambiarEstado(UUID id, EstadoAplicacion nuevoEstado) {
        Aplicacion aplicacion = obtenerPorId(id);
        aplicacion.setEstado(nuevoEstado);
        return aplicacionRepository.save(aplicacion);
    }

    /**
     * Valida los datos para la creación de una nueva aplicación.
     * Implementa el principio DRY centralizando validaciones.
     */
    private void validarDatosCreacion(AplicacionCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de la aplicación son requeridos");
        }

        // Validar llave identificadora única
        if (aplicacionRepository.existsByLlaveIdentificadora(request.llaveIdentificadora())) {
            throw new IllegalArgumentException("Ya existe una aplicación con la llave identificadora: " + request.llaveIdentificadora());
        }

        // Validar URL única
        if (aplicacionRepository.existsByUrl(request.url())) {
            throw new IllegalArgumentException("Ya existe una aplicación con la URL: " + request.url());
        }
    }

    /**
     * Valida los datos para la actualización de una aplicación.
     */
    private void validarDatosActualizacion(AplicacionUpdateRequest request, Aplicacion aplicacionExistente) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de la aplicación son requeridos");
        }

        // Validar llave identificadora única (excluyendo la aplicación actual)
        if (!aplicacionExistente.getLlaveIdentificadora().equals(request.llaveIdentificadora())) {
            if (aplicacionRepository.existsByLlaveIdentificadora(request.llaveIdentificadora())) {
                throw new IllegalArgumentException("Ya existe una aplicación con la llave identificadora: " + request.llaveIdentificadora());
            }
        }

        // Validar URL única (excluyendo la aplicación actual)
        if (!aplicacionExistente.getUrl().equals(request.url())) {
            if (aplicacionRepository.existsByUrl(request.url())) {
                throw new IllegalArgumentException("Ya existe una aplicación con la URL: " + request.url());
            }
        }
    }
}
