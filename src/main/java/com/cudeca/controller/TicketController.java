package com.cudeca.controller;

import com.cudeca.model.dto.TicketDTO;
import com.cudeca.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controlador para gestionar la generación y envío de tickets de eventos.
 *
 * Expone endpoints para:
 * 1. Generar y enviar tickets por correo (con PDF y QR)
 * 2. Descargar PDF del ticket sin enviar correo
 * 3. Verificar el estado del servicio
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "API para gestión de tickets de eventos con PDF y códigos QR")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Genera un ticket completo (PDF + QR) y lo envía por correo.
     *
     * POST /api/tickets/generar-y-enviar
     *
     * Ejemplo de JSON:
     * {
     *     "nombreEvento": "Concierto CUDECA 2024",
     *     "lugarEvento": "Palacio de Congresos",
     *     "fechaEventoFormato": "15/12/2024 20:00",
     *     "descripcionEvento": "Concierto solidario",
     *     "nombreUsuario": "Juan Pérez",
     *     "emailUsuario": "juan@example.com",
     *     "codigoAsiento": "A-001",
     *     "fila": 5,
     *     "columna": 12,
     *     "zonaRecinto": "Zona Premium",
     *     "codigoQR": "TICKET-2024-001-A",
     *     "tipoEntrada": "Entrada Vip",
     *     "precio": "50€"
     * }
     *
     * @param ticketDTO Datos del ticket a generar
     * @return ResponseEntity con estado del envío
     */
    @PostMapping("/generar-y-enviar")
    @Operation(summary = "Generar ticket y enviar por correo",
            description = "Genera un PDF con QR y lo envía al usuario por correo electrónico")
    public ResponseEntity<Map<String, Object>> generarYEnviarTicket(@RequestBody TicketDTO ticketDTO) {
        try {
            boolean exitoso = ticketService.generarYEnviarTicket(ticketDTO);

            if (exitoso) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Ticket generado y enviado correctamente",
                        "email", ticketDTO.getEmailUsuario(),
                        "codigoTicket", ticketDTO.getCodigoAsiento()
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "status", "error",
                        "message", "Error al generar o enviar el ticket"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "Error inesperado: " + e.getMessage()
            ));
        }
    }

    /**
     * Descarga directa del PDF del ticket sin enviar correo.
     *
     * POST /api/tickets/descargar-pdf
     *
     * Útil para:
     * - Descargas manuales desde interfaz web
     * - Tickets sin envío de correo
     * - Reimpresión de tickets
     *
     * @param ticketDTO Datos del ticket
     * @return ResponseEntity con el PDF para descargar
     */
    @PostMapping("/descargar-pdf")
    @Operation(summary = "Descargar PDF del ticket",
            description = "Genera y descarga el PDF del ticket sin enviar correo")
    public ResponseEntity<byte[]> descargarTicketPDF(@RequestBody TicketDTO ticketDTO) {
        try {
            byte[] pdfBytes = ticketService.generarTicketPdf(ticketDTO);

            // Configurar headers para descarga de PDF
            String nombreArchivo = "ticket-" + ticketDTO.getCodigoAsiento() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(nombreArchivo, StandardCharsets.UTF_8)
                            .build()
            );
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint de prueba para validar que el servicio de tickets está activo.
     *
     * GET /api/tickets/health
     *
     * @return Estado del servicio
     */
    @GetMapping("/health")
    @Operation(summary = "Verificar estado del servicio",
            description = "Verifica que el servicio de tickets está funcionando")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Servicio de tickets activo",
                "versión", "1.0"
        ));
    }
}

