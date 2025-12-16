package com.cudeca.config;

import com.cudeca.service.*;
import com.cudeca.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {}, properties = {"spring.main.allow-bean-definition-overriding=true"})
@Import({SecurityConfig.class, JwtAuthFilter.class, SecurityConfigTest.TestConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.cudeca.controller.EventoController eventoController;
    
    @MockitoBean
    private EntradaEmitidaRepository entradaEmitidaRepository;
    
    @MockitoBean
    private EventoRepository eventoRepository;
    
    @MockitoBean
    private UsuarioRepository usuarioRepository;

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
        public CheckoutService checkoutService() {
            return mock(CheckoutService.class);
        }

        @Bean
        @Primary
        public DatosFiscalesService datosFiscalesService() {
            return mock(DatosFiscalesService.class);
        }

        @Bean
        @Primary
        public PerfilUsuarioService perfilUsuarioService() {
            return mock(PerfilUsuarioService.class);
        }

        @Bean
        @Primary
        public QrValidadorService qrValidadorService() {
            return mock(QrValidadorService.class);
        }

        @Bean
        @Primary
        public TicketService ticketService() {
            return mock(TicketService.class);
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
                // Esperamos 400 porque el cuerpo está vacío, pero no 401/403, lo que prueba que es público
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Público: GET /api/eventos debe ser accesible sin autenticación")
    void getEventosShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Privado: POST /api/eventos debe estar protegido")
    void postEventosShouldBeProtected() throws Exception {
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden()); // o isUnauthorized(), dependiendo de la configuración exacta
    }
}
