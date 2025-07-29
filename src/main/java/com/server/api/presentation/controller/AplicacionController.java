package com.server.api.presentation.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.api.application.service.AplicacionService;
import com.server.api.domain.dto.aplicacion.AplicacionCreateRequest;
import com.server.api.domain.dto.aplicacion.AplicacionResponse;
import com.server.api.domain.dto.aplicacion.AplicacionSummary;
import com.server.api.domain.dto.aplicacion.AplicacionUpdateRequest;
import com.server.api.domain.dto.aplicacion.EstadoAplicacionDto;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;
import com.server.api.domain.mapper.AplicacionMapper;
import com.server.api.presentation.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * Controlador REST para gestión de aplicaciones.
 * Implementa el principio SRP al manejar únicamente las peticiones HTTP relacionadas con aplicaciones.
 * Sigue el patrón de Clean Architecture manteniendo la lógica de presentación separada del negocio.
 */
@RestController
@RequestMapping("/api/aplicaciones")
@Validated
@Tag(name = "Aplicaciones", description = "Operaciones CRUD para aplicaciones del sistema")
public class AplicacionController {

    private final AplicacionService aplicacionService;
    private final AplicacionMapper aplicacionMapper;

    public AplicacionController(AplicacionService aplicacionService, AplicacionMapper aplicacionMapper) {
        this.aplicacionService = aplicacionService;
        this.aplicacionMapper = aplicacionMapper;
    }

