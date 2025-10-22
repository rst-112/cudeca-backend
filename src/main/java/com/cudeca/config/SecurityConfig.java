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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir acceso sin autenticación a endpoints públicos (login, registro, quizás listar eventos públicos)
                        // ¡¡AJUSTA ESTAS RUTAS SEGÚN TU DISEÑO DE API!!
                        .requestMatchers("/api/auth/**", "/api/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );
        // Aquí se añadiría la configuración para JWT (filtros, etc.) más adelante

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Define los orígenes permitidos (frontend local y desplegado)
        // ¡¡IMPORTANTE!! Reemplaza "TU_FRONTEND_URL_VERCEL.vercel.app" con tu URL real de Vercel
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://TU_FRONTEND_URL_VERCEL.vercel.app"
        ));
        // métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite todas las cabeceras comunes
        configuration.setAllowedHeaders(List.of("*"));
        // Permite el envío de credenciales (cookies, encabezados de autorización)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todos los endpoints bajo /api/
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}