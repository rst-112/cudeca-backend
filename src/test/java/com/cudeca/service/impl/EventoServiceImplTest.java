package com.cudeca.service.impl;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.evento.SeatMapLayoutDTO;
import com.cudeca.dto.mapper.EventoMapper;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.enums.EstadoEvento;
import com.cudeca.model.evento.Evento;
import com.cudeca.repository.EventoRepository;
import com.cudeca.service.SeatMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceImplTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private EventoMapper eventoMapper;

    @Mock
    private SeatMapService seatMapService;

    @InjectMocks
    private EventoServiceImpl eventoService;

    @Test
    void getAllEventos_ShouldReturnList() {
        // Arrange
        Evento evento = new Evento();
        evento.setId(1L);
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setId(1L);

        when(eventoRepository.findAll()).thenReturn(Arrays.asList(evento));
        when(eventoMapper.toEventoDTO(evento)).thenReturn(eventoDTO);

        // Act
        List<EventoDTO> result = eventoService.getAllEventos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventoRepository, times(1)).findAll();
    }

    @Test
    void getEventoById_WhenExists_ShouldReturnDto() {
        // Arrange
        Long id = 1L;
        Evento evento = new Evento();
        evento.setId(id);
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setId(id);

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));
        when(eventoMapper.toEventoDTO(evento)).thenReturn(eventoDTO);

        // Act
        EventoDTO result = eventoService.getEventoById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getEventoById_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.getEventoById(id));
    }

    @Test
    void createEvento_ShouldSaveAndReturnDto() {
        // Arrange
        EventCreationRequest request = new EventCreationRequest();
        request.setNombre("Test Event");

        Evento evento = new Evento();
        evento.setNombre("Test Event");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setNombre("Test Event");

        EventoDTO expectedDto = new EventoDTO();
        expectedDto.setId(1L);
        expectedDto.setNombre("Test Event");

        when(eventoMapper.toEvento(request)).thenReturn(evento);
        when(eventoRepository.saveAndFlush(evento)).thenReturn(savedEvento);
        when(eventoMapper.toEventoDTO(savedEvento)).thenReturn(expectedDto);

        // Act
        EventoDTO result = eventoService.createEvento(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Event", result.getNombre());
        verify(eventoRepository, times(1)).saveAndFlush(evento);
    }

    @Test
    void publicarEvento_ShouldChangeStatusToPublicado() {
        // Arrange
        Long id = 1L;
        Evento evento = new Evento();
        evento.setId(id);
        evento.setEstado(EstadoEvento.BORRADOR);

        Evento eventoPublicado = new Evento();
        eventoPublicado.setId(id);
        eventoPublicado.setEstado(EstadoEvento.PUBLICADO);

        EventoDTO dtoPublicado = new EventoDTO();
        dtoPublicado.setEstado(EstadoEvento.PUBLICADO);

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoPublicado);
        when(eventoMapper.toEventoDTO(eventoPublicado)).thenReturn(dtoPublicado);

        // Act
        EventoDTO result = eventoService.publicarEvento(id);

        // Assert
        assertEquals(EstadoEvento.PUBLICADO, result.getEstado());
        verify(eventoRepository).save(evento);
    }

    @Test
    void deleteEvento_WhenExists_ShouldDelete() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.existsById(id)).thenReturn(true);

        // Act
        eventoService.deleteEvento(id);

        // Assert
        verify(eventoRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteEvento_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.deleteEvento(id));
        verify(eventoRepository, never()).deleteById(any());
    }

    @Test
    void createEvento_WithLayout_ShouldSaveLayoutAndReturnDto() {
        // Arrange
        EventCreationRequest request = new EventCreationRequest();
        request.setNombre("Test Event");
        SeatMapLayoutDTO layout = new SeatMapLayoutDTO();
        request.setLayout(layout);

        Evento evento = new Evento();
        evento.setNombre("Test Event");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setNombre("Test Event");

        EventoDTO expectedDto = new EventoDTO();
        expectedDto.setId(1L);
        expectedDto.setNombre("Test Event");

        when(eventoMapper.toEvento(request)).thenReturn(evento);
        when(eventoRepository.saveAndFlush(evento)).thenReturn(savedEvento);
        when(eventoMapper.toEventoDTO(savedEvento)).thenReturn(expectedDto);

        // Act
        EventoDTO result = eventoService.createEvento(request);

        // Assert
        assertNotNull(result);
        verify(seatMapService, times(1)).guardarDiseño(savedEvento, layout);
        verify(eventoRepository, times(1)).saveAndFlush(evento);
    }

    @Test
    void updateEvento_ShouldUpdateAndReturnDto() {
        // Arrange
        Long id = 1L;
        EventCreationRequest request = new EventCreationRequest();
        request.setNombre("Updated Event");

        Evento evento = new Evento();
        evento.setId(id);
        evento.setNombre("Old Event");

        Evento updatedEvento = new Evento();
        updatedEvento.setId(id);
        updatedEvento.setNombre("Updated Event");

        EventoDTO expectedDto = new EventoDTO();
        expectedDto.setId(id);
        expectedDto.setNombre("Updated Event");

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(updatedEvento);
        when(eventoMapper.toEventoDTO(updatedEvento)).thenReturn(expectedDto);

        // Act
        EventoDTO result = eventoService.updateEvento(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Event", result.getNombre());
        verify(eventoMapper, times(1)).updateEventoFromRequest(request, evento);
        verify(eventoRepository, times(1)).save(evento);
    }

    @Test
    void updateEvento_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        EventCreationRequest request = new EventCreationRequest();
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.updateEvento(id, request));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void publicarEvento_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.publicarEvento(id));
    }

    @Test
    void cancelarEvento_ShouldChangeStatusToCancelado() {
        // Arrange
        Long id = 1L;
        Evento evento = new Evento();
        evento.setId(id);
        evento.setEstado(EstadoEvento.PUBLICADO);

        Evento eventoCancelado = new Evento();
        eventoCancelado.setId(id);
        eventoCancelado.setEstado(EstadoEvento.CANCELADO);

        EventoDTO dtoCancelado = new EventoDTO();
        dtoCancelado.setEstado(EstadoEvento.CANCELADO);

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoCancelado);
        when(eventoMapper.toEventoDTO(eventoCancelado)).thenReturn(dtoCancelado);

        // Act
        EventoDTO result = eventoService.cancelarEvento(id);

        // Assert
        assertEquals(EstadoEvento.CANCELADO, result.getEstado());
        verify(eventoRepository).save(evento);
    }

    @Test
    void cancelarEvento_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.cancelarEvento(id));
    }

    @Test
    void finalizarEvento_ShouldChangeStatusToFinalizado() {
        // Arrange
        Long id = 1L;
        Evento evento = new Evento();
        evento.setId(id);
        evento.setEstado(EstadoEvento.PUBLICADO);

        Evento eventoFinalizado = new Evento();
        eventoFinalizado.setId(id);
        eventoFinalizado.setEstado(EstadoEvento.FINALIZADO);

        EventoDTO dtoFinalizado = new EventoDTO();
        dtoFinalizado.setEstado(EstadoEvento.FINALIZADO);

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenReturn(eventoFinalizado);
        when(eventoMapper.toEventoDTO(eventoFinalizado)).thenReturn(dtoFinalizado);

        // Act
        EventoDTO result = eventoService.finalizarEvento(id);

        // Assert
        assertEquals(EstadoEvento.FINALIZADO, result.getEstado());
        verify(eventoRepository).save(evento);
    }

    @Test
    void finalizarEvento_WhenNotExists_ShouldThrowException() {
        // Arrange
        Long id = 1L;
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> eventoService.finalizarEvento(id));
    }
}
