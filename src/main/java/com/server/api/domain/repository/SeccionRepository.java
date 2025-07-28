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

import com.server.api.domain.entity.Seccion;

/**
 * Repositorio para la entidad Seccion.
 * Proporciona operaciones CRUD y consultas personalizadas.
 * Sigue el principio DRY reutilizando funcionalidad de JpaRepository.
 */
@Repository
public interface SeccionRepository extends JpaRepository<Seccion, UUID> {

    /**
     * Busca todas las secciones activas (no eliminadas).
     */
    @Query("SELECT s FROM Seccion s WHERE s.deletedAt IS NULL")
    List<Seccion> findAllActive();

    /**
     * Busca todas las secciones activas con paginación.
     */
    @Query("SELECT s FROM Seccion s WHERE s.deletedAt IS NULL")
    Page<Seccion> findAllActive(Pageable pageable);

    /**
     * Busca una sección por ID solo si está activa.
     */
    @Query("SELECT s FROM Seccion s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Seccion> findByIdAndActive(@Param("id") UUID id);

    /**
     * Busca secciones por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT s FROM Seccion s WHERE s.deletedAt IS NULL AND " +
           "LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Seccion> findByNombreContainingIgnoreCaseAndActive(@Param("nombre") String nombre);

    /**
     * Busca secciones por nombre con paginación.
     */
    @Query("SELECT s FROM Seccion s WHERE s.deletedAt IS NULL AND " +
           "LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Seccion> findByNombreContainingIgnoreCaseAndActive(@Param("nombre") String nombre, Pageable pageable);

    /**
     * Busca secciones por texto en nombre o descripción.
     */
    @Query("SELECT s FROM Seccion s WHERE s.deletedAt IS NULL AND " +
           "(LOWER(s.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Seccion> findByTextoEnNombreOrDescripcion(@Param("texto") String texto);

    /**
     * Verifica si existe una sección con el nombre especificado (excluyendo una ID específica).
     * Útil para validar duplicados en actualizaciones.
     */
    @Query("SELECT COUNT(s) > 0 FROM Seccion s WHERE s.deletedAt IS NULL AND " +
           "LOWER(s.nombre) = LOWER(:nombre) AND s.id != :excludeId")
    boolean existsByNombreIgnoreCaseAndIdNot(@Param("nombre") String nombre, @Param("excludeId") UUID excludeId);

    /**
     * Verifica si existe una sección con el nombre especificado.
     * Útil para validar duplicados en creación.
     */
    @Query("SELECT COUNT(s) > 0 FROM Seccion s WHERE s.deletedAt IS NULL AND " +
           "LOWER(s.nombre) = LOWER(:nombre)")
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);

    /**
     * Cuenta el total de secciones activas.
     */
    @Query("SELECT COUNT(s) FROM Seccion s WHERE s.deletedAt IS NULL")
    long countActive();
}
