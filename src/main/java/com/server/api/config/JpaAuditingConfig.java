package com.server.api.config;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración para habilitar la auditoría de JPA.
 * Permite que las anotaciones @CreatedDate y @LastModifiedDate funcionen automáticamente.
 * Configurado para usar OffsetDateTime en lugar de LocalDateTime.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    /**
     * Proveedor de fechas que retorna OffsetDateTime.now() para la auditoría.
     * Necesario para que las anotaciones @CreatedDate y @LastModifiedDate
     * funcionen correctamente con OffsetDateTime.
     */
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
