package com.cudeca.controller;

import com.cudeca.config.JwtAuthFilter;
import com.cudeca.config.SecurityConfig;
import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.service.CheckoutService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CheckoutController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void procesarCheckout_Success() throws Exception {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUsuarioId(1L);
        request.setEmailContacto("test@example.com");

        CheckoutResponse response = new CheckoutResponse();
        response.setCompraId(100L);
        response.setEstado("PENDIENTE");
        response.setTotal(BigDecimal.valueOf(50.00));
        response.setUrlPasarela("https://pasarela.com/pagar/100");
        response.setMensaje("Compra creada exitosamente");

        when(checkoutService.procesarCheckout(any(CheckoutRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compraId").value(100L))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(50.00))
                .andExpect(jsonPath("$.urlPasarela").value("https://pasarela.com/pagar/100"));
    }

    @Test
    void procesarCheckout_ValidationError() throws Exception {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUsuarioId(1L);

        when(checkoutService.procesarCheckout(any(CheckoutRequest.class)))
                .thenThrow(new IllegalArgumentException("Email de contacto es requerido"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Email de contacto es requerido"));
    }

    @Test
    void procesarCheckout_StateError() throws Exception {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUsuarioId(1L);

        when(checkoutService.procesarCheckout(any(CheckoutRequest.class)))
                .thenThrow(new IllegalStateException("Asiento no disponible"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Asiento no disponible"));
    }

    @Test
    void procesarCheckout_InternalError() throws Exception {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setUsuarioId(1L);

        when(checkoutService.procesarCheckout(any(CheckoutRequest.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error procesando la compra"));
    }

    @Test
    void obtenerDetallesCompra_Success() throws Exception {
        // Arrange
        Long compraId = 100L;
        CheckoutResponse response = new CheckoutResponse();
        response.setCompraId(compraId);
        response.setEstado("CONFIRMADO");
        response.setTotal(BigDecimal.valueOf(75.50));

        when(checkoutService.obtenerDetallesCompra(compraId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/checkout/{compraId}", compraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compraId").value(100L))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(75.50));
    }

    @Test
    void obtenerDetallesCompra_NotFound() throws Exception {
        // Arrange
        Long compraId = 999L;
        when(checkoutService.obtenerDetallesCompra(compraId))
                .thenThrow(new IllegalArgumentException("Compra no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/checkout/{compraId}", compraId))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerDetallesCompra_InternalError() throws Exception {
        // Arrange
        Long compraId = 100L;
        when(checkoutService.obtenerDetallesCompra(compraId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/checkout/{compraId}", compraId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void confirmarPago_Success() throws Exception {
        // Arrange
        Long compraId = 100L;
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", "TXN123");

        when(checkoutService.confirmarPago(compraId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/confirmar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pago confirmado exitosamente"))
                .andExpect(jsonPath("$.compraId").value(100L));
    }

    @Test
    void confirmarPago_EstadoInvalido() throws Exception {
        // Arrange
        Long compraId = 100L;
        when(checkoutService.confirmarPago(compraId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/confirmar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No se pudo confirmar el pago. La compra no está en estado válido."));
    }

    @Test
    void confirmarPago_CompraNoEncontrada() throws Exception {
        // Arrange
        Long compraId = 999L;
        when(checkoutService.confirmarPago(compraId))
                .thenThrow(new IllegalArgumentException("Compra no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/confirmar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmarPago_InternalError() throws Exception {
        // Arrange
        Long compraId = 100L;
        when(checkoutService.confirmarPago(compraId))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/confirmar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error interno confirmando el pago"));
    }

    @Test
    void cancelarCompra_Success() throws Exception {
        // Arrange
        Long compraId = 100L;
        Map<String, String> payload = new HashMap<>();
        payload.put("motivo", "Usuario canceló voluntariamente");

        when(checkoutService.cancelarCompra(eq(compraId), any(String.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/cancelar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Compra cancelada exitosamente"))
                .andExpect(jsonPath("$.compraId").value(100L));
    }

    @Test
    void cancelarCompra_EstadoInvalido() throws Exception {
        // Arrange
        Long compraId = 100L;
        Map<String, String> payload = new HashMap<>();
        payload.put("motivo", "Test");

        when(checkoutService.cancelarCompra(eq(compraId), any(String.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/cancelar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No se pudo cancelar la compra. Verifica su estado."));
    }

    @Test
    void cancelarCompra_SinMotivo() throws Exception {
        // Arrange
        Long compraId = 100L;
        Map<String, String> payload = new HashMap<>();

        when(checkoutService.cancelarCompra(eq(compraId), eq("Sin motivo especificado"))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/cancelar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelarCompra_CompraNoEncontrada() throws Exception {
        // Arrange
        Long compraId = 999L;
        Map<String, String> payload = new HashMap<>();
        payload.put("motivo", "Test");

        when(checkoutService.cancelarCompra(eq(compraId), any(String.class)))
                .thenThrow(new IllegalArgumentException("Compra no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/cancelar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarCompra_InternalError() throws Exception {
        // Arrange
        Long compraId = 100L;
        Map<String, String> payload = new HashMap<>();
        payload.put("motivo", "Test");

        when(checkoutService.cancelarCompra(eq(compraId), any(String.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(post("/api/checkout/{compraId}/cancelar", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error interno cancelando la compra"));
    }
}
