package com.cudeca.service.impl;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.mapper.EventoMapper;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.enums.EstadoEvento;
import com.cudeca.model.evento.Evento;
import com.cudeca.repository.EventoRepository;
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
        when(eventoRepository.save(evento)).thenReturn(savedEvento);
        when(eventoMapper.toEventoDTO(savedEvento)).thenReturn(expectedDto);

        // Act
        EventoDTO result = eventoService.createEvento(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Event", result.getNombre());
        verify(eventoRepository, times(1)).save(evento);
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
}
