package com.cudeca.service.impl;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.impl.JwtServiceImpl; // Si la interfaz existe, usar esta.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Dependencias MOCK (simuladas)
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtServiceImpl jwtService; // Usamos la interfaz
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication; // Para simular la respuesta del Manager

    // Clase bajo test (Se inyectan las dependencias simuladas)
    @InjectMocks private AuthServiceImpl authService;

    // Datos de prueba
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Usuario mockUsuario;
    private final String ENCODED_PASSWORD = "hashedPassword123";
    private final String TEST_TOKEN = "test.token.generated";
    private final String TEST_EMAIL = "test@user.com";

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Test User", TEST_EMAIL, "SecurePass123");
        loginRequest = new LoginRequest(TEST_EMAIL, "SecurePass123");

        // Creamos una instancia de Usuario (usaremos 'registerRequest' como datos)
        mockUsuario = Usuario.builder()
                .id(1L)
                .nombre("Test User")
                .email(TEST_EMAIL)
                .passwordHash(ENCODED_PASSWORD) // NOTA: El método setPassword del builder/setter es 'passwordHash' en el código real.
                .build();
    }

    // =========================================================================================
    // 1. REGISTRO (register)
    // =========================================================================================

    @Test
    @DisplayName("Debe registrar un nuevo usuario y devolver un token")
    void testRegisterSuccess() {
        // 1. Configurar Mocks: Lo que deben devolver
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty()); // Email no existe
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // 2. Ejecutar
        AuthResponse response = authService.register(registerRequest);

        // 3. Verificar: Que el código hizo lo que debía
        // Verificamos que se llamó al encoder y al repositorio (persistir)
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));

        // Verificamos el resultado
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getJwtToken());
    }

    @Test
    @DisplayName("Debe fallar al registrar si el email ya existe")
    void testRegisterFailureDuplicateEmail() {
        // 1. Configurar Mocks: Devolver un usuario (simulando que ya existe)
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUsuario));

        // 2. Ejecutar y Verificar Excepción: Esperamos que lance una RuntimeException (como está en tu código)
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));

        // 3. Verificar: Que NO se intentó guardar ni codificar
        verify(usuarioRepository, times(0)).save(any(Usuario.class));
        verify(passwordEncoder, times(0)).encode(anyString());
    }

    // =========================================================================================
    // 2. LOGIN (login)
    // =========================================================================================

    @Test
    @DisplayName("Debe loguear al usuario y devolver un token si las credenciales son válidas")
    void testLoginSuccess() {
        // 1. Configurar Mocks:
        // Simular que el AuthenticationManager acepta las credenciales
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        // El Manager devuelve un principal que debe ser el usuario, pero lo recuperamos del Repo
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUsuario));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // 2. Ejecutar
        AuthResponse response = authService.login(loginRequest);

        // 3. Verificar:
        // Verificamos que se llamó al Manager y al generador de tokens
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(mockUsuario);

        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getJwtToken());
    }

    @Test
    @DisplayName("Debe fallar el login y lanzar excepción si las credenciales son incorrectas")
    void testLoginFailureBadCredentials() {
        // 1. Configurar Mocks: Simular que el Manager lanza la excepción
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        // 2. Ejecutar y Verificar Excepción: Esperamos la excepción específica de Spring Security
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        // 3. Verificar: Que NO se intentó generar el token
        verify(jwtService, never()).generateToken(any());
    }
}