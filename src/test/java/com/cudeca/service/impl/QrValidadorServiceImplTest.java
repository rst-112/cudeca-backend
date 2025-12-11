package com.cudeca.service.impl;

import com.cudeca.dto.QrValidacionDTO;
import com.cudeca.dto.QrValidacionResponseDTO;
import com.cudeca.model.enums.EstadoEntrada;
import com.cudeca.model.negocio.EntradaEmitida;
import com.cudeca.model.negocio.ValidacionEntrada;
import com.cudeca.repository.EntradaEmitidaRepository;
import com.cudeca.repository.ValidacionEntradaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para QrValidadorServiceImpl.
 * Cobertura esperada: >95%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para QrValidadorServiceImpl")
class QrValidadorServiceImplTest {

    @Mock
    private EntradaEmitidaRepository entradaEmitidaRepository;

    @Mock
    private ValidacionEntradaRepository validacionEntradaRepository;

    @InjectMocks
    private QrValidadorServiceImpl qrValidadorService;

    private QrValidacionDTO qrValidacionDTO;
    private EntradaEmitida entradaValida;
    private String codigoQR;
    private String dispositivoId;

    @BeforeEach
    void setUp() {
        codigoQR = "QR-TEST-123456";
        dispositivoId = "DEVICE-001";

        qrValidacionDTO = QrValidacionDTO.builder()
                .codigoQR(codigoQR)
                .dispositivoId(dispositivoId)
                .build();

        entradaValida = EntradaEmitida.builder()
                .id(1L)
                .codigoQR(codigoQR)
                .estado(EstadoEntrada.VALIDA)
                .build();
    }

