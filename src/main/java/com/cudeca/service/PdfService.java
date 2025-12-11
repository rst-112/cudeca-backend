package com.cudeca.service;

import com.cudeca.dto.TicketDTO;

/**
 * Interfaz para la generación de PDFs de tickets.
 * Permite crear PDFs profesionales con información del evento,
 * detalles del asiento/entrada y códigos QR incrustados.
 */
public interface PdfService {

    /**
     * Genera un PDF de ticket con los datos proporcionados.
     * El PDF incluye:
     * - Logo y datos del evento
     * - Información del usuario y asiento
     * - Código QR incrustado
     *
     * @param ticketDTO Datos necesarios para generar el ticket
     * @return Array de bytes con el contenido del PDF
     * @throws Exception Si ocurre un error durante la generación
     */
    byte[] generarPdfTicket(TicketDTO ticketDTO) throws Exception;

    /**
     * Genera un PDF de ticket con un código QR personalizado.
     * Útil cuando se quiere usar un QR diferente al generado automáticamente.
     *
     * @param ticketDTO     Datos necesarios para generar el ticket
     * @param imagenQRBytes Bytes de la imagen QR pre-generada
     * @return Array de bytes con el contenido del PDF
     * @throws Exception Si ocurre un error durante la generación
     */
    byte[] generarPdfTicketConQR(TicketDTO ticketDTO, byte[] imagenQRBytes) throws Exception;
}

