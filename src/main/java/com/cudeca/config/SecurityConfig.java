package com.cudeca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        try {
            log.info("Inicializando configuración de seguridad HTTP para el perfil: {}", activeProfile);
            return buildHttpSecurity(http);
        } catch (Exception e) {
            log.error("Error al configurar la seguridad HTTP", e);
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
        log.debug("Aplicando políticas de seguridad...");
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/public/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated()
                );
        log.debug("Seguridad HTTP configurada correctamente");
        return http.build();
    }

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

        log.debug("Configurando CORS: origins={} methods={}", configuration.getAllowedOrigins(), configuration.getAllowedMethods());

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
