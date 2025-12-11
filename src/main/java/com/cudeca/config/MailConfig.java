package com.cudeca.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuración de JavaMailSender para Spring Boot.
 * <p>
 * Spring Boot configura automáticamente JavaMailSender basado en la configuración
 * en application.yml. Esta clase proporciona configuración adicional si es necesaria.
 * <p>
 * Propiedades configurables en application.yml:
 * - spring.mail.host: Host del servidor SMTP
 * - spring.mail.port: Puerto del servidor SMTP
 * - spring.mail.username: Usuario para autenticación SMTP
 * - spring.mail.password: Contraseña para autenticación SMTP
 * - spring.mail.default-encoding: Codificación de caracteres
 * - spring.mail.properties.mail.smtp.auth: Habilitar autenticación SMTP
 * - spring.mail.properties.mail.smtp.starttls.enable: Habilitar STARTTLS
 */
@Configuration
@Profile("!test")
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    private final MailProperties mailProperties;

    // Constructor defensivo para evitar exposición de representación interna
    public MailConfig(MailProperties mailProperties) {
        // Crear una nueva instancia para evitar mutación externa
        this.mailProperties = new MailProperties();
        if (mailProperties != null) {
            this.mailProperties.setHost(mailProperties.getHost());
            this.mailProperties.setPort(mailProperties.getPort());
            this.mailProperties.setUsername(mailProperties.getUsername());
            this.mailProperties.setPassword(mailProperties.getPassword());
            this.mailProperties.setDefaultEncoding(mailProperties.getDefaultEncoding());
            if (mailProperties.getProperties() != null) {
                this.mailProperties.getProperties().putAll(mailProperties.getProperties());
            }
        }
    }

    /**
     * Configura el JavaMailSender de forma explícita.
     * Aunque Spring Boot lo hace automáticamente, este bean permite personalizaciones.
     *
     * @return JavaMailSender configurado
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Configuración básica - con validaciones nulas
        if (mailProperties.getHost() != null) {
            mailSender.setHost(mailProperties.getHost());
        }

        if (mailProperties.getPort() != null) {
            mailSender.setPort(mailProperties.getPort());
        }

        if (mailProperties.getUsername() != null) {
            mailSender.setUsername(mailProperties.getUsername());
        }

        if (mailProperties.getPassword() != null) {
            mailSender.setPassword(mailProperties.getPassword());
        }

        // Configurar encoding si está disponible
        if (mailProperties.getDefaultEncoding() != null) {
            mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }

        // Configuración de propiedades SMTP
        Properties properties = new Properties();
        if (mailProperties.getProperties() != null) {
            properties.putAll(mailProperties.getProperties());
        }

        // Asegurar propiedades de timeout
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.writetimeout", "5000");

        mailSender.setJavaMailProperties(properties);

        return mailSender;
    }
}
