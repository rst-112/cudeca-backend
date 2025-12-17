package com.cudeca.controller;

import com.cudeca.config.JwtAuthFilter;
import com.cudeca.config.SecurityConfig;
import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.EntradaEmitida;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.MovimientoMonedero;
import com.cudeca.service.PerfilUsuarioService;
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

import java.math.BigDecimal;
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
    controllers = PerfilUsuarioController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class PerfilUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PerfilUsuarioService perfilUsuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void obtenerPerfil_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        UserProfileDTO perfil = new UserProfileDTO();
        perfil.setId(usuarioId);
        perfil.setNombre("Juan Pérez");
        perfil.setEmail("juan@example.com");

        when(perfilUsuarioService.obtenerPerfilPorId(usuarioId)).thenReturn(perfil);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    void obtenerPerfil_NotFound() throws Exception {
        // Arrange
        Long usuarioId = 999L;
        when(perfilUsuarioService.obtenerPerfilPorId(usuarioId))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}", usuarioId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void obtenerPerfil_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.obtenerPerfilPorId(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}", usuarioId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno obteniendo perfil"));
    }

    @Test
    void obtenerPerfilPorEmail_Success() throws Exception {
        // Arrange
        String email = "juan@example.com";
        UserProfileDTO perfil = new UserProfileDTO();
        perfil.setId(1L);
        perfil.setEmail(email);
        perfil.setNombre("Juan Pérez");

        when(perfilUsuarioService.obtenerPerfilPorEmail(email)).thenReturn(Optional.of(perfil));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void obtenerPerfilPorEmail_NotFound() throws Exception {
        // Arrange
        String email = "noexiste@example.com";
        when(perfilUsuarioService.obtenerPerfilPorEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/perfil/email/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPerfilPorEmail_InternalError() throws Exception {
        // Arrange
        String email = "juan@example.com";
        when(perfilUsuarioService.obtenerPerfilPorEmail(email))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/email/{email}", email))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno obteniendo perfil"));
    }

    @Test
    void actualizarPerfil_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "Juan Pérez Actualizado");
        payload.put("direccion", "Nueva Calle 456");

        UserProfileDTO perfilActualizado = new UserProfileDTO();
        perfilActualizado.setId(usuarioId);
        perfilActualizado.setNombre("Juan Pérez Actualizado");

        when(perfilUsuarioService.actualizarPerfil(eq(usuarioId), eq("Juan Pérez Actualizado"), eq("Nueva Calle 456")))
                .thenReturn(perfilActualizado);

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"));
    }

    @Test
    void actualizarPerfil_ValidationError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "");

        when(perfilUsuarioService.actualizarPerfil(eq(usuarioId), any(), any()))
                .thenThrow(new IllegalArgumentException("Nombre no puede estar vacío"));

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nombre no puede estar vacío"));
    }

    @Test
    void actualizarPerfil_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "Juan");

        when(perfilUsuarioService.actualizarPerfil(eq(usuarioId), any(), any()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(put("/api/perfil/{usuarioId}", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno actualizando perfil"));
    }

    @Test
    void obtenerEntradas_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        com.cudeca.dto.EntradaUsuarioDTO entrada1 = new com.cudeca.dto.EntradaUsuarioDTO();
        entrada1.setId(1L);

        com.cudeca.dto.EntradaUsuarioDTO entrada2 = new com.cudeca.dto.EntradaUsuarioDTO();
        entrada2.setId(2L);

        List<com.cudeca.dto.EntradaUsuarioDTO> entradas = Arrays.asList(entrada1, entrada2);
        when(perfilUsuarioService.obtenerEntradasUsuario(usuarioId)).thenReturn(entradas);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void obtenerEntradas_UsuarioNotFound() throws Exception {
        // Arrange
        Long usuarioId = 999L;
        when(perfilUsuarioService.obtenerEntradasUsuario(usuarioId))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas", usuarioId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void obtenerEntradas_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.obtenerEntradasUsuario(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas", usuarioId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno obteniendo entradas"));
    }

    @Test
    void descargarPDFEntrada_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Long entradaId = 1L;
        byte[] pdfBytes = "PDF content".getBytes();

        when(perfilUsuarioService.generarPDFEntrada(entradaId, usuarioId)).thenReturn(pdfBytes);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas/{entradaId}/pdf", usuarioId, entradaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"entrada_1.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void descargarPDFEntrada_EntradaNotFound() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Long entradaId = 999L;
        when(perfilUsuarioService.generarPDFEntrada(entradaId, usuarioId))
                .thenThrow(new IllegalArgumentException("Entrada no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas/{entradaId}/pdf", usuarioId, entradaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entrada no encontrada"));
    }

    @Test
    void descargarPDFEntrada_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Long entradaId = 1L;
        when(perfilUsuarioService.generarPDFEntrada(entradaId, usuarioId))
                .thenThrow(new RuntimeException("Error generando PDF"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/entradas/{entradaId}/pdf", usuarioId, entradaId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno generando PDF"));
    }

    @Test
    void obtenerMonedero_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        Monedero monedero = new Monedero();
        monedero.setId(1L);
        monedero.setSaldo(BigDecimal.valueOf(100.00));

        when(perfilUsuarioService.obtenerMonedero(usuarioId)).thenReturn(monedero);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.saldo").value(100.00));
    }

    @Test
    void obtenerMonedero_UsuarioNotFound() throws Exception {
        // Arrange
        Long usuarioId = 999L;
        when(perfilUsuarioService.obtenerMonedero(usuarioId))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero", usuarioId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void obtenerMonedero_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.obtenerMonedero(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero", usuarioId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno obteniendo monedero"));
    }

    @Test
    void obtenerMovimientosMonedero_Success() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        MovimientoMonedero mov1 = new MovimientoMonedero();
        mov1.setId(1L);
        mov1.setImporte(BigDecimal.valueOf(50.00));

        MovimientoMonedero mov2 = new MovimientoMonedero();
        mov2.setId(2L);
        mov2.setImporte(BigDecimal.valueOf(20.00));

        List<MovimientoMonedero> movimientos = Arrays.asList(mov1, mov2);
        when(perfilUsuarioService.obtenerMovimientosMonedero(usuarioId)).thenReturn(movimientos);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero/movimientos", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].importe").value(50.00))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].importe").value(20.00));
    }

    @Test
    void obtenerMovimientosMonedero_UsuarioNotFound() throws Exception {
        // Arrange
        Long usuarioId = 999L;
        when(perfilUsuarioService.obtenerMovimientosMonedero(usuarioId))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero/movimientos", usuarioId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void obtenerMovimientosMonedero_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.obtenerMovimientosMonedero(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/monedero/movimientos", usuarioId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno obteniendo movimientos"));
    }

    @Test
    void verificarExistencia_Existe() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.existeUsuario(usuarioId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/existe", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(true));
    }

    @Test
    void verificarExistencia_NoExiste() throws Exception {
        // Arrange
        Long usuarioId = 999L;
        when(perfilUsuarioService.existeUsuario(usuarioId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/existe", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(false));
    }

    @Test
    void verificarExistencia_InternalError() throws Exception {
        // Arrange
        Long usuarioId = 1L;
        when(perfilUsuarioService.existeUsuario(usuarioId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/perfil/{usuarioId}/existe", usuarioId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.existe").value(false));
    }
}
