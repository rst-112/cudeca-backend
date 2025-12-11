package com.cudeca.controller;

import com.cudeca.dto.TicketDTO;
import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.evento.*;
import com.cudeca.model.negocio.*;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.repository.EntradaEmitidaRepository;
import com.cudeca.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TicketController.
 * Cobertura objetivo: >90%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketController - Tests Unitarios")
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private EntradaEmitidaRepository entradaRepository;

    @InjectMocks
    private TicketController ticketController;

    private EntradaEmitida entradaEmitida;
    private Evento evento;
    private Usuario usuario;
    private Compra compra;
    private ArticuloEntrada articulo;
    private TipoEntrada tipoEntrada;
    private Asiento asiento;
    private ZonaRecinto zona;

    @BeforeEach
    void setUp() {
        // Crear evento
        evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Concierto de Rock");
        evento.setLugar("Auditorio Municipal");
        evento.setFechaInicio(OffsetDateTime.of(2024, 12, 31, 20, 0, 0, 0, ZoneOffset.UTC));
        evento.setDescripcion("Gran concierto de fin de año");

        // Crear usuario
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan@example.com");

        // Crear tipo de entrada
        tipoEntrada = new TipoEntrada();
        tipoEntrada.setId(1L);
        tipoEntrada.setNombre("Entrada VIP");
        tipoEntrada.setEvento(evento);

        // Crear zona
        zona = new ZonaRecinto();
        zona.setId(1L);
        zona.setNombre("Zona Preferente");

        // Crear asiento
        asiento = new Asiento();
        asiento.setId(1L);
        asiento.setFila(5);
        asiento.setColumna(10);
        asiento.setCodigoEtiqueta("A5-10");
        asiento.setZona(zona);

        // Crear compra
        compra = new Compra();
        compra.setId(1L);
        compra.setUsuario(usuario);
        compra.setEmailContacto("juan@example.com");
        compra.setEstado(EstadoCompra.COMPLETADA);

        // Crear artículo
        articulo = new ArticuloEntrada();
        articulo.setId(1L);
        articulo.setCompra(compra);
        articulo.setTipoEntrada(tipoEntrada);
        articulo.setAsiento(asiento);
        articulo.setPrecioUnitario(new BigDecimal("50.00"));

        // Crear entrada emitida
        entradaEmitida = new EntradaEmitida();
        entradaEmitida.setId(1L);
        entradaEmitida.setCodigoQR("TICKET-2024-001-ABC");
        entradaEmitida.setArticuloEntrada(articulo);
    }

    // ==================== TESTS DESCARGAR TICKET PDF ====================

    @Test
    @DisplayName("Descargar PDF - Entrada válida - Devuelve 200 con PDF")
    void testDescargarTicketPDF_EntradaValida() throws Exception {
        // Arrange
        byte[] pdfBytes = "PDF_CONTENT".getBytes();
        
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class))).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(pdfBytes, response.getBody());
        
        // Verificar headers
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().toString()
                .contains("ticket-TICKET-2024-001-ABC.pdf"));
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarTicketPdf(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Descargar PDF - Entrada no encontrada - Devuelve 404")
    void testDescargarTicketPDF_EntradaNoEncontrada() throws Exception {
        // Arrange
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(999L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, never()).generarTicketPdf(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Descargar PDF - Error al generar PDF - Devuelve 500")
    void testDescargarTicketPDF_ErrorGenerarPDF() throws Exception {
        // Arrange
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class)))
                .thenThrow(new Exception("Error generando PDF"));

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarTicketPdf(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Descargar PDF - Usuario invitado sin usuario - Devuelve 200")
    void testDescargarTicketPDF_UsuarioInvitado() throws Exception {
        // Arrange
        compra.setUsuario(null);
        compra.setEmailContacto("invitado@example.com");
        byte[] pdfBytes = "PDF_CONTENT".getBytes();
        
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class))).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarTicketPdf(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Descargar PDF - Entrada sin asiento (General) - Devuelve 200")
    void testDescargarTicketPDF_EntradaGeneral() throws Exception {
        // Arrange
        articulo.setAsiento(null);
        byte[] pdfBytes = "PDF_CONTENT".getBytes();
        
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class))).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarTicketPdf(any(TicketDTO.class));
    }

    // ==================== TESTS REENVIAR EMAIL ====================

    @Test
    @DisplayName("Reenviar email - Entrada válida - Devuelve 200")
    void testReenviarEmail_EntradaValida() {
        // Arrange
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarYEnviarTicket(any(TicketDTO.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = ticketController.reenviarEmail(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Correo reenviado correctamente", response.getBody());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarYEnviarTicket(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Reenviar email - Entrada no encontrada - Devuelve 404")
    void testReenviarEmail_EntradaNoEncontrada() {
        // Arrange
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = ticketController.reenviarEmail(999L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entrada no encontrada", response.getBody());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, never()).generarYEnviarTicket(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Reenviar email - Error al enviar - Devuelve 500")
    void testReenviarEmail_ErrorEnviar() {
        // Arrange
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarYEnviarTicket(any(TicketDTO.class)))
                .thenThrow(new RuntimeException("Error enviando email"));

        // Act
        ResponseEntity<?> response = ticketController.reenviarEmail(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error enviando correo", response.getBody());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarYEnviarTicket(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Reenviar email - Usuario invitado - Devuelve 200")
    void testReenviarEmail_UsuarioInvitado() {
        // Arrange
        compra.setUsuario(null);
        compra.setEmailContacto("invitado@example.com");
        
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarYEnviarTicket(any(TicketDTO.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = ticketController.reenviarEmail(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarYEnviarTicket(any(TicketDTO.class));
    }

    @Test
    @DisplayName("Reenviar email - Entrada sin asiento - Devuelve 200")
    void testReenviarEmail_EntradaGeneral() {
        // Arrange
        articulo.setAsiento(null);
        
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarYEnviarTicket(any(TicketDTO.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = ticketController.reenviarEmail(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(entradaRepository, times(1)).findById(anyLong());
        verify(ticketService, times(1)).generarYEnviarTicket(any(TicketDTO.class));
    }

    // ==================== TESTS MAPEO DTO ====================

    @Test
    @DisplayName("Mapear entrada a DTO - Con todos los datos")
    void testMapearEntradaADTO_ConTodosDatos() throws Exception {
        // Arrange
        byte[] pdfBytes = "PDF".getBytes();
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class))).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verificar que se llamó con un DTO correctamente mapeado
        verify(ticketService).generarTicketPdf(argThat(dto -> 
            dto.getNombreEvento().equals("Concierto de Rock") &&
            dto.getLugarEvento().equals("Auditorio Municipal") &&
            dto.getNombreUsuario().equals("Juan Pérez") &&
            dto.getEmailUsuario().equals("juan@example.com") &&
            dto.getCodigoAsiento().equals("A5-10") &&
            dto.getCodigoQR().equals("TICKET-2024-001-ABC") &&
            dto.getTipoEntrada().equals("Entrada VIP") &&
            dto.getZonaRecinto().equals("Zona Preferente")
        ));
    }

    @Test
    @DisplayName("Mapear entrada a DTO - Sin asiento (General)")
    void testMapearEntradaADTO_SinAsiento() throws Exception {
        // Arrange
        articulo.setAsiento(null);
        byte[] pdfBytes = "PDF".getBytes();
        when(entradaRepository.findById(anyLong())).thenReturn(Optional.of(entradaEmitida));
        when(ticketService.generarTicketPdf(any(TicketDTO.class))).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = ticketController.descargarTicketPDF(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verificar que se mapeó correctamente sin asiento
        verify(ticketService).generarTicketPdf(argThat(dto -> 
            dto.getCodigoAsiento().equals("General") &&
            dto.getZonaRecinto().equals("General") &&
            dto.getFila() == null &&
            dto.getColumna() == null
        ));
    }
}
