package com.cudeca.service.impl;

import com.cudeca.dto.TicketDTO;
import com.cudeca.service.EmailService;
import com.cudeca.service.PdfService;
import com.cudeca.service.QrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para TicketServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para TicketServiceImpl")
class TicketServiceImplTest {

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private PdfService pdfService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private TicketDTO ticketDTO;
    private byte[] imagenQR;
    private byte[] pdfBytes;

    @BeforeEach
    void setUp() {
        ticketDTO = TicketDTO.builder()
                .codigoQR("QR-TEST-123")
                .nombreEvento("Concierto CUDECA 2024")
                .emailUsuario("usuario@test.com")
                .nombreUsuario("Juan Pérez")
                .lugarEvento("Auditorio Principal")
                .fechaEventoFormato("15/12/2024 20:00")
                .codigoAsiento("A1")
                .build();

        imagenQR = new byte[]{1, 2, 3, 4, 5};
        pdfBytes = new byte[]{10, 20, 30, 40, 50};
    }

    @Test
    @DisplayName("Debe generar y enviar ticket exitosamente")
    void debeGenerarYEnviarTicketExitosamente() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR))).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(
                anyString(), anyString(), anyString(), any(byte[].class), anyString()
        );

        // Act
        boolean resultado = ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        assertTrue(resultado);
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService).generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR));
        verify(emailService).enviarCorreoConAdjunto(
                eq(ticketDTO.getEmailUsuario()),
                eq("🎟️ Tu entrada para: Concierto CUDECA 2024"),
                argThat(html -> html != null && html.contains("Hola <strong>Juan Pérez</strong>")),
                eq(pdfBytes),
                eq("Entrada_QR-TEST-123.pdf")
        );
    }

    @Test
    @DisplayName("Debe retornar false cuando falla la generación del QR")
    void debeRetornarFalseCuandoFallaGeneracionQR() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenThrow(new RuntimeException("Error QR"));

        // Act
        boolean resultado = ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        assertFalse(resultado);
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService, never()).generarPdfTicketConQR(any(), any());
        verify(emailService, never()).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debe retornar false cuando falla la generación del PDF")
    void debeRetornarFalseCuandoFallaGeneracionPDF() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenThrow(new RuntimeException("Error PDF"));

        // Act
        boolean resultado = ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        assertFalse(resultado);
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService).generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR));
        verify(emailService, never()).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debe retornar false cuando falla el envío del email")
    void debeRetornarFalseCuandoFallaEnvioEmail() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doThrow(new RuntimeException("Error Email")).when(emailService).enviarCorreoConAdjunto(
                anyString(), anyString(), anyString(), any(byte[].class), anyString()
        );

        // Act
        boolean resultado = ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        assertFalse(resultado);
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService).generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR));
        verify(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debe generar ticket PDF sin enviar email")
    void debeGenerarTicketPdfSinEnviarEmail() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(ticketDTO.getCodigoQR())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR))).thenReturn(pdfBytes);

        // Act
        byte[] resultado = ticketService.generarTicketPdf(ticketDTO);

        // Assert
        assertNotNull(resultado);
        assertArrayEquals(pdfBytes, resultado);
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService).generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR));
        verify(emailService, never()).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando generarTicketPdf falla en QR")
    void debeLanzarExcepcionCuandoGenerarTicketPdfFallaEnQR() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenThrow(new RuntimeException("Error QR"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            ticketService.generarTicketPdf(ticketDTO);
        });
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService, never()).generarPdfTicketConQR(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando generarTicketPdf falla en PDF")
    void debeLanzarExcepcionCuandoGenerarTicketPdfFallaEnPDF() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenThrow(new RuntimeException("Error PDF"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            ticketService.generarTicketPdf(ticketDTO);
        });
        verify(qrCodeService).generarCodigoQR(ticketDTO.getCodigoQR());
        verify(pdfService).generarPdfTicketConQR(eq(ticketDTO), eq(imagenQR));
    }

    @Test
    @DisplayName("Debe generar HTML básico correctamente")
    void debeGenerarHtmlBasicoCorrectamente() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());

        // Act
        ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        verify(emailService).enviarCorreoConAdjunto(
                anyString(),
                anyString(),
                argThat(html -> html.contains("Hola <strong>Juan Pérez</strong>") &&
                        html.contains("Concierto CUDECA 2024") &&
                        html.contains("Gracias por tu colaboración")),
                any(),
                anyString()
        );
    }

    @Test
    @DisplayName("Debe manejar TicketDTO con valores nulos")
    void debeManejarTicketDTOConValoresNulos() throws Exception {
        // Arrange
        TicketDTO dtoIncompleto = TicketDTO.builder()
                .codigoQR("QR-NULL-TEST")
                .nombreEvento(null)
                .nombreUsuario(null)
                .emailUsuario("test@test.com")
                .build();

        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());

        // Act
        boolean resultado = ticketService.generarYEnviarTicket(dtoIncompleto);

        // Assert
        assertTrue(resultado);
        verify(emailService).enviarCorreoConAdjunto(
                eq("test@test.com"),
                eq("🎟️ Tu entrada para: null"),
                anyString(),
                eq(pdfBytes),
                eq("Entrada_QR-NULL-TEST.pdf")
        );
    }

    @Test
    @DisplayName("Debe generar asunto de email correctamente")
    void debeGenerarAsuntoEmailCorrectamente() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());

        // Act
        ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        verify(emailService).enviarCorreoConAdjunto(
                anyString(),
                eq("🎟️ Tu entrada para: Concierto CUDECA 2024"),
                anyString(),
                any(),
                anyString()
        );
    }

    @Test
    @DisplayName("Debe usar nombre de archivo correcto para PDF")
    void debeUsarNombreArchivoCorrectoPDF() throws Exception {
        // Arrange
        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());

        // Act
        ticketService.generarYEnviarTicket(ticketDTO);

        // Assert
        verify(emailService).enviarCorreoConAdjunto(
                anyString(),
                anyString(),
                anyString(),
                any(),
                eq("Entrada_QR-TEST-123.pdf")
        );
    }

    @Test
    @DisplayName("Debe procesar múltiples tickets secuencialmente")
    void debeProcesarMultiplesTicketsSecuencialmente() throws Exception {
        // Arrange
        TicketDTO ticket1 = TicketDTO.builder().codigoQR("QR-001").nombreEvento("Evento 1").emailUsuario("user1@test.com").nombreUsuario("User 1").build();
        TicketDTO ticket2 = TicketDTO.builder().codigoQR("QR-002").nombreEvento("Evento 2").emailUsuario("user2@test.com").nombreUsuario("User 2").build();

        when(qrCodeService.generarCodigoQR(anyString())).thenReturn(imagenQR);
        when(pdfService.generarPdfTicketConQR(any(), any())).thenReturn(pdfBytes);
        doNothing().when(emailService).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());

        // Act
        boolean resultado1 = ticketService.generarYEnviarTicket(ticket1);
        boolean resultado2 = ticketService.generarYEnviarTicket(ticket2);

        // Assert
        assertTrue(resultado1);
        assertTrue(resultado2);
        verify(qrCodeService, times(2)).generarCodigoQR(anyString());
        verify(pdfService, times(2)).generarPdfTicketConQR(any(), any());
        verify(emailService, times(2)).enviarCorreoConAdjunto(anyString(), anyString(), anyString(), any(), anyString());
    }
}
