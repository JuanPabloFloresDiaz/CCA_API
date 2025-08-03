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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.api.domain.dto.accion.AccionCreateRequest;
import com.server.api.domain.dto.accion.AccionUpdateRequest;
import com.server.api.domain.entity.Accion;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.repository.AccionRepository;
import com.server.api.domain.repository.AplicacionRepository;
import com.server.api.domain.repository.SeccionRepository;

/**
 * Tests de integración para AccionController.
 * Valida el funcionamiento completo del endpoint con base de datos real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "user", roles = "USER")
class AccionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccionRepository accionRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    private Accion accionPrueba;
    private Aplicacion aplicacionPrueba;
    private Seccion seccionPrueba;
    private UUID accionIdPrueba;
    private UUID aplicacionIdPrueba;
    private UUID seccionIdPrueba;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        accionRepository.deleteAll();
        aplicacionRepository.deleteAll();
        seccionRepository.deleteAll();
        
        // Crear aplicación de prueba
        aplicacionPrueba = new Aplicacion();
        aplicacionPrueba.setNombre("Sistema de Pruebas");
        aplicacionPrueba.setDescripcion("Sistema para testing");
        aplicacionPrueba.setUrl("https://test.example.com");
        aplicacionPrueba.setLlaveIdentificadora("TEST_SYSTEM");
        aplicacionPrueba.setEstado(EstadoAplicacion.ACTIVO);
        aplicacionPrueba = aplicacionRepository.save(aplicacionPrueba);
        aplicacionIdPrueba = aplicacionPrueba.getId();

        // Crear sección de prueba
        seccionPrueba = new Seccion();
        seccionPrueba.setNombre("Gestión de Usuarios");
        seccionPrueba.setDescripcion("Sección para gestionar usuarios");
        seccionPrueba = seccionRepository.save(seccionPrueba);
        seccionIdPrueba = seccionPrueba.getId();

        // Crear acción de prueba
        accionPrueba = new Accion();
        accionPrueba.setNombre("Crear Usuario");
        accionPrueba.setDescripcion("Permite crear nuevos usuarios en el sistema");
        accionPrueba.setAplicacion(aplicacionPrueba);
        accionPrueba.setSeccion(seccionPrueba);
        accionPrueba = accionRepository.save(accionPrueba);
        accionIdPrueba = accionPrueba.getId();
    }

    @Test
    @DisplayName("POST /api/acciones - Debería crear una acción exitosamente")
    void crearAccion_DeberiaCrearExitosamente() throws Exception {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Eliminar Usuario",
            "Permite eliminar usuarios del sistema",
            aplicacionIdPrueba,
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Acción creada exitosamente"))
                .andExpect(jsonPath("$.data.nombre").value("Eliminar Usuario"))
                .andExpect(jsonPath("$.data.descripcion").value("Permite eliminar usuarios del sistema"))
                .andExpect(jsonPath("$.data.aplicacion.nombre").value("Sistema de Pruebas"))
                .andExpect(jsonPath("$.data.seccion.nombre").value("Gestión de Usuarios"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("POST /api/acciones - Debería fallar con nombre duplicado")
    void crearAccion_DeberiaFallarConNombreDuplicado() throws Exception {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Crear Usuario", // Nombre duplicado
            "Otra descripción",
            aplicacionIdPrueba,
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ya existe una acción con el nombre")));
    }

    @Test
    @DisplayName("POST /api/acciones - Debería fallar con aplicación inexistente")
    void crearAccion_DeberiaFallarConAplicacionInexistente() throws Exception {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Nueva Acción",
            "Descripción de nueva acción",
            UUID.randomUUID(), // Aplicación inexistente
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/acciones - Debería fallar con sección inexistente")
    void crearAccion_DeberiaFallarConSeccionInexistente() throws Exception {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "Nueva Acción",
            "Descripción de nueva acción",
            aplicacionIdPrueba,
            UUID.randomUUID() // Sección inexistente
        );

        // When & Then
        mockMvc.perform(post("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/acciones/{id} - Debería retornar acción por ID")
    void obtenerAccionPorId_DeberiaRetornarAccion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/{id}", accionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(accionIdPrueba.toString()))
                .andExpect(jsonPath("$.data.nombre").value("Crear Usuario"))
                .andExpect(jsonPath("$.data.aplicacion.nombre").value("Sistema de Pruebas"))
                .andExpect(jsonPath("$.data.seccion.nombre").value("Gestión de Usuarios"));
    }

    @Test
    @DisplayName("GET /api/acciones/{id} - Debería fallar con ID inexistente")
    void obtenerAccionPorId_DeberiaFallarConIdInexistente() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/acciones - Debería retornar lista de acciones")
    void obtenerTodasLasAcciones_DeberiaRetornarLista() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nombre").value("Crear Usuario"));
    }

    @Test
    @DisplayName("GET /api/acciones - Debería filtrar por nombre")
    void obtenerAccionesFiltradaPorNombre_DeberiaRetornarCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .param("nombre", "Usuario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nombre").value("Crear Usuario"));
    }

    @Test
    @DisplayName("GET /api/acciones - Debería filtrar por aplicación")
    void obtenerAccionesFiltradaPorAplicacion_DeberiaRetornarCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .param("aplicacionId", aplicacionIdPrueba.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].aplicacionNombre").value("Sistema de Pruebas"));
    }

    @Test
    @DisplayName("GET /api/acciones - Debería filtrar por sección")
    void obtenerAccionesFiltradaPorSeccion_DeberiaRetornarCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .param("seccionId", seccionIdPrueba.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].seccionNombre").value("Gestión de Usuarios"));
    }

    @Test
    @DisplayName("GET /api/acciones - Debería filtrar por texto")
    void obtenerAccionesFiltradaPorTexto_DeberiaRetornarCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .param("texto", "crear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nombre").value("Crear Usuario"));
    }

    @Test
    @DisplayName("GET /api/acciones/paginado - Debería retornar página de acciones")
    void obtenerAccionesPaginadas_DeberiaRetornarPagina() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/paginado")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    @DisplayName("PUT /api/acciones/{id} - Debería actualizar acción exitosamente")
    void actualizarAccion_DeberiaActualizarExitosamente() throws Exception {
        // Given
        AccionUpdateRequest request = new AccionUpdateRequest(
            "Actualizar Usuario",
            "Permite actualizar información de usuarios",
            aplicacionIdPrueba,
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(put("/api/acciones/{id}", accionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.nombre").value("Actualizar Usuario"))
                .andExpect(jsonPath("$.data.descripcion").value("Permite actualizar información de usuarios"));
    }

    @Test
    @DisplayName("PUT /api/acciones/{id} - Debería fallar con ID inexistente")
    void actualizarAccion_DeberiaFallarConIdInexistente() throws Exception {
        // Given
        AccionUpdateRequest request = new AccionUpdateRequest(
            "Actualizar Usuario",
            "Permite actualizar información de usuarios",
            aplicacionIdPrueba,
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(put("/api/acciones/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/acciones/{id} - Debería eliminar acción exitosamente")
    void eliminarAccion_DeberiaEliminarExitosamente() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/acciones/{id}", accionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/acciones/{id} - Debería fallar con ID inexistente")
    void eliminarAccion_DeberiaFallarConIdInexistente() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/acciones/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/acciones/{id}/restaurar - Debería restaurar acción exitosamente")
    void restaurarAccion_DeberiaRestaurarExitosamente() throws Exception {
        // Given: Primero eliminamos la acción
        mockMvc.perform(delete("/api/acciones/{id}", accionIdPrueba));

        // When & Then: Ahora la restauramos
        mockMvc.perform(post("/api/acciones/{id}/restaurar", accionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Acción restaurada exitosamente"))
                .andExpect(jsonPath("$.data.nombre").value("Crear Usuario"));
    }

    @Test
    @DisplayName("POST /api/acciones/{id}/restaurar - Debería fallar con acción no eliminada")
    void restaurarAccion_DeberiaFallarConAccionNoEliminada() throws Exception {
        // When & Then: Intentar restaurar una acción que no está eliminada
        mockMvc.perform(post("/api/acciones/{id}/restaurar", accionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no está eliminada")));
    }

    @Test
    @DisplayName("GET /api/acciones/estadisticas - Debería retornar estadísticas")
    void obtenerEstadisticasAcciones_DeberiaRetornarEstadisticas() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/estadisticas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAcciones").value(1))
                .andExpect(jsonPath("$.data.accionesPorAplicacion").value(0))
                .andExpect(jsonPath("$.data.accionesPorSeccion").value(0));
    }

    @Test
    @DisplayName("GET /api/acciones/estadisticas - Debería retornar estadísticas por aplicación")
    void obtenerEstadisticasPorAplicacion_DeberiaRetornarEstadisticas() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/estadisticas")
                        .param("aplicacionId", aplicacionIdPrueba.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAcciones").value(1))
                .andExpect(jsonPath("$.data.accionesPorAplicacion").value(1))
                .andExpect(jsonPath("$.data.accionesPorSeccion").value(0));
    }

    @Test
    @DisplayName("GET /api/acciones/estadisticas - Debería retornar estadísticas por sección")
    void obtenerEstadisticasPorSeccion_DeberiaRetornarEstadisticas() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones/estadisticas")
                        .param("seccionId", seccionIdPrueba.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAcciones").value(1))
                .andExpect(jsonPath("$.data.accionesPorAplicacion").value(0))
                .andExpect(jsonPath("$.data.accionesPorSeccion").value(1));
    }

    @Test
    @DisplayName("POST /api/acciones - Debería fallar con datos inválidos")
    void crearAccion_DeberiaFallarConDatosInvalidos() throws Exception {
        // Given
        AccionCreateRequest request = new AccionCreateRequest(
            "", // Nombre vacío
            "Descripción válida",
            aplicacionIdPrueba,
            seccionIdPrueba
        );

        // When & Then
        mockMvc.perform(post("/api/acciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/acciones - Debería retornar lista vacía sin coincidencias")
    void obtenerAcciones_DeberiaRetornarListaVaciaSinCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/acciones")
                        .param("nombre", "NoExiste")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
