package com.cudeca.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Configuración de test para proporcionar un mock de JavaMailSender
 * y evitar dependencias reales de SMTP durante los tests.
 */
@TestConfiguration
public class MailConfigTest {

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}
