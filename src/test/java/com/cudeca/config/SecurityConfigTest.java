package com.cudeca.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private final SecurityConfig config = spy(new SecurityConfig());

    @Test
    @DisplayName("Debe lanzar IllegalStateException si buildHttpSecurity falla")
    void testSecurityFilterChain_ThrowsIllegalStateExceptionOnFailure() throws Exception {
        var mockHttp = mock(HttpSecurity.class);

        // Simulamos fallo interno del método buildHttpSecurity
        doThrow(new RuntimeException("Simulated failure"))
                .when(config)
                .buildHttpSecurity(mockHttp);

        var ex = assertThrows(IllegalStateException.class,
                () -> config.securityFilterChain(mockHttp));

        assertEquals("Error al configurar la seguridad HTTP", ex.getMessage());
        assertEquals("Simulated failure", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("Debe crear correctamente la configuración CORS")
    void testCorsConfigurationSource_ReturnsValidConfig() {
        var source = config.corsConfigurationSource();
        assertNotNull(source);

        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        var cors = source.getCorsConfiguration(request);

        assertNotNull(cors);
        assertNotNull(cors.getAllowedOrigins());
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:5173"));
        assertNotNull(cors.getAllowedMethods());
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
    }
}
