package com.cudeca.service;

import com.cudeca.dto.TicketDTO;

/**
 * Interfaz para la orquestación de la generación y envío de tickets.
 * Coordina la creación del PDF, generación del QR y envío por correo.
 */
public interface TicketService {

    /**
     * Genera un ticket (PDF con QR) y lo envía por correo electrónico al usuario.
     * <p>
     * Proceso:
     * 1. Genera un código QR con el codigoQR del ticket
     * 2. Genera un PDF con la imagen QR incrustada
     * 3. Envía el PDF por correo al emailUsuario
     *
     * @param ticketDTO Datos necesarios para el ticket
     * @return true si el proceso fue exitoso, false si hubo error
     */
    boolean generarYEnviarTicket(TicketDTO ticketDTO);

    /**
     * Genera un ticket (PDF con QR) y lo retorna como array de bytes
     * sin enviar correo.
     *
     * @param ticketDTO Datos necesarios para el ticket
     * @return Array de bytes con el PDF generado
     * @throws Exception Si ocurre un error durante la generación
     */
    byte[] generarTicketPdf(TicketDTO ticketDTO) throws Exception;
}

