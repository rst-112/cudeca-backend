package com.cudeca.controller;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.dto.usuario.UserResponse;
import com.cudeca.service.AuthService;
import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Test", "test@mail.com", "Pass123!");
        loginRequest = new LoginRequest("test@mail.com", "Pass123!");

        authResponse = AuthResponse.builder()
                .token("fake-jwt-token")
                .user(UserResponse.builder()
                        .id(1L)
                        .nombre("Test User")
                        .email("test@mail.com")
                        .rol("COMPRADOR")
                        .build())
                .build();
    }

    @Test
    @DisplayName("POST /register - 200 OK")
    void registerSuccess() {
        when(authService.register(any())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fake-jwt-token", response.getBody().getToken());
    }

    @Test
    @DisplayName("POST /register - Lanza excepción si email existe")
    void registerConflict() {
        // Simulamos que el servicio lanza la excepción
        when(authService.register(any())).thenThrow(new EmailAlreadyExistsException("Email existe"));

        // Verificamos que el controlador la propaga
        assertThrows(EmailAlreadyExistsException.class, () ->
                authController.register(registerRequest)
        );
    }

    @Test
    @DisplayName("POST /login - 200 OK")
    void loginSuccess() {
        when(authService.login(any())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST /login - Lanza excepción si credenciales mal")
    void loginFailure() {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad creds"));

        assertThrows(BadCredentialsException.class, () ->
                authController.login(loginRequest)
        );
    }

    @Test
    @DisplayName("POST /register - Verifica que el token no esté vacío")
    void registerReturnsNonEmptyToken() {
        when(authService.register(any())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertNotNull(response.getBody());
        assertFalse(response.getBody().getToken().isEmpty());
    }

    @Test
    @DisplayName("POST /login - Verifica que el token no esté vacío")
    void loginReturnsNonEmptyToken() {
        when(authService.login(any())).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertNotNull(response.getBody());
        assertFalse(response.getBody().getToken().isEmpty());
    }

    @Test
    @DisplayName("GET /validate - 200 OK con token válido")
    void validateTokenSuccess() {
        // Crear un objeto Authentication mockeado
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@mail.com", null, null
        );

        ResponseEntity<Map<String, Boolean>> response = authController.validateToken(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("valid"));
    }

    @Test
    @DisplayName("GET /validate - Retorna false si authentication es null")
    void validateTokenWithNullAuthentication() {
        ResponseEntity<Map<String, Boolean>> response = authController.validateToken(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("valid"));
    }
}
