package com.cudeca.service.impl;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.model.usuario.Rol;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.RolRepository;
import com.cudeca.repository.UsuarioRepository;
import com.cudeca.service.JwtService;
import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Usuario mockUsuario;
    private final String TEST_TOKEN = "token.test";
    private final String TEST_EMAIL = "test@user.com";

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Test", TEST_EMAIL, "Pass123!");
        loginRequest = new LoginRequest(TEST_EMAIL, "Pass123!");

        // Crear mock de Rol
        Rol mockRol = new Rol();
        mockRol.setId(1L);
        mockRol.setNombre("COMPRADOR");

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setEmail(TEST_EMAIL);

        // Configurar el mock de rolRepository para que devuelva el rol por defecto
        when(rolRepository.findByNombreIgnoreCase("COMPRADOR")).thenReturn(Optional.of(mockRol));
    }

    @Test
    @DisplayName("Registro exitoso")
    void testRegisterSuccess() {
        // Arrange
        // Eliminada la línea conflictiva de findByEmail.
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // Act
        AuthResponse res = authService.register(registerRequest);

        // Assert
        assertNotNull(res);
        assertEquals(TEST_TOKEN, res.getToken());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro falla si email existe")
    void testRegisterFailureDuplicateEmail() {
        // Arrange
        // Aquí SÍ necesitamos que devuelva true, porque queremos provocar el error
        when(usuarioRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));

        // Verify
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Login exitoso")
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUsuario));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        AuthResponse res = authService.login(loginRequest);

        assertEquals(TEST_TOKEN, res.getToken());
    }

    @Test
    @DisplayName("Login falla con credenciales malas")
    void testLoginFailureBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad creds"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Registro con contraseña que requiere codificación")
    void testRegisterPasswordEncoding() {
        // Arrange
        String rawPassword = "Pass123!";
        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("Login usuario no encontrado")
    void testLoginUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Registro guarda usuario con datos correctos")
    void testRegisterSavesCorrectUserData() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // Capturamos el usuario que se guarda
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario savedUser = invocation.getArgument(0);
            assertEquals("Test", savedUser.getNombre());
            assertEquals(TEST_EMAIL, savedUser.getEmail());
            assertEquals("hashedPass", savedUser.getPassword());
            return mockUsuario;
        });

        // Act
        authService.register(registerRequest);

        // Assert
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Login genera token con usuario correcto")
    void testLoginGeneratesTokenWithCorrectUser() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUsuario));
        when(jwtService.generateToken(mockUsuario)).thenReturn(TEST_TOKEN);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        verify(jwtService).generateToken(mockUsuario);
        assertEquals(TEST_TOKEN, response.getToken());
    }

    @Test
    @DisplayName("AuthResponse contiene token válido después de registro")
    void testRegisterReturnsValidAuthResponse() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
        assertEquals(TEST_TOKEN, response.getToken());
    }
}
