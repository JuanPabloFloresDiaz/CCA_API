package com.server.api.application.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.server.api.domain.dto.tipousuario.TipoUsuarioCreateRequest;
import com.server.api.domain.dto.tipousuario.TipoUsuarioResponse;
import com.server.api.domain.dto.tipousuario.TipoUsuarioSummary;
import com.server.api.domain.dto.tipousuario.TipoUsuarioUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.TipoUsuario;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;
import com.server.api.domain.mapper.TipoUsuarioMapper;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.TipoUsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Tests unitarios para TipoUsuarioService.
 * Utiliza Mockito para aislar la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TipoUsuarioService - Tests Unitarios")
class TipoUsuarioServiceTest {

    @Mock
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Mock
    private AplicacionRepository aplicacionRepository;

    @Mock
    private TipoUsuarioMapper tipoUsuarioMapper;

    @InjectMocks
    private TipoUsuarioService tipoUsuarioService;

    private UUID tipoUsuarioId;
    private UUID aplicacionId;
    private TipoUsuario tipoUsuario;
    private Aplicacion aplicacion;
    private TipoUsuarioCreateRequest createRequest;
    private TipoUsuarioUpdateRequest updateRequest;
    private TipoUsuarioResponse tipoUsuarioResponse;
    private TipoUsuarioSummary tipoUsuarioSummary;

    @BeforeEach
    void setUp() {
        tipoUsuarioId = UUID.randomUUID();
        aplicacionId = UUID.randomUUID();

        // Crear aplicación mock
        aplicacion = new Aplicacion();
        aplicacion.setId(aplicacionId);
        aplicacion.setNombre("Sistema Principal");
        aplicacion.setDescripcion("Sistema de administración principal");

        // Crear tipo de usuario mock
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setId(tipoUsuarioId);
        tipoUsuario.setNombre("Administrador");
        tipoUsuario.setDescripcion("Usuario administrador del sistema");
        tipoUsuario.setEstado(EstadoTipoUsuario.ACTIVO);
        tipoUsuario.setAplicacion(aplicacion);
        tipoUsuario.setCreatedAt(OffsetDateTime.now());
        tipoUsuario.setUpdatedAt(OffsetDateTime.now());

        // Crear DTOs mock
        createRequest = new TipoUsuarioCreateRequest(
            "Administrador",
            "Usuario administrador del sistema",
            aplicacionId
        );

        updateRequest = new TipoUsuarioUpdateRequest(
            "Administrador Actualizado",
            "Descripción actualizada",
            aplicacionId,
            "ACTIVO"
        );

        tipoUsuarioResponse = new TipoUsuarioResponse(
            tipoUsuarioId,
            "Administrador",
            "Usuario administrador del sistema",
            aplicacionId,
            "Sistema Principal",
            "ACTIVO",
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        tipoUsuarioSummary = new TipoUsuarioSummary(
            tipoUsuarioId,
            "Administrador",
            "Usuario administrador del sistema",
            "Sistema Principal",
            "ACTIVO"
        );
    }

    @Test
    @DisplayName("Crear tipo de usuario - Exitoso")
    void crearTipoUsuario_Exitoso() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(tipoUsuarioRepository.existsByNombreAndAplicacionIdAndDeletedAtIsNull(
            eq("Administrador"), eq(aplicacionId), eq(null))).thenReturn(false);
        when(tipoUsuarioMapper.toEntity(createRequest, aplicacion)).thenReturn(tipoUsuario);
        when(tipoUsuarioRepository.save(tipoUsuario)).thenReturn(tipoUsuario);
        when(tipoUsuarioMapper.toResponse(tipoUsuario)).thenReturn(tipoUsuarioResponse);

        // When
        TipoUsuarioResponse result = tipoUsuarioService.crear(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nombre()).isEqualTo("Administrador");
        verify(aplicacionRepository).findById(aplicacionId);
        verify(tipoUsuarioRepository).existsByNombreAndAplicacionIdAndDeletedAtIsNull(
            eq("Administrador"), eq(aplicacionId), eq(null));
        verify(tipoUsuarioRepository).save(tipoUsuario);
        verify(tipoUsuarioMapper).toEntity(createRequest, aplicacion);
        verify(tipoUsuarioMapper).toResponse(tipoUsuario);
    }

