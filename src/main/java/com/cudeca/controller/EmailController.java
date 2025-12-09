package com.cudeca.controller;

import com.cudeca.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para gestionar el envío de correos electrónicos.
 * Proporciona endpoints para pruebas y validación del servicio de mail.
 */
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email", description = "API para gestión de envío de correos electrónicos")
public class EmailController {

    private final EmailService emailService;

    /**
     * Envía un correo de prueba a la dirección especificada.
     * Útil para validar que la configuración SMTP está correcta.
     *
     * GET /api/email/send-test-email?to=test@example.com
     *
     * @param to Dirección de correo destinatario
     * @return Mapa con el estado del envío
     */
    @GetMapping("/send-test-email")
    @Operation(summary = "Enviar correo de prueba",
               description = "Envía un correo de prueba para validar la configuración SMTP")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestParam(defaultValue = "test@example.com") String to) {
        try {
            emailService.sendTestEmail(to);
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "Correo de prueba enviado a " + to
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error al enviar correo: " + e.getMessage()
            ));
        }
    }

    /**
     * Envía un correo HTML simple (sin adjuntos).
     *
     * POST /api/email/send-html
     *
     * Ejemplo de JSON:
     * {
     *     "to": "usuario@example.com",
     *     "asunto": "Bienvenido",
     *     "contenido": "<h1>Hola!</h1><p>Este es un correo HTML</p>"
     * }
     *
     * @param correoRequest Datos del correo (to, asunto, contenido)
     * @return Mapa con el estado del envío
     */
    @PostMapping("/send-html")
    @Operation(summary = "Enviar correo HTML",
               description = "Envía un correo en formato HTML sin adjuntos")
    public ResponseEntity<Map<String, String>> enviarCorreoHtml(
            @RequestBody Map<String, String> correoRequest) {
        try {
            String to = correoRequest.get("to");
            String asunto = correoRequest.get("asunto");
            String contenido = correoRequest.get("contenido");

            if (to == null || asunto == null || contenido == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Faltan parámetros requeridos: to, asunto, contenido"
                ));
            }

            emailService.enviarCorreoHtml(to, asunto, contenido);
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "Correo HTML enviado a " + to
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error al enviar correo: " + e.getMessage()
            ));
        }
    }
}