package com.server.api.domain.dto.seccion;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para DTOs de Sección.
 * Prueba validaciones y comportamientos de los Records.
 */
@DisplayName("DTOs de Sección - Tests Unitarios")
class SeccionDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("SeccionCreateRequest - Validación exitosa")
    void seccionCreateRequest_DeberiaValidarExitosamente() {
        // Given
        SeccionCreateRequest request = new SeccionCreateRequest(
            "Gestión de Usuarios",
            "Sección para administrar usuarios del sistema"
        );

        // When
        Set<ConstraintViolation<SeccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Gestión de Usuarios");
        assertThat(request.descripcion()).isEqualTo("Sección para administrar usuarios del sistema");
    }

    @Test
    @DisplayName("SeccionCreateRequest - Fallar con nombre vacío")
    void seccionCreateRequest_DeberiaFallarConNombreVacio() {
        // Given
        SeccionCreateRequest request = new SeccionCreateRequest(
            "",
            "Descripción válida"
        );

        // When
        Set<ConstraintViolation<SeccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // @NotBlank y @Size van a fallar
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().equals("El nombre es requerido"))).isTrue();
    }

    @Test
    @DisplayName("SeccionCreateRequest - Fallar con nombre muy largo")
    void seccionCreateRequest_DeberiaFallarConNombreMuyLargo() {
        // Given
        String nombreLargo = "a".repeat(101); // Más de 100 caracteres
        SeccionCreateRequest request = new SeccionCreateRequest(
            nombreLargo,
            "Descripción válida"
        );

        // When
        Set<ConstraintViolation<SeccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("El nombre debe tener entre 2 y 100 caracteres");
    }

    @Test
    @DisplayName("SeccionCreateRequest - Fallar con descripción muy larga")
    void seccionCreateRequest_DeberiaFallarConDescripcionMuyLarga() {
        // Given
        String descripcionLarga = "a".repeat(1001); // Más de 1000 caracteres
        SeccionCreateRequest request = new SeccionCreateRequest(
            "Nombre válido",
            descripcionLarga
        );

        // When
        Set<ConstraintViolation<SeccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("La descripción no debe exceder los 1000 caracteres");
    }

    @Test
    @DisplayName("SeccionUpdateRequest - Validación exitosa")
    void seccionUpdateRequest_DeberiaValidarExitosamente() {
        // Given
        SeccionUpdateRequest request = new SeccionUpdateRequest(
            "Gestión de Usuarios Actualizada",
            "Descripción actualizada"
        );

        // When
        Set<ConstraintViolation<SeccionUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Gestión de Usuarios Actualizada");
        assertThat(request.descripcion()).isEqualTo("Descripción actualizada");
    }

    @Test
    @DisplayName("SeccionResponse - Constructor y getters")
    void seccionResponse_DeberiaFuncionarCorrectamente() {
        // Given
        UUID id = UUID.randomUUID();
        SeccionResponse response = new SeccionResponse(
            id,
            "Gestión de Usuarios",
            "Descripción de la sección",
            true,
            null,
            null,
            null
        );

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.nombre()).isEqualTo("Gestión de Usuarios");
        assertThat(response.descripcion()).isEqualTo("Descripción de la sección");
        assertThat(response.activo()).isTrue();
        assertThat(response.createdAt()).isNull();
        assertThat(response.updatedAt()).isNull();
        assertThat(response.deletedAt()).isNull();
    }

    @Test
    @DisplayName("SeccionSummary - Constructor compacto con valores por defecto")
    void seccionSummary_DeberiaAplicarValoresPorDefecto() {
        // Given
        UUID id = UUID.randomUUID();
        SeccionSummary summary = new SeccionSummary(
            id,
            "Gestión de Usuarios",
            "Descripción de la sección",
            null // activo es null, debería ser true por defecto
        );

        // Then
        assertThat(summary.activo()).isTrue(); // Valor por defecto aplicado
        assertThat(summary.id()).isEqualTo(id);
        assertThat(summary.nombre()).isEqualTo("Gestión de Usuarios");
        assertThat(summary.descripcion()).isEqualTo("Descripción de la sección");
    }

    @Test
    @DisplayName("SeccionSummary - Truncar descripción larga")
    void seccionSummary_DeberiaTruncarDescripcionLarga() {
        // Given
        UUID id = UUID.randomUUID();
        String descripcionLarga = "Esta es una descripción muy larga que excede los 100 caracteres permitidos para el summary y debería ser truncada automáticamente por el constructor compacto del record";
        
        SeccionSummary summary = new SeccionSummary(
            id,
            "Gestión de Usuarios",
            descripcionLarga,
            true
        );

        // Then
        assertThat(summary.descripcion()).hasSize(100); // 97 + "..."
        assertThat(summary.descripcion()).endsWith("...");
        assertThat(summary.descripcion()).startsWith("Esta es una descripción muy larga");
    }

    @Test
    @DisplayName("SeccionSummary - No truncar descripción corta")
    void seccionSummary_NoDeberiaTruncarDescripcionCorta() {
        // Given
        UUID id = UUID.randomUUID();
        String descripcionCorta = "Descripción corta";
        
        SeccionSummary summary = new SeccionSummary(
            id,
            "Gestión de Usuarios",
            descripcionCorta,
            true
        );

        // Then
        assertThat(summary.descripcion()).isEqualTo("Descripción corta");
        assertThat(summary.descripcion()).doesNotEndWith("...");
    }

    @Test
    @DisplayName("Records - Igualdad y hashCode")
    void records_DeberianTenerIgualdadYHashCodeCorrectos() {
        // Given
        UUID id = UUID.randomUUID();
        SeccionSummary summary1 = new SeccionSummary(id, "Nombre", "Descripción", true);
        SeccionSummary summary2 = new SeccionSummary(id, "Nombre", "Descripción", true);
        SeccionSummary summary3 = new SeccionSummary(UUID.randomUUID(), "Nombre", "Descripción", true);

        // Then
        assertThat(summary1).isEqualTo(summary2);
        assertThat(summary1.hashCode()).isEqualTo(summary2.hashCode());
        assertThat(summary1).isNotEqualTo(summary3);
        assertThat(summary1.hashCode()).isNotEqualTo(summary3.hashCode());
    }

    @Test
    @DisplayName("Records - toString contiene todos los campos")
    void records_ToStringDeberiaContenerTodosLosCampos() {
        // Given
        UUID id = UUID.randomUUID();
        SeccionSummary summary = new SeccionSummary(id, "Gestión", "Descripción", true);

        // When
        String toString = summary.toString();

        // Then
        assertThat(toString).contains("SeccionSummary");
        assertThat(toString).contains(id.toString());
        assertThat(toString).contains("Gestión");
        assertThat(toString).contains("Descripción");
        assertThat(toString).contains("true");
    }
}
