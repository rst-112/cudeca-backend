package com.cudeca.config;

import com.cudeca.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Mockeamos los servicios para que Spring pueda levantar el contexto
    // sin necesitar la lógica real de JWT o Base de Datos.
    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    // --- TEST DE RUTAS PÚBLICAS ---

    @Test
    @DisplayName("Debe permitir acceso anónimo a /api/auth/login")
    void loginEndpointShouldBePublic() throws Exception {
        // Intentamos hacer un POST (aunque el cuerpo esté vacío).
        // Si devuelve 400 (Bad Request) significa que la seguridad pasó (no es 401/403).
        // Si devolviera 401, significaría que está bloqueado.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(400)); // Esperamos 400 porque el JSON está vacío, no 401.
    }

    @Test
    @DisplayName("Debe permitir acceso anónimo a /api/auth/register")
    void registerEndpointShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(400)); // Igual que arriba, pasó el filtro de seguridad.
    }

    @Test
    @DisplayName("Debe permitir acceso a Swagger UI")
    void swaggerShouldBePublic() throws Exception {
        // Swagger suele redirigir, así que 3xx o 200 es éxito de seguridad.
        // Lo importante es que NO sea 401.
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe permitir acceso al Health Check público")
    void publicApiShouldBeAccessible() throws Exception {
        // Basado en tu HealthCheckController
        mockMvc.perform(get("/api/public/test"))
                .andExpect(status().isOk());
    }

    // --- TEST DE RUTAS PRIVADAS ---

    @Test
    @DisplayName("Debe bloquear acceso anónimo a rutas protegidas")
    void protectedEndpointShouldBeBlockedForAnonymous() throws Exception {
        // Intentamos acceder a una ruta que no está en la lista de permitAll
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isForbidden()); // Debería dar 403 Forbidden o 401 Unauthorized
    }

    @Test
    @WithMockUser // Simula un usuario autenticado para este test
    @DisplayName("Debe permitir acceso autenticado a rutas protegidas")
    void protectedEndpointShouldBeAllowedForAuthenticatedUser() throws Exception {
        // Como usamos @WithMockUser, Spring Security cree que ya pasamos el filtro JWT.
        // Si el endpoint no existe, dará 404, pero eso significa que PASÓ la seguridad.
        // Si la seguridad fallara, daría 401/403.
        mockMvc.perform(get("/api/cualquier-ruta-privada"))
                .andExpect(status().isNotFound()); // 404 es éxito de seguridad aquí (llegó al Dispatcher)
    }

    // --- TEST DE CORS ---

    @Test
    @DisplayName("Debe aplicar configuración CORS correctamente")
    void corsConfigurationShouldWork() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Origin", "http://localhost:5173")) // Origen permitido en tu config
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}