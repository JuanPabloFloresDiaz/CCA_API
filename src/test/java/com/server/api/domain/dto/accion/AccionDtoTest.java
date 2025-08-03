package com.server.api.domain.dto.accion;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Tests unitarios para DTOs de Accion.
 * Valida la construcción, validación y comportamientos de los Records.
 */
@DisplayName("Accion DTOs - Tests Unitarios")
class AccionDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("AccionCreateRequest - Validación exitosa")
    void accionCreateRequest_DeberiaValidarExitosamente() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Crear Usuario",
            "Permite crear nuevos usuarios en el sistema",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Crear Usuario");
        assertThat(request.descripcion()).isEqualTo("Permite crear nuevos usuarios en el sistema");
        assertThat(request.aplicacionId()).isNotNull();
        assertThat(request.seccionId()).isNotNull();
    }

    @Test
    @DisplayName("AccionCreateRequest - Fallar con nombre vacío")
    void accionCreateRequest_DeberiaFallarConNombreVacio() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "",
            "Descripción válida",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El nombre es requerido"))).isTrue();
    }

    @Test
    @DisplayName("AccionCreateRequest - Fallar con nombre muy largo")
    void accionCreateRequest_DeberiaFallarConNombreMuyLargo() {
        // Given
        String nombreLargo = "a".repeat(101); // Más de 100 caracteres
        AccionCreateRequest request = new AccionCreateRequest(
            nombreLargo,
            "Descripción válida",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El nombre debe tener entre 2 y 100 caracteres"))).isTrue();
    }

    @Test
    @DisplayName("AccionCreateRequest - Fallar con descripción muy larga")
    void accionCreateRequest_DeberiaFallarConDescripcionMuyLarga() {
        // Given
        String descripcionLarga = "a".repeat(1001); // Más de 1000 caracteres
        AccionCreateRequest request = new AccionCreateRequest(
            "Nombre Válido",
            descripcionLarga,
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("La descripción no puede exceder 1000 caracteres"))).isTrue();
    }

    @Test
    @DisplayName("AccionCreateRequest - Fallar con aplicacionId nulo")
    void accionCreateRequest_DeberiaFallarConAplicacionIdNulo() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Nombre Válido",
            "Descripción válida",
            null,
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El ID de la aplicación es requerido"))).isTrue();
    }

    @Test
    @DisplayName("AccionCreateRequest - Fallar con seccionId nulo")
    void accionCreateRequest_DeberiaFallarConSeccionIdNulo() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Nombre Válido",
            "Descripción válida",
            UUID.randomUUID(),
            null
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El ID de la sección es requerido"))).isTrue();
    }

    @Test
    @DisplayName("AccionUpdateRequest - Validación exitosa")
    void accionUpdateRequest_DeberiaValidarExitosamente() {
        // Given
        AccionUpdateRequest request = new AccionUpdateRequest(
            "Actualizar Usuario",
            "Permite actualizar información de usuarios",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Actualizar Usuario");
        assertThat(request.descripcion()).isEqualTo("Permite actualizar información de usuarios");
        assertThat(request.aplicacionId()).isNotNull();
        assertThat(request.seccionId()).isNotNull();
    }

    @Test
    @DisplayName("AccionResponse - Constructor y getters")
    void accionResponse_DeberiaFuncionarCorrectamente() {
        // Given
        UUID id = UUID.randomUUID();
        AccionResponse.AplicacionBasicInfo aplicacion = new AccionResponse.AplicacionBasicInfo(
            UUID.randomUUID(),
            "Sistema de Usuarios",
            "USER_SYSTEM"
        );
        AccionResponse.SeccionBasicInfo seccion = new AccionResponse.SeccionBasicInfo(
            UUID.randomUUID(),
            "Gestión de Usuarios"
        );
        
        AccionResponse response = new AccionResponse(
            id,
            "Eliminar Usuario",
            "Permite eliminar usuarios del sistema",
            aplicacion,
            seccion,
            true,
            null,
            null,
            null
        );

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.nombre()).isEqualTo("Eliminar Usuario");
        assertThat(response.descripcion()).isEqualTo("Permite eliminar usuarios del sistema");
        assertThat(response.aplicacion().nombre()).isEqualTo("Sistema de Usuarios");
        assertThat(response.seccion().nombre()).isEqualTo("Gestión de Usuarios");
        assertThat(response.activo()).isTrue();
        assertThat(response.createdAt()).isNull();
        assertThat(response.updatedAt()).isNull();
    }

    @Test
    @DisplayName("AccionSummary - Constructor y getters")
    void accionSummary_DeberiaFuncionarCorrectamente() {
        // Given
        UUID id = UUID.randomUUID();
        AccionSummary summary = new AccionSummary(
            id,
            "Consultar Usuario",
            "Consulta información de usuarios del sistema",
            "Sistema de Usuarios",
            "Gestión de Usuarios",
            true
        );

        // Then
        assertThat(summary.id()).isEqualTo(id);
        assertThat(summary.nombre()).isEqualTo("Consultar Usuario");
        assertThat(summary.descripcion()).isEqualTo("Consulta información de usuarios del sistema");
        assertThat(summary.aplicacionNombre()).isEqualTo("Sistema de Usuarios");
        assertThat(summary.seccionNombre()).isEqualTo("Gestión de Usuarios");
        assertThat(summary.activo()).isTrue();
    }

    @Test
    @DisplayName("AccionSummary - Descripción truncada automáticamente")
    void accionSummary_DescripcionTruncadaAutomaticamente() {
        // Given
        String descripcionLarga = "a".repeat(150); // Más de 100 caracteres
        AccionSummary summary = new AccionSummary(
            UUID.randomUUID(),
            "Acción con descripción larga",
            descripcionLarga,
            "App",
            "Sección",
            true
        );

        // Then
        assertThat(summary.descripcion()).hasSize(100); // Truncada a 100 caracteres
        assertThat(summary.descripcion()).endsWith("...");
    }

    @Test
    @DisplayName("AccionResponse - Método estaEliminada funciona correctamente")
    void accionResponse_MetodoEstaEliminadaFuncionaCorrectamente() {
        // Given
        AccionResponse responseActiva = new AccionResponse(
            UUID.randomUUID(), "Test", "Test", null, null, true, null, null, null
        );
        AccionResponse responseEliminada = new AccionResponse(
            UUID.randomUUID(), "Test", "Test", null, null, false, null, null, java.time.OffsetDateTime.now()
        );

        // Then
        assertThat(responseActiva.estaEliminada()).isFalse();
        assertThat(responseEliminada.estaEliminada()).isTrue();
    }

    @Test
    @DisplayName("AccionResponse - Método estaActiva funciona correctamente")
    void accionResponse_MetodoEstaActivaFuncionaCorrectamente() {
        // Given
        AccionResponse responseActiva = new AccionResponse(
            UUID.randomUUID(), "Test", "Test", null, null, true, null, null, null
        );
        AccionResponse responseInactiva = new AccionResponse(
            UUID.randomUUID(), "Test", "Test", null, null, false, null, null, null
        );
        AccionResponse responseEliminada = new AccionResponse(
            UUID.randomUUID(), "Test", "Test", null, null, true, null, null, java.time.OffsetDateTime.now()
        );

        // Then
        assertThat(responseActiva.estaActiva()).isTrue();
        assertThat(responseInactiva.estaActiva()).isFalse();
        assertThat(responseEliminada.estaActiva()).isFalse();
    }

    @Test
    @DisplayName("Records - Igualdad y hashCode")
    void records_DeberianTenerIgualdadYHashCodeCorrectos() {
        // Given
        UUID appId = UUID.randomUUID();
        UUID secId = UUID.randomUUID();
        
        AccionCreateRequest request1 = new AccionCreateRequest("Nombre", "Desc", appId, secId);
        AccionCreateRequest request2 = new AccionCreateRequest("Nombre", "Desc", appId, secId);
        AccionCreateRequest request3 = new AccionCreateRequest("Otro", "Desc", appId, secId);

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    @DisplayName("AccionCreateRequest - Descripción opcional puede ser nula")
    void accionCreateRequest_DescripcionOpcionalPuedeSerNula() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Acción Sin Descripción",
            null,
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<AccionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.descripcion()).isNull();
    }

    @Test
    @DisplayName("AccionCreateRequest - Constructor compacto limpia espacios")
    void accionCreateRequest_ConstructorCompactoLimpiaEspacios() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "  Nombre con espacios  ",
            "  Descripción con espacios  ",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // Then
        assertThat(request.nombre()).isEqualTo("Nombre con espacios");
        assertThat(request.descripcion()).isEqualTo("Descripción con espacios");
    }

    @Test
    @DisplayName("AccionCreateRequest - Constructor compacto convierte descripción vacía a null")
    void accionCreateRequest_ConstructorCompactoConvierteDescripcionVaciaANull() {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Nombre válido",
            "   ",  // Solo espacios
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // Then
        assertThat(request.descripcion()).isNull();
    }
}