    @Test
    @DisplayName("Crear tipo de usuario - Aplicación no encontrada")
    void crearTipoUsuario_AplicacionNoEncontrada_DeberiaLanzarExcepcion() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tipoUsuarioService.crear(createRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Aplicación no encontrada");

        verify(aplicacionRepository).findById(aplicacionId);
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear tipo de usuario - Nombre duplicado")
    void crearTipoUsuario_NombreDuplicado_DeberiaLanzarExcepcion() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(tipoUsuarioRepository.existsByNombreAndAplicacionIdAndDeletedAtIsNull(
            eq("Administrador"), eq(aplicacionId), eq(null))).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tipoUsuarioService.crear(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe un tipo de usuario con el nombre");

        verify(aplicacionRepository).findById(aplicacionId);
        verify(tipoUsuarioRepository).existsByNombreAndAplicacionIdAndDeletedAtIsNull(
            eq("Administrador"), eq(aplicacionId), eq(null));
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener por ID - Exitoso")
    void obtenerPorId_Exitoso() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.of(tipoUsuario));
        when(tipoUsuarioMapper.toResponse(tipoUsuario)).thenReturn(tipoUsuarioResponse);

        // When
        TipoUsuarioResponse result = tipoUsuarioService.obtenerPorId(tipoUsuarioId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(tipoUsuarioId);
        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioMapper).toResponse(tipoUsuario);
    }

