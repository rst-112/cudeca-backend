package com.cudeca.service.impl;

import com.cudeca.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${application.mail.sender}")
    private String senderEmail;

    @Override
    @Async
    public void sendTestEmail(String to) {
        log.info("Enviando correo de prueba a: {}", to);
        enviarCorreoSimple(to, "Correo de Prueba", "Funciona correctamente");
    }

    @Override
    @Async
    public void enviarCorreoHtml(String to, String asunto, String contenidoHtml) {
        // En métodos async, capturamos excepciones aquí porque no se propagan al Controller
        try {
            log.info("Enviando correo HTML a: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            helper.setFrom(senderEmail);

            mailSender.send(message);
            log.info("Correo enviado OK a: {}", to);
        } catch (Exception e) {
            log.error("Fallo crítico enviando email asíncrono a {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarCorreoConAdjunto(String to, String asunto, String contenidoHtml,
                                       byte[] pdfBytes, String nombreArchivoAdjunto) {
        try {
            log.info("Enviando correo con adjunto a: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            helper.setFrom(senderEmail);

            helper.addAttachment(nombreArchivoAdjunto, () -> new java.io.ByteArrayInputStream(pdfBytes));

            mailSender.send(message);
            log.info("Correo con PDF enviado OK a: {}", to);
        } catch (Exception e) {
            log.error("Fallo crítico enviando ticket a {}: {}", to, e.getMessage());
        }
    }

    // Helper privado para enviar correos de texto plano
    private void enviarCorreoSimple(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Correo simple enviado correctamente a: {}", to);
        } catch (Exception e) {
            // Logueamos el error pero no lanzamos excepción para no romper el flujo asíncrono
            log.error("Error al enviar correo simple a {}: {}", to, e.getMessage());
        }
    }
}