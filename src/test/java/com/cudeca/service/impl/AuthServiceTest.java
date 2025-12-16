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
    @Mock
    private com.cudeca.repository.MonederoRepository monederoRepository;
    @Mock
    private com.cudeca.repository.InvitadoRepository invitadoRepository;
    @Mock
    private com.cudeca.repository.CompraRepository compraRepository;
    @Mock
    private com.cudeca.repository.VerificacionCuentaRepository verificacionRepository;
    @Mock
    private com.cudeca.service.EmailService emailService;

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

    @Test
    @DisplayName("Debe migrar datos de invitado al registrar usuario con el mismo email")
    void testMigrarDatosDeInvitado_ConCompras() {
        // Arrange
        com.cudeca.model.usuario.Invitado invitado = new com.cudeca.model.usuario.Invitado();
        invitado.setId(1L);
        invitado.setEmail(TEST_EMAIL);

        com.cudeca.model.negocio.Compra compra1 = new com.cudeca.model.negocio.Compra();
        compra1.setId(1L);
        compra1.setInvitado(invitado);

        com.cudeca.model.negocio.Compra compra2 = new com.cudeca.model.negocio.Compra();
        compra2.setId(2L);
        compra2.setInvitado(invitado);

        invitado.setCompras(new java.util.ArrayList<>(java.util.List.of(compra1, compra2)));

        when(usuarioRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);
        when(invitadoRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(invitado));

        // Act
        authService.register(registerRequest);

        // Assert
        verify(invitadoRepository).findByEmail(TEST_EMAIL);
        verify(compraRepository).saveAll(anyList());
        verify(invitadoRepository).delete(invitado);
    }

    @Test
    @DisplayName("Debe manejar registro cuando no hay invitado previo")
    void testMigrarDatosDeInvitado_SinInvitado() {
        // Arrange
        when(usuarioRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);
        when(invitadoRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act
        authService.register(registerRequest);

        // Assert
        verify(invitadoRepository).findByEmail(TEST_EMAIL);
        verify(invitadoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe migrar datos de invitado sin compras")
    void testMigrarDatosDeInvitado_SinCompras() {
        // Arrange
        com.cudeca.model.usuario.Invitado invitado = new com.cudeca.model.usuario.Invitado();
        invitado.setId(1L);
        invitado.setEmail(TEST_EMAIL);
        invitado.setCompras(new java.util.ArrayList<>());

        when(usuarioRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(TEST_TOKEN);
        when(invitadoRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(invitado));

        // Act
        authService.register(registerRequest);

        // Assert
        verify(invitadoRepository).findByEmail(TEST_EMAIL);
        verify(invitadoRepository).delete(invitado);
    }

    @Test
    @DisplayName("Debe solicitar recuperación de password exitosamente")
    void testSolicitarRecuperacionPassword_Exitoso() {
        // Arrange
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUsuario));

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);
        com.cudeca.service.EmailService emailService = mock(com.cudeca.service.EmailService.class);

        // Usar reflection para inyectar los mocks
        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);

            java.lang.reflect.Field emailField = AuthServiceImpl.class.getDeclaredField("emailService");
            emailField.setAccessible(true);
            emailField.set(authService, emailService);
        } catch (Exception e) {
            fail("Error al inyectar mocks: " + e.getMessage());
        }

        // Act
        authService.solicitarRecuperacionPassword(TEST_EMAIL);

        // Assert
        verify(usuarioRepository).findByEmail(TEST_EMAIL);
        verify(verificacionRepository).anularTokensPrevios(mockUsuario.getId());
        verify(verificacionRepository).save(any(com.cudeca.model.usuario.VerificacionCuenta.class));
        verify(emailService).enviarCorreoHtml(eq(TEST_EMAIL), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe no hacer nada si el usuario no existe al solicitar recuperación")
    void testSolicitarRecuperacionPassword_UsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);
        com.cudeca.service.EmailService emailService = mock(com.cudeca.service.EmailService.class);

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);

            java.lang.reflect.Field emailField = AuthServiceImpl.class.getDeclaredField("emailService");
            emailField.setAccessible(true);
            emailField.set(authService, emailService);
        } catch (Exception e) {
            fail("Error al inyectar mocks: " + e.getMessage());
        }

        // Act
        authService.solicitarRecuperacionPassword("noexiste@test.com");

        // Assert
        verify(usuarioRepository).findByEmail("noexiste@test.com");
        verify(verificacionRepository, never()).save(any());
        verify(emailService, never()).enviarCorreoHtml(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe restablecer password exitosamente")
    void testRestablecerPassword_Exitoso() {
        // Arrange
        String token = "valid-token-123";
        String nuevaPassword = "NewPass123!";

        com.cudeca.model.usuario.VerificacionCuenta verificacion = 
            com.cudeca.model.usuario.VerificacionCuenta.builder()
                .id(1L)
                .token(token)
                .usuario(mockUsuario)
                .usado(false)
                .expiraEn(java.time.OffsetDateTime.now().plusHours(1))
                .build();

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);

        when(verificacionRepository.findByToken(token)).thenReturn(Optional.of(verificacion));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUsuario);
        when(passwordEncoder.encode(nuevaPassword)).thenReturn("encoded-new-pass");

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Act
        authService.restablecerPassword(token, nuevaPassword);

        // Assert
        verify(verificacionRepository).findByToken(token);
        verify(usuarioRepository).save(mockUsuario);
        verify(verificacionRepository).save(verificacion);
        assertTrue(verificacion.isUsado());
    }

    @Test
    @DisplayName("Debe lanzar excepción si token no es válido")
    void testRestablecerPassword_TokenInvalido() {
        // Arrange
        String tokenInvalido = "invalid-token";

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);

        when(verificacionRepository.findByToken(tokenInvalido)).thenReturn(Optional.empty());

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.restablecerPassword(tokenInvalido, "NewPass123!")
        );

        verify(verificacionRepository).findByToken(tokenInvalido);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si token ya fue usado")
    void testRestablecerPassword_TokenYaUsado() {
        // Arrange
        String token = "used-token";

        com.cudeca.model.usuario.VerificacionCuenta verificacion = 
            com.cudeca.model.usuario.VerificacionCuenta.builder()
                .id(1L)
                .token(token)
                .usuario(mockUsuario)
                .usado(true)
                .expiraEn(java.time.OffsetDateTime.now().plusHours(1))
                .build();

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);

        when(verificacionRepository.findByToken(token)).thenReturn(Optional.of(verificacion));

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.restablecerPassword(token, "NewPass123!")
        );

        verify(verificacionRepository).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si token ha expirado")
    void testRestablecerPassword_TokenExpirado() {
        // Arrange
        String token = "expired-token";

        com.cudeca.model.usuario.VerificacionCuenta verificacion = 
            com.cudeca.model.usuario.VerificacionCuenta.builder()
                .id(1L)
                .token(token)
                .usuario(mockUsuario)
                .usado(false)
                .expiraEn(java.time.OffsetDateTime.now().minusHours(1))
                .build();

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);

        when(verificacionRepository.findByToken(token)).thenReturn(Optional.of(verificacion));

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.restablecerPassword(token, "NewPass123!")
        );

        verify(verificacionRepository).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si verificación no tiene usuario asociado")
    void testRestablecerPassword_SinUsuarioAsociado() {
        // Arrange
        String token = "token-sin-usuario";

        com.cudeca.model.usuario.VerificacionCuenta verificacion = 
            com.cudeca.model.usuario.VerificacionCuenta.builder()
                .id(1L)
                .token(token)
                .usuario(null)
                .usado(false)
                .expiraEn(java.time.OffsetDateTime.now().plusHours(1))
                .build();

        com.cudeca.repository.VerificacionCuentaRepository verificacionRepository = 
            mock(com.cudeca.repository.VerificacionCuentaRepository.class);

        when(verificacionRepository.findByToken(token)).thenReturn(Optional.of(verificacion));

        try {
            java.lang.reflect.Field verificacionField = AuthServiceImpl.class.getDeclaredField("verificacionRepository");
            verificacionField.setAccessible(true);
            verificacionField.set(authService, verificacionRepository);
        } catch (Exception e) {
            fail("Error al inyectar mock: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            authService.restablecerPassword(token, "NewPass123!")
        );

        verify(verificacionRepository).findByToken(token);
        verify(usuarioRepository, never()).save(any());
    }
}
