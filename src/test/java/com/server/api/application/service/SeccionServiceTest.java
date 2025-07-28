package com.server.api.application.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.server.api.domain.dto.seccion.SeccionCreateRequest;
import com.server.api.domain.dto.seccion.SeccionResponse;
import com.server.api.domain.dto.seccion.SeccionSummary;
import com.server.api.domain.dto.seccion.SeccionUpdateRequest;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.mapper.SeccionMapper;
import com.server.api.domain.repository.SeccionRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Tests unitarios para SeccionService.
 * Usa mocks para aislar la lógica de negocio del servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeccionService - Tests Unitarios")
class SeccionServiceTest {

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private SeccionMapper seccionMapper;

    @InjectMocks
    private SeccionService seccionService;

    private SeccionCreateRequest createRequest;
    private SeccionUpdateRequest updateRequest;
    private Seccion seccion;
    private SeccionResponse seccionResponse;
    private SeccionSummary seccionSummary;
    private UUID seccionId;

    @BeforeEach
    void setUp() {
        seccionId = UUID.randomUUID();
        
        // Datos de prueba
        createRequest = new SeccionCreateRequest(
            "Gestión de Usuarios",
            "Sección para administrar usuarios del sistema"
        );
        
        updateRequest = new SeccionUpdateRequest(
            "Gestión de Usuarios Actualizada",
            "Sección actualizada para administrar usuarios del sistema"
        );
        
        seccion = new Seccion();
        seccion.setId(seccionId);
        seccion.setNombre("Gestión de Usuarios");
        seccion.setDescripcion("Sección para administrar usuarios del sistema");
        seccion.setCreatedAt(OffsetDateTime.now());
        seccion.setUpdatedAt(OffsetDateTime.now());
        
        seccionResponse = new SeccionResponse(
            seccionId,
            "Gestión de Usuarios",
            "Sección para administrar usuarios del sistema",
            true,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null
        );
        
        seccionSummary = new SeccionSummary(
            seccionId,
            "Gestión de Usuarios",
            "Sección para administrar usuarios del sistema",
            true
        );
    }

