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
     * Método para cerrar la sesión
     */
    public void cerrarSesion() {
        this.estado = EstadoSesion.CERRADA;
        this.fechaFin = OffsetDateTime.now();
    }

    /**
     * Método para marcar la sesión como expirada
     */
    public void expirar() {
        this.estado = EstadoSesion.EXPIRADA;
        this.fechaFin = OffsetDateTime.now();
    }

    /**
     * Verifica si la sesión está activa
     */
    public boolean estaActiva() {
        return this.estado == EstadoSesion.ACTIVA && 
               OffsetDateTime.now().isBefore(this.fechaExpiracion);
    }

    /**
     * Verifica si la sesión ha expirado
     */
    public boolean haExpirado() {
        return OffsetDateTime.now().isAfter(this.fechaExpiracion);
    }
}
