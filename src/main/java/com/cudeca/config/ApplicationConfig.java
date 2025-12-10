package com.cudeca.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración de infraestructura para Beans de seguridad y autenticación.
 * Esta clase rompe el ciclo de dependencia al externalizar los Beans requeridos por SecurityConfig.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    // Inyección del UserDetailsService (UserServiceImpl está anotado con @Service)
    private final UserDetailsService userDetailsService;

    // ------------------------------------------------------------------------
    // BEANS DE SERVICIO CORE
    // ------------------------------------------------------------------------


    /**
     * Define el Bean de PasswordEncoder, utilizando el algoritmo BCrypt.
     *
     * @return El codificador de contraseñas para hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ------------------------------------------------------------------------
    // BEANS DE AUTENTICACIÓN
    // ------------------------------------------------------------------------

    /**
     * Define el Proveedor de Autenticación (AuthenticationProvider).
     * Este Bean combina el servicio que carga al usuario (UserDetailsService)
     * con el servicio que codifica la contraseña (PasswordEncoder).
     *
     * @return Instancia configurada de DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Asignamos el servicio que busca al usuario (UserServiceImpl inyectado)
        authProvider.setUserDetailsService(userDetailsService);
        // Asignamos el codificador (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el AuthenticationManager, que es la interfaz principal usada
     * por AuthService.java para procesar el intento de login.
     *
     * @param config Configuración de autenticación de Spring.
     * @return El AuthenticationManager expuesto.
     * @throws Exception Si falla al obtener el manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}