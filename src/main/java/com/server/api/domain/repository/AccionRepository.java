package com.server.api.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.server.api.domain.entity.Accion;

/**
 * Repositorio para la entidad Accion.
 * Proporciona operaciones CRUD y consultas personalizadas.
 * Sigue el principio DRY reutilizando funcionalidad de JpaRepository.
 * Implementa el principio DIP al depender de abstracciones de Spring Data.
 */
@Repository
public interface AccionRepository extends JpaRepository<Accion, UUID> {

    /**
     * Busca todas las acciones activas (no eliminadas).
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL")
    List<Accion> findAllActive();

    /**
     * Busca todas las acciones activas con paginación.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL")
    Page<Accion> findAllActive(Pageable pageable);

    /**
     * Busca una acción por ID solo si está activa.
     */
    @Query("SELECT a FROM Accion a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Accion> findByIdAndActive(@Param("id") UUID id);

    /**
     * Busca acciones por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Accion> findByNombreContainingIgnoreCaseAndActive(@Param("nombre") String nombre);

    /**
     * Busca acciones por nombre con paginación.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Accion> findByNombreContainingIgnoreCaseAndActive(@Param("nombre") String nombre, Pageable pageable);

    /**
     * Busca acciones por texto en nombre o descripción.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "(LOWER(a.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Accion> findByTextoEnNombreOrDescripcion(@Param("texto") String texto);

    /**
     * Busca acciones por aplicación.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND a.aplicacion.id = :aplicacionId")
    List<Accion> findByAplicacionIdAndActive(@Param("aplicacionId") UUID aplicacionId);

    /**
     * Busca acciones por aplicación con paginación.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND a.aplicacion.id = :aplicacionId")
    Page<Accion> findByAplicacionIdAndActive(@Param("aplicacionId") UUID aplicacionId, Pageable pageable);

    /**
     * Busca acciones por sección.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND a.seccion.id = :seccionId")
    List<Accion> findBySeccionIdAndActive(@Param("seccionId") UUID seccionId);

    /**
     * Busca acciones por sección con paginación.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND a.seccion.id = :seccionId")
    Page<Accion> findBySeccionIdAndActive(@Param("seccionId") UUID seccionId, Pageable pageable);

    /**
     * Busca acciones por aplicación y sección.
     */
    @Query("SELECT a FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "a.aplicacion.id = :aplicacionId AND a.seccion.id = :seccionId")
    List<Accion> findByAplicacionIdAndSeccionIdAndActive(
        @Param("aplicacionId") UUID aplicacionId, 
        @Param("seccionId") UUID seccionId
    );

    /**
     * Verifica si existe una acción con el nombre dado en una aplicación y sección específica.
     * Útil para validar duplicados en creación.
     */
    @Query("SELECT COUNT(a) > 0 FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "LOWER(a.nombre) = LOWER(:nombre) AND a.aplicacion.id = :aplicacionId AND a.seccion.id = :seccionId")
    boolean existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(
        @Param("nombre") String nombre, 
        @Param("aplicacionId") UUID aplicacionId, 
        @Param("seccionId") UUID seccionId
    );

    /**
     * Verifica si existe una acción con el nombre dado en una aplicación y sección específica (excluyendo una ID específica).
     * Útil para validar duplicados en actualizaciones.
     */
    @Query("SELECT COUNT(a) > 0 FROM Accion a WHERE a.deletedAt IS NULL AND " +
           "LOWER(a.nombre) = LOWER(:nombre) AND a.aplicacion.id = :aplicacionId AND " +
           "a.seccion.id = :seccionId AND a.id != :excludeId")
    boolean existsByNombreIgnoreCaseAndAplicacionIdAndSeccionIdAndIdNot(
        @Param("nombre") String nombre, 
        @Param("aplicacionId") UUID aplicacionId, 
        @Param("seccionId") UUID seccionId, 
        @Param("excludeId") UUID excludeId
    );

    /**
     * Cuenta el total de acciones activas.
     */
    @Query("SELECT COUNT(a) FROM Accion a WHERE a.deletedAt IS NULL")
    long countActive();

    /**
     * Cuenta acciones por aplicación.
     */
    @Query("SELECT COUNT(a) FROM Accion a WHERE a.deletedAt IS NULL AND a.aplicacion.id = :aplicacionId")
    long countByAplicacionIdAndActive(@Param("aplicacionId") UUID aplicacionId);

    /**
     * Cuenta acciones por sección.
     */
    @Query("SELECT COUNT(a) FROM Accion a WHERE a.deletedAt IS NULL AND a.seccion.id = :seccionId")
    long countBySeccionIdAndActive(@Param("seccionId") UUID seccionId);
}