    @Test
    @DisplayName("Crear sección - Caso exitoso")
    void crear_DeberiaCrearSeccionExitosamente() {
        // Given
        when(seccionRepository.existsByNombreIgnoreCase(anyString())).thenReturn(false);
        when(seccionMapper.toEntity(createRequest)).thenReturn(seccion);
        when(seccionRepository.save(seccion)).thenReturn(seccion);
        when(seccionMapper.toResponse(seccion)).thenReturn(seccionResponse);

        // When
        SeccionResponse resultado = seccionService.crear(createRequest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Gestión de Usuarios");
        assertThat(resultado.descripcion()).isEqualTo("Sección para administrar usuarios del sistema");
        assertThat(resultado.activo()).isTrue();

        verify(seccionRepository).existsByNombreIgnoreCase("Gestión de Usuarios");
        verify(seccionMapper).toEntity(createRequest);
        verify(seccionRepository).save(seccion);
        verify(seccionMapper).toResponse(seccion);
    }

    @Test
    @DisplayName("Crear sección - Debería fallar si el nombre ya existe")
    void crear_DeberiaFallarSiNombreYaExiste() {
        // Given
        when(seccionRepository.existsByNombreIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> seccionService.crear(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Ya existe una sección con el nombre: Gestión de Usuarios");

        verify(seccionRepository).existsByNombreIgnoreCase("Gestión de Usuarios");
        verify(seccionMapper, never()).toEntity(any());
        verify(seccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener por ID - Caso exitoso")
    void obtenerPorId_DeberiaRetornarSeccionExistente() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(seccionMapper.toResponse(seccion)).thenReturn(seccionResponse);

        // When
        SeccionResponse resultado = seccionService.obtenerPorId(seccionId);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(seccionId);
        assertThat(resultado.nombre()).isEqualTo("Gestión de Usuarios");

        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionMapper).toResponse(seccion);
    }

    @Test
    @DisplayName("Obtener por ID - Debería fallar si no existe")
    void obtenerPorId_DeberiaFallarSiNoExiste() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seccionService.obtenerPorId(seccionId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Sección no encontrada con ID: " + seccionId);

        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Obtener todas - Caso exitoso")
    void obtenerTodas_DeberiaRetornarListaDeSecciones() {
        // Given
        List<Seccion> secciones = List.of(seccion);
        List<SeccionSummary> summaries = List.of(seccionSummary);
        
        when(seccionRepository.findAllActive()).thenReturn(secciones);
        when(seccionMapper.toSummaryList(secciones)).thenReturn(summaries);

        // When
        List<SeccionSummary> resultado = seccionService.obtenerTodas();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Gestión de Usuarios");

        verify(seccionRepository).findAllActive();
        verify(seccionMapper).toSummaryList(secciones);
    }

    @Test
    @DisplayName("Obtener todas con paginación - Caso exitoso")
    void obtenerTodasConPaginacion_DeberiaRetornarPaginaDeSecciones() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Seccion> secciones = List.of(seccion);
        Page<Seccion> seccionesPage = new PageImpl<>(secciones, pageable, 1);
        List<SeccionSummary> summaries = List.of(seccionSummary);
        
        when(seccionRepository.findAllActive(pageable)).thenReturn(seccionesPage);
        when(seccionMapper.toSummaryList(secciones)).thenReturn(summaries);

        // When
        Page<SeccionSummary> resultado = seccionService.obtenerTodas(pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).nombre()).isEqualTo("Gestión de Usuarios");

        verify(seccionRepository).findAllActive(pageable);
        verify(seccionMapper).toSummaryList(secciones);
    }

    @Test
    @DisplayName("Buscar por nombre - Caso exitoso")
    void buscarPorNombre_DeberiaRetornarSeccionesCoincidentes() {
        // Given
        String nombre = "Gestión";
        List<Seccion> secciones = List.of(seccion);
        List<SeccionSummary> summaries = List.of(seccionSummary);
        
        when(seccionRepository.findByNombreContainingIgnoreCaseAndActive(nombre)).thenReturn(secciones);
        when(seccionMapper.toSummaryList(secciones)).thenReturn(summaries);

        // When
        List<SeccionSummary> resultado = seccionService.buscarPorNombre(nombre);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).contains("Gestión");

        verify(seccionRepository).findByNombreContainingIgnoreCaseAndActive(nombre);
        verify(seccionMapper).toSummaryList(secciones);
    }

    @Test
    @DisplayName("Actualizar sección - Caso exitoso")
    void actualizar_DeberiaActualizarSeccionExitosamente() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(seccionRepository.existsByNombreIgnoreCaseAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        when(seccionRepository.save(seccion)).thenReturn(seccion);
        when(seccionMapper.toResponse(seccion)).thenReturn(seccionResponse);

        // When
        SeccionResponse resultado = seccionService.actualizar(seccionId, updateRequest);

        // Then
        assertThat(resultado).isNotNull();
        
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionMapper).updateEntity(seccion, updateRequest);
        verify(seccionRepository).save(seccion);
        verify(seccionMapper).toResponse(seccion);
    }

    @Test
    @DisplayName("Actualizar sección - Debería fallar si no existe")
    void actualizar_DeberiaFallarSiNoExiste() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seccionService.actualizar(seccionId, updateRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Sección no encontrada con ID: " + seccionId);

        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionMapper, never()).updateEntity(any(), any());
        verify(seccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar sección - Caso exitoso")
    void eliminar_DeberiaEliminarSeccionExitosamente() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(seccionRepository.save(any(Seccion.class))).thenReturn(seccion);

        // When
        seccionService.eliminar(seccionId);

        // Then
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionRepository).save(any(Seccion.class));
    }

    @Test
    @DisplayName("Eliminar sección - Debería fallar si no existe")
    void eliminar_DeberiaFallarSiNoExiste() {
        // Given
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seccionService.eliminar(seccionId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Sección no encontrada con ID: " + seccionId);

        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(seccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Contar secciones - Caso exitoso")
    void contarSecciones_DeberiaRetornarConteoCorrect() {
        // Given
        long expectedCount = 5L;
        when(seccionRepository.countActive()).thenReturn(expectedCount);

        // When
        long resultado = seccionService.contarSecciones();

        // Then
        assertThat(resultado).isEqualTo(expectedCount);
        verify(seccionRepository).countActive();
    }

    @Test
    @DisplayName("Existe por nombre - Caso exitoso")
    void existePorNombre_DeberiaRetornarVerdaderoSiExiste() {
        // Given
        String nombre = "Gestión de Usuarios";
        when(seccionRepository.existsByNombreIgnoreCase(nombre)).thenReturn(true);

        // When
        boolean resultado = seccionService.existePorNombre(nombre);

        // Then
        assertThat(resultado).isTrue();
        verify(seccionRepository).existsByNombreIgnoreCase(nombre);
    }
}
