package com.server.api.domain.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
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
 * Entidad que representa la auditoría de accesos al sistema.
 * Registra todas las actividades y accesos de los usuarios.
 * Utiliza particionamiento por fecha para optimizar el rendimiento.
 * Sigue el principio SRP al manejar únicamente información de auditoría.
 * 
 * Nota: Esta entidad NO extiende BaseEntity porque usa una clave compuesta
 * y maneja sus propios campos de auditoría.
 */
@Entity
@Table(name = "auditoria_accesos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaAcceso {

    @EmbeddedId
    private AuditoriaAccesoId id;

    @Column(name = "email_usuario", nullable = false, length = 100)
    private String emailUsuario;

    @Column(name = "ip_origen", nullable = false, length = 45)
    private String ipOrigen;

    @Column(name = "informacion_dispositivo", columnDefinition = "TEXT")
    private String informacionDispositivo;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10, nullable = false)
    private EstadoAuditoria estado = EstadoAuditoria.EXITOSO;

    // Campos de auditoría propios (no heredados)
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Puede ser null para intentos fallidos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accion_id", nullable = false)
    private Accion accion;

    /**
     * Enum para el estado de la auditoría
     */
    public enum EstadoAuditoria {
        EXITOSO("exitoso"),
        FALLIDO("fallido");

        private final String valor;

        EstadoAuditoria(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    /**
     * Realiza un borrado lógico estableciendo la fecha de eliminación.
     */
    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Verifica si la entidad ha sido borrada lógicamente.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Restaura una entidad borrada lógicamente.
     */
    public void restore() {
        this.deletedAt = null;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Constructor de conveniencia para crear auditorías exitosas
     */
    public static AuditoriaAcceso exitoso(Usuario usuario, String emailUsuario, 
                                         Aplicacion aplicacion, Accion accion,
                                         String ipOrigen, String mensaje) {
        AuditoriaAcceso auditoria = new AuditoriaAcceso();
        auditoria.setId(new AuditoriaAccesoId(java.util.UUID.randomUUID(), OffsetDateTime.now()));
        auditoria.setUsuario(usuario);
        auditoria.setEmailUsuario(emailUsuario);
        auditoria.setAplicacion(aplicacion);
        auditoria.setAccion(accion);
        auditoria.setIpOrigen(ipOrigen);
        auditoria.setMensaje(mensaje);
        auditoria.setEstado(EstadoAuditoria.EXITOSO);
        return auditoria;
    }

    /**
     * Constructor de conveniencia para crear auditorías fallidas
     */
    public static AuditoriaAcceso fallido(String emailUsuario, Aplicacion aplicacion, 
                                         Accion accion, String ipOrigen, String mensaje) {
        AuditoriaAcceso auditoria = new AuditoriaAcceso();
        auditoria.setId(new AuditoriaAccesoId(java.util.UUID.randomUUID(), OffsetDateTime.now()));
        auditoria.setUsuario(null); // Sin usuario para intentos fallidos
        auditoria.setEmailUsuario(emailUsuario);
        auditoria.setAplicacion(aplicacion);
        auditoria.setAccion(accion);
        auditoria.setIpOrigen(ipOrigen);
        auditoria.setMensaje(mensaje);
        auditoria.setEstado(EstadoAuditoria.FALLIDO);
        return auditoria;
    }
}
