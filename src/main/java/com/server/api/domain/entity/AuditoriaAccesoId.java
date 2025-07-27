package com.server.api.domain.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa la clave compuesta para la entidad AuditoriaAcceso.
 * Utiliza ID y fecha para el particionamiento de la tabla.
 * Sigue el principio SRP al manejar únicamente la identificación compuesta.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaAccesoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private OffsetDateTime fecha;

    /**
     * Implementación personalizada de equals para claves compuestas.
     * Requerido por JPA para el manejo correcto de la clave primaria.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditoriaAccesoId that = (AuditoriaAccesoId) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(fecha, that.fecha);
    }

    /**
     * Implementación personalizada de hashCode para claves compuestas.
     * Requerido por JPA para el manejo correcto de la clave primaria.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, fecha);
    }
}
