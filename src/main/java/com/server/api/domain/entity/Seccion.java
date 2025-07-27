package com.server.api.domain.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa las secciones del sistema.
 * Las secciones agrupan funcionalidades y acciones relacionadas.
 * Sigue el principio SRP al manejar únicamente información de secciones.
 */
@Entity
@Table(name = "secciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seccion extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Relaciones
    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    private List<Accion> acciones;
}
