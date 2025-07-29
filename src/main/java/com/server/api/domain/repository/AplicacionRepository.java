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

import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;

/**
 * Repository para operaciones de persistencia de Aplicacion.
 * Extiende JpaRepository para operaciones CRUD básicas.
 * Implementa el principio DIP al depender de abstracciones de Spring Data.
 */
@Repository
public interface AplicacionRepository extends JpaRepository<Aplicacion, UUID> {

    /**
     * Busca una aplicación por su llave identificadora.
     * Útil para validaciones de unicidad y autenticación.
     * 
     * @param llaveIdentificadora la llave única de la aplicación
     * @return Optional con la aplicación si existe
     */
    Optional<Aplicacion> findByLlaveIdentificadora(String llaveIdentificadora);

    /**
     * Verifica si existe una aplicación con la llave identificadora dada.
     * Implementa el principio YAGNI - solo lo que necesitamos ahora.
     * 
     * @param llaveIdentificadora la llave a verificar
     * @return true si existe, false si no
     */
    boolean existsByLlaveIdentificadora(String llaveIdentificadora);

    /**
     * Busca aplicaciones por nombre (búsqueda parcial, insensible a mayúsculas).
     * 
     * @param nombre parte del nombre a buscar
     * @return lista de aplicaciones que coinciden
     */
    @Query("SELECT a FROM Aplicacion a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Aplicacion> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca aplicaciones por estado específico.
     * 
     * @param estado el estado a filtrar
     * @return lista de aplicaciones con el estado especificado
     */
    List<Aplicacion> findByEstado(EstadoAplicacion estado);

    /**
     * Obtiene aplicaciones activas con paginación.
     * Implementa el principio KISS manteniendo la consulta simple.
     * 
     * @param pageable configuración de paginación
     * @return página de aplicaciones activas
     */
    @Query("SELECT a FROM Aplicacion a WHERE a.estado = :estado")
    Page<Aplicacion> findByEstado(@Param("estado") EstadoAplicacion estado, Pageable pageable);

    /**
     * Cuenta el número de aplicaciones activas.
     * Útil para estadísticas y dashboards.
     * 
     * @return número de aplicaciones activas
     */
    @Query("SELECT COUNT(a) FROM Aplicacion a WHERE a.estado = 'ACTIVO'")
    long countAplicacionesActivas();

    /**
     * Busca aplicaciones por URL (para evitar duplicados).
     * 
     * @param url la URL a verificar
     * @return Optional con la aplicación si existe
     */
    Optional<Aplicacion> findByUrl(String url);

    /**
     * Verifica si existe una aplicación con la URL dada.
     * 
     * @param url la URL a verificar
     * @return true si existe, false si no
     */
    boolean existsByUrl(String url);
}
