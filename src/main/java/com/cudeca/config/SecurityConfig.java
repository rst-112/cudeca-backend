package com.cudeca.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de Spring Security para la aplicación.
 * Define la cadena de filtros HTTP, las políticas CORS, y las reglas de autorización.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Inyección del filtro JWT (el Portero) para la validación del token
    private final JwtAuthFilter jwtAuthFilter;

    // Inyección del Proveedor de Autenticación (AuthManager, PasswordEncoder, UserDetailsService)
    private final AuthenticationProvider authenticationProvider;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    /**
     * Define la cadena de filtros de seguridad HTTP principal.
     *
     * @param http Configuración de seguridad HTTP.
     * @return El filtro de cadena configurado.
     * @throws Exception Si ocurre un error en la configuración de la seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Manejo de CORS (Usamos el Bean definido abajo)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Deshabilitar CSRF (Crucial para APIs REST con tokens)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Manejo de Sesión: Configurado como STATELESS (Sin estado), obligatorio para JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Reglas de Autorización (Permisos de acceso)
                .authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (Auth + Swagger)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/pagos/webhook"
                        ).permitAll()

                        // Eventos públicos (GET)
                        .requestMatchers(HttpMethod.GET, "/api/eventos", "/api/eventos/**").permitAll()

                        // Todo lo demás requiere Token
                        .anyRequest().authenticated()
                )

                // 5. Asignar el proveedor de autenticación (el que verifica claves)
                .authenticationProvider(authenticationProvider)

                // 6. Añadir el filtro JWT antes del filtro estándar de login/password
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define la configuración de CORS para el backend.
     * Permite peticiones desde el frontend local (Vite/React) y el entorno de producción (Vercel).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (frontend local y producción)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", frontendUrl));

        // Métodos y cabeceras permitidas
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Permite todas las cabeceras
        configuration.setAllowCredentials(true); // Permite cookies y cabeceras de auth

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica la regla a todas las rutas de la API
        return source;
    }
}
