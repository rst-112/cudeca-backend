package com.cudeca.controller;

import com.cudeca.config.JwtAuthFilter;
import com.cudeca.config.SecurityConfig;
import com.cudeca.dto.DatosFiscalesDTO;
import com.cudeca.service.DatosFiscalesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = DatosFiscalesController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class DatosFiscalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatosFiscalesService datosFiscalesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void obtenerDatosFiscalesPorUsuario_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        DatosFiscalesDTO datos1 = new DatosFiscalesDTO();
        datos1.setId(1L);
        datos1.setNombreCompleto("Juan Pérez");
        datos1.setNif("12345678A");

        DatosFiscalesDTO datos2 = new DatosFiscalesDTO();
        datos2.setId(2L);
        datos2.setNombreCompleto("María García");
        datos2.setNif("87654321B");

        List<DatosFiscalesDTO> listaExpected = Arrays.asList(datos1, datos2);
        when(datosFiscalesService.obtenerDatosFiscalesPorUsuario(usuarioId)).thenReturn(listaExpected);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/datos-fiscales", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nombreCompleto").value("María García"));
    }

    @Test
    void obtenerDatosFiscalesPorId_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscalesDTO datos = new DatosFiscalesDTO();
        datos.setId(id);
        datos.setNombreCompleto("Juan Pérez");
        datos.setNif("12345678A");

        when(datosFiscalesService.obtenerPorId(id, usuarioId))
                .thenReturn(datos);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"));
    }

    @Test
    void obtenerDatosFiscalesPorId_NotFound() throws Exception {
        // Arrange
        Long id = 999L;
        Long usuarioId = 1L;
        when(datosFiscalesService.obtenerPorId(id, usuarioId))
                .thenThrow(new IllegalArgumentException("Dirección no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerDatosFiscalesPorId_Forbidden() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        when(datosFiscalesService.obtenerPorId(id, usuarioId))
                .thenThrow(new SecurityException("No tienes permiso"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearDatosFiscales_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        DatosFiscalesDTO datosFiscales = new DatosFiscalesDTO();
        datosFiscales.setNombreCompleto("Juan Pérez");
        datosFiscales.setNif("12345678A");
        datosFiscales.setDireccion("Calle Principal 123");
        datosFiscales.setCiudad("Madrid");
        datosFiscales.setCodigoPostal("28001");
        datosFiscales.setPais("España");

        DatosFiscalesDTO datosFiscalesCreado = new DatosFiscalesDTO();
        datosFiscalesCreado.setId(1L);
        datosFiscalesCreado.setNombreCompleto("Juan Pérez");
        datosFiscalesCreado.setNif("12345678A");

        when(datosFiscalesService.crearDatosFiscales(eq(usuarioId), any(DatosFiscalesDTO.class)))
                .thenReturn(datosFiscalesCreado);

        // Act & Assert
        mockMvc.perform(post("/api/perfil/{usuarioId}/datos-fiscales", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"));
    }

    @Test
    void crearDatosFiscales_ValidationError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        DatosFiscalesDTO datosFiscales = new DatosFiscalesDTO();
        datosFiscales.setNif("INVALID");

        when(datosFiscalesService.crearDatosFiscales(eq(usuarioId), any(DatosFiscalesDTO.class)))
                .thenThrow(new IllegalArgumentException("NIF inválido"));

        // Act & Assert
        mockMvc.perform(post("/api/perfil/{usuarioId}/datos-fiscales", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void actualizarDatosFiscales_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscalesDTO datosFiscales = new DatosFiscalesDTO();
        datosFiscales.setNombreCompleto("Juan Pérez Actualizado");
        datosFiscales.setNif("12345678A");
        datosFiscales.setDireccion("Calle Nueva");
        datosFiscales.setCiudad("Madrid");
        datosFiscales.setCodigoPostal("28001");
        datosFiscales.setPais("España");

        DatosFiscalesDTO datosFiscalesActualizado = new DatosFiscalesDTO();
        datosFiscalesActualizado.setId(id);
        datosFiscalesActualizado.setNombreCompleto("Juan Pérez Actualizado");

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), eq(usuarioId), any(DatosFiscalesDTO.class)))
                .thenReturn(datosFiscalesActualizado);

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez Actualizado"));
    }

    @Test
    void actualizarDatosFiscales_ValidationError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscalesDTO datosFiscales = new DatosFiscalesDTO();
        datosFiscales.setNif("12345678A");

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), eq(usuarioId), any(DatosFiscalesDTO.class)))
                .thenThrow(new IllegalArgumentException("Datos no encontrados"));

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void actualizarDatosFiscales_SecurityError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscalesDTO datosFiscales = new DatosFiscalesDTO();
        datosFiscales.setNif("12345678A");

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), eq(usuarioId), any(DatosFiscalesDTO.class)))
                .thenThrow(new SecurityException("No tienes permiso"));

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void eliminarDatosFiscales_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        doNothing().when(datosFiscalesService).eliminarDatosFiscales(id, usuarioId);

        // Act & Assert
        mockMvc.perform(delete("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarDatosFiscales_NotFound() throws Exception {
        // Arrange
        Long id = 999L;
        Long usuarioId = 1L;
        doThrow(new IllegalArgumentException("Dirección no encontrada"))
                .when(datosFiscalesService).eliminarDatosFiscales(id, usuarioId);

        // Act & Assert
        mockMvc.perform(delete("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void eliminarDatosFiscales_SecurityError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        doThrow(new SecurityException("No tienes permiso"))
                .when(datosFiscalesService).eliminarDatosFiscales(id, usuarioId);

        // Act & Assert
        mockMvc.perform(delete("/api/perfil/{usuarioId}/datos-fiscales/{id}", usuarioId, id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void validarNIF_Valido() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "12345678A");
        when(datosFiscalesService.validarNIF("12345678A")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/perfil/{usuarioId}/datos-fiscales/validar-nif", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    void validarNIF_Invalido() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "INVALID");
        when(datosFiscalesService.validarNIF("INVALID")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/perfil/{usuarioId}/datos-fiscales/validar-nif", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }
}
