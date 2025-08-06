package com.server.api.domain.dto.tipousuario;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Tests unitarios para DTOs de TipoUsuario.
 * Valida la construcción, validación y comportamientos de los Records.
 */
@DisplayName("TipoUsuario DTOs - Tests Unitarios")
class TipoUsuarioDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Validación exitosa")
    void tipoUsuarioCreateRequest_DeberiaValidarExitosamente() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Administrador",
            "Usuario con permisos administrativos completos",
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Administrador");
        assertThat(request.descripcion()).isEqualTo("Usuario con permisos administrativos completos");
        assertThat(request.aplicacionId()).isNotNull();
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Nombre nulo debería fallar")
    void tipoUsuarioCreateRequest_NombreNulo_DeberiaFallar() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            null,
            "Descripción válida",
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre es obligatorio");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Nombre vacío debería fallar")
    void tipoUsuarioCreateRequest_NombreVacio_DeberiaFallar() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "",
            "Descripción válida",
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre es obligatorio");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Nombre muy corto debería fallar")
    void tipoUsuarioCreateRequest_NombreMuyCorto_DeberiaFallar() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "A",
            "Descripción válida",
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre debe tener entre 2 y 100 caracteres");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Nombre muy largo debería fallar")
    void tipoUsuarioCreateRequest_NombreMuyLargo_DeberiaFallar() {
        // Given
        String nombreLargo = "A".repeat(101);
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            nombreLargo,
            "Descripción válida",
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre debe tener entre 2 y 100 caracteres");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Descripción muy larga debería fallar")
    void tipoUsuarioCreateRequest_DescripcionMuyLarga_DeberiaFallar() {
        // Given
        String descripcionLarga = "A".repeat(501);
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Nombre válido",
            descripcionLarga,
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("La descripción no puede exceder los 500 caracteres");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - AplicacionId nulo debería fallar")
    void tipoUsuarioCreateRequest_AplicacionIdNulo_DeberiaFallar() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Nombre válido",
            "Descripción válida",
            null
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("La aplicación es obligatoria");
    }

    @Test
    @DisplayName("TipoUsuarioCreateRequest - Descripción nula es válida")
    void tipoUsuarioCreateRequest_DescripcionNula_EsValida() {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Nombre válido",
            null,
            UUID.randomUUID()
        );

        // When
        Set<ConstraintViolation<TipoUsuarioCreateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("TipoUsuarioUpdateRequest - Validación exitosa")
    void tipoUsuarioUpdateRequest_DeberiaValidarExitosamente() {
        // Given
        TipoUsuarioUpdateRequest request = new TipoUsuarioUpdateRequest(
            "Administrador Actualizado",
            "Descripción actualizada",
            UUID.randomUUID(),
            "ACTIVO"
        );

        // When
        Set<ConstraintViolation<TipoUsuarioUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.nombre()).isEqualTo("Administrador Actualizado");
        assertThat(request.descripcion()).isEqualTo("Descripción actualizada");
        assertThat(request.aplicacionId()).isNotNull();
        assertThat(request.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("TipoUsuarioUpdateRequest - Campos opcionales nulos son válidos")
    void tipoUsuarioUpdateRequest_CamposOpcionalesNulos_SonValidos() {
        // Given
        TipoUsuarioUpdateRequest request = new TipoUsuarioUpdateRequest(
            "Nombre mínimo",
            null,
            null,
            null
        );

        // When
        Set<ConstraintViolation<TipoUsuarioUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.aplicacionId()).isNull();
        assertThat(request.estado()).isNull();
        assertThat(request.descripcion()).isNull();
    }

    @Test
    @DisplayName("TipoUsuarioResponse - Construcción exitosa")
    void tipoUsuarioResponse_ConstruccionExitosa() {
        // Given
        UUID id = UUID.randomUUID();
        UUID aplicacionId = UUID.randomUUID();
        TipoUsuarioResponse response = new TipoUsuarioResponse(
            id,
            "Administrador",
            "Descripción del tipo de usuario",
            aplicacionId,
            "Sistema Principal",
            "ACTIVO",
            null,
            null
        );

        // Then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.nombre()).isEqualTo("Administrador");
        assertThat(response.descripcion()).isEqualTo("Descripción del tipo de usuario");
        assertThat(response.aplicacionId()).isEqualTo(aplicacionId);
        assertThat(response.aplicacionNombre()).isEqualTo("Sistema Principal");
        assertThat(response.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("TipoUsuarioSummary - Construcción exitosa")
    void tipoUsuarioSummary_ConstruccionExitosa() {
        // Given
        UUID id = UUID.randomUUID();
        TipoUsuarioSummary summary = new TipoUsuarioSummary(
            id,
            "Administrador",
            "Descripción breve",
            "Sistema Principal",
            "ACTIVO"
        );

        // Then
        assertThat(summary.id()).isEqualTo(id);
        assertThat(summary.nombre()).isEqualTo("Administrador");
        assertThat(summary.descripcion()).isEqualTo("Descripción breve");
        assertThat(summary.aplicacionNombre()).isEqualTo("Sistema Principal");
        assertThat(summary.estado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("EstadoTipoUsuario - Enum debe tener valores correctos")
    void estadoTipoUsuario_DeberíaTenerValoresCorrectos() {
        // Then
        assertThat(EstadoTipoUsuario.ACTIVO.getValor()).isEqualTo("activo");
        assertThat(EstadoTipoUsuario.INACTIVO.getValor()).isEqualTo("inactivo");
        assertThat(EstadoTipoUsuario.values()).hasSize(2);
    }

    @Test
    @DisplayName("EstadoTipoUsuario - Valores del enum son únicos")
    void estadoTipoUsuario_ValoresDelEnumSonUnicos() {
        // Given
        EstadoTipoUsuario[] estados = EstadoTipoUsuario.values();

        // Then
        assertThat(estados[0]).isNotEqualTo(estados[1]);
        assertThat(estados[0].getValor()).isNotEqualTo(estados[1].getValor());
    }
}
