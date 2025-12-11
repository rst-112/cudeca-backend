package com.cudeca.config;

import com.cudeca.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod") // Solo se ejecuta en Producción (Render)
@RequiredArgsConstructor
@Slf4j
public class EmailTestRunner implements CommandLineRunner {

    private final EmailService emailService;

    @Value("${application.mail.sender}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        log.info("🚀 INICIANDO PRUEBA DE CORREO AL ARRANCAR...");
        try {
            // Se envía un correo a ti mismo (al remitente configurado)
            emailService.sendTestEmail(adminEmail);
            log.info("✅ PRUEBA DE CORREO FINALIZADA: Revisa tu bandeja de entrada.");
        } catch (Exception e) {
            log.error("❌ FALLO EN PRUEBA DE CORREO: Verifica tus credenciales de Brevo.", e);
        }
    }
}