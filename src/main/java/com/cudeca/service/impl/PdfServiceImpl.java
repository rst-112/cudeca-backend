package com.cudeca.service.impl;

import com.cudeca.model.dto.TicketDTO;
import com.cudeca.service.PdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementación de PdfService utilizando OpenPDF.
 *
 * OpenPDF es la alternativa moderna y mantenida a iText 2.x.
 * Ofrece simplicidad, sin la complejidad de iText 7.x, pero con mayor mantenimiento que iText 2.x.
 * Excelente para generar PDFs sencillos y profesionales sin curva de aprendizaje pronunciada.
 */
@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    private static final float MARGEN_IZQUIERDO = 20;
    private static final float MARGEN_DERECHO = 20;
    private static final float MARGEN_SUPERIOR = 20;
    private static final float MARGEN_INFERIOR = 20;

    /**
     * Genera un PDF de ticket generando automáticamente el código QR.
     *
     * @param ticketDTO Datos del ticket
     * @return Array de bytes con el PDF
     * @throws Exception Si ocurre un error
     */
    @Override
    public byte[] generarPdfTicket(TicketDTO ticketDTO) throws Exception {
        log.info("Generando PDF de ticket para evento: {}", ticketDTO.getNombreEvento());

        // Este método será sobrecargado por generarPdfTicketConQR en la realidad
        // pero para que funcione solo, crearemos un QR vacío o sin QR
        byte[] imagenQR = new byte[]{};
        return generarPdfTicketConQR(ticketDTO, imagenQR);
    }

    /**
     * Genera un PDF de ticket con un código QR personalizado incrustado.
     *
     * Estructura del PDF:
     * 1. Encabezado con logo/titulo CUDECA
     * 2. Información del evento (nombre, lugar, fecha)
     * 3. Información del usuario (nombre, email)
     * 4. Información del asiento (código, fila, columna, zona)
     * 5. Código QR (lado derecho)
     *
     * @param ticketDTO Datos del ticket
     * @param imagenQRBytes Bytes de la imagen QR
     * @return Array de bytes con el PDF
     * @throws Exception Si ocurre un error
     */
    @Override
    public byte[] generarPdfTicketConQR(TicketDTO ticketDTO, byte[] imagenQRBytes) throws Exception {
        log.debug("Generando PDF de ticket con QR para usuario: {}", ticketDTO.getNombreUsuario());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Crear documento PDF
            Document document = new Document(PageSize.A4, MARGEN_IZQUIERDO, MARGEN_DERECHO,
                    MARGEN_SUPERIOR, MARGEN_INFERIOR);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // ========== SECCIÓN 1: ENCABEZADO Y LOGO ==========
            agregarEncabezado(document);

            document.add(new Paragraph(" ")); // Espacio en blanco

            // ========== SECCIÓN 2: INFORMACIÓN DEL EVENTO ==========
            agregarInformacionEvento(document, ticketDTO);

            document.add(new Paragraph(" "));

            // ========== SECCIÓN 3: INFORMACIÓN DEL USUARIO ==========
            agregarInformacionUsuario(document, ticketDTO);

            document.add(new Paragraph(" "));

            // ========== SECCIÓN 4: INFORMACIÓN DEL ASIENTO ==========
            agregarInformacionAsiento(document, ticketDTO);

            document.add(new Paragraph(" "));

            // ========== SECCIÓN 5: CÓDIGO QR (OPCIONAL) ==========
            if (imagenQRBytes != null && imagenQRBytes.length > 0) {
                agregarCodigoQR(document, imagenQRBytes);
            }

            document.add(new Paragraph(" "));

            // ========== PIE DE PÁGINA ==========
            agregarPiePagina(document);

            document.close();

            log.info("PDF de ticket generado exitosamente. Tamaño: {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (DocumentException e) {
            log.error("Error al generar el documento PDF: {}", e.getMessage(), e);
            throw new IOException("Error al generar PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al generar PDF: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Agrega el encabezado del documento con el logo/título de CUDECA.
     */
    private void agregarEncabezado(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("CUDECA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("ENTRADA DE EVENTO", FontFactory.getFont(FontFactory.HELVETICA, 12));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);

        // Línea divisoria
        Paragraph linea = new Paragraph("_".repeat(60));
        linea.setAlignment(Element.ALIGN_CENTER);
        document.add(linea);
    }

    /**
     * Agrega la sección de información del evento.
     */
    private void agregarInformacionEvento(Document document, TicketDTO ticketDTO) throws DocumentException {
        Paragraph tituloEvento = new Paragraph("INFORMACIÓN DEL EVENTO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        tituloEvento.setSpacingBefore(10);
        document.add(tituloEvento);

        document.add(crearFilaDatos("Evento:", ticketDTO.getNombreEvento()));
        document.add(crearFilaDatos("Lugar:", ticketDTO.getLugarEvento()));
        document.add(crearFilaDatos("Fecha y Hora:", ticketDTO.getFechaEventoFormato()));
        document.add(crearFilaDatos("Descripción:", ticketDTO.getDescripcionEvento()));
    }

    /**
     * Agrega la sección de información del usuario.
     */
    private void agregarInformacionUsuario(Document document, TicketDTO ticketDTO) throws DocumentException {
        Paragraph tituloUsuario = new Paragraph("INFORMACIÓN DEL ASISTENTE",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        tituloUsuario.setSpacingBefore(10);
        document.add(tituloUsuario);

        document.add(crearFilaDatos("Nombre:", ticketDTO.getNombreUsuario()));
        document.add(crearFilaDatos("Email:", ticketDTO.getEmailUsuario()));
    }

    /**
     * Agrega la sección de información del asiento/entrada.
     */
    private void agregarInformacionAsiento(Document document, TicketDTO ticketDTO) throws DocumentException {
        Paragraph tituloAsiento = new Paragraph("INFORMACIÓN DE ENTRADA",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        tituloAsiento.setSpacingBefore(10);
        document.add(tituloAsiento);

        document.add(crearFilaDatos("Código de Entrada:", ticketDTO.getCodigoAsiento()));
        document.add(crearFilaDatos("Tipo de Entrada:", ticketDTO.getTipoEntrada()));
        document.add(crearFilaDatos("Zona:", ticketDTO.getZonaRecinto()));

        if (ticketDTO.getFila() != null && ticketDTO.getColumna() != null) {
            String ubicacion = "Fila " + ticketDTO.getFila() + ", Asiento " + ticketDTO.getColumna();
            document.add(crearFilaDatos("Ubicación:", ubicacion));
        }

        if (ticketDTO.getPrecio() != null) {
            document.add(crearFilaDatos("Precio:", ticketDTO.getPrecio()));
        }
    }

    /**
     * Agrega el código QR al documento.
     */
    private void agregarCodigoQR(Document document, byte[] imagenQRBytes) throws DocumentException, IOException {
        Paragraph tituloQR = new Paragraph("CÓDIGO DE ACCESO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        tituloQR.setSpacingBefore(10);
        document.add(tituloQR);

        Image qrImage = Image.getInstance(imagenQRBytes);
        qrImage.scaleToFit(150, 150);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        Paragraph instrucciones = new Paragraph("Escanea este código QR en la entrada del evento",
                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC));
        instrucciones.setAlignment(Element.ALIGN_CENTER);
        document.add(instrucciones);
    }

    /**
     * Agrega el pie de página del documento.
     */
    private void agregarPiePagina(Document document) throws DocumentException {
        Paragraph linea = new Paragraph("_".repeat(60));
        linea.setAlignment(Element.ALIGN_CENTER);
        document.add(linea);

        Paragraph pie = new Paragraph("Gracias por tu asistencia. ¡Nos vemos en el evento!",
                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC));
        pie.setAlignment(Element.ALIGN_CENTER);
        pie.setSpacingBefore(10);
        document.add(pie);

        Paragraph contacto = new Paragraph("www.cudeca.org | info@cudeca.org | +34 123 456 789",
                FontFactory.getFont(FontFactory.HELVETICA, 9));
        contacto.setAlignment(Element.ALIGN_CENTER);
        document.add(contacto);
    }

    /**
     * Crea una fila de datos con etiqueta y valor.
     */
    private Paragraph crearFilaDatos(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        p.add(new Chunk(" " + (valor != null ? valor : "N/A"),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        p.setSpacingAfter(8);
        return p;
    }
}

