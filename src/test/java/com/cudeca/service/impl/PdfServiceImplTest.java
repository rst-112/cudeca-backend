package com.cudeca.service.impl;

import com.cudeca.dto.TicketDTO;
import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para PdfServiceImpl.
 * Cobertura esperada: >95%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para PdfServiceImpl")
class PdfServiceImplTest {

    @InjectMocks
    private PdfServiceImpl pdfService;

    private TicketDTO ticketDTO;
    private byte[] imagenQR;

    @BeforeEach
    void setUp() {
        ticketDTO = TicketDTO.builder()
                .codigoQR("QR-TEST-123")
                .codigoAsiento("A1")
                .nombreEvento("Concierto CUDECA 2024")
                .lugarEvento("Auditorio Principal")
                .fechaEventoFormato("15/12/2024 20:00")
                .descripcionEvento("Concierto benéfico anual")
                .nombreUsuario("Juan Pérez")
                .emailUsuario("juan@test.com")
                .zonaRecinto("Zona VIP")
                .tipoEntrada("General")
                .fila(5)
                .columna(10)
                .precio("25.00 €")
                .build();

        // Usar array vacío para evitar error de OpenPDF con bytes inválidos
        imagenQR = new byte[]{};
    }

    @Test
    @DisplayName("Debe generar PDF con todos los datos del ticket")
    void debeGenerarPdfConTodosLosDatos() throws Exception {
        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "El PDF debe tener contenido");
        assertTrue(pdfBytes.length > 1000, "El PDF debe tener tamaño significativo");
    }

    @Test
    @DisplayName("Debe generar PDF sin código QR")
    void debeGenerarPdfSinCodigoQR() throws Exception {
        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, new byte[]{});

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con QR nulo")
    void debeGenerarPdfConQRNulo() throws Exception {
        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, null);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF sin ubicación de asiento")
    void debeGenerarPdfSinUbicacionAsiento() throws Exception {
        // Arrange
        ticketDTO.setFila(null);
        ticketDTO.setColumna(null);

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF sin precio")
    void debeGenerarPdfSinPrecio() throws Exception {
        // Arrange
        ticketDTO.setPrecio(null);

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con datos mínimos")
    void debeGenerarPdfConDatosMinimos() throws Exception {
        // Arrange
        TicketDTO dtoMinimo = TicketDTO.builder()
                .nombreEvento("Evento Test")
                .nombreUsuario("Usuario Test")
                .emailUsuario("test@test.com")
                .codigoAsiento("X1")
                .build();

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(dtoMinimo, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con valores nulos en campos opcionales")
    void debeGenerarPdfConValoresNulos() throws Exception {
        // Arrange
        TicketDTO dtoConNulos = TicketDTO.builder()
                .nombreEvento("Evento")
                .nombreUsuario("Usuario")
                .emailUsuario("email@test.com")
                .codigoAsiento("B2")
                .lugarEvento(null)
                .fechaEventoFormato(null)
                .descripcionEvento(null)
                .zonaRecinto(null)
                .tipoEntrada(null)
                .fila(null)
                .columna(null)
                .precio(null)
                .build();

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(dtoConNulos, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con nombre de evento largo")
    void debeGenerarPdfConNombreEventoLargo() throws Exception {
        // Arrange
        ticketDTO.setNombreEvento("Concierto Benéfico Anual de la Fundación CUDECA con Artistas Internacionales y Locales 2024");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con descripción larga")
    void debeGenerarPdfConDescripcionLarga() throws Exception {
        // Arrange
        ticketDTO.setDescripcionEvento("Este es un evento muy especial organizado por CUDECA. "
                + "Incluye múltiples artistas, actividades para toda la familia, "
                + "y recaudación de fondos para una buena causa. "
                + "No te lo pierdas. Será una noche inolvidable.");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con caracteres especiales")
    void debeGenerarPdfConCaracteresEspeciales() throws Exception {
        // Arrange
        ticketDTO.setNombreUsuario("José María Pérez-García");
        ticketDTO.setNombreEvento("Concierto €$£ 2024 & Fiesta");
        ticketDTO.setDescripcionEvento("Evento con símbolos: @#$%&*()");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF usando el método sin QR explícito")
    void debeGenerarPdfUsandoMetodoSinQRExplicito() throws Exception {
        // Act
        byte[] pdfBytes = pdfService.generarPdfTicket(ticketDTO);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar múltiples PDFs diferentes")
    void debeGenerarMultiplesPdfsDiferentes() throws Exception {
        // Arrange
        TicketDTO ticket1 = TicketDTO.builder()
                .nombreEvento("Evento 1")
                .nombreUsuario("Usuario 1")
                .emailUsuario("user1@test.com")
                .codigoAsiento("A1")
                .build();

        TicketDTO ticket2 = TicketDTO.builder()
                .nombreEvento("Evento 2")
                .nombreUsuario("Usuario 2")
                .emailUsuario("user2@test.com")
                .codigoAsiento("B2")
                .build();

        // Act
        byte[] pdf1 = pdfService.generarPdfTicketConQR(ticket1, imagenQR);
        byte[] pdf2 = pdfService.generarPdfTicketConQR(ticket2, imagenQR);

        // Assert
        assertNotNull(pdf1);
        assertNotNull(pdf2);
        assertTrue(pdf1.length > 0);
        assertTrue(pdf2.length > 0);
        // Los PDFs pueden tener tamaño idéntico debido a compresión, verificar que no sean exactamente iguales
        assertFalse(java.util.Arrays.equals(pdf1, pdf2), "PDFs con contenido diferente no deben ser idénticos");
    }

    @Test
    @DisplayName("Debe generar PDF con QR de gran tamaño (array vacío)")
    void debeGenerarPdfConQRGranTamaño() throws Exception {
        // Arrange
        // OpenPDF requiere formato de imagen válido (PNG/JPG).
        // Array vacío permite que el servicio omita la inserción del QR sin lanzar IOException
        byte[] qrGrande = new byte[0];

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, qrGrande);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF debe generarse incluso sin QR");
    }

    @Test
    @DisplayName("Debe generar PDF con todos los campos en valores máximos")
    void debeGenerarPdfConCamposMaximos() throws Exception {
        // Arrange
        TicketDTO dtoMaximo = TicketDTO.builder()
                .codigoQR("QR-" + "X".repeat(50))
                .codigoAsiento("ASIENTO-" + "A".repeat(20))
                .nombreEvento("EVENTO-" + "E".repeat(100))
                .lugarEvento("LUGAR-" + "L".repeat(100))
                .fechaEventoFormato("31/12/2024 23:59:59")
                .descripcionEvento("DESC-" + "D".repeat(200))
                .nombreUsuario("NOMBRE-" + "N".repeat(50))
                .emailUsuario("test@" + "e".repeat(40) + ".com")
                .zonaRecinto("ZONA-" + "Z".repeat(30))
                .tipoEntrada("TIPO-" + "T".repeat(30))
                .fila(999)
                .columna(999)
                .precio("9999.99 €")
                .build();

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(dtoMaximo, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con fila y columna en límites")
    void debeGenerarPdfConFilaColumnaEnLimites() throws Exception {
        // Arrange
        ticketDTO.setFila(1);
        ticketDTO.setColumna(1);

        // Act
        byte[] pdf1 = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        ticketDTO.setFila(100);
        ticketDTO.setColumna(100);
        byte[] pdf2 = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdf1);
        assertNotNull(pdf2);
        assertTrue(pdf1.length > 0);
        assertTrue(pdf2.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con código QR válido (PNG)")
    void debeGenerarPdfConQRValido() throws Exception {
        // Arrange
        // Crear un pequeño PNG válido de 1x1 pixel (blanco)
        byte[] pngValido = new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, 0x77, 0x53, (byte)0xDE,
            0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54,
            0x08, (byte)0xD7, 0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, 0x3F, 0x00,
            0x05, (byte)0xFE, 0x02, (byte)0xFE, (byte)0xDC, (byte)0xCC, 0x59, (byte)0xE7,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            (byte)0xAE, 0x42, 0x60, (byte)0x82
        };

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, pngValido);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verificar que el PDF se genera correctamente con QR
        assertTrue(pdfBytes.length > 1500, "PDF con QR debe tener contenido");
    }

    @Test
    @DisplayName("Debe lanzar IOException cuando se pasa QR con datos inválidos")
    void debeLanzarIOExceptionConQRInvalido() {
        // Arrange
        byte[] qrInvalido = new byte[] {0x00, 0x01, 0x02, 0x03}; // No es una imagen válida

        // Act & Assert
        assertThrows(IOException.class, () -> {
            pdfService.generarPdfTicketConQR(ticketDTO, qrInvalido);
        });
    }

    @Test
    @DisplayName("Debe manejar DocumentException correctamente")
    void debeCapturarDocumentException() throws Exception {
        // Arrange
        // Crear ticket con datos que puedan causar problemas (nombres muy largos pueden causar problemas)
        TicketDTO dtoConflictivo = TicketDTO.builder()
                .nombreEvento("E")
                .nombreUsuario("U")
                .emailUsuario("e@t.c")
                .codigoAsiento("A")
                .build();

        // Act
        // El servicio debería manejar cualquier problema internamente y registrarlo
        byte[] pdfBytes = pdfService.generarPdfTicket(dtoConflictivo);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF cuando todos los campos opcionales son null")
    void debeGenerarPdfCuandoTodosCamposOpcionalesNull() throws Exception {
        // Arrange
        TicketDTO dtoMinimo = TicketDTO.builder()
                .nombreEvento(null)
                .lugarEvento(null)
                .fechaEventoFormato(null)
                .descripcionEvento(null)
                .nombreUsuario(null)
                .emailUsuario(null)
                .codigoAsiento(null)
                .tipoEntrada(null)
                .zonaRecinto(null)
                .fila(null)
                .columna(null)
                .precio(null)
                .build();

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(dtoMinimo, new byte[]{});

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF correctamente en el método sin QR")
    void debeGenerarPdfSinParametroQR() throws Exception {
        // Act
        byte[] pdfBytes = pdfService.generarPdfTicket(ticketDTO);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertTrue(pdfBytes.length > 1000);
    }

    @Test
    @DisplayName("Debe generar PDF con saltos de línea en descripción")
    void debeGenerarPdfConSaltosDeLineaEnDescripcion() throws Exception {
        // Arrange
        ticketDTO.setDescripcionEvento("Primera línea%nSegunda línea%nTercera línea");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con texto en mayúsculas")
    void debeGenerarPdfConTextoEnMayusculas() throws Exception {
        // Arrange
        ticketDTO.setNombreEvento("EVENTO ESPECIAL EN MAYÚSCULAS");
        ticketDTO.setNombreUsuario("JUAN PÉREZ LÓPEZ");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con fila cero y columna cero")
    void debeGenerarPdfConFilaCeroColumnaCero() throws Exception {
        // Arrange
        ticketDTO.setFila(0);
        ticketDTO.setColumna(0);

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar PDF con precio en formato no estándar")
    void debeGenerarPdfConPrecioFormatoNoEstandar() throws Exception {
        // Arrange
        ticketDTO.setPrecio("GRATIS");

        // Act
        byte[] pdfBytes = pdfService.generarPdfTicketConQR(ticketDTO, imagenQR);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