    /**
     * Crea una nueva aplicación.
     * Implementa el principio de validación automática con Bean Validation.
     */
    @PostMapping
    @Operation(summary = "Crear nueva aplicación", description = "Crea una nueva aplicación en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Aplicación creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto - Llave o URL ya existente")
    })
    public ResponseEntity<ApiResponse<AplicacionResponse>> crear(@Valid @RequestBody AplicacionCreateRequest request) {
        try {
            Aplicacion aplicacion = aplicacionService.crear(request);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Aplicación creada exitosamente", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    /**
     * Obtiene una aplicación por su ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener aplicación por ID", description = "Obtiene una aplicación específica por su identificador único")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Aplicación encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aplicación no encontrada")
    })
    public ResponseEntity<ApiResponse<AplicacionResponse>> obtenerPorId(
            @Parameter(description = "ID único de la aplicación") @PathVariable UUID id) {
        try {
            Aplicacion aplicacion = aplicacionService.obtenerPorId(id);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.ok(new ApiResponse<>("Aplicación encontrada", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene una aplicación por su llave identificadora.
     */
    @GetMapping("/llave/{llaveIdentificadora}")
    @Operation(summary = "Obtener aplicación por llave identificadora", description = "Obtiene una aplicación por su llave única")
    public ResponseEntity<ApiResponse<AplicacionResponse>> obtenerPorLlaveIdentificadora(
            @Parameter(description = "Llave identificadora única") @PathVariable String llaveIdentificadora) {
        try {
            Aplicacion aplicacion = aplicacionService.obtenerPorLlaveIdentificadora(llaveIdentificadora);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.ok(new ApiResponse<>("Aplicación encontrada", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todas las aplicaciones activas.
     */
    @GetMapping
    @Operation(summary = "Obtener todas las aplicaciones", description = "Obtiene la lista de todas las aplicaciones activas")
    public ResponseEntity<ApiResponse<List<AplicacionSummary>>> obtenerTodas() {
        List<Aplicacion> aplicaciones = aplicacionService.obtenerTodas();
        List<AplicacionSummary> respuesta = aplicaciones.stream()
                .map(aplicacionMapper::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("Lista de aplicaciones obtenida exitosamente", respuesta));
    }

    /**
     * Obtiene aplicaciones con paginación.
     */
    @GetMapping("/paginado")
    @Operation(summary = "Obtener aplicaciones paginadas", description = "Obtiene aplicaciones con paginación y ordenamiento")
    public ResponseEntity<ApiResponse<Page<AplicacionSummary>>> obtenerTodas(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Aplicacion> aplicaciones = aplicacionService.obtenerTodas(pageable);
        Page<AplicacionSummary> respuesta = aplicaciones.map(aplicacionMapper::toSummary);
        
        return ResponseEntity.ok(new ApiResponse<>("Aplicaciones paginadas obtenidas exitosamente", respuesta));
    }

    /**
     * Busca aplicaciones por nombre.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar aplicaciones por nombre", description = "Busca aplicaciones que contengan el texto especificado en el nombre")
    public ResponseEntity<ApiResponse<List<AplicacionSummary>>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String nombre) {
        List<Aplicacion> aplicaciones = aplicacionService.buscarPorNombre(nombre);
        List<AplicacionSummary> respuesta = aplicaciones.stream()
                .map(aplicacionMapper::toSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("Búsqueda completada exitosamente", respuesta));
    }

    /**
     * Actualiza una aplicación existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar aplicación", description = "Actualiza una aplicación existente con nuevos datos")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Aplicación actualizada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aplicación no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflicto - Llave o URL ya existente")
    })
    public ResponseEntity<ApiResponse<AplicacionResponse>> actualizar(
            @Parameter(description = "ID de la aplicación a actualizar") @PathVariable UUID id,
            @Valid @RequestBody AplicacionUpdateRequest request) {
        try {
            Aplicacion aplicacion = aplicacionService.actualizar(id, request);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.ok(new ApiResponse<>("Aplicación actualizada exitosamente", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    /**
     * Elimina una aplicación (soft delete).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar aplicación", description = "Elimina una aplicación del sistema (eliminación lógica)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Aplicación eliminada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Aplicación no encontrada")
    })
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la aplicación a eliminar") @PathVariable UUID id) {
        try {
            aplicacionService.eliminar(id);
            return ResponseEntity.ok(new ApiResponse<>("Aplicación eliminada exitosamente", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Restaura una aplicación eliminada.
     */
    @PatchMapping("/{id}/restaurar")
    @Operation(summary = "Restaurar aplicación", description = "Restaura una aplicación previamente eliminada")
    public ResponseEntity<ApiResponse<AplicacionResponse>> restaurar(
            @Parameter(description = "ID de la aplicación a restaurar") @PathVariable UUID id) {
        try {
            Aplicacion aplicacion = aplicacionService.restaurar(id);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.ok(new ApiResponse<>("Aplicación restaurada exitosamente", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambia el estado de una aplicación.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de una aplicación (ACTIVO/INACTIVO)")
    public ResponseEntity<ApiResponse<AplicacionResponse>> cambiarEstado(
            @Parameter(description = "ID de la aplicación") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam EstadoAplicacionDto estado) {
        try {
            // Convertir DTO a enum de dominio
            EstadoAplicacion estadoDominio = aplicacionMapper.toEntityEstado(estado);
            Aplicacion aplicacion = aplicacionService.cambiarEstado(id, estadoDominio);
            AplicacionResponse respuesta = aplicacionMapper.toResponse(aplicacion);
            return ResponseEntity.ok(new ApiResponse<>("Estado cambiado exitosamente", respuesta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    /**
     * Cuenta el número de aplicaciones activas.
     */
    @GetMapping("/contar")
    @Operation(summary = "Contar aplicaciones activas", description = "Obtiene el número total de aplicaciones activas")
    public ResponseEntity<ApiResponse<Long>> contarAplicacionesActivas() {
        long count = aplicacionService.contarAplicacionesActivas();
        return ResponseEntity.ok(new ApiResponse<>("Conteo obtenido exitosamente", count));
    }

    /**
     * Verifica si existe una aplicación con la llave identificadora dada.
     */
    @GetMapping("/existe/{llaveIdentificadora}")
    @Operation(summary = "Verificar existencia por llave", description = "Verifica si existe una aplicación con la llave identificadora especificada")
    public ResponseEntity<ApiResponse<Boolean>> existePorLlaveIdentificadora(
            @Parameter(description = "Llave identificadora a verificar") @PathVariable String llaveIdentificadora) {
        boolean existe = aplicacionService.existePorLlaveIdentificadora(llaveIdentificadora);
        String mensaje = existe ? "La aplicación existe" : "La aplicación no existe";
        return ResponseEntity.ok(new ApiResponse<>(mensaje, existe));
    }
}
