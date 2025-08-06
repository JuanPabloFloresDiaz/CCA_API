package com.server.api.infrastructure.controller;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.api.domain.dto.tipousuario.TipoUsuarioCreateRequest;
import com.server.api.domain.dto.tipousuario.TipoUsuarioUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;
import com.server.api.domain.entity.TipoUsuario;
import com.server.api.domain.entity.TipoUsuario.EstadoTipoUsuario;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.TipoUsuarioRepository;

/**
 * Tests de integración para TipoUsuarioController.
 * Valida el funcionamiento completo del endpoint con base de datos real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "user", roles = "USER")
class TipoUsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    private TipoUsuario tipoUsuarioPrueba;
    private Aplicacion aplicacionPrueba;
    private UUID tipoUsuarioIdPrueba;
    private UUID aplicacionIdPrueba;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        tipoUsuarioRepository.deleteAll();
        aplicacionRepository.deleteAll();
        
        // Crear aplicación de prueba
        aplicacionPrueba = new Aplicacion();
        aplicacionPrueba.setNombre("Sistema de Pruebas");
        aplicacionPrueba.setDescripcion("Sistema para testing");
        aplicacionPrueba.setEstado(EstadoAplicacion.ACTIVO);
        aplicacionPrueba = aplicacionRepository.save(aplicacionPrueba);
        aplicacionIdPrueba = aplicacionPrueba.getId();
        
        // Crear tipo de usuario de prueba
        tipoUsuarioPrueba = new TipoUsuario();
        tipoUsuarioPrueba.setNombre("Administrador Test");
        tipoUsuarioPrueba.setDescripcion("Tipo de usuario para pruebas");
        tipoUsuarioPrueba.setEstado(EstadoTipoUsuario.ACTIVO);
        tipoUsuarioPrueba.setAplicacion(aplicacionPrueba);
        tipoUsuarioPrueba = tipoUsuarioRepository.save(tipoUsuarioPrueba);
        tipoUsuarioIdPrueba = tipoUsuarioPrueba.getId();
    }

    @Test
    @DisplayName("POST /api/tipos-usuario - Crear tipo de usuario exitoso")
    void crearTipoUsuario_Exitoso() throws Exception {
        // Given
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Usuario Estándar",
            "Usuario con permisos básicos",
            aplicacionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/tipos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("TipoUsuario creado exitosamente"))
                .andExpect(jsonPath("$.data.nombre").value("Usuario Estándar"))
                .andExpect(jsonPath("$.data.descripcion").value("Usuario con permisos básicos"))
                .andExpect(jsonPath("$.data.aplicacionId").value(aplicacionIdPrueba.toString()))
                .andExpect(jsonPath("$.data.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/tipos-usuario - Datos inválidos")
    void crearTipoUsuario_DatosInvalidos_DeberiaRetornar400() throws Exception {
        // Given - Nombre muy corto
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "A",
            "Descripción válida",
            aplicacionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/tipos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/tipos-usuario - Aplicación no existe")
    void crearTipoUsuario_AplicacionNoExiste_DeberiaRetornar404() throws Exception {
        // Given
        UUID aplicacionInexistente = UUID.randomUUID();
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Usuario Válido",
            "Descripción válida",
            aplicacionInexistente
        );

        // When & Then
        mockMvc.perform(post("/api/tipos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario/{id} - Obtener por ID exitoso")
    void obtenerTipoUsuarioPorId_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario/{id}", tipoUsuarioIdPrueba))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tipoUsuarioIdPrueba.toString()))
                .andExpect(jsonPath("$.data.nombre").value("Administrador Test"))
                .andExpect(jsonPath("$.data.descripcion").value("Tipo de usuario para pruebas"))
                .andExpect(jsonPath("$.data.aplicacionId").value(aplicacionIdPrueba.toString()))
                .andExpect(jsonPath("$.data.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario/{id} - ID no existe")
    void obtenerTipoUsuarioPorId_NoExiste_DeberiaRetornar404() throws Exception {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/tipos-usuario/{id}", idInexistente))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario - Obtener todos paginado")
    void obtenerTodosPaginado_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Administrador Test"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario - Filtrar por nombre")
    void obtenerTodosConFiltroNombre_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario")
                .param("nombre", "Admin")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Administrador Test"));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario - Filtrar por aplicación")
    void obtenerTodosConFiltroAplicacion_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario")
                .param("aplicacionId", aplicacionIdPrueba.toString())
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].aplicacionNombre").value("Sistema de Pruebas"));
    }

    @Test
    @DisplayName("PUT /api/tipos-usuario/{id} - Actualizar exitoso")
    void actualizarTipoUsuario_Exitoso() throws Exception {
        // Given
        TipoUsuarioUpdateRequest request = new TipoUsuarioUpdateRequest(
            "Administrador Actualizado",
            "Descripción actualizada",
            aplicacionIdPrueba,
            "ACTIVO"
        );

        // When & Then
        mockMvc.perform(put("/api/tipos-usuario/{id}", tipoUsuarioIdPrueba)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("TipoUsuario actualizado exitosamente"))
                .andExpect(jsonPath("$.data.nombre").value("Administrador Actualizado"))
                .andExpect(jsonPath("$.data.descripcion").value("Descripción actualizada"));
    }

    @Test
    @DisplayName("PUT /api/tipos-usuario/{id} - ID no existe")
    void actualizarTipoUsuario_NoExiste_DeberiaRetornar404() throws Exception {
        // Given
        UUID idInexistente = UUID.randomUUID();
        TipoUsuarioUpdateRequest request = new TipoUsuarioUpdateRequest(
            "Nombre Válido",
            "Descripción válida",
            aplicacionIdPrueba,
            "ACTIVO"
        );

        // When & Then
        mockMvc.perform(put("/api/tipos-usuario/{id}", idInexistente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/tipos-usuario/{id} - Eliminar exitoso")
    void eliminarTipoUsuario_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tipos-usuario/{id}", tipoUsuarioIdPrueba))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("TipoUsuario eliminado exitosamente"));

        // Verificar que fue eliminado (soft delete)
        mockMvc.perform(get("/api/tipos-usuario/{id}", tipoUsuarioIdPrueba))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tipos-usuario/{id} - ID no existe")
    void eliminarTipoUsuario_NoExiste_DeberiaRetornar404() throws Exception {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/tipos-usuario/{id}", idInexistente))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PATCH /api/tipos-usuario/{id}/estado - Cambiar estado exitoso")
    void cambiarEstadoTipoUsuario_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/tipos-usuario/{id}/estado", tipoUsuarioIdPrueba)
                .param("estado", "INACTIVO"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado actualizado exitosamente"))
                .andExpect(jsonPath("$.data.estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario/estadisticas - Obtener estadísticas")
    void obtenerEstadisticas_Exitoso() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario/estadisticas")
                .param("aplicacionId", aplicacionIdPrueba.toString())
                .param("estado", "ACTIVO"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPorAplicacion").value(1))
                .andExpect(jsonPath("$.data.totalPorEstado").value(1))
                .andExpect(jsonPath("$.data.totalGeneral").value(1));
    }

    @Test
    @DisplayName("POST /api/tipos-usuario - Nombre duplicado en misma aplicación")
    void crearTipoUsuario_NombreDuplicado_DeberiaRetornar400() throws Exception {
        // Given - Intentar crear con el mismo nombre y aplicación
        TipoUsuarioCreateRequest request = new TipoUsuarioCreateRequest(
            "Administrador Test", // Mismo nombre que el existente
            "Otra descripción",
            aplicacionIdPrueba // Misma aplicación
        );

        // When & Then
        mockMvc.perform(post("/api/tipos-usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ya existe un tipo de usuario con el nombre 'Administrador Test' en la aplicación especificada"));
    }

    @Test
    @DisplayName("GET /api/tipos-usuario - Sin resultados")
    void obtenerTodosConFiltroSinResultados_DeberiaRetornarVacio() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tipos-usuario")
                .param("nombre", "NoExiste")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
