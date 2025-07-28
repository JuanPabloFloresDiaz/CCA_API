package com.server.api.domain.entity;

import java.time.OffsetDateTime;
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
 * Entidad que representa los usuarios del sistema.
 * Sigue el principio SRP al manejar únicamente datos de usuarios.
 * La lógica de negocio se delega a servicios especializados.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends BaseEntity {

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 10)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    // Campos para Autenticación de Dos Factores (2FA)
    @Column(name = "dos_factor_activo", nullable = false)
    private Boolean dosFactorActivo = false;

    @Column(name = "dos_factor_secreto_totp", length = 255)
    private String dosFactorSecretoTotp;

    // Campos para Seguridad de Sesión
    @Column(name = "intentos_fallidos_sesion", nullable = false)
    private Integer intentosFallidosSesion = 0;

    @Column(name = "fecha_ultimo_intento_fallido")
    private OffsetDateTime fechaUltimoIntentoFallido;

    @Column(name = "fecha_bloqueo_sesion")
    private OffsetDateTime fechaBloqueoSesion;

    // Campos para Gestión de Contraseñas
    @Column(name = "fecha_ultimo_cambio_contrasena", nullable = false)
    private OffsetDateTime fechaUltimoCambioContrasena = OffsetDateTime.now();

    @Column(name = "requiere_cambio_contrasena", nullable = false)
    private Boolean requiereCambioContrasena = false;

    // Relaciones
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<UsuarioTipoUsuario> usuariosTipoUsuario;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Sesion> sesiones;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<AuditoriaAcceso> auditoriasAcceso;

    /**
     * Enum para el estado del usuario
     */
    public enum EstadoUsuario {
        ACTIVO("activo"),
        INACTIVO("inactivo");

        private final String valor;

        EstadoUsuario(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    // ✅ Solo métodos de consulta simple (queries), sin lógica de negocio
    
    /**
     * Verifica si el usuario tiene 2FA activo.
     * Método de consulta simple, sin lógica de negocio.
     */
    public boolean tieneDosFactorActivo() {
        return Boolean.TRUE.equals(this.dosFactorActivo);
    }

    /**
     * Verifica si el usuario está en estado activo.
     * Método de consulta simple, sin lógica de negocio.
     */
    public boolean estaActivo() {
        return EstadoUsuario.ACTIVO.equals(this.estado);
    }
}
