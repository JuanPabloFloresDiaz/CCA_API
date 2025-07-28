package com.server.api.domain.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa las sesiones de usuario en el sistema.
 * Maneja el ciclo de vida de las sesiones y información de seguridad.
 * Sigue el principio SRP al manejar únicamente información de sesiones.
 */
@Entity
@Table(name = "sesiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sesion extends BaseEntity {

    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    @Column(name = "email_usuario", nullable = false, length = 100)
    private String emailUsuario;

    @Column(name = "ip_origen", nullable = false, length = 45)
    private String ipOrigen;

    @Column(name = "informacion_dispositivo", columnDefinition = "TEXT")
    private String informacionDispositivo;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio = OffsetDateTime.now();

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10, nullable = false)
    private EstadoSesion estado = EstadoSesion.ACTIVA;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Enum para el estado de la sesión
     */
    public enum EstadoSesion {
        ACTIVA("activa"),
        CERRADA("cerrada"),
        EXPIRADA("expirada");

        private final String valor;

        EstadoSesion(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    /**
     * Verifica si la sesión está activa.
     * Método de consulta simple.
     */
    public boolean estaActiva() {
        return EstadoSesion.ACTIVA.equals(this.estado);
    }

    /**
     * Verifica si la sesión ha expirado por tiempo.
     * Método de consulta simple.
     */
    public boolean haExpiradoPorTiempo() {
        return OffsetDateTime.now().isAfter(this.fechaExpiracion);
    }

    /**
     * Verifica si la sesión está cerrada.
     * Método de consulta simple.
     */
    public boolean estaCerrada() {
        return EstadoSesion.CERRADA.equals(this.estado);
    }
}
