package com.cudeca.controller;

import com.cudeca.dto.QrValidacionDTO;
import com.cudeca.dto.QrValidacionResponseDTO;
import com.cudeca.service.QrValidadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para QrValidadorController.
 * Cobertura objetivo: >90%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QrValidadorController - Tests Unitarios")
class QrValidadorControllerTest {

    @Mock
    private QrValidadorService qrValidadorService;

    @InjectMocks
    private QrValidadorController qrValidadorController;

    private QrValidacionDTO validacionDTO;
    private QrValidacionResponseDTO responseOK;
    private QrValidacionResponseDTO responseNoEncontrado;
    private QrValidacionResponseDTO responseYaUsada;
    private QrValidacionResponseDTO responseAnulada;

    @BeforeEach
    void setUp() {
        validacionDTO = QrValidacionDTO.builder()
                .codigoQR("TICKET-2024-001")
                .dispositivoId("DEVICE-SCANNER-001")
                .build();

        responseOK = QrValidacionResponseDTO.builder()
                .estado("OK")
                .mensaje("Entrada validada exitosamente")
                .entradaId(1L)
                .codigoQR("TICKET-2024-001")
                .estadoAnterior("VALIDA")
                .estadoActual("USADA")
                .timestamp(System.currentTimeMillis())
                .build();

        responseNoEncontrado = QrValidacionResponseDTO.builder()
                .estado("ERROR_NO_ENCONTRADO")
                .mensaje("Código QR no válido")
                .codigoQR("TICKET-2024-001")
                .timestamp(System.currentTimeMillis())
                .build();

        responseYaUsada = QrValidacionResponseDTO.builder()
                .estado("ERROR_YA_USADA")
                .mensaje("Esta entrada ya ha sido utilizada")
                .entradaId(1L)
                .codigoQR("TICKET-2024-001")
                .estadoAnterior("USADA")
                .estadoActual("USADA")
                .timestamp(System.currentTimeMillis())
                .build();

        responseAnulada = QrValidacionResponseDTO.builder()
                .estado("ERROR_ANULADA")
                .mensaje("Esta entrada ha sido anulada")
                .entradaId(1L)
                .codigoQR("TICKET-2024-001")
                .estadoAnterior("ANULADA")
                .estadoActual("ANULADA")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ==================== TESTS VALIDAR CÓDIGO QR ====================

    @Test
    @DisplayName("Validar código QR - OK - Devuelve 200")
    void testValidarCodigoQR_OK() {
        // Arrange
        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenReturn(responseOK);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getEstado());
        assertEquals("Entrada validada exitosamente", response.getBody().getMensaje());
        assertEquals(1L, response.getBody().getEntradaId());

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    @Test
    @DisplayName("Validar código QR - ERROR_NO_ENCONTRADO - Devuelve 400")
    void testValidarCodigoQR_NoEncontrado() {
        // Arrange
        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenReturn(responseNoEncontrado);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_NO_ENCONTRADO", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    @Test
    @DisplayName("Validar código QR - ERROR_YA_USADA - Devuelve 400")
    void testValidarCodigoQR_YaUsada() {
        // Arrange
        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenReturn(responseYaUsada);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_YA_USADA", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    @Test
    @DisplayName("Validar código QR - ERROR_ANULADA - Devuelve 400")
    void testValidarCodigoQR_Anulada() {
        // Arrange
        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenReturn(responseAnulada);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_ANULADA", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    @Test
    @DisplayName("Validar código QR - Estado desconocido - Devuelve 200")
    void testValidarCodigoQR_EstadoDesconocido() {
        // Arrange
        QrValidacionResponseDTO responseDesconocido = QrValidacionResponseDTO.builder()
                .estado("ESTADO_DESCONOCIDO")
                .mensaje("Estado no reconocido")
                .codigoQR("TICKET-2024-001")
                .timestamp(System.currentTimeMillis())
                .build();

        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenReturn(responseDesconocido);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ESTADO_DESCONOCIDO", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    @Test
    @DisplayName("Validar código QR - Exception del servicio - Devuelve 500")
    void testValidarCodigoQR_ExceptionEnServicio() {
        // Arrange
        when(qrValidadorService.validarCodigoQR(any(QrValidacionDTO.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.validarCodigoQR(validacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_SERVIDOR", response.getBody().getEstado());
        assertTrue(response.getBody().getMensaje().contains("Error inesperado"));

        verify(qrValidadorService, times(1)).validarCodigoQR(any(QrValidacionDTO.class));
    }

    // ==================== TESTS CONSULTAR ENTRADA ====================

    @Test
    @DisplayName("Consultar entrada - ENCONTRADA - Devuelve 200")
    void testConsultarEntrada_Encontrada() {
        // Arrange
        QrValidacionResponseDTO responseEncontrada = QrValidacionResponseDTO.builder()
                .estado("ENCONTRADA")
                .mensaje("Entrada encontrada en el sistema")
                .entradaId(1L)
                .codigoQR("TICKET-2024-001")
                .estadoActual("VALIDA")
                .timestamp(System.currentTimeMillis())
                .build();

        when(qrValidadorService.consultarEntrada(anyString()))
                .thenReturn(responseEncontrada);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.consultarEntrada("TICKET-2024-001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ENCONTRADA", response.getBody().getEstado());
        assertEquals("Entrada encontrada en el sistema", response.getBody().getMensaje());
        assertEquals(1L, response.getBody().getEntradaId());

        verify(qrValidadorService, times(1)).consultarEntrada(anyString());
    }

    @Test
    @DisplayName("Consultar entrada - No encontrada - Devuelve 400")
    void testConsultarEntrada_NoEncontrada() {
        // Arrange
        when(qrValidadorService.consultarEntrada(anyString()))
                .thenReturn(responseNoEncontrado);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.consultarEntrada("TICKET-INVALIDO");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_NO_ENCONTRADO", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).consultarEntrada(anyString());
    }

    @Test
    @DisplayName("Consultar entrada - Exception del servicio - Devuelve 500")
    void testConsultarEntrada_ExceptionEnServicio() {
        // Arrange
        when(qrValidadorService.consultarEntrada(anyString()))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.consultarEntrada("TICKET-2024-001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR_SERVIDOR", response.getBody().getEstado());
        assertTrue(response.getBody().getMensaje().contains("Error inesperado"));
        assertEquals("TICKET-2024-001", response.getBody().getCodigoQR());

        verify(qrValidadorService, times(1)).consultarEntrada(anyString());
    }

    @Test
    @DisplayName("Consultar entrada - Estado diferente a ENCONTRADA - Devuelve 400")
    void testConsultarEntrada_EstadoDiferente() {
        // Arrange
        QrValidacionResponseDTO responseOtro = QrValidacionResponseDTO.builder()
                .estado("OTRO_ESTADO")
                .mensaje("Otro estado")
                .codigoQR("TICKET-2024-001")
                .timestamp(System.currentTimeMillis())
                .build();

        when(qrValidadorService.consultarEntrada(anyString()))
                .thenReturn(responseOtro);

        // Act
        ResponseEntity<QrValidacionResponseDTO> response = qrValidadorController.consultarEntrada("TICKET-2024-001");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OTRO_ESTADO", response.getBody().getEstado());

        verify(qrValidadorService, times(1)).consultarEntrada(anyString());
    }
}
