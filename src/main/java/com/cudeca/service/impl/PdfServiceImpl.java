package com.cudeca.service.impl;

import com.cudeca.dto.TicketDTO;
import com.cudeca.model.negocio.ArticuloCompra;
import com.cudeca.model.negocio.ArticuloEntrada;
import com.cudeca.model.negocio.Compra;
import com.cudeca.service.PdfService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    // --- COLORES CORPORATIVOS & ESTILO ---
    private static final Color COLOR_CUDECA = new Color(0, 166, 81); // Verde corporativo
    private static final Color COLOR_GRIS_OSCURO = new Color(60, 60, 60);
    private static final Color COLOR_GRIS_FONDO = new Color(245, 245, 245); // Gris muy suave para fondos
    private static final Color COLOR_BORDE = new Color(200, 200, 200); // Gris para líneas finas

    // --- FUENTES ---
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font FONT_VALUE_LARGE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
    private static final Font FONT_SMALL_ITALIC = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
    private static final Font FONT_CODE = FontFactory.getFont(FontFactory.COURIER_BOLD, 10, Color.DARK_GRAY);

    // ==========================================
    // MÉTODOS PÚBLICOS (INTERFAZ)
    // ==========================================

    @Override
    public byte[] generarPdfTicket(TicketDTO ticketDTO) throws Exception {
        return generarPdfTicketConQR(ticketDTO, new byte[]{});
    }

    /**
     * Genera un PDF de una sola página con la entrada individual.
     */
    @Override
    public byte[] generarPdfTicketConQR(TicketDTO ticket, byte[] qrBytes) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);

            document.open();
            pintarPaginaTicket(document, ticket, qrBytes); // Reutilizamos la lógica de pintado
            document.close();

            return out.toByteArray();
        } catch (DocumentException e) {
            log.error("Error OpenPDF: {}", e.getMessage());
            throw new IOException("Error generando PDF de ticket único", e);
        }
    }

    /**
     * Genera un PDF multipágina:
     * - Pág 1: Resumen de compra (Factura)
     * - Pág 2+: Entradas individuales
     */
    @Override
    public byte[] generarPdfCompraCompleta(Compra compra, List<TicketDTO> entradas, List<byte[]> codigosQR) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);

            document.open();

            // 1. PÁGINA RESUMEN
            pintarPaginaResumen(document, compra);

            // 2. PÁGINAS DE ENTRADAS
            for (int i = 0; i < entradas.size(); i++) {
                document.newPage(); // Salto de página
                pintarPaginaTicket(document, entradas.get(i), codigosQR.get(i));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            log.error("Error OpenPDF Multipágina: {}", e.getMessage());
            throw new IOException("Error generando PDF de compra completa", e);
        }
    }

    // ==========================================
    // LÓGICA DE PINTADO (PRIVADA)
    // ==========================================

    /**
     * Pinta el diseño de una entrada individual en la página actual.
     */
    private void pintarPaginaTicket(Document document, TicketDTO ticket, byte[] qrBytes) throws DocumentException, IOException {
        // 1. CABECERA
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cellHeader = new PdfPCell(new Phrase("CUDECA | Fundación", FONT_HEADER));
        cellHeader.setBackgroundColor(COLOR_CUDECA);
        cellHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellHeader.setPadding(15);
        cellHeader.setPaddingLeft(20);
        cellHeader.setBorder(Rectangle.NO_BORDER);

        headerTable.addCell(cellHeader);
        document.add(headerTable);

        document.add(Chunk.NEWLINE);

        // Título
        Paragraph title = new Paragraph("ENTRADA OFICIAL",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_CUDECA));
        title.setSpacingAfter(20);
        title.setIndentationLeft(10);
        document.add(title);

        // 2. CUERPO PRINCIPAL (Tabla 2 Columnas)
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{65f, 35f}); // 65% Info, 35% QR

        // --- COLUMNA IZQUIERDA: DATOS ---
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingRight(20);

        // BLOQUE: EVENTO
        addSectionLabel(infoCell, "EVENTO");
        addFieldValue(infoCell, ticket.getNombreEvento(), true);

        addSpacer(infoCell, 5);

        // Fecha y Lugar
        addFieldLabel(infoCell, "FECHA Y HORA");
        addFieldValue(infoCell, ticket.getFechaEventoFormato(), false);

        addSpacer(infoCell, 5);

        addFieldLabel(infoCell, "LUGAR");
        addFieldValue(infoCell, ticket.getLugarEvento(), false);

        addSeparatorLine(infoCell);

        // BLOQUE: ASISTENTE & ENTRADA
        addSectionLabel(infoCell, "ASISTENTE");
        addFieldValue(infoCell, ticket.getNombreUsuario(), false);

        addSpacer(infoCell, 8);

        // Precio Integrado
        String tipoConPrecio = String.format("%s (%s)", ticket.getTipoEntrada(), ticket.getPrecio());
        addFieldLabel(infoCell, "TIPO DE ENTRADA");
        addFieldValue(infoCell, tipoConPrecio, false);

        addSpacer(infoCell, 8);

        // Ubicación
        addFieldLabel(infoCell, "UBICACIÓN");
        if (ticket.getZonaRecinto() != null && !"General".equalsIgnoreCase(ticket.getZonaRecinto())) {
            String asientoInfo = String.format("%s - Fila %s | Asiento %s",
                    ticket.getZonaRecinto(),
                    ticket.getFila() != null ? ticket.getFila() : "-",
                    ticket.getColumna() != null ? ticket.getColumna() : "-");
            addFieldValue(infoCell, asientoInfo, false);
        } else {
            addFieldValue(infoCell, "Acceso General / De pie", false);
        }

        mainTable.addCell(infoCell);

        // --- COLUMNA DERECHA: CAJA QR ---
        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.BOX);
        qrCell.setBorderColor(COLOR_BORDE);
        qrCell.setBorderWidth(1f);
        qrCell.setBackgroundColor(COLOR_GRIS_FONDO);
        qrCell.setPadding(15);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (qrBytes != null && qrBytes.length > 0) {
            Image qrImg = Image.getInstance(qrBytes);
            qrImg.scaleToFit(140, 140);
            qrImg.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrImg);

            Paragraph pSpace = new Paragraph(" ");
            pSpace.setSpacingAfter(5);
            qrCell.addElement(pSpace);

            Paragraph qrCodeText = new Paragraph(ticket.getCodigoQR(), FONT_CODE);
            qrCodeText.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrCodeText);

            Paragraph scanInstruction = new Paragraph("ID de seguridad", FONT_SMALL_ITALIC);
            scanInstruction.setAlignment(Element.ALIGN_CENTER);
            scanInstruction.setSpacingAfter(10);
            qrCell.addElement(scanInstruction);
        }

        mainTable.addCell(qrCell);
        document.add(mainTable);

        // 3. FOOTER
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(Rectangle.TOP);
        footerCell.setBorderColor(COLOR_BORDE);
        footerCell.setPaddingTop(15);

        Paragraph legalText = new Paragraph(
                """
                        Gracias por colaborar con la Fundación Cudeca.
                        Por favor, muestra el código QR a la entrada desde tu móvil o impreso.
                        La reventa de esta entrada está prohibida. Cudeca se reserva el derecho de admisión.""",
                FONT_SMALL_ITALIC);
        legalText.setAlignment(Element.ALIGN_CENTER);

        footerCell.addElement(legalText);
        footerTable.addCell(footerCell);

        document.add(footerTable);
    }

    /**
     * Pinta la página de resumen de compra (Factura simplificada).
     */
    private void pintarPaginaResumen(Document document, Compra compra) throws DocumentException {
        // Cabecera Resumen (Gris Oscuro para diferenciar)
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell cellHeader = new PdfPCell(new Phrase("RESUMEN DE COMPRA", FONT_HEADER));
        cellHeader.setBackgroundColor(COLOR_GRIS_OSCURO);
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setPadding(15);
        cellHeader.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cellHeader);
        document.add(headerTable);

        document.add(Chunk.NEWLINE);

        // Info General
        Paragraph info = new Paragraph();
        info.add(new Chunk("Referencia: ", FONT_LABEL));
        info.add(new Chunk("#" + compra.getId() + "\n", FONT_VALUE));
        info.add(new Chunk("Fecha: ", FONT_LABEL));
        // Formateo de fecha seguro
        String fechaStr = compra.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        info.add(new Chunk(fechaStr + "\n", FONT_VALUE));
        info.add(new Chunk("Cliente: ", FONT_LABEL));
        info.add(new Chunk(compra.getUsuario().getNombre() + "\n", FONT_VALUE));
        document.add(info);

        document.add(Chunk.NEWLINE);

        // Tabla de Artículos
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 2});

        crearCeldaTablaResumen(table, "CANT", true);
        crearCeldaTablaResumen(table, "DESCRIPCIÓN", true);
        crearCeldaTablaResumen(table, "PRECIO", true);

        BigDecimal total = BigDecimal.ZERO;
        for (ArticuloCompra art : compra.getArticulos()) {
            crearCeldaTablaResumen(table, String.valueOf(art.getCantidad()), false);

            String desc;
            if (art instanceof ArticuloEntrada ae) {
                desc = "Entrada: " + ae.getTipoEntrada().getNombre();
                if (ae.getTipoEntrada().getEvento() != null) {
                    desc += " - " + ae.getTipoEntrada().getEvento().getNombre();
                }
            } else {
                desc = "Donación / Otro";
            }
            crearCeldaTablaResumen(table, desc, false);
            crearCeldaTablaResumen(table, art.getPrecioUnitario() + "€", false);

            total = total.add(art.getPrecioUnitario().multiply(new BigDecimal(art.getCantidad())));
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Total
        Paragraph pTotal = new Paragraph("TOTAL: " + total + "€",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_CUDECA));
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTotal);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Este documento sirve como justificante de su compra. En las páginas siguientes encontrará sus entradas válidas para el acceso.", FONT_SMALL_ITALIC));
    }

    // --- HELPERS DE DISEÑO ---

    private void addSectionLabel(PdfPCell cell, String text) {
        Paragraph p = new Paragraph(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_CUDECA));
        p.setSpacingAfter(4);
        cell.addElement(p);
    }

    private void addFieldLabel(PdfPCell cell, String text) {
        Paragraph p = new Paragraph(text, FONT_LABEL);
        p.setSpacingAfter(2);
        cell.addElement(p);
    }

    private void addFieldValue(PdfPCell cell, String text, boolean isLarge) {
        Paragraph p = new Paragraph(text != null ? text : "-", isLarge ? FONT_VALUE_LARGE : FONT_VALUE);
        p.setSpacingAfter(4);
        cell.addElement(p);
    }

    private void addSpacer(PdfPCell cell, float size) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(size);
        cell.addElement(p);
    }

    private void addSeparatorLine(PdfPCell cell) {
        addSpacer(cell, 10);
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(COLOR_BORDE);
        ls.setLineWidth(0.5f);
        cell.addElement(ls);
        addSpacer(cell, 10);
    }

    private void crearCeldaTablaResumen(PdfPTable table, String texto, boolean esHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, esHeader ? FONT_VALUE : FONT_LABEL)); // Reutilizando fuentes del ticket
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(COLOR_BORDE);
        if (esHeader) cell.setBackgroundColor(COLOR_GRIS_FONDO);
        table.addCell(cell);
    }
}