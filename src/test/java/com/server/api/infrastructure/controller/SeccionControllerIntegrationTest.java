package com.server.api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.api.domain.dto.seccion.SeccionCreateRequest;
import com.server.api.domain.dto.seccion.SeccionUpdateRequest;
import com.server.api.domain.entity.Seccion;
import com.server.api.domain.repository.SeccionRepository;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para SeccionController.
 * Valida el funcionamiento completo del endpoint con base de datos real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "user", roles = "USER")
class SeccionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeccionRepository seccionRepository;

    private Seccion seccionPrueba;
    private UUID seccionIdPrueba;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        seccionRepository.deleteAll();
        
        // Crear sección de prueba
        seccionPrueba = new Seccion();
        seccionPrueba.setNombre("Sección de Prueba");
        seccionPrueba.setDescripcion("Descripción de prueba para testing");
        seccionPrueba.setCreatedAt(OffsetDateTime.now());
        seccionPrueba.setUpdatedAt(OffsetDateTime.now());
        
        seccionPrueba = seccionRepository.save(seccionPrueba);
        seccionIdPrueba = seccionPrueba.getId();
    }

    @Test
    @DisplayName("POST /api/secciones - Debería crear una sección exitosamente")
    void crearSeccion_DeberiaCrearExitosamente() throws Exception {
        // Given
        SeccionCreateRequest request = new SeccionCreateRequest(
                "Nueva Sección",
                "Descripción de la nueva sección"
        );

        // When & Then
        mockMvc.perform(post("/api/secciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Nueva Sección"))
                .andExpect(jsonPath("$.descripcion").value("Descripción de la nueva sección"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("GET /api/secciones/{id} - Debería retornar sección por ID")
    void obtenerSeccionPorId_DeberiaRetornarSeccion() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/secciones/{id}", seccionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(seccionIdPrueba.toString()))
                .andExpect(jsonPath("$.nombre").value("Sección de Prueba"));
    }

    @Test
    @DisplayName("GET /api/secciones - Debería retornar lista de secciones")
    void obtenerTodasLasSecciones_DeberiaRetornarLista() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/secciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Sección de Prueba"));
    }

    @Test
    @DisplayName("PUT /api/secciones/{id} - Debería actualizar sección exitosamente")
    void actualizarSeccion_DeberiaActualizarExitosamente() throws Exception {
        // Given
        SeccionUpdateRequest request = new SeccionUpdateRequest(
                "Sección Actualizada",
                "Descripción actualizada"
        );

        // When & Then
        mockMvc.perform(put("/api/secciones/{id}", seccionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Sección Actualizada"))
                .andExpect(jsonPath("$.descripcion").value("Descripción actualizada"));
    }

    @Test
    @DisplayName("DELETE /api/secciones/{id} - Debería eliminar sección exitosamente")
    void eliminarSeccion_DeberiaEliminarExitosamente() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/secciones/{id}", seccionIdPrueba)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
