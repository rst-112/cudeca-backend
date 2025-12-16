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

            // 1. Generar QR (Rápido, en memoria)
            byte[] imagenQR = qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR());

            // 2. Generar PDF (Rápido, en memoria)
            byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

            // 3. Enviar Email
            String asunto = "Tu entrada para " + ticketDTO.getNombreEvento();
            String html = generarHtmlBasico(ticketDTO); // Mover lógica HTML aquí o usar método privado
            String nombreFichero = "Entrada_Cudeca.pdf";

            emailService.enviarCorreoConAdjunto(
                    ticketDTO.getEmailUsuario(),
                    asunto,
                    html,
                    pdfBytes,
                    nombreFichero
            );

            return true; // Asumimos éxito al encolar
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

    private String generarHtmlBasico(TicketDTO dto) {
        return "<h1>Hola " + dto.getNombreUsuario() + "</h1>" +
                "<p>Aquí tienes tu entrada para <strong>" + dto.getNombreEvento() + "</strong>.</p>" +
                "<p>Disfruta del evento.</p>";
    }
}