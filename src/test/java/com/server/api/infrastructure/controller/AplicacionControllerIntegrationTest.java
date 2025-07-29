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
import com.server.api.domain.dto.aplicacion.AplicacionCreateRequest;
import com.server.api.domain.dto.aplicacion.AplicacionUpdateRequest;
import com.server.api.domain.entity.Aplicacion;
import com.server.api.domain.entity.Aplicacion.EstadoAplicacion;
import com.server.api.domain.repository.AplicacionRepository;

/**
 * Tests de integración para AplicacionController.
 * Valida el funcionamiento completo del endpoint con base de datos real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "user", roles = "USER")
class AplicacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    private Aplicacion aplicacionPrueba;
    private UUID aplicacionIdPrueba;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        aplicacionRepository.deleteAll();
        
        // Crear aplicación de prueba
        aplicacionPrueba = new Aplicacion();
        aplicacionPrueba.setNombre("Aplicación de Prueba");
        aplicacionPrueba.setDescripcion("Descripción de prueba para testing");
        aplicacionPrueba.setUrl("https://test-app.example.com");
        aplicacionPrueba.setLlaveIdentificadora("TEST_APP_INTEGRATION");
        aplicacionPrueba.setEstado(EstadoAplicacion.ACTIVO);
        
        aplicacionPrueba = aplicacionRepository.save(aplicacionPrueba);
        aplicacionIdPrueba = aplicacionPrueba.getId();
    }

    @Test
    @DisplayName("POST /api/aplicaciones - Debería crear una aplicación exitosamente")
    void crearAplicacion_DeberiaCrearExitosamente() throws Exception {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
                "Nueva Aplicación",
                "Descripción de la nueva aplicación",
                "https://nueva-app.example.com",
                "NUEVA_APP_001",
                "ACTIVO"
        );

        // When & Then
        mockMvc.perform(post("/api/aplicaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Nueva Aplicación"))
                .andExpect(jsonPath("$.descripcion").value("Descripción de la nueva aplicación"))
                .andExpect(jsonPath("$.url").value("https://nueva-app.example.com"))
                .andExpect(jsonPath("$.llaveIdentificadora").value("NUEVA_APP_001"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/aplicaciones - Debería fallar con llave duplicada")
    void crearAplicacion_DeberiaFallarConLlaveDuplicada() throws Exception {
        // Given
        AplicacionCreateRequest request = new AplicacionCreateRequest(
                "Otra Aplicación",
                "Descripción de otra aplicación",
                "https://otra-app.example.com",
                "TEST_APP_INTEGRATION", // Llave duplicada
                "ACTIVO"
        );

        // When & Then
        mockMvc.perform(post("/api/aplicaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/aplicaciones/{id} - Debería retornar aplicación por ID")
    void obtenerAplicacionPorId_DeberiaRetornarAplicacion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/aplicaciones/{id}", aplicacionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(aplicacionIdPrueba.toString()))
                .andExpect(jsonPath("$.nombre").value("Aplicación de Prueba"))
                .andExpect(jsonPath("$.llaveIdentificadora").value("TEST_APP_INTEGRATION"));
    }

    @Test
    @DisplayName("GET /api/aplicaciones - Debería retornar lista de aplicaciones")
    void obtenerTodasLasAplicaciones_DeberiaRetornarLista() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/aplicaciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Aplicación de Prueba"));
    }

    @Test
    @DisplayName("GET /api/aplicaciones/buscar - Debería buscar por nombre")
    void buscarAplicacionesPorNombre_DeberiaRetornarCoincidencias() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/aplicaciones/buscar")
                        .param("nombre", "Prueba")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Aplicación de Prueba"));
    }

    @Test
    @DisplayName("PUT /api/aplicaciones/{id} - Debería actualizar aplicación exitosamente")
    void actualizarAplicacion_DeberiaActualizarExitosamente() throws Exception {
        // Given
        AplicacionUpdateRequest request = new AplicacionUpdateRequest(
                "Aplicación Actualizada",
                "Descripción actualizada",
                "https://app-actualizada.example.com",
                "TEST_APP_UPDATED",
                "INACTIVO"
        );

        // When & Then
        mockMvc.perform(put("/api/aplicaciones/{id}", aplicacionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Aplicación Actualizada"))
                .andExpect(jsonPath("$.descripcion").value("Descripción actualizada"))
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("PATCH /api/aplicaciones/{id}/estado - Debería cambiar estado exitosamente")
    void cambiarEstadoAplicacion_DeberiaCambiarExitosamente() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/aplicaciones/{id}/estado", aplicacionIdPrueba)
                        .param("estado", "INACTIVO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("DELETE /api/aplicaciones/{id} - Debería eliminar aplicación exitosamente")
    void eliminarAplicacion_DeberiaEliminarExitosamente() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/aplicaciones/{id}", aplicacionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/aplicaciones/estadisticas - Debería retornar estadísticas")
    void obtenerEstadisticasAplicaciones_DeberiaRetornarEstadisticas() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/aplicaciones/estadisticas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAplicaciones").value(1));
    }

    @Test
    @DisplayName("GET /api/aplicaciones/llave/{llave} - Debería retornar aplicación por llave")
    void obtenerAplicacionPorLlave_DeberiaRetornarAplicacion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/aplicaciones/llave/{llave}", "TEST_APP_INTEGRATION")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.llaveIdentificadora").value("TEST_APP_INTEGRATION"))
                .andExpect(jsonPath("$.nombre").value("Aplicación de Prueba"));
    }
}
