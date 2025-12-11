package com.cudeca.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtServiceImpl.
 * Valida la generación, validación y extracción de información de tokens JWT.
 */
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        // Configuramos los valores que normalmente vendrían de application.yml
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 horas

        // Creamos un usuario de prueba
        userDetails = User.builder()
                .username("test@cudeca.org")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void testGenerateToken_WithUserDetails() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token, "El token no debe ser nulo");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");
        assertEquals(3, token.split("\\.").length, "El token JWT debe tener 3 partes");
    }

    @Test
    void testGenerateToken_WithExtraClaims() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 123L);
        extraClaims.put("role", "ADMIN");

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertNotNull(token, "El token no debe ser nulo");

        // Verificamos que los claims extras estén en el token
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        assertEquals(123L, userId, "El userId debe estar en el token");
        assertEquals("ADMIN", role, "El role debe estar en el token");
    }

    @Test
    void testExtractUsername() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("test@cudeca.org", username, "El username extraído debe coincidir");
    }

    @Test
    void testIsTokenValid_WithValidToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid, "El token debe ser válido");
    }

    @Test
    void testIsTokenValid_WithDifferentUser() {
        // Given
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("otro@cudeca.org")
                .password("password")
                .roles("USER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid, "El token no debe ser válido para otro usuario");
    }

    @Test
    void testIsTokenValid_WithExpiredToken() {
        // Given - Creamos un servicio con expiración muy corta
        JwtServiceImpl shortLivedJwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(shortLivedJwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtExpiration", -1000L); // Ya expirado

        String token = shortLivedJwtService.generateToken(userDetails);

        // When & Then - Se lanza ExpiredJwtException al validar el token expirado
        try {
            boolean isValid = shortLivedJwtService.isTokenValid(token, userDetails);
            assertFalse(isValid, "El token expirado no debe ser válido");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Se espera esta excepción con tokens expirados
            assertTrue(true, "Se lanzó correctamente la ExpiredJwtException");
        }
    }

    @Test
    void testExtractClaim_ExtractionDate() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        Claims claims = jwtService.extractClaim(token, claims1 -> claims1);

        // Then
        assertNotNull(claims.getIssuedAt(), "La fecha de emisión debe estar presente");
        assertNotNull(claims.getExpiration(), "La fecha de expiración debe estar presente");
        assertEquals("test@cudeca.org", claims.getSubject(), "El subject debe ser el username");
    }

    @Test
    void testExtractClaim_CustomClaim() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customField", "customValue");
        String token = jwtService.generateToken(extraClaims, userDetails);

        // When
        String customValue = jwtService.extractClaim(token,
                claims -> claims.get("customField", String.class));

        // Then
        assertEquals("customValue", customValue, "El claim personalizado debe extraerse correctamente");
    }

    @Test
    void testTokenStructure() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // Then - Un JWT válido tiene exactamente 3 partes separadas por puntos
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Un JWT debe tener header, payload y signature");

        // Cada parte debe contener datos (no estar vacía)
        assertFalse(parts[0].isEmpty(), "El header no debe estar vacío");
        assertFalse(parts[1].isEmpty(), "El payload no debe estar vacío");
        assertFalse(parts[2].isEmpty(), "La signature no debe estar vacía");
    }

    @Test
    void testTokenExpirationTime() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("testClaim", "testValue");
        String token = jwtService.generateToken(extraClaims, userDetails);

        // When
        long issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt().getTime());
        long expiration = jwtService.extractClaim(token, claims -> claims.getExpiration().getTime());
        long duration = expiration - issuedAt;

        // Then - Debe ser aproximadamente 24 horas (86400000 ms)
        assertEquals(86400000L, duration, 1000L,
                "La duración del token debe ser aproximadamente 24 horas");
    }
}

