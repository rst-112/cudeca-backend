package com.cudeca.service.impl;

import com.cudeca.model.dto.TicketDTO;
import com.cudeca.service.EmailService;
import com.cudeca.service.PdfService;
import com.cudeca.service.QrCodeService;
import com.cudeca.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación de TicketService.
 *
 * Orquesta todo el flujo de generación y envío de tickets:
 * 1. Genera un código QR con el contenido especificado
 * 2. Crea un PDF con la información del evento y QR incrustado
 * 3. Envía el PDF por correo al usuario
 *
 * Utiliza inyección de dependencias para comunicarse con:
 * - QrCodeService: generación de códigos QR
 * - PdfService: creación de PDFs
 * - EmailService: envío de correos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final EmailService emailService;

    /**
     * Genera un ticket PDF con QR y lo envía por correo.
     *
     * Proceso:
     * 1. Genera el código QR a partir del codigoQR del DTO
     * 2. Crea el PDF con el QR incrustado
     * 3. Envía el PDF al correo del usuario
     *
     * @param ticketDTO Datos del ticket
     * @return true si todo fue exitoso, false si hubo error
     */
    @Override
    public boolean generarYEnviarTicket(TicketDTO ticketDTO) {
        try {
            log.info("Iniciando generación y envío de ticket para usuario: {} (evento: {})",
                    ticketDTO.getNombreUsuario(), ticketDTO.getNombreEvento());

            // ========== PASO 1: GENERAR CÓDIGO QR ==========
            log.debug("Paso 1: Generando código QR con contenido: {}", ticketDTO.getCodigoQR());
            byte[] imagenQR = qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR());
            log.debug("Código QR generado. Tamaño: {} bytes", imagenQR.length);

            // ========== PASO 2: GENERAR PDF CON QR ==========
            log.debug("Paso 2: Generando PDF del ticket");
            byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);
            log.info("PDF generado exitosamente. Tamaño: {} bytes", pdfBytes.length);

            // ========== PASO 3: ENVIAR POR CORREO ==========
            log.debug("Paso 3: Enviando PDF por correo a: {}", ticketDTO.getEmailUsuario());
            String asunto = construirAsuntoCorreo(ticketDTO);
            String contenidoHtml = construirContenidoHtmlCorreo(ticketDTO);
            String nombreArchivo = construirNombreArchivoTicket(ticketDTO);

            emailService.enviarCorreoConAdjunto(
                    ticketDTO.getEmailUsuario(),
                    asunto,
                    contenidoHtml,
                    pdfBytes,
                    nombreArchivo
            );

            log.info("Ticket generado y enviado exitosamente a: {}", ticketDTO.getEmailUsuario());
            return true;

        } catch (Exception e) {
            log.error("Error al generar o enviar ticket para usuario {}: {}",
                    ticketDTO.getNombreUsuario(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Genera un PDF de ticket sin enviar correo.
     *
     * @param ticketDTO Datos del ticket
     * @return Array de bytes con el PDF
     * @throws Exception Si ocurre un error
     */
    @Override
    public byte[] generarTicketPdf(TicketDTO ticketDTO) throws Exception {
        log.info("Generando PDF de ticket para usuario: {} (sin envío de correo)",
                ticketDTO.getNombreUsuario());

        try {
            // Generar QR
            byte[] imagenQR = qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR());

            // Generar PDF con QR
            byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

            log.info("PDF generado exitosamente. Tamaño: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Error al generar PDF de ticket: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Construye el asunto del correo a partir de los datos del ticket.
     */
    private String construirAsuntoCorreo(TicketDTO ticketDTO) {
        return String.format("Tu entrada para %s - CUDECA", ticketDTO.getNombreEvento());
    }

    /**
     * Construye el contenido HTML del correo.
     * Proporciona un formato atractivo e informativo.
     */
    private String construirContenidoHtmlCorreo(TicketDTO ticketDTO) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            color: #333;
                            background-color: #f5f5f5;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background-color: #ffffff;
                            padding: 20px;
                            border-radius: 8px;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            text-align: center;
                            border-bottom: 2px solid #007bff;
                            padding-bottom: 15px;
                            margin-bottom: 20px;
                        }
                        .header h1 {
                            color: #007bff;
                            margin: 0;
                        }
                        .section {
                            margin-bottom: 15px;
                        }
                        .section h3 {
                            color: #007bff;
                            border-bottom: 1px solid #ddd;
                            padding-bottom: 8px;
                        }
                        .section p {
                            margin: 5px 0;
                        }
                        .footer {
                            text-align: center;
                            border-top: 1px solid #ddd;
                            padding-top: 15px;
                            margin-top: 20px;
                            color: #666;
                            font-size: 12px;
                        }
                        .important {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 10px;
                            border-radius: 4px;
                            margin: 15px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Tu entrada está lista!</h1>
                            <p>Evento CUDECA</p>
                        </div>

                        <div class="section">
                            <h3>Hola, %s</h3>
                            <p>Te confirmamos que tu entrada para el evento <strong>%s</strong> está lista para descargar.</p>
                        </div>

                        <div class="section">
                            <h3>Detalles del Evento</h3>
                            <p><strong>Evento:</strong> %s</p>
                            <p><strong>Lugar:</strong> %s</p>
                            <p><strong>Fecha y Hora:</strong> %s</p>
                        </div>

                        <div class="section">
                            <h3>Tu Entrada</h3>
                            <p><strong>Código:</strong> %s</p>
                            <p><strong>Tipo:</strong> %s</p>
                            <p><strong>Zona:</strong> %s</p>
                        </div>

                        <div class="important">
                            <strong>⚠️ Importante:</strong> Adjunto a este correo encontrarás tu PDF con código QR.
                            Presenta este documento en la entrada del evento para acceder.
                        </div>

                        <div class="section">
                            <h3>Instrucciones de Acceso</h3>
                            <ol>
                                <li>Descarga el PDF adjunto</li>
                                <li>Preséntalo en la entrada del evento (en móvil o impreso)</li>
                                <li>Escanea el código QR para validar tu entrada</li>
                            </ol>
                        </div>

                        <div class="footer">
                            <p>© 2024 CUDECA. Todos los derechos reservados.</p>
                            <p>www.cudeca.org | info@cudeca.org</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                ticketDTO.getNombreUsuario(),
                ticketDTO.getNombreEvento(),
                ticketDTO.getNombreEvento(),
                ticketDTO.getLugarEvento(),
                ticketDTO.getFechaEventoFormato(),
                ticketDTO.getCodigoAsiento(),
                ticketDTO.getTipoEntrada(),
                ticketDTO.getZonaRecinto()
        );
    }

    /**
     * Construye el nombre del archivo PDF del ticket.
     */
    private String construirNombreArchivoTicket(TicketDTO ticketDTO) {
        String nombreLimpio = ticketDTO.getNombreEvento()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
        String codigoLimpio = ticketDTO.getCodigoAsiento().replaceAll("[^a-zA-Z0-9]", "_");
        return String.format("Ticket_%s_%s.pdf", nombreLimpio, codigoLimpio);
    }
}

