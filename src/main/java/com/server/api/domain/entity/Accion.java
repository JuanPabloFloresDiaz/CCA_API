package com.server.api.domain.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa las acciones que se pueden realizar en las aplicaciones.
 * Cada acción pertenece a una aplicación y una sección específica.
 * Sigue el principio SRP al manejar únicamente información de acciones.
 */
@Entity
@Table(name = "acciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Accion extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;

    @OneToMany(mappedBy = "accion", cascade = CascadeType.ALL)
    private List<PermisoTipoUsuario> permisos;

    @OneToMany(mappedBy = "accion", cascade = CascadeType.ALL)
    private List<AuditoriaAcceso> auditoriasAcceso;
}
