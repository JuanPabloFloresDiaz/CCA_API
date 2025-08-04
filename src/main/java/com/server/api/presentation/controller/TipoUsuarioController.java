package com.server.api.presentation.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.server.api.application.service.TipoUsuarioService;
import com.server.api.domain.dto.tipousuario.TipoUsuarioCreateRequest;
import com.server.api.domain.dto.tipousuario.TipoUsuarioResponse;
import com.server.api.domain.dto.tipousuario.TipoUsuarioSummary;
import com.server.api.domain.dto.tipousuario.TipoUsuarioUpdateRequest;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;
import com.server.api.presentation.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de tipos de usuario.
 * Proporciona endpoints para operaciones CRUD y consultas especializadas.
 * Sigue principios REST y manejo consistente de respuestas.
 */
@RestController
@RequestMapping("/api/tipos-usuario")
@RequiredArgsConstructor
@Slf4j
public class TipoUsuarioController {

    private final TipoUsuarioService tipoUsuarioService;

    /**
     * Crea un nuevo tipo de usuario
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TipoUsuarioResponse>> crear(@Valid @RequestBody TipoUsuarioCreateRequest request) {
        log.info("Petición para crear tipo de usuario: {}", request.nombre());
        
        TipoUsuarioResponse tipoUsuario = tipoUsuarioService.crear(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Tipo de usuario creado exitosamente", tipoUsuario));
    }

    /**
     * Obtiene un tipo de usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoUsuarioResponse>> obtenerPorId(@PathVariable UUID id) {
        log.debug("Petición para obtener tipo de usuario por ID: {}", id);
        
        TipoUsuarioResponse tipoUsuario = tipoUsuarioService.obtenerPorId(id);
        
        return ResponseEntity.ok(ApiResponse.success("Tipo de usuario encontrado", tipoUsuario));
    }

    /**
     * Obtiene todos los tipos de usuario con paginación y filtros
     */
    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<Page<TipoUsuarioSummary>>> obtenerTodosPaginado(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) UUID aplicacionId,
            @RequestParam(required = false) EstadoTipoUsuario estado) {
        
        log.debug("Petición para obtener tipos de usuario paginados");
        
        Page<TipoUsuarioSummary> tiposUsuario = tipoUsuarioService.obtenerTodos(pageable, nombre, aplicacionId, estado);
        
        return ResponseEntity.ok(ApiResponse.success("Página de tipos de usuario obtenida exitosamente", tiposUsuario));
    }

    /**
     * Actualiza un tipo de usuario existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TipoUsuarioResponse>> actualizar(
            @PathVariable UUID id, 
            @Valid @RequestBody TipoUsuarioUpdateRequest request) {
        
        log.info("Petición para actualizar tipo de usuario con ID: {}", id);
        
        TipoUsuarioResponse tipoUsuario = tipoUsuarioService.actualizar(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Tipo de usuario actualizado exitosamente", tipoUsuario));
    }

    /**
     * Elimina un tipo de usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("Petición para eliminar tipo de usuario con ID: {}", id);
        
        tipoUsuarioService.eliminar(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambia el estado de un tipo de usuario
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<TipoUsuarioResponse>> cambiarEstado(
            @PathVariable UUID id, 
            @RequestParam EstadoTipoUsuario estado) {
        
        log.info("Petición para cambiar estado del tipo de usuario con ID: {} a {}", id, estado);
        
        TipoUsuarioResponse tipoUsuario = tipoUsuarioService.cambiarEstado(id, estado);
        
        return ResponseEntity.ok(ApiResponse.success("Estado del tipo de usuario cambiado exitosamente", tipoUsuario));
    }

    /**
     * Obtiene estadísticas de tipos de usuario
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerEstadisticas(
            @RequestParam(required = false) UUID aplicacionId,
            @RequestParam(required = false) EstadoTipoUsuario estado) {
        
        log.debug("Petición para obtener estadísticas de tipos de usuario");
        
        Map<String, Long> estadisticas = tipoUsuarioService.obtenerEstadisticas(aplicacionId, estado);
        
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas exitosamente", estadisticas));
    }
}
