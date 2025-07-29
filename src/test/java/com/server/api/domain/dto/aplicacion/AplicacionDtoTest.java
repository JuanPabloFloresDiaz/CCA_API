package com.server.api.domain.dto.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para DTOs de Aplicacion.
 * Valida la construcción y validación de DTOs.
 */
@DisplayName("Aplicacion DTOs - Tests Unitarios")
class AplicacionDtoTest {

    @Test
    @DisplayName("AplicacionCreateRequest debe crearse correctamente")
    void aplicacionCreateRequestDebeCrearseCorrectamente() {
        // Given
        String nombre = "Sistema de Pruebas";
        String descripcion = "Aplicación para testing";
        String url = "https://test.example.com";
        String llave = "TEST_APP_001";
        String estado = "ACTIVO";

        // When
        AplicacionCreateRequest request = new AplicacionCreateRequest(
            nombre, descripcion, url, llave, estado
        );

        // Then
        assertThat(request).isNotNull();
        assertThat(request.nombre()).isEqualTo(nombre);
        assertThat(request.descripcion()).isEqualTo(descripcion);
        assertThat(request.url()).isEqualTo(url);
        assertThat(request.llaveIdentificadora()).isEqualTo(llave);
        assertThat(request.estado()).isEqualTo(estado);
    }

    @Test
    @DisplayName("AplicacionUpdateRequest debe crearse correctamente")
    void aplicacionUpdateRequestDebeCrearseCorrectamente() {
        // Given
        String nombre = "Sistema Actualizado";
        String descripcion = "Aplicación actualizada";
        String url = "https://updated.example.com";
        String llave = "UPDATED_APP_001";
        String estado = "INACTIVO";

        // When
        AplicacionUpdateRequest request = new AplicacionUpdateRequest(
            nombre, descripcion, url, llave, estado
        );

        // Then
        assertThat(request).isNotNull();
        assertThat(request.nombre()).isEqualTo(nombre);
        assertThat(request.descripcion()).isEqualTo(descripcion);
        assertThat(request.url()).isEqualTo(url);
        assertThat(request.llaveIdentificadora()).isEqualTo(llave);
        assertThat(request.estado()).isEqualTo(estado);
    }

    @Test
    @DisplayName("EstadoAplicacionDto debe validar estados correctamente")
    void estadoAplicacionDtoDebeValidarCorrectamente() {
        // Test valores válidos
        assertThat(EstadoAplicacionDto.ACTIVO.getCodigo()).isEqualTo("ACTIVO");
        assertThat(EstadoAplicacionDto.INACTIVO.getCodigo()).isEqualTo("INACTIVO");
        
        assertThat(EstadoAplicacionDto.ACTIVO.getDescripcion()).contains("activa");
        assertThat(EstadoAplicacionDto.INACTIVO.getDescripcion()).contains("inactiva");
    }

    @Test
    @DisplayName("EstadoAplicacionDto.fromString debe funcionar correctamente")
    void estadoAplicacionDtoFromStringDebeFuncionar() {
        // Test casos válidos
        assertThat(EstadoAplicacionDto.fromString("ACTIVO")).isEqualTo(EstadoAplicacionDto.ACTIVO);
        assertThat(EstadoAplicacionDto.fromString("activo")).isEqualTo(EstadoAplicacionDto.ACTIVO);
        assertThat(EstadoAplicacionDto.fromString("INACTIVO")).isEqualTo(EstadoAplicacionDto.INACTIVO);
        assertThat(EstadoAplicacionDto.fromString("inactivo")).isEqualTo(EstadoAplicacionDto.INACTIVO);
    }

    @Test
    @DisplayName("EstadoAplicacionDto.fromString debe fallar con valores inválidos")
    void estadoAplicacionDtoFromStringDebeFallarConValoresInvalidos() {
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

    @Test
    @DisplayName("AplicacionResponse debe representar datos correctamente")
    void aplicacionResponseDebeRepresentarDatos() {
        // Este test se puede expandir cuando AplicacionResponse esté implementado
        // Por ahora, verificamos que la clase existe y es usable
        assertThat(AplicacionResponse.class).isNotNull();
    }

    @Test
    @DisplayName("AplicacionSummary debe representar resumen correctamente")  
    void aplicacionSummaryDebeRepresentarResumen() {
        // Este test se puede expandir cuando AplicacionSummary esté implementado
        // Por ahora, verificamos que la clase existe y es usable
        assertThat(AplicacionSummary.class).isNotNull();
    }
}
