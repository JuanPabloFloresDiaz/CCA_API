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

import com.server.api.domain.entity.TipoUsuario;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;

/**
 * Repositorio para la gestión de tipos de usuario.
 * Proporciona operaciones de acceso a datos para la entidad TipoUsuario.
 */
@Repository
public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, UUID> {

    /**
     * Busca tipos de usuario activos (no eliminados) por ID
     */
    Optional<TipoUsuario> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Busca todos los tipos de usuario activos (no eliminados)
     */
    List<TipoUsuario> findByDeletedAtIsNull();

    /**
     * Busca tipos de usuario activos paginados
     */
    Page<TipoUsuario> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Busca tipos de usuario por nombre (case insensitive) y activos
     */
    @Query("SELECT t FROM TipoUsuario t WHERE t.deletedAt IS NULL AND LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<TipoUsuario> findByNombreContainingIgnoreCaseAndDeletedAtIsNull(@Param("nombre") String nombre, Pageable pageable);

    /**
     * Busca tipos de usuario por aplicación y activos
     */
    @Query("SELECT t FROM TipoUsuario t WHERE t.deletedAt IS NULL AND t.aplicacion.id = :aplicacionId")
    Page<TipoUsuario> findByAplicacionIdAndDeletedAtIsNull(@Param("aplicacionId") UUID aplicacionId, Pageable pageable);

    /**
     * Busca tipos de usuario por estado y activos
     */
    @Query("SELECT t FROM TipoUsuario t WHERE t.deletedAt IS NULL AND t.estado = :estado")
    Page<TipoUsuario> findByEstadoAndDeletedAtIsNull(@Param("estado") EstadoTipoUsuario estado, Pageable pageable);

    /**
     * Busca tipos de usuario con múltiples filtros
     */
    @Query("SELECT t FROM TipoUsuario t WHERE t.deletedAt IS NULL " +
           "AND (:nombre IS NULL OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:aplicacionId IS NULL OR t.aplicacion.id = :aplicacionId) " +
           "AND (:estado IS NULL OR t.estado = :estado)")
    Page<TipoUsuario> findByFilters(@Param("nombre") String nombre, 
                                   @Param("aplicacionId") UUID aplicacionId,
                                   @Param("estado") EstadoTipoUsuario estado,
                                   Pageable pageable);

    /**
     * Verifica si existe un tipo de usuario con el mismo nombre y aplicación (excluyendo el ID dado)
     */
    @Query("SELECT COUNT(t) > 0 FROM TipoUsuario t WHERE t.deletedAt IS NULL " +
           "AND LOWER(t.nombre) = LOWER(:nombre) AND t.aplicacion.id = :aplicacionId " +
           "AND (:excludeId IS NULL OR t.id != :excludeId)")
    boolean existsByNombreAndAplicacionIdAndDeletedAtIsNull(@Param("nombre") String nombre, 
                                                           @Param("aplicacionId") UUID aplicacionId,
                                                           @Param("excludeId") UUID excludeId);

    /**
     * Cuenta tipos de usuario activos
     */
    @Query("SELECT COUNT(t) FROM TipoUsuario t WHERE t.deletedAt IS NULL")
    long countByDeletedAtIsNull();

    /**
     * Cuenta tipos de usuario por aplicación
     */
    @Query("SELECT COUNT(t) FROM TipoUsuario t WHERE t.deletedAt IS NULL AND t.aplicacion.id = :aplicacionId")
    long countByAplicacionIdAndDeletedAtIsNull(@Param("aplicacionId") UUID aplicacionId);

    /**
     * Cuenta tipos de usuario por estado
     */
    @Query("SELECT COUNT(t) FROM TipoUsuario t WHERE t.deletedAt IS NULL AND t.estado = :estado")
    long countByEstadoAndDeletedAtIsNull(@Param("estado") EstadoTipoUsuario estado);
}
