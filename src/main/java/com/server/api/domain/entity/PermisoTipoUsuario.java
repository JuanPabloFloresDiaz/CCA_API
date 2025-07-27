package com.server.api.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa los permisos asignados a los tipos de usuario.
 * Define qué acciones puede realizar cada tipo de usuario.
 * Sigue el principio SRP al manejar únicamente la relación permisos-tipo de usuario.
 */
@Entity
@Table(name = "permisos_tipo_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoTipoUsuario extends BaseEntity {

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_usuario_id", nullable = false)
    private TipoUsuario tipoUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accion_id", nullable = false)
    private Accion accion;
}
