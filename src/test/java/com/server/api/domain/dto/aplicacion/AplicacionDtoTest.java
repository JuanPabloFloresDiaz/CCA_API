package com.server.api.domain.dto.aplicacion;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Tests unitarios para DTOs de Aplicacion.
 * Valida la construcción, validación y comportamientos de los Records.
 */
@DisplayName("Aplicacion DTOs - Tests Unitarios")
class AplicacionDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Validación exitosa")
    void aplicacionCreateRequest_DeberiaValidarExitosamente() {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "Sistema de Pruebas",
            "Aplicación para testing del sistema",
            "https://test.example.com",
            "TEST_APP_001",
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Sistema de Pruebas");
        assertThat(request.descripcion()).isEqualTo("Aplicación para testing del sistema");
        assertThat(request.url()).isEqualTo("https://test.example.com");
        assertThat(request.llaveIdentificadora()).isEqualTo("TEST_APP_001");
        assertThat(request.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Fallar con nombre vacío")
    void aplicacionCreateRequest_DeberiaFallarConNombreVacio() {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "",
            "Descripción válida",
            "https://test.example.com",
            "TEST_APP_001",
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El nombre es requerido"))).isTrue();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Fallar con nombre muy largo")
    void aplicacionCreateRequest_DeberiaFallarConNombreMuyLargo() {
        // Given
        String nombreLargo = "a".repeat(101); // Más de 100 caracteres
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            nombreLargo,
            "Descripción válida",
            "https://test.example.com",
            "TEST_APP_001",
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El nombre debe tener entre 2 y 100 caracteres"))).isTrue();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Fallar con URL inválida")
    void aplicacionCreateRequest_DeberiaFallarConUrlInvalida() {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "Sistema Válido",
            "Descripción válida",
            "ftp://invalid.url",
            "TEST_APP_001",
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("La URL debe comenzar con http:// o https://"))).isTrue();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Fallar con llave identificadora inválida")
    void aplicacionCreateRequest_DeberiaFallarConLlaveInvalida() {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "Sistema Válido",
            "Descripción válida",
            "https://test.example.com",
            "test-app-001", // Minúsculas y guiones no permitidos
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("La llave identificadora solo puede contener letras mayúsculas, números y guiones bajos"))).isTrue();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Fallar con estado inválido")
    void aplicacionCreateRequest_DeberiaFallarConEstadoInvalido() {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "Sistema Válido",
            "Descripción válida",
            "https://test.example.com",
            "TEST_APP_001",
            "PENDIENTE" // Estado no válido
        );

        // When
        Set<ConstraintViolation<AplicacionCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations.stream()
            .anyMatch(v -> v.getMessage().contains("El estado debe ser ACTIVO o INACTIVO"))).isTrue();
    }

    @Test
    @DisplayName("AplicacionCreateRequest - Estado por defecto ACTIVO")
    void aplicacionCreateRequest_DeberiaAsignarEstadoPorDefecto() {
        // Given & When
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            "Sistema Válido",
            "Descripción válida",
            "https://test.example.com",
            "TEST_APP_001",
            null // Estado nulo
        );

        // Then
        assertThat(request.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("AplicacionUpdateRequest - Validación exitosa")
    void aplicacionUpdateRequest_DeberiaValidarExitosamente() {
        // Given
        AplicacionUpdateRequest request = new AplicacionUpdateRequest(
            "Sistema Actualizado",
            "Descripción actualizada",
            "https://updated.example.com",
            "UPDATED_APP_001",
            "INACTIVO"
        );

        // When
        Set<ConstraintViolation<AplicacionUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Sistema Actualizado");
        assertThat(request.descripcion()).isEqualTo("Descripción actualizada");
        assertThat(request.url()).isEqualTo("https://updated.example.com");
        assertThat(request.llaveIdentificadora()).isEqualTo("UPDATED_APP_001");
        assertThat(request.estado()).isEqualTo("INACTIVO");
    }

    @Test
    @DisplayName("AplicacionResponse - Constructor y getters")
    void aplicacionResponse_DeberiaFuncionarCorrectamente() {
        // Given
        UUID id = UUID.randomUUID();
        AplicacionResponse response = new AplicacionResponse(
            id,
            "Sistema de Pruebas",
            "Descripción de la aplicación",
            "https://test.example.com",
            "TEST_APP_001",
            "ACTIVO",
            null,
            null
        );

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.nombre()).isEqualTo("Sistema de Pruebas");
        assertThat(response.descripcion()).isEqualTo("Descripción de la aplicación");
        assertThat(response.url()).isEqualTo("https://test.example.com");
        assertThat(response.llaveIdentificadora()).isEqualTo("TEST_APP_001");
        assertThat(response.estado()).isEqualTo("ACTIVO");
        assertThat(response.createdAt()).isNull();
        assertThat(response.updatedAt()).isNull();
    }

    @Test
    @DisplayName("AplicacionSummary - Constructor y getters")
    void aplicacionSummary_DeberiaFuncionarCorrectamente() {
        // Given
        UUID id = UUID.randomUUID();
        AplicacionSummary summary = new AplicacionSummary(
            id,
            "Sistema de Pruebas",
            "https://test.example.com",
            "TEST_APP_001",
            "ACTIVO"
        );

        // Then
        assertThat(summary.id()).isEqualTo(id);
        assertThat(summary.nombre()).isEqualTo("Sistema de Pruebas");
        assertThat(summary.url()).isEqualTo("https://test.example.com");
        assertThat(summary.llaveIdentificadora()).isEqualTo("TEST_APP_001");
        assertThat(summary.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("Records - Igualdad y hashCode")
    void records_DeberianTenerIgualdadYHashCodeCorrectos() {
        // Given
        UUID id = UUID.randomUUID();
        AplicacionSummary summary1 = new AplicacionSummary(id, "Nombre", "https://test.com", "TEST_001", "ACTIVO");
        AplicacionSummary summary2 = new AplicacionSummary(id, "Nombre", "https://test.com", "TEST_001", "ACTIVO");
        AplicacionSummary summary3 = new AplicacionSummary(UUID.randomUUID(), "Nombre", "https://test.com", "TEST_001", "ACTIVO");

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
        AplicacionSummary summary = new AplicacionSummary(id, "Sistema", "https://test.com", "TEST_001", "ACTIVO");

        // When
        String toString = summary.toString();

        // Then
        assertThat(toString).contains("AplicacionSummary");
        assertThat(toString).contains(id.toString());
        assertThat(toString).contains("Sistema");
        assertThat(toString).contains("https://test.com");
        assertThat(toString).contains("TEST_001");
        assertThat(toString).contains("ACTIVO");
    }

    @Test
    @DisplayName("EstadoAplicacionDto debe validar estados correctamente")
    void estadoAplicacionDto_DebeValidarCorrectamente() {
        // Test valores válidos
        assertThat(EstadoAplicacionDto.ACTIVO.getCodigo()).isEqualTo("ACTIVO");
        assertThat(EstadoAplicacionDto.INACTIVO.getCodigo()).isEqualTo("INACTIVO");
        
        assertThat(EstadoAplicacionDto.ACTIVO.getDescripcion()).contains("activa");
        assertThat(EstadoAplicacionDto.INACTIVO.getDescripcion()).contains("inactiva");
    }

    @Test
    @DisplayName("EstadoAplicacionDto.fromString debe funcionar correctamente")
    void estadoAplicacionDto_FromString_DebeFuncionar() {
        // Test casos válidos
        assertThat(EstadoAplicacionDto.fromString("ACTIVO")).isEqualTo(EstadoAplicacionDto.ACTIVO);
        assertThat(EstadoAplicacionDto.fromString("activo")).isEqualTo(EstadoAplicacionDto.ACTIVO);
        assertThat(EstadoAplicacionDto.fromString("INACTIVO")).isEqualTo(EstadoAplicacionDto.INACTIVO);
        assertThat(EstadoAplicacionDto.fromString("inactivo")).isEqualTo(EstadoAplicacionDto.INACTIVO);
    }

    @Test
    @DisplayName("EstadoAplicacionDto.fromString debe fallar con valores inválidos")
    void estadoAplicacionDto_FromString_DebeFallarConValoresInvalidos() {
        // Test casos inválidos
        assertThatThrownBy(() -> EstadoAplicacionDto.fromString(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser nulo o vacío");

        assertThatThrownBy(() -> EstadoAplicacionDto.fromString(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser nulo o vacío");

        assertThatThrownBy(() -> EstadoAplicacionDto.fromString("INVALIDO"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Estado de aplicación inválido");
    }
}
