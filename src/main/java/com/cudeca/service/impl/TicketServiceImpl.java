package com.cudeca.service.impl;

import com.cudeca.dto.TicketDTO;
import com.cudeca.service.EmailService;
import com.cudeca.service.PdfService;
import com.cudeca.service.QrCodeService;
import com.cudeca.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final EmailService emailService;

    @Override
    public boolean generarYEnviarTicket(TicketDTO ticketDTO) {
        try {
            log.info("Procesando ticket para: {}", ticketDTO.getEmailUsuario());

            // 1. Generar QR
            byte[] imagenQR = qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR());

            // 2. Generar PDF
            byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

            // 3. Enviar Email
            String asunto = "🎟️ Tu entrada para: " + ticketDTO.getNombreEvento();
            String html = generarHtmlProfesional(ticketDTO);
            String nombreFichero = "Entrada_" + ticketDTO.getCodigoQR() + ".pdf";

            emailService.enviarCorreoConAdjunto(
                    ticketDTO.getEmailUsuario(),
                    asunto,
                    html,
                    pdfBytes,
                    nombreFichero
            );

            return true;
        } catch (Exception e) {
            log.error("Error orquestando ticket: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] generarTicketPdf(TicketDTO ticketDTO) throws Exception {
        byte[] imagenQR = qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR());
        return pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);
    }

    /**
     * Genera un correo HTML responsivo y con estilo corporativo.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        value = "VA_FORMAT_STRING_USES_NEWLINE",
        justification = "Text block con \\n es más legible que %n en este contexto HTML"
    )
    private String generarHtmlProfesional(TicketDTO dto) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background-color: #00A651; color: white; padding: 20px; text-align: center; }
                        .content { padding: 30px; color: #333333; line-height: 1.6; }
                        .ticket-summary { background-color: #f9f9f9; border-left: 4px solid #00A651; padding: 15px; margin: 20px 0; }
                        .btn { display: inline-block; background-color: #00A651; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }
                        .footer { background-color: #333333; color: #888888; padding: 20px; text-align: center; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Tu entrada está lista!</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>Gracias por tu colaboración. Aquí tienes tu entrada confirmada.</p>
                
                            <div class="ticket-summary">
                                <p><strong>📅 Evento:</strong> %s</p>
                                <p><strong>📍 Lugar:</strong> %s</p>
                                <p><strong>⏰ Fecha:</strong> %s</p>
                                <p><strong>🎟️ Tipo:</strong> %s</p>
                            </div>
                
                            <p>Hemos adjuntado un <strong>PDF</strong> con tu entrada oficial y el código QR necesario para el acceso.</p>
                            <p>Por favor, tenlo preparado en tu móvil o impreso al llegar al evento.</p>
                        </div>
                        <div class="footer">
                            <p>Fundación Cudeca - Cuidados del Cáncer</p>
                            <p>Este es un correo automático, por favor no respondas.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                dto.getNombreUsuario(),
                dto.getNombreEvento(),
                dto.getLugarEvento(),
                dto.getFechaEventoFormato(),
                dto.getTipoEntrada()
        );
    }
}