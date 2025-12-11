package com.cudeca.service.impl;

import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserServiceImpl.
 * Valida la carga de usuarios desde la base de datos para Spring Security.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .email("test@cudeca.org")
                .passwordHash("encodedPassword123")
                .nombre("Test Usuario")
                .build();
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Given
        when(usuarioRepository.findByEmail("test@cudeca.org"))
                .thenReturn(Optional.of(usuarioMock));

        // When
        UserDetails userDetails = userService.loadUserByUsername("test@cudeca.org");

        // Then
        assertNotNull(userDetails, "UserDetails no debe ser nulo");
        assertEquals("test@cudeca.org", userDetails.getUsername(),
                "El username debe coincidir con el email");
        assertEquals("encodedPassword123", userDetails.getPassword(),
                "La contraseña debe coincidir");

        // Verificamos que se llamó al repositorio
        verify(usuarioRepository, times(1)).findByEmail("test@cudeca.org");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Given
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("noexiste@cudeca.org"),
                "Debe lanzar UsernameNotFoundException cuando el usuario no existe"
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"),
                "El mensaje de error debe indicar que el usuario no fue encontrado");
        assertTrue(exception.getMessage().contains("noexiste@cudeca.org"),
                "El mensaje debe incluir el email buscado");

        verify(usuarioRepository, times(1)).findByEmail("noexiste@cudeca.org");
    }

    @Test
    void testLoadUserByUsername_WithNullEmail() {
        // Given
        when(usuarioRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(null),
                "Debe lanzar excepción cuando el email es null"
        );

        verify(usuarioRepository, times(1)).findByEmail(null);
    }

    @Test
    void testLoadUserByUsername_WithEmptyEmail() {
        // Given
        when(usuarioRepository.findByEmail(""))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(""),
                "Debe lanzar excepción cuando el email está vacío"
        );

        verify(usuarioRepository, times(1)).findByEmail("");
    }

    @Test
    void testLoadUserByUsername_VerifyUserDetailsProperties() {
        // Given
        when(usuarioRepository.findByEmail("test@cudeca.org"))
                .thenReturn(Optional.of(usuarioMock));

        // When
        UserDetails userDetails = userService.loadUserByUsername("test@cudeca.org");

        // Then - Verificamos que UserDetails contiene la información correcta
        assertAll("UserDetails properties",
                () -> assertEquals("test@cudeca.org", userDetails.getUsername()),
                () -> assertEquals("encodedPassword123", userDetails.getPassword()),
                () -> assertNotNull(userDetails.getAuthorities(),
                        "Las autoridades no deben ser null"),
                () -> assertTrue(userDetails.isEnabled(),
                        "El usuario activo debe estar habilitado"),
                () -> assertTrue(userDetails.isAccountNonExpired(),
                        "La cuenta no debe estar expirada"),
                () -> assertTrue(userDetails.isAccountNonLocked(),
                        "La cuenta no debe estar bloqueada"),
                () -> assertTrue(userDetails.isCredentialsNonExpired(),
                        "Las credenciales no deben estar expiradas")
        );
    }

    @Test
    void testLoadUserByUsername_MultipleCallsSameEmail() {
        // Given
        when(usuarioRepository.findByEmail("test@cudeca.org"))
                .thenReturn(Optional.of(usuarioMock));

        // When - Llamamos múltiples veces
        UserDetails userDetails1 = userService.loadUserByUsername("test@cudeca.org");
        UserDetails userDetails2 = userService.loadUserByUsername("test@cudeca.org");

        // Then
        assertNotNull(userDetails1);
        assertNotNull(userDetails2);
        assertEquals(userDetails1.getUsername(), userDetails2.getUsername());

        // Verificamos que se llamó al repositorio dos veces
        verify(usuarioRepository, times(2)).findByEmail("test@cudeca.org");
    }

    @Test
    void testLoadUserByUsername_CaseSensitive() {
        // Given
        Usuario upperCaseUser = Usuario.builder()
                .email("TEST@CUDECA.ORG")
                .passwordHash("password")
                .nombre("Test")
                .build();

        when(usuarioRepository.findByEmail("TEST@CUDECA.ORG"))
                .thenReturn(Optional.of(upperCaseUser));
        when(usuarioRepository.findByEmail("test@cudeca.org"))
                .thenReturn(Optional.of(usuarioMock));

        // When
        UserDetails upperCase = userService.loadUserByUsername("TEST@CUDECA.ORG");
        UserDetails lowerCase = userService.loadUserByUsername("test@cudeca.org");

        // Then
        assertNotEquals(upperCase.getUsername(), lowerCase.getUsername(),
                "Los emails con diferentes mayúsculas deben tratarse como diferentes");
    }
}

