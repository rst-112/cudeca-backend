package com.cudeca.service;

/**
 * Interfaz para el envío de correos electrónicos.
 * Permite enviar correos de prueba, correos HTML simples y correos con adjuntos (PDF).
 */
public interface EmailService {

    /**
     * Envía un correo de prueba a una dirección de correo.
     * Utilizado para validar la configuración del servicio de correo.
     *
     * @param to Dirección de correo destinatario
     * @throws Exception Si ocurre un error al enviar el correo
     */
    void sendTestEmail(String to) throws Exception;

    /**
     * Envía un correo HTML simple sin adjuntos.
     *
     * @param to Dirección de correo destinatario
     * @param asunto Asunto del correo
     * @param contenidoHtml Contenido del correo en formato HTML
     * @throws Exception Si ocurre un error al enviar el correo
     */
    void enviarCorreoHtml(String to, String asunto, String contenidoHtml) throws Exception;

    /**
     * Envía un correo HTML con PDF adjunto.
     * Utilizado principalmente para enviar tickets de eventos a los usuarios.
     *
     * @param to Dirección de correo destinatario
     * @param asunto Asunto del correo
     * @param contenidoHtml Contenido del correo en formato HTML
     * @param pdfBytes Array de bytes con el contenido del PDF
     * @param nombreArchivoAdjunto Nombre del archivo PDF a adjuntar (incluir extensión .pdf)
     * @throws Exception Si ocurre un error al enviar el correo
     */
    void enviarCorreoConAdjunto(String to, String asunto, String contenidoHtml, byte[] pdfBytes, String nombreArchivoAdjunto) throws Exception;
}
