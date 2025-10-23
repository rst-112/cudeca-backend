package com.cudeca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad básica de la aplicación.
 * Define políticas de CORS, sesiones y autorización de endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para HTTP.
     *
     * @param http configuración de seguridad HTTP
     * @return el SecurityFilterChain construido
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        try {
            return buildHttpSecurity(http);
        } catch (Exception e) {
            throw new IllegalStateException("Error al configurar la seguridad HTTP", e);
        }
    }

    /**
     * Aplica la configuración HTTP real. Separado para permitir testeo más granular.
     *
     * @param http configuración HTTP de Spring Security
     * @return cadena de filtros configurada
     * @throws Exception si ocurre un fallo al construir la configuración
     */
    SecurityFilterChain buildHttpSecurity(final HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/public/**", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    /**
     * Configura la fuente de configuración CORS.
     *
     * @return configuración CORS para toda la API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://cudeca-frontend.vercel.app"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
