package com.server.api.application.service;

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

import com.server.api.domain.dto.accion.AccionCreateRequest;
import com.server.api.domain.dto.accion.AccionResponse;
import com.server.api.domain.dto.accion.AccionSummary;
import com.server.api.domain.dto.accion.AccionUpdateRequest;
import com.server.api.domain.entity.Accion;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.mapper.AccionMapper;
import com.server.api.domain.repository.AccionRepository;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.SeccionRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Tests unitarios para AccionService.
 * Utiliza Mockito para aislar la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccionService - Tests Unitarios")
class AccionServiceTest {

    @Mock
    private AccionRepository accionRepository;

    @Mock
    private AplicacionRepository aplicacionRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private AccionMapper accionMapper;

    @InjectMocks
    private AccionService accionService;

    private UUID accionId;
    private UUID aplicacionId;
    private UUID seccionId;
    private Accion accion;
    private Aplicacion aplicacion;
    private Seccion seccion;
    private AccionCreateRequest createRequest;
    private AccionUpdateRequest updateRequest;
    private AccionResponse accionResponse;
    private AccionSummary accionSummary;

    @BeforeEach
    void setUp() {
        accionId = UUID.randomUUID();
        aplicacionId = UUID.randomUUID();
        seccionId = UUID.randomUUID();

        // Configurar entidades mock
        aplicacion = new Aplicacion();
        aplicacion.setId(aplicacionId);
        aplicacion.setNombre("Sistema de Usuarios");

        seccion = new Seccion();
        seccion.setId(seccionId);
        seccion.setNombre("Gestión de Usuarios");

        accion = new Accion();
        accion.setId(accionId);
        accion.setNombre("Crear Usuario");
        accion.setDescripcion("Permite crear nuevos usuarios");
        accion.setAplicacion(aplicacion);
        accion.setSeccion(seccion);

        // Configurar DTOs mock
        createRequest = new AccionCreateRequest(
            "Crear Usuario",
            "Permite crear nuevos usuarios",
            aplicacionId,
            seccionId
        );

        updateRequest = new AccionUpdateRequest(
            "Actualizar Usuario",
            "Permite actualizar usuarios existentes",
            aplicacionId,
            seccionId
        );

        AccionResponse.AplicacionBasicInfo aplicacionInfo = new AccionResponse.AplicacionBasicInfo(
            aplicacionId, "Sistema de Usuarios", "USER_SYSTEM");
        AccionResponse.SeccionBasicInfo seccionInfo = new AccionResponse.SeccionBasicInfo(
            seccionId, "Gestión de Usuarios");

        accionResponse = new AccionResponse(
            accionId,
            "Crear Usuario",
            "Permite crear nuevos usuarios",
            aplicacionInfo,
            seccionInfo,
            true,
            null,
            null,
            null
        );

        accionSummary = new AccionSummary(
            accionId,
            "Crear Usuario",
            "Permite crear nuevos usuarios",
            "Sistema de Usuarios",
            "Gestión de Usuarios",
            true
        );
    }

