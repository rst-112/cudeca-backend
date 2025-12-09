package com.cudeca.service.impl;

import com.cudeca.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Implementación de EmailService utilizando JavaMailSender de Spring.
 *
 * Soporta:
 * - Envío de correos de prueba simples
 * - Envío de correos HTML
 * - Envío de correos con adjuntos (PDF)
 *
 * Requiere configuración en application.yml/application.properties
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envía un correo de prueba a la dirección especificada.
     *
     * @param to Dirección de correo destinatario
     * @throws Exception Si ocurre un error al enviar el correo
     */
    @Override
    public void sendTestEmail(String to) throws Exception {
        log.info("Enviando correo de prueba a: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Correo de Prueba - CUDECA");
            helper.setText(generarContenidoPrueba(), true);
            helper.setFrom("noreply@cudeca.org");

            mailSender.send(message);
            log.info("Correo de prueba enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("Error al enviar correo de prueba a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Envía un correo HTML simple sin adjuntos.
     *
     * @param to Dirección de correo destinatario
     * @param asunto Asunto del correo
     * @param contenidoHtml Contenido del correo en formato HTML
     * @throws Exception Si ocurre un error al enviar el correo
     */
    @Override
    public void enviarCorreoHtml(String to, String asunto, String contenidoHtml) throws Exception {
        log.info("Enviando correo HTML a: {} con asunto: {}", to, asunto);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            helper.setFrom("noreply@cudeca.org");

            mailSender.send(message);
            log.info("Correo HTML enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("Error al enviar correo HTML a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Envía un correo HTML con PDF adjunto.
     *
     * @param to Dirección de correo destinatario
     * @param asunto Asunto del correo
     * @param contenidoHtml Contenido del correo en formato HTML
     * @param pdfBytes Array de bytes con el contenido del PDF
     * @param nombreArchivoAdjunto Nombre del archivo PDF a adjuntar
     * @throws Exception Si ocurre un error al enviar el correo
     */
    @Override
    public void enviarCorreoConAdjunto(String to, String asunto, String contenidoHtml,
                                       byte[] pdfBytes, String nombreArchivoAdjunto) throws Exception {
        log.info("Enviando correo con adjunto a: {} (archivo: {})", to, nombreArchivoAdjunto);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            helper.setFrom("noreply@cudeca.org");

            helper.addAttachment(nombreArchivoAdjunto, () -> new java.io.ByteArrayInputStream(pdfBytes));

            mailSender.send(message);
            log.info("Correo con adjunto enviado exitosamente a: {} (tamaño PDF: {} bytes)",
                    to, pdfBytes.length);

        } catch (Exception e) {
            log.error("Error al enviar correo con adjunto a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Genera el contenido HTML para el correo de prueba.
     */
    private String generarContenidoPrueba() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; }
                        .container { max-width: 600px; margin: 20px auto; background-color: #f9f9f9; 
                                    padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }
                        .header { text-align: center; color: #007bff; border-bottom: 2px solid #007bff; 
                                 padding-bottom: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Correo de Prueba!</h1>
                        </div>
                        <p>Hola,</p>
                        <p>Este es un correo de prueba de CUDECA para verificar que la configuración 
                           del servicio de correo es correcta.</p>
                        <p>Si recibiste este mensaje, ¡significa que todo está funcionando correctamente!</p>
                        <p>Saludos,<br>CUDECA</p>
                    </div>
                </body>
                </html>
                """;
    }
}

