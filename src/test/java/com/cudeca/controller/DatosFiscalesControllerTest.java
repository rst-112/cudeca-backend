package com.cudeca.controller;

import com.cudeca.config.JwtAuthFilter;
import com.cudeca.config.SecurityConfig;
import com.cudeca.model.usuario.DatosFiscales;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
        DatosFiscales datos1 = new DatosFiscales();
        datos1.setId(1L);
        datos1.setNombreCompleto("Juan Pérez");
        datos1.setNif("12345678A");

        DatosFiscales datos2 = new DatosFiscales();
        datos2.setId(2L);
        datos2.setNombreCompleto("María García");
        datos2.setNif("87654321B");

        List<DatosFiscales> listaExpected = Arrays.asList(datos1, datos2);
        when(datosFiscalesService.obtenerDatosFiscalesPorUsuario(usuarioId)).thenReturn(listaExpected);

        // Act & Assert
        mockMvc.perform(get("/api/datos-fiscales/usuario/{usuarioId}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].nombreCompleto").value("María García"));
    }

    @Test
    void obtenerDatosFiscalesPorUsuario_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(datosFiscalesService.obtenerDatosFiscalesPorUsuario(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/datos-fiscales/usuario/{usuarioId}", usuarioId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerDatosFiscalesPorId_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscales datos = new DatosFiscales();
        datos.setId(id);
        datos.setNombreCompleto("Juan Pérez");
        datos.setNif("12345678A");

        when(datosFiscalesService.obtenerDatosFiscalesPorId(id, usuarioId))
                .thenReturn(Optional.of(datos));

        // Act & Assert
        mockMvc.perform(get("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"));
    }

    @Test
    void obtenerDatosFiscalesPorId_NotFound() throws Exception {
        // Arrange
        Long id = 999L;
        Long usuarioId = 1L;
        when(datosFiscalesService.obtenerDatosFiscalesPorId(id, usuarioId))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerDatosFiscalesPorId_InternalError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        when(datosFiscalesService.obtenerDatosFiscalesPorId(id, usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void crearDatosFiscales_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        DatosFiscales datosFiscales = new DatosFiscales();
        datosFiscales.setNombreCompleto("Juan Pérez");
        datosFiscales.setNif("12345678A");
        datosFiscales.setDireccion("Calle Principal 123");

        DatosFiscales datosFiscalesCreado = new DatosFiscales();
        datosFiscalesCreado.setId(1L);
        datosFiscalesCreado.setNombreCompleto("Juan Pérez");
        datosFiscalesCreado.setNif("12345678A");

        when(datosFiscalesService.crearDatosFiscales(any(DatosFiscales.class), eq(usuarioId)))
                .thenReturn(datosFiscalesCreado);

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/usuario/{usuarioId}", usuarioId)
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
        DatosFiscales datosFiscales = new DatosFiscales();
        datosFiscales.setNif("INVALID");

        when(datosFiscalesService.crearDatosFiscales(any(DatosFiscales.class), eq(usuarioId)))
                .thenThrow(new IllegalArgumentException("NIF inválido"));

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/usuario/{usuarioId}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NIF inválido"));
    }

    @Test
    void crearDatosFiscales_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        DatosFiscales datosFiscales = new DatosFiscales();
        datosFiscales.setNombreCompleto("Juan Pérez");

        when(datosFiscalesService.crearDatosFiscales(any(DatosFiscales.class), eq(usuarioId)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/usuario/{usuarioId}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno creando datos fiscales"));
    }

    @Test
    void actualizarDatosFiscales_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscales datosFiscales = new DatosFiscales();
        datosFiscales.setNombreCompleto("Juan Pérez Actualizado");
        datosFiscales.setNif("12345678A");

        DatosFiscales datosFiscalesActualizado = new DatosFiscales();
        datosFiscalesActualizado.setId(id);
        datosFiscalesActualizado.setNombreCompleto("Juan Pérez Actualizado");

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), any(DatosFiscales.class), eq(usuarioId)))
                .thenReturn(datosFiscalesActualizado);

        // Act & Assert
        mockMvc.perform(put("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString())
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
        DatosFiscales datosFiscales = new DatosFiscales();

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), any(DatosFiscales.class), eq(usuarioId)))
                .thenThrow(new IllegalArgumentException("Datos no encontrados"));

        // Act & Assert
        mockMvc.perform(put("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos no encontrados"));
    }

    @Test
    void actualizarDatosFiscales_InternalError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        DatosFiscales datosFiscales = new DatosFiscales();

        when(datosFiscalesService.actualizarDatosFiscales(eq(id), any(DatosFiscales.class), eq(usuarioId)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(put("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosFiscales)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno actualizando datos fiscales"));
    }

    @Test
    void eliminarDatosFiscales_Success() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        when(datosFiscalesService.eliminarDatosFiscales(id, usuarioId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Datos fiscales eliminados exitosamente"));
    }

    @Test
    void eliminarDatosFiscales_NotFound() throws Exception {
        // Arrange
        Long id = 999L;
        Long usuarioId = 1L;
        when(datosFiscalesService.eliminarDatosFiscales(id, usuarioId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Datos fiscales no encontrados o no pertenecen al usuario"));
    }

    @Test
    void eliminarDatosFiscales_InternalError() throws Exception {
        // Arrange
        Long id = 1L;
        Long usuarioId = 1L;
        when(datosFiscalesService.eliminarDatosFiscales(id, usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(delete("/api/datos-fiscales/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error interno eliminando datos fiscales"));
    }

    @Test
    void validarNIF_Valido() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "12345678A");
        when(datosFiscalesService.validarNIF("12345678A")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/validar-nif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.mensaje").value("NIF válido"));
    }

    @Test
    void validarNIF_Invalido() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "INVALID");
        when(datosFiscalesService.validarNIF("INVALID")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/validar-nif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("NIF inválido"));
    }

    @Test
    void validarNIF_NifNull() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/validar-nif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("NIF no proporcionado"));
    }

    @Test
    void validarNIF_NifBlank() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "   ");

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/validar-nif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("NIF no proporcionado"));
    }

    @Test
    void validarNIF_InternalError() throws Exception {
        // Arrange
        Map<String, String> payload = new HashMap<>();
        payload.put("nif", "12345678A");
        when(datosFiscalesService.validarNIF("12345678A"))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        mockMvc.perform(post("/api/datos-fiscales/validar-nif")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("Error validando NIF"));
    }
}