    @Test
    @DisplayName("Obtener por ID - No encontrado")
    void obtenerPorId_NoEncontrado_DeberiaLanzarExcepcion() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tipoUsuarioService.obtenerPorId(tipoUsuarioId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("TipoUsuario no encontrado");

        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Obtener todos paginados - Exitoso")
    void obtenerTodos_Exitoso() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String nombre = null;
        UUID aplicacionIdFiltro = null;
        EstadoTipoUsuario estado = null;
        Page<TipoUsuario> tiposUsuarioPage = new PageImpl<>(List.of(tipoUsuario));
        when(tipoUsuarioRepository.findByFilters(nombre, aplicacionIdFiltro, estado, pageable))
            .thenReturn(tiposUsuarioPage);
        when(tipoUsuarioMapper.toSummaryList(List.of(tipoUsuario))).thenReturn(List.of(tipoUsuarioSummary));

        // When
        Page<TipoUsuarioSummary> result = tipoUsuarioService.obtenerTodos(pageable, nombre, aplicacionIdFiltro, estado);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nombre()).isEqualTo("Administrador");
        verify(tipoUsuarioRepository).findByFilters(nombre, aplicacionIdFiltro, estado, pageable);
        verify(tipoUsuarioMapper).toSummaryList(List.of(tipoUsuario));
    }

    @Test
    @DisplayName("Buscar con filtros - Exitoso")
    void buscarConFiltros_Exitoso() {
        // Given
        String nombre = "Admin";
        Pageable pageable = PageRequest.of(0, 10);
        EstadoTipoUsuario estado = EstadoTipoUsuario.ACTIVO;
        Page<TipoUsuario> tiposUsuarioPage = new PageImpl<>(List.of(tipoUsuario));
        when(tipoUsuarioRepository.findByFilters(nombre, aplicacionId, estado, pageable))
            .thenReturn(tiposUsuarioPage);
        when(tipoUsuarioMapper.toSummaryList(List.of(tipoUsuario))).thenReturn(List.of(tipoUsuarioSummary));

        // When
        Page<TipoUsuarioSummary> result = tipoUsuarioService.obtenerTodos(pageable, nombre, aplicacionId, estado);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(tipoUsuarioRepository).findByFilters(nombre, aplicacionId, estado, pageable);
        verify(tipoUsuarioMapper).toSummaryList(List.of(tipoUsuario));
    }

    @Test
    @DisplayName("Actualizar - Exitoso")
    void actualizar_Exitoso() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.of(tipoUsuario));
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(tipoUsuarioRepository.existsByNombreAndAplicacionIdAndDeletedAtIsNull(
            eq("Administrador Actualizado"), eq(aplicacionId), eq(tipoUsuarioId))).thenReturn(false);
        when(tipoUsuarioRepository.save(tipoUsuario)).thenReturn(tipoUsuario);
        when(tipoUsuarioMapper.toResponse(tipoUsuario)).thenReturn(tipoUsuarioResponse);

        // When
        TipoUsuarioResponse result = tipoUsuarioService.actualizar(tipoUsuarioId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(aplicacionRepository).findById(aplicacionId);
        verify(tipoUsuarioMapper).updateEntity(tipoUsuario, updateRequest, aplicacion);
        verify(tipoUsuarioRepository).save(tipoUsuario);
        verify(tipoUsuarioMapper).toResponse(tipoUsuario);
    }

    @Test
    @DisplayName("Actualizar - Tipo de usuario no encontrado")
    void actualizar_TipoUsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tipoUsuarioService.actualizar(tipoUsuarioId, updateRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("TipoUsuario no encontrado");

        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar - Exitoso")
    void eliminar_Exitoso() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.of(tipoUsuario));
        when(tipoUsuarioRepository.save(tipoUsuario)).thenReturn(tipoUsuario);

        // When
        tipoUsuarioService.eliminar(tipoUsuarioId);

        // Then
        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioRepository).save(tipoUsuario);
        assertThat(tipoUsuario.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Eliminar - Tipo de usuario no encontrado")
    void eliminar_TipoUsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        // Given
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tipoUsuarioService.eliminar(tipoUsuarioId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("TipoUsuario no encontrado");

        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar estado - Exitoso")
    void cambiarEstado_Exitoso() {
        // Given
        EstadoTipoUsuario nuevoEstado = EstadoTipoUsuario.INACTIVO;
        when(tipoUsuarioRepository.findByIdAndDeletedAtIsNull(tipoUsuarioId)).thenReturn(Optional.of(tipoUsuario));
        when(tipoUsuarioRepository.save(tipoUsuario)).thenReturn(tipoUsuario);
        when(tipoUsuarioMapper.toResponse(tipoUsuario)).thenReturn(tipoUsuarioResponse);

        // When
        TipoUsuarioResponse result = tipoUsuarioService.cambiarEstado(tipoUsuarioId, nuevoEstado);

        // Then
        assertThat(result).isNotNull();
        verify(tipoUsuarioRepository).findByIdAndDeletedAtIsNull(tipoUsuarioId);
        verify(tipoUsuarioRepository).save(tipoUsuario);
        verify(tipoUsuarioMapper).toResponse(tipoUsuario);
    }

    @Test
    @DisplayName("Obtener estadísticas - Exitoso")
    void obtenerEstadisticas_Exitoso() {
        // Given
        EstadoTipoUsuario estado = EstadoTipoUsuario.ACTIVO;
        when(tipoUsuarioRepository.countByAplicacionIdAndDeletedAtIsNull(aplicacionId)).thenReturn(3L);
        when(tipoUsuarioRepository.countByEstadoAndDeletedAtIsNull(estado)).thenReturn(2L);
        when(tipoUsuarioRepository.countByDeletedAtIsNull()).thenReturn(5L);

        // When
        Map<String, Long> estadisticas = tipoUsuarioService.obtenerEstadisticas(aplicacionId, estado);

        // Then
        assertThat(estadisticas).isNotNull();
        assertThat(estadisticas.get("totalPorAplicacion")).isEqualTo(3L);
        assertThat(estadisticas.get("totalPorEstado")).isEqualTo(2L);
        assertThat(estadisticas.get("totalGeneral")).isEqualTo(5L);
        verify(tipoUsuarioRepository).countByAplicacionIdAndDeletedAtIsNull(aplicacionId);
        verify(tipoUsuarioRepository).countByEstadoAndDeletedAtIsNull(estado);
        verify(tipoUsuarioRepository).countByDeletedAtIsNull();
    }
}
