package com.server.api.domain.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa las aplicaciones registradas en el sistema de control de acceso.
 * Cada aplicación puede tener múltiples tipos de usuario, acciones y auditorías.
 * Sigue el principio SRP al manejar únicamente información de aplicaciones.
 */
@Entity
@Table(name = "aplicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aplicacion extends BaseEntity {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "url", nullable = false, length = 255)
    private String url;

    @Column(name = "llave_identificadora", nullable = false, length = 100, unique = true)
    private String llaveIdentificadora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10)
    private EstadoAplicacion estado = EstadoAplicacion.ACTIVO;

    // Relaciones
    @OneToMany(mappedBy = "aplicacion", cascade = CascadeType.ALL)
    private List<TipoUsuario> tiposUsuario;

    @OneToMany(mappedBy = "aplicacion", cascade = CascadeType.ALL)
    private List<Accion> acciones;

    @OneToMany(mappedBy = "aplicacion", cascade = CascadeType.ALL)
    private List<AuditoriaAcceso> auditoriasAcceso;

    /**
     * Enum para el estado de la aplicación
     */
    public enum EstadoAplicacion {
        ACTIVO("activo"),
        INACTIVO("inactivo");

        private final String valor;

        EstadoAplicacion(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }
}
