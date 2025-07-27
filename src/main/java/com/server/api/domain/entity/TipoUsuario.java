package com.server.api.domain.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidad que representa los tipos de usuario para las aplicaciones.
 * Define roles y perfiles que pueden ser asignados a los usuarios.
 * Sigue el principio SRP al manejar únicamente información de tipos de usuario.
 */
@Entity
@Table(name = "tipo_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoUsuario extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10)
    private EstadoTipoUsuario estado = EstadoTipoUsuario.ACTIVO;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @OneToMany(mappedBy = "tipoUsuario", cascade = CascadeType.ALL)
    private List<PermisoTipoUsuario> permisos;

    @OneToMany(mappedBy = "tipoUsuario", cascade = CascadeType.ALL)
    private List<UsuarioTipoUsuario> usuariosTipoUsuario;

    /**
     * Enum para el estado del tipo de usuario
     */
    public enum EstadoTipoUsuario {
        ACTIVO("activo"),
        INACTIVO("inactivo");

        private final String valor;

        EstadoTipoUsuario(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }
}
