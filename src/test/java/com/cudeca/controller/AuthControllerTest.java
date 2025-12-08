package com.cudeca.controller;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.service.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAuthService authService;

    @Test
    @DisplayName("POST /register - Debe devolver 200 OK y un token para un registro válido")
    void register_WhenRequestIsValid_ShouldReturnOkAndToken() throws Exception {
        // Arrange
        RegisterRequest validRequest = new RegisterRequest(
                "Test User",
                "test@example.com",
                "Password123!"
        );
        AuthResponse authResponse = new AuthResponse("fake-jwt-token");

        // Simulamos el comportamiento del servicio
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    @DisplayName("POST /register - Debe devolver 400 Bad Request para un email inválido")
    void register_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "Test User",
                "not-an-email", // Email inválido
                "Password123!"
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.email").value("El formato del email no es válido."));
    }

    @Test
    @DisplayName("POST /register - Debe devolver 400 Bad Request para una contraseña corta")
    void register_WhenPasswordIsShort_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "Test User",
                "test@example.com",
                "short" // Contraseña inválida
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.size").exists()) // O un mensaje más específico si lo tienes
                .andExpect(jsonPath("$.errors.pattern").exists());
    }
}
