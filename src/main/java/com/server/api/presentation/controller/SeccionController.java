package com.server.api.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.server.api.application.service.SeccionService;
import com.server.api.domain.dto.seccion.DisponibilidadNombre;
import com.server.api.domain.dto.seccion.EstadisticasSecciones;
import com.server.api.domain.dto.seccion.SeccionCreateRequest;
import com.server.api.domain.dto.seccion.SeccionResponse;
import com.server.api.domain.dto.seccion.SeccionSummary;
import com.server.api.domain.dto.seccion.SeccionUpdateRequest;
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
 * Controlador REST para gestión de secciones.
 * Proporciona endpoints CRUD siguiendo principios REST.
 * Documentado con OpenAPI/Swagger para facilitar el testing.
 */
@RestController
@RequestMapping("/api/secciones")
@Tag(name = "Secciones", description = "Gestión de secciones del sistema")
@CrossOrigin(origins = "*")
public class SeccionController {

    private final SeccionService seccionService;

    public SeccionController(SeccionService seccionService) {
        this.seccionService = seccionService;
    }

    @Operation(
        summary = "Crear nueva sección",
        description = "Crea una nueva sección en el sistema. El nombre debe ser único."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Sección creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Error de validación",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SeccionResponse>> crear(
            @Valid @RequestBody 
            @Parameter(description = "Datos para crear la sección", required = true)
            SeccionCreateRequest request) {
        
        try {
            SeccionResponse response = seccionService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Sección creada exitosamente", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Obtener sección por ID",
        description = "Obtiene la información completa de una sección específica."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sección encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Sección no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeccionResponse>> obtenerPorId(
            @PathVariable 
            @Parameter(description = "ID único de la sección", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            SeccionResponse response = seccionService.obtenerPorId(id);
            return ResponseEntity.ok(new ApiResponse<>("Sección encontrada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Listar todas las secciones",
        description = "Obtiene una lista de todas las secciones activas del sistema."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de secciones obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<SeccionSummary>>> obtenerTodas(
            @RequestParam(value = "nombre", required = false)
            @Parameter(description = "Filtrar por nombre (búsqueda parcial)", example = "Gestión")
            String nombre,
            
            @RequestParam(value = "texto", required = false)
            @Parameter(description = "Buscar texto en nombre o descripción", example = "usuarios")
            String texto) {
        
        List<SeccionSummary> secciones;
        
        if (texto != null && !texto.trim().isEmpty()) {
            secciones = seccionService.buscarPorTexto(texto.trim());
        } else if (nombre != null && !nombre.trim().isEmpty()) {
            secciones = seccionService.buscarPorNombre(nombre.trim());
        } else {
            secciones = seccionService.obtenerTodas();
        }
        
        return ResponseEntity.ok(new ApiResponse<>("Lista de secciones obtenida exitosamente", secciones));
    }

    @Operation(
        summary = "Listar secciones con paginación",
        description = "Obtiene una lista paginada de secciones activas."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Página de secciones obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<SeccionSummary>>> obtenerTodasPaginado(
            @PageableDefault(size = 10, sort = "nombre")
            @Parameter(description = "Configuración de paginación (page, size, sort)")
            Pageable pageable,
            
            @RequestParam(value = "nombre", required = false)
            @Parameter(description = "Filtrar por nombre (búsqueda parcial)", example = "Gestión")
            String nombre) {
        
        Page<SeccionSummary> secciones;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            secciones = seccionService.buscarPorNombre(nombre.trim(), pageable);
        } else {
            secciones = seccionService.obtenerTodas(pageable);
        }
        
        return ResponseEntity.ok(new ApiResponse<>("Página de secciones obtenida exitosamente", secciones));
    }

    @Operation(
        summary = "Actualizar sección",
        description = "Actualiza la información de una sección existente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sección actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Sección no encontrada",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Error de validación",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeccionResponse>> actualizar(
            @PathVariable 
            @Parameter(description = "ID único de la sección", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            
            @Valid @RequestBody 
            @Parameter(description = "Datos para actualizar la sección", required = true)
            SeccionUpdateRequest request) {
        
        try {
            SeccionResponse response = seccionService.actualizar(id, request);
            return ResponseEntity.ok(new ApiResponse<>("Sección actualizada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Eliminar sección",
        description = "Elimina una sección del sistema (soft delete). La sección puede ser restaurada posteriormente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Sección eliminada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Sección no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable 
            @Parameter(description = "ID único de la sección", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            seccionService.eliminar(id);
            return ResponseEntity.ok(new ApiResponse<>("Sección eliminada exitosamente", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Restaurar sección eliminada",
        description = "Restaura una sección que fue eliminada previamente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Sección restaurada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeccionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "La sección no está eliminada o nombre duplicado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Sección no encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/{id}/restaurar")
    public ResponseEntity<ApiResponse<SeccionResponse>> restaurar(
            @PathVariable 
            @Parameter(description = "ID único de la sección", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id) {
        
        try {
            SeccionResponse response = seccionService.restaurar(id);
            return ResponseEntity.ok(new ApiResponse<>("Sección restaurada exitosamente", response));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @Operation(
        summary = "Obtener estadísticas de secciones",
        description = "Obtiene información estadística sobre las secciones del sistema."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasSecciones>> obtenerEstadisticas() {
        long totalSecciones = seccionService.contarSecciones();
        
        EstadisticasSecciones estadisticas = new EstadisticasSecciones(totalSecciones);
        return ResponseEntity.ok(new ApiResponse<>("Estadísticas obtenidas exitosamente", estadisticas));
    }

    @Operation(
        summary = "Verificar disponibilidad de nombre",
        description = "Verifica si un nombre de sección está disponible para uso."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resultado de la verificación obtenido",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/verificar-nombre")
    public ResponseEntity<ApiResponse<DisponibilidadNombre>> verificarNombre(
            @RequestParam("nombre") 
            @Parameter(description = "Nombre a verificar", required = true, example = "Nueva Sección")
            String nombre) {
        
        boolean existe = seccionService.existePorNombre(nombre);
        DisponibilidadNombre disponibilidad = new DisponibilidadNombre(nombre, !existe, existe);
        
        String mensaje = existe ? "El nombre ya está en uso" : "El nombre está disponible";
        return ResponseEntity.ok(new ApiResponse<>(mensaje, disponibilidad));
    }
}
