package com.cudeca.config;

import com.cudeca.service.JwtService;
import com.cudeca.service.impl.JwtServiceImpl;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class SecurityTestConfig {

    // Mock del JwtService para no necesitar claves secretas reales en tests
    @Bean
    @Primary
    public JwtService jwtService() {
        return Mockito.mock(JwtServiceImpl.class);
    }

    // UserDetailsService en memoria para no necesitar BD en tests de seguridad
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
