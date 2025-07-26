package com.server.api.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad base que contiene campos comunes para auditoría y borrado lógico.
 * Implementa el principio DRY evitando repetir estos campos en cada entidad.
 * Sigue el principio SOLID (SRP) al tener una sola responsabilidad: proveer funcionalidad de auditoría.
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted_at IS NULL")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * Realiza un borrado lógico estableciendo la fecha de eliminación.
     * Implementa el principio KISS manteniendo la lógica simple.
     */
    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    /**
     * Verifica si la entidad ha sido borrada lógicamente.
     * @return true si la entidad está marcada como eliminada
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Restaura una entidad borrada lógicamente.
     */
    public void restore() {
        this.deletedAt = null;
    }
}