    @Test
    @DisplayName("Crear acción - Debería crear exitosamente")
    void crear_DeberiaCrearExitosamente() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(anyString(), eq(aplicacionId), eq(seccionId)))
            .thenReturn(false);
        when(accionMapper.toEntity(createRequest)).thenReturn(accion);
        when(accionRepository.save(any(Accion.class))).thenReturn(accion);
        when(accionMapper.toResponse(accion)).thenReturn(accionResponse);

        // When
        AccionResponse resultado = accionService.crear(createRequest);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Crear Usuario");
        verify(aplicacionRepository).findById(aplicacionId);
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(accionRepository).existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(anyString(), eq(aplicacionId), eq(seccionId));
        verify(accionRepository).save(any(Accion.class));
        verify(accionMapper).toResponse(accion);
    }

    @Test
    @DisplayName("Crear acción - Debería fallar con aplicación no encontrada")
    void crear_DeberiaFallarConAplicacionNoEncontrada() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accionService.crear(createRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Aplicación no encontrada");

        verify(aplicacionRepository).findById(aplicacionId);
        verify(seccionRepository, never()).findByIdAndActive(any());
        verify(accionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear acción - Debería fallar con sección no encontrada")
    void crear_DeberiaFallarConSeccionNoEncontrada() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accionService.crear(createRequest))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Sección no encontrada");

        verify(aplicacionRepository).findById(aplicacionId);
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(accionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear acción - Debería fallar con nombre duplicado")
    void crear_DeberiaFallarConNombreDuplicado() {
        // Given
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(anyString(), eq(aplicacionId), eq(seccionId)))
            .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> accionService.crear(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Ya existe una acción con el nombre");

        verify(aplicacionRepository).findById(aplicacionId);
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(accionRepository).existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(anyString(), eq(aplicacionId), eq(seccionId));
        verify(accionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener por ID - Debería retornar acción")
    void obtenerPorId_DeberiaRetornarAccion() {
        // Given
        when(accionRepository.findByIdAndActive(accionId)).thenReturn(Optional.of(accion));
        when(accionMapper.toResponse(accion)).thenReturn(accionResponse);

        // When
        AccionResponse resultado = accionService.obtenerPorId(accionId);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(accionId);
        verify(accionRepository).findByIdAndActive(accionId);
        verify(accionMapper).toResponse(accion);
    }

    @Test
    @DisplayName("Obtener por ID - Debería fallar con acción no encontrada")
    void obtenerPorId_DeberiaFallarConAccionNoEncontrada() {
        // Given
        when(accionRepository.findByIdAndActive(accionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accionService.obtenerPorId(accionId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Acción no encontrada");

        verify(accionRepository).findByIdAndActive(accionId);
        verify(accionMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Obtener todas - Debería retornar lista de acciones")
    void obtenerTodas_DeberiaRetornarListaDeAcciones() {
        // Given
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findAllActive()).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.obtenerTodas();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Crear Usuario");
        verify(accionRepository).findAllActive();
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Obtener todas con paginación - Debería retornar página de acciones")
    void obtenerTodasConPaginacion_DeberiaRetornarPaginaDeAcciones() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Accion> acciones = List.of(accion);
        Page<Accion> pageAcciones = new PageImpl<>(acciones, pageable, 1);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findAllActive(pageable)).thenReturn(pageAcciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        Page<AccionSummary> resultado = accionService.obtenerTodas(pageable);

        // Then
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(accionRepository).findAllActive(pageable);
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Buscar por nombre - Debería retornar acciones coincidentes")
    void buscarPorNombre_DeberiaRetornarAccionesCoincidentes() {
        // Given
        String nombre = "Usuario";
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findByNombreContainingIgnoreCaseAndActive(nombre)).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.buscarPorNombre(nombre);

        // Then
        assertThat(resultado).hasSize(1);
        verify(accionRepository).findByNombreContainingIgnoreCaseAndActive(nombre);
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Buscar por aplicación - Debería retornar acciones de la aplicación")
    void buscarPorAplicacion_DeberiaRetornarAccionesDeLaAplicacion() {
        // Given
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findByAplicacionIdAndActive(aplicacionId)).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.buscarPorAplicacion(aplicacionId);

        // Then
        assertThat(resultado).hasSize(1);
        verify(accionRepository).findByAplicacionIdAndActive(aplicacionId);
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Buscar por sección - Debería retornar acciones de la sección")
    void buscarPorSeccion_DeberiaRetornarAccionesDeLaSeccion() {
        // Given
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findBySeccionIdAndActive(seccionId)).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.buscarPorSeccion(seccionId);

        // Then
        assertThat(resultado).hasSize(1);
        verify(accionRepository).findBySeccionIdAndActive(seccionId);
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Buscar por aplicación y sección - Debería retornar acciones específicas")
    void buscarPorAplicacionYSeccion_DeberiaRetornarAccionesEspecificas() {
        // Given
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findByAplicacionIdAndSeccionIdAndActive(aplicacionId, seccionId)).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.buscarPorAplicacionYSeccion(aplicacionId, seccionId);

        // Then
        assertThat(resultado).hasSize(1);
        verify(accionRepository).findByAplicacionIdAndSeccionIdAndActive(aplicacionId, seccionId);
        verify(accionMapper).toSummaryList(acciones);
    }

    @Test
    @DisplayName("Actualizar acción - Debería actualizar exitosamente")
    void actualizar_DeberiaActualizarExitosamente() {
        // Given
        when(accionRepository.findByIdAndActive(accionId)).thenReturn(Optional.of(accion));
        when(aplicacionRepository.findById(aplicacionId)).thenReturn(Optional.of(aplicacion));
        when(seccionRepository.findByIdAndActive(seccionId)).thenReturn(Optional.of(seccion));
        when(accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionIdAndIdNot(
            anyString(), eq(aplicacionId), eq(seccionId), eq(accionId))).thenReturn(false);
        when(accionRepository.save(accion)).thenReturn(accion);
        when(accionMapper.toResponse(accion)).thenReturn(accionResponse);

        // When
        AccionResponse resultado = accionService.actualizar(accionId, updateRequest);

        // Then
        assertThat(resultado).isNotNull();
        verify(accionRepository).findByIdAndActive(accionId);
        verify(aplicacionRepository).findById(aplicacionId);
        verify(seccionRepository).findByIdAndActive(seccionId);
        verify(accionMapper).updateEntity(accion, updateRequest);
        verify(accionRepository).save(accion);
        verify(accionMapper).toResponse(accion);
    }

    @Test
    @DisplayName("Eliminar acción - Debería eliminar exitosamente")
    void eliminar_DeberiaEliminarExitosamente() {
        // Given
        when(accionRepository.findByIdAndActive(accionId)).thenReturn(Optional.of(accion));
        when(accionRepository.save(accion)).thenReturn(accion);

        // When
        accionService.eliminar(accionId);

        // Then
        verify(accionRepository).findByIdAndActive(accionId);
        verify(accionRepository).save(accion);
    }

    @Test
    @DisplayName("Restaurar acción - Debería restaurar exitosamente")
    void restaurar_DeberiaRestaurarExitosamente() {
        // Given
        accion.softDelete(); // Simular que está eliminada
        when(accionRepository.findById(accionId)).thenReturn(Optional.of(accion));
        when(accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionIdAndIdNot(
            anyString(), eq(aplicacionId), eq(seccionId), eq(accionId))).thenReturn(false);
        when(accionRepository.save(accion)).thenReturn(accion);
        when(accionMapper.toResponse(accion)).thenReturn(accionResponse);

        // When
        AccionResponse resultado = accionService.restaurar(accionId);

        // Then
        assertThat(resultado).isNotNull();
        verify(accionRepository).findById(accionId);
        verify(accionRepository).existsByNombreIgnoreCaseAndAplicacionIdAndSeccionIdAndIdNot(
            anyString(), eq(aplicacionId), eq(seccionId), eq(accionId));
        verify(accionRepository).save(accion);
        verify(accionMapper).toResponse(accion);
    }

    @Test
    @DisplayName("Contar acciones - Debería retornar total de acciones activas")
    void contarAcciones_DeberiaRetornarTotalDeAccionesActivas() {
        // Given
        when(accionRepository.countActive()).thenReturn(5L);

        // When
        long resultado = accionService.contarAcciones();

        // Then
        assertThat(resultado).isEqualTo(5L);
        verify(accionRepository).countActive();
    }

    @Test
    @DisplayName("Contar por aplicación - Debería retornar total por aplicación")
    void contarPorAplicacion_DeberiaRetornarTotalPorAplicacion() {
        // Given
        when(accionRepository.countByAplicacionIdAndActive(aplicacionId)).thenReturn(3L);

        // When
        long resultado = accionService.contarPorAplicacion(aplicacionId);

        // Then
        assertThat(resultado).isEqualTo(3L);
        verify(accionRepository).countByAplicacionIdAndActive(aplicacionId);
    }

    @Test
    @DisplayName("Existe por nombre - Debería verificar existencia correctamente")
    void existePorNombre_DeberiaVerificarExistenciaCorrectamente() {
        // Given
        String nombre = "Crear Usuario";
        when(accionRepository.existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(
            nombre, aplicacionId, seccionId)).thenReturn(true);

        // When
        boolean resultado = accionService.existePorNombre(nombre, aplicacionId, seccionId);

        // Then
        assertThat(resultado).isTrue();
        verify(accionRepository).existsByNombreIgnoreCaseAndAplicacionIdAndSeccionId(
            nombre, aplicacionId, seccionId);
    }

    @Test
    @DisplayName("Buscar por texto - Debería buscar en nombre y descripción")
    void buscarPorTexto_DeberiaBuscarEnNombreYDescripcion() {
        // Given
        String texto = "usuario";
        List<Accion> acciones = List.of(accion);
        List<AccionSummary> summaries = List.of(accionSummary);
        
        when(accionRepository.findByTextoEnNombreOrDescripcion(texto)).thenReturn(acciones);
        when(accionMapper.toSummaryList(acciones)).thenReturn(summaries);

        // When
        List<AccionSummary> resultado = accionService.buscarPorTexto(texto);

        // Then
        assertThat(resultado).hasSize(1);
        verify(accionRepository).findByTextoEnNombreOrDescripcion(texto);
        verify(accionMapper).toSummaryList(acciones);
    }
}
