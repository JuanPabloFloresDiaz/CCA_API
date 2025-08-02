package com.server.api.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.api.application.service.AccionService;
import com.server.api.domain.dto.accion.AccionCreateRequest;
import com.server.api.domain.dto.accion.AccionResponse;
import com.server.api.domain.dto.accion.AccionSummary;
import com.server.api.domain.dto.accion.AccionUpdateRequest;
import com.server.api.presentation.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

/**
 * Controlador REST para gestión de acciones.
 * Implementa el principio SRP al manejar únicamente las peticiones HTTP relacionadas con acciones.
 * Sigue el patrón de Clean Architecture manteniendo la lógica de presentación separada del negocio.
 * Proporciona endpoints CRUD siguiendo principios REST.
 */
@RestController
@RequestMapping("/api/acciones")
@Validated
@Tag(name = "Acciones", description = "Gestión de acciones del sistema")
@CrossOrigin(origins = "*")
public class AccionController {

    private final AccionService accionService;

    public AccionController(AccionService accionService) {
        this.accionService = accionService;
    }

    @Operation(
        summary = "Crear nueva acción",
        description = "Crea una nueva acción en el sistema asociada a una aplicación y sección específicas."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Acción creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Aplicación o sección no encontrada",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Error de validación",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AccionResponse>> crear(
            @Valid @RequestBody 
            @Parameter(description = "Datos para crear la acción", required = true)
            AccionCreateRequest request) {
        
        try {
            AccionResponse response = accionService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Acción creada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Obtener acción por ID",
        description = "Obtiene una acción específica por su identificador único."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Acción encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Acción no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccionResponse>> obtenerPorId(
            @PathVariable 
            @Parameter(description = "ID único de la acción", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            AccionResponse response = accionService.obtenerPorId(id);
            return ResponseEntity.ok(new ApiResponse<>("Acción encontrada", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Listar todas las acciones",
        description = "Obtiene una lista de todas las acciones activas del sistema con opciones de filtrado."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de acciones obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccionSummary>>> obtenerTodas(
            @RequestParam(value = "nombre", required = false)
            @Parameter(description = "Filtrar por nombre (búsqueda parcial)", example = "Crear")
            String nombre,
            
            @RequestParam(value = "texto", required = false)
            @Parameter(description = "Buscar texto en nombre o descripción", example = "usuario")
            String texto,
            
            @RequestParam(value = "aplicacionId", required = false)
            @Parameter(description = "Filtrar por ID de aplicación", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID aplicacionId,
            
            @RequestParam(value = "seccionId", required = false)
            @Parameter(description = "Filtrar por ID de sección", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID seccionId) {
        
        List<AccionSummary> acciones;
        
        if (aplicacionId != null && seccionId != null) {
            acciones = accionService.buscarPorAplicacionYSeccion(aplicacionId, seccionId);
        } else if (aplicacionId != null) {
            acciones = accionService.buscarPorAplicacion(aplicacionId);
        } else if (seccionId != null) {
            acciones = accionService.buscarPorSeccion(seccionId);
        } else if (nombre != null && !nombre.trim().isEmpty()) {
            acciones = accionService.buscarPorNombre(nombre.trim());
        } else if (texto != null && !texto.trim().isEmpty()) {
            acciones = accionService.buscarPorTexto(texto.trim());
        } else {
            acciones = accionService.obtenerTodas();
        }
        
        return ResponseEntity.ok(new ApiResponse<>("Lista de acciones obtenida exitosamente", acciones));
    }

    @Operation(
        summary = "Listar acciones con paginación",
        description = "Obtiene una lista paginada de acciones activas con opciones de filtrado."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Página de acciones obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<Page<AccionSummary>>> obtenerTodasPaginado(
            @PageableDefault(size = 10, sort = "nombre")
            @Parameter(description = "Configuración de paginación (page, size, sort)")
            Pageable pageable,
            
            @RequestParam(value = "nombre", required = false)
            @Parameter(description = "Filtrar por nombre (búsqueda parcial)", example = "Crear")
            String nombre,
            
            @RequestParam(value = "aplicacionId", required = false)
            @Parameter(description = "Filtrar por ID de aplicación", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID aplicacionId,
            
            @RequestParam(value = "seccionId", required = false)
            @Parameter(description = "Filtrar por ID de sección", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID seccionId) {
        
        Page<AccionSummary> acciones;
        
        if (aplicacionId != null) {
            acciones = accionService.buscarPorAplicacion(aplicacionId, pageable);
        } else if (seccionId != null) {
            acciones = accionService.buscarPorSeccion(seccionId, pageable);
        } else if (nombre != null && !nombre.trim().isEmpty()) {
            acciones = accionService.buscarPorNombre(nombre.trim(), pageable);
        } else {
            acciones = accionService.obtenerTodas(pageable);
        }
        
        return ResponseEntity.ok(new ApiResponse<>("Página de acciones obtenida exitosamente", acciones));
    }

    @Operation(
        summary = "Actualizar acción",
        description = "Actualiza la información de una acción existente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Acción actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Acción, aplicación o sección no encontrada",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Error de validación",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccionResponse>> actualizar(
            @PathVariable 
            @Parameter(description = "ID único de la acción", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            
            @Valid @RequestBody 
            @Parameter(description = "Datos para actualizar la acción", required = true)
            AccionUpdateRequest request) {
        
        try {
            AccionResponse response = accionService.actualizar(id, request);
            return ResponseEntity.ok(new ApiResponse<>("Acción actualizada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Eliminar acción",
        description = "Elimina una acción del sistema (soft delete). La acción puede ser restaurada posteriormente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Acción eliminada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Acción no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable 
            @Parameter(description = "ID único de la acción", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            accionService.eliminar(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @Operation(
        summary = "Restaurar acción eliminada",
        description = "Restaura una acción que fue eliminada previamente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Acción restaurada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "La acción no está eliminada o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Acción no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/{id}/restaurar")
    public ResponseEntity<ApiResponse<AccionResponse>> restaurar(
            @PathVariable 
            @Parameter(description = "ID único de la acción", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            AccionResponse response = accionService.restaurar(id);
            return ResponseEntity.ok(new ApiResponse<>("Acción restaurada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Obtener estadísticas de acciones",
        description = "Obtiene estadísticas básicas sobre las acciones del sistema."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasAcciones>> obtenerEstadisticas(
            @RequestParam(value = "aplicacionId", required = false)
            @Parameter(description = "ID de aplicación para estadísticas específicas")
            UUID aplicacionId,
            
            @RequestParam(value = "seccionId", required = false)
            @Parameter(description = "ID de sección para estadísticas específicas")
            UUID seccionId) {
        
        long total = accionService.contarAcciones();
        long porAplicacion = aplicacionId != null ? accionService.contarPorAplicacion(aplicacionId) : 0;
        long porSeccion = seccionId != null ? accionService.contarPorSeccion(seccionId) : 0;
        
        EstadisticasAcciones estadisticas = new EstadisticasAcciones(
            total,
            porAplicacion,
            porSeccion
        );
        
        return ResponseEntity.ok(new ApiResponse<>("Estadísticas obtenidas exitosamente", estadisticas));
    }

    /**
     * DTO para estadísticas de acciones
     */
    @Schema(description = "Estadísticas de acciones del sistema")
    public record EstadisticasAcciones(
            @Schema(description = "Total de acciones activas", example = "25")
            long totalAcciones,
            
            @Schema(description = "Acciones en la aplicación especificada", example = "8")
            long accionesPorAplicacion,
            
            @Schema(description = "Acciones en la sección especificada", example = "5")
            long accionesPorSeccion
    ) {}
}
