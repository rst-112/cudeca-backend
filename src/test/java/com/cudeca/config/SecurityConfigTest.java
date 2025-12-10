package com.cudeca.config;

import com.cudeca.service.AuthService;
import com.cudeca.service.EmailService;
import com.cudeca.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, JwtAuthFilter.class, SecurityConfigTest.TestConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Configuración de beans falsos para que SecurityConfig arranque
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        @Primary
        public AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        @Primary
        public EmailService emailService() {
            return mock(EmailService.class);
        }

        @Bean
        @Primary
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("test").password("password").roles("USER").build()
            );
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService());
            provider.setPasswordEncoder(passwordEncoder());
            return provider;
        }
    }

    @Test
    @DisplayName("Público: /api/auth/login debe ser accesible")
    void loginShouldBePublic() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Privado: /api/eventos debe estar protegido")
    void protectedRouteShouldBeBlocked() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("Privado con Auth: /api/eventos no debe ser bloqueado por seguridad")
    void protectedRouteAllowedWithAuth() throws Exception {
        // Con autenticación, la seguridad permite el acceso.
        // El recurso no existe, así que puede ser 404 o 500 (según GlobalExceptionHandler),
        // pero lo importante es que NO sea 401 (Unauthorized) ni 403 (Forbidden).
        mockMvc.perform(get("/api/eventos"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Se esperaba acceso permitido pero se recibió: " + status);
                    }
                });
    }
}