    @Test
    @DisplayName("Debe validar entrada VALIDA exitosamente y cambiar a USADA")
    void debeValidarEntradaValidaExitosamente() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));
        when(entradaEmitidaRepository.save(any(EntradaEmitida.class))).thenReturn(entradaValida);
        when(validacionEntradaRepository.save(any(ValidacionEntrada.class))).thenReturn(new ValidacionEntrada());

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals("OK", response.getEstado());
        assertEquals("Entrada validada exitosamente. Estado cambiado a USADA.", response.getMensaje());
        assertEquals(1L, response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertEquals("VALIDA", response.getEstadoAnterior());
        assertEquals("USADA", response.getEstadoActual());
        assertNotNull(response.getTimestamp());

        // Verificar que se guardó la entrada con estado USADA
        ArgumentCaptor<EntradaEmitida> entradaCaptor = ArgumentCaptor.forClass(EntradaEmitida.class);
        verify(entradaEmitidaRepository).save(entradaCaptor.capture());
        assertEquals(EstadoEntrada.USADA, entradaCaptor.getValue().getEstado());

        // Verificar que se guardó la validación
        ArgumentCaptor<ValidacionEntrada> validacionCaptor = ArgumentCaptor.forClass(ValidacionEntrada.class);
        verify(validacionEntradaRepository).save(validacionCaptor.capture());
        assertEquals(dispositivoId, validacionCaptor.getValue().getDispositivoId());
    }

    @Test
    @DisplayName("Debe retornar ERROR_NO_ENCONTRADO cuando no existe la entrada")
    void debeRetornarErrorNoEncontrado() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.empty());

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals("ERROR_NO_ENCONTRADO", response.getEstado());
        assertEquals("Código QR no válido. La entrada no existe en el sistema.", response.getMensaje());
        assertNull(response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertNull(response.getEstadoAnterior());
        assertNull(response.getEstadoActual());
        assertNotNull(response.getTimestamp());

        verify(entradaEmitidaRepository, never()).save(any());
        verify(validacionEntradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar ERROR_YA_USADA cuando la entrada ya fue usada")
    void debeRetornarErrorYaUsada() {
        // Arrange
        entradaValida.setEstado(EstadoEntrada.USADA);
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals("ERROR_YA_USADA", response.getEstado());
        assertEquals("Esta entrada ya ha sido utilizada. No puede ser validada de nuevo.", response.getMensaje());
        assertEquals(1L, response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertEquals("USADA", response.getEstadoAnterior());
        assertEquals("USADA", response.getEstadoActual());
        assertNotNull(response.getTimestamp());

        verify(entradaEmitidaRepository, never()).save(any());
        verify(validacionEntradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar ERROR_ANULADA cuando la entrada está anulada")
    void debeRetornarErrorAnulada() {
        // Arrange
        entradaValida.setEstado(EstadoEntrada.ANULADA);
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        assertNotNull(response);
        assertEquals("ERROR_ANULADA", response.getEstado());
        assertEquals("Esta entrada ha sido anulada y no puede ser validada.", response.getMensaje());
        assertEquals(1L, response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertEquals("ANULADA", response.getEstadoAnterior());
        assertEquals("ANULADA", response.getEstadoActual());
        assertNotNull(response.getTimestamp());

        verify(entradaEmitidaRepository, never()).save(any());
        verify(validacionEntradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe consultar entrada existente sin cambiar su estado")
    void debeConsultarEntradaExistente() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));

        // Act
        QrValidacionResponseDTO response = qrValidadorService.consultarEntrada(codigoQR);

        // Assert
        assertNotNull(response);
        assertEquals("ENCONTRADA", response.getEstado());
        assertEquals("Entrada encontrada en el sistema.", response.getMensaje());
        assertEquals(1L, response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertEquals("VALIDA", response.getEstadoActual());
        assertNull(response.getEstadoAnterior());
        assertNotNull(response.getTimestamp());

        verify(entradaEmitidaRepository, never()).save(any());
        verify(validacionEntradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar ERROR_NO_ENCONTRADO al consultar entrada inexistente")
    void debeRetornarErrorNoEncontradoAlConsultar() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.empty());

        // Act
        QrValidacionResponseDTO response = qrValidadorService.consultarEntrada(codigoQR);

        // Assert
        assertNotNull(response);
        assertEquals("ERROR_NO_ENCONTRADO", response.getEstado());
        assertEquals("Código QR no válido. La entrada no existe en el sistema.", response.getMensaje());
        assertNull(response.getEntradaId());
        assertEquals(codigoQR, response.getCodigoQR());
        assertNull(response.getEstadoActual());
        assertNotNull(response.getTimestamp());

        verify(entradaEmitidaRepository, never()).save(any());
        verify(validacionEntradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe consultar entrada USADA sin intentar cambiar su estado")
    void debeConsultarEntradaUsada() {
        // Arrange
        entradaValida.setEstado(EstadoEntrada.USADA);
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));

        // Act
        QrValidacionResponseDTO response = qrValidadorService.consultarEntrada(codigoQR);

        // Assert
        assertNotNull(response);
        assertEquals("ENCONTRADA", response.getEstado());
        assertEquals("USADA", response.getEstadoActual());
        verify(entradaEmitidaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe consultar entrada ANULADA sin intentar cambiar su estado")
    void debeConsultarEntradaAnulada() {
        // Arrange
        entradaValida.setEstado(EstadoEntrada.ANULADA);
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));

        // Act
        QrValidacionResponseDTO response = qrValidadorService.consultarEntrada(codigoQR);

        // Assert
        assertNotNull(response);
        assertEquals("ENCONTRADA", response.getEstado());
        assertEquals("ANULADA", response.getEstadoActual());
        verify(entradaEmitidaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar múltiples entradas secuencialmente")
    void debeValidarMultiplesEntradasSecuencialmente() {
        // Arrange
        EntradaEmitida entrada1 = EntradaEmitida.builder().id(1L).codigoQR("QR-001").estado(EstadoEntrada.VALIDA).build();
        EntradaEmitida entrada2 = EntradaEmitida.builder().id(2L).codigoQR("QR-002").estado(EstadoEntrada.VALIDA).build();

        when(entradaEmitidaRepository.findByCodigoQR("QR-001")).thenReturn(Optional.of(entrada1));
        when(entradaEmitidaRepository.findByCodigoQR("QR-002")).thenReturn(Optional.of(entrada2));
        when(entradaEmitidaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(validacionEntradaRepository.save(any())).thenReturn(new ValidacionEntrada());

        QrValidacionDTO dto1 = QrValidacionDTO.builder().codigoQR("QR-001").dispositivoId(dispositivoId).build();
        QrValidacionDTO dto2 = QrValidacionDTO.builder().codigoQR("QR-002").dispositivoId(dispositivoId).build();

        // Act
        QrValidacionResponseDTO response1 = qrValidadorService.validarCodigoQR(dto1);
        QrValidacionResponseDTO response2 = qrValidadorService.validarCodigoQR(dto2);

        // Assert
        assertEquals("OK", response1.getEstado());
        assertEquals("OK", response2.getEstado());
        verify(entradaEmitidaRepository, times(2)).save(any());
        verify(validacionEntradaRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Debe manejar validación con dispositivo ID nulo")
    void debeManejarValidacionConDispositivoIdNulo() {
        // Arrange
        QrValidacionDTO dtoSinDispositivo = QrValidacionDTO.builder()
                .codigoQR(codigoQR)
                .dispositivoId(null)
                .build();

        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));
        when(entradaEmitidaRepository.save(any())).thenReturn(entradaValida);
        when(validacionEntradaRepository.save(any())).thenReturn(new ValidacionEntrada());

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(dtoSinDispositivo);

        // Assert
        assertEquals("OK", response.getEstado());
        verify(validacionEntradaRepository).save(any());
    }

    @Test
    @DisplayName("Debe incluir timestamp en todas las respuestas")
    void debeIncluirTimestampEnTodasLasRespuestas() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));
        when(entradaEmitidaRepository.save(any())).thenReturn(entradaValida);
        when(validacionEntradaRepository.save(any())).thenReturn(new ValidacionEntrada());

        long tiempoAntes = System.currentTimeMillis();

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        long tiempoDespues = System.currentTimeMillis();

        // Assert
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp() >= tiempoAntes);
        assertTrue(response.getTimestamp() <= tiempoDespues);
    }

    @Test
    @DisplayName("Debe validar entrada y preservar ID correctamente")
    void debeValidarEntradaYPreservarIdCorrectamente() {
        // Arrange
        Long entradaId = 999L;
        entradaValida.setId(entradaId);

        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));
        when(entradaEmitidaRepository.save(any())).thenReturn(entradaValida);
        when(validacionEntradaRepository.save(any())).thenReturn(new ValidacionEntrada());

        // Act
        QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        assertEquals(entradaId, response.getEntradaId());
    }

    @Test
    @DisplayName("Debe crear registro de validación con entrada asociada")
    void debeCrearRegistroValidacionConEntradaAsociada() {
        // Arrange
        when(entradaEmitidaRepository.findByCodigoQR(codigoQR)).thenReturn(Optional.of(entradaValida));
        when(entradaEmitidaRepository.save(any())).thenReturn(entradaValida);
        when(validacionEntradaRepository.save(any())).thenReturn(new ValidacionEntrada());

        // Act
        qrValidadorService.validarCodigoQR(qrValidacionDTO);

        // Assert
        ArgumentCaptor<ValidacionEntrada> captor = ArgumentCaptor.forClass(ValidacionEntrada.class);
        verify(validacionEntradaRepository).save(captor.capture());

        ValidacionEntrada validacion = captor.getValue();
        assertNotNull(validacion.getEntradaEmitida());
        assertEquals(entradaValida.getId(), validacion.getEntradaEmitida().getId());
        assertEquals(dispositivoId, validacion.getDispositivoId());
    }
}
