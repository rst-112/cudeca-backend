package com.cudeca.service.impl;

import com.cudeca.dto.evento.AsientoDTO;
import com.cudeca.dto.evento.SeatMapLayoutDTO;
import com.cudeca.dto.evento.ZonaDTO;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.enums.EstadoAsiento;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.Evento;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.evento.ZonaRecinto;
import com.cudeca.repository.AsientoRepository;
import com.cudeca.repository.TipoEntradaRepository;
import com.cudeca.repository.ZonaRecintoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatMapServiceImplTest {

    @Mock
    private ZonaRecintoRepository zonaRepository;

    @Mock
    private AsientoRepository asientoRepository;

    @Mock
    private TipoEntradaRepository tipoEntradaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SeatMapServiceImpl seatMapService;

    private Evento evento;
    private SeatMapLayoutDTO layout;

    @BeforeEach
    void setUp() {
        evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Test Event");
    }

    @Test
    void guardarDiseño_WithNullLayout_ShouldDoNothing() {
        // Act
        seatMapService.guardarDiseño(evento, null);

        // Assert
        verify(zonaRepository, never()).save(any());
        verify(asientoRepository, never()).saveAll(any());
    }

    @Test
    void guardarDiseño_WithNullZonas_ShouldDoNothing() {
        // Arrange
        layout = new SeatMapLayoutDTO();
        layout.setZonas(null);

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        verify(zonaRepository, never()).save(any());
        verify(asientoRepository, never()).saveAll(any());
    }

    @Test
    void guardarDiseño_WithEmptyZonas_ShouldNotSaveAnything() {
        // Arrange
        layout = new SeatMapLayoutDTO();
        layout.setZonas(new ArrayList<>());

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        verify(zonaRepository, never()).save(any());
        verify(asientoRepository, never()).saveAll(any());
    }

    @Test
    void guardarDiseño_WithZonaWithoutAsientos_ShouldSaveZonaOnly() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("VIP");
        zonaDto.setAsientos(null);
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);
        savedZona.setNombre("VIP");

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        ArgumentCaptor<ZonaRecinto> zonaCaptor = ArgumentCaptor.forClass(ZonaRecinto.class);
        verify(zonaRepository, times(1)).save(zonaCaptor.capture());
        
        ZonaRecinto capturedZona = zonaCaptor.getValue();
        assertEquals("VIP", capturedZona.getNombre());
        assertEquals(0, capturedZona.getAforoTotal());
        assertEquals(evento, capturedZona.getEvento());
        
        verify(asientoRepository, never()).saveAll(any());
    }

    @Test
    void guardarDiseño_WithZonaWithAsientos_ShouldSaveZonaAndAsientos() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("General");
        
        AsientoDTO asientoDto = new AsientoDTO();
        asientoDto.setEtiqueta("A1");
        asientoDto.setFila(1);
        asientoDto.setColumna(1);
        asientoDto.setX(100);
        asientoDto.setY(200);
        asientoDto.setForma("circle");
        asientoDto.setTipoEntradaId(1L);
        
        zonaDto.setAsientos(Arrays.asList(asientoDto));
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);
        savedZona.setNombre("General");

        TipoEntrada tipoEntrada = new TipoEntrada();
        tipoEntrada.setId(1L);
        tipoEntrada.setNombre("General");

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"x\":100.0,\"y\":200.0,\"forma\":\"circle\"}");

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        verify(zonaRepository, times(1)).save(any(ZonaRecinto.class));
        
        ArgumentCaptor<List<Asiento>> asientosCaptor = ArgumentCaptor.forClass(List.class);
        verify(asientoRepository, times(1)).saveAll(asientosCaptor.capture());
        
        List<Asiento> capturedAsientos = asientosCaptor.getValue();
        assertEquals(1, capturedAsientos.size());
        
        Asiento capturedAsiento = capturedAsientos.get(0);
        assertEquals("A1", capturedAsiento.getCodigoEtiqueta());
        assertEquals(1, capturedAsiento.getFila());
        assertEquals(1, capturedAsiento.getColumna());
        assertEquals(EstadoAsiento.LIBRE, capturedAsiento.getEstado());
        assertEquals(savedZona, capturedAsiento.getZona());
        assertEquals(tipoEntrada, capturedAsiento.getTipoEntrada());
    }

    @Test
    void guardarDiseño_WithMultipleAsientos_ShouldSaveAll() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("VIP");
        
        AsientoDTO asiento1 = new AsientoDTO();
        asiento1.setEtiqueta("V1");
        asiento1.setFila(1);
        asiento1.setColumna(1);
        asiento1.setX(100);
        asiento1.setY(100);
        asiento1.setForma("circle");
        asiento1.setTipoEntradaId(1L);
        
        AsientoDTO asiento2 = new AsientoDTO();
        asiento2.setEtiqueta("V2");
        asiento2.setFila(1);
        asiento2.setColumna(2);
        asiento2.setX(150);
        asiento2.setY(100);
        asiento2.setForma("circle");
        asiento2.setTipoEntradaId(1L);
        
        zonaDto.setAsientos(Arrays.asList(asiento1, asiento2));
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);

        TipoEntrada tipoEntrada = new TipoEntrada();
        tipoEntrada.setId(1L);

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);
        when(tipoEntradaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrada));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        ArgumentCaptor<List<Asiento>> asientosCaptor = ArgumentCaptor.forClass(List.class);
        verify(asientoRepository, times(1)).saveAll(asientosCaptor.capture());
        
        List<Asiento> capturedAsientos = asientosCaptor.getValue();
        assertEquals(2, capturedAsientos.size());
        assertEquals("V1", capturedAsientos.get(0).getCodigoEtiqueta());
        assertEquals("V2", capturedAsientos.get(1).getCodigoEtiqueta());
    }

    @Test
    void guardarDiseño_WithTipoEntradaNotFound_ShouldThrowException() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("General");
        
        AsientoDTO asientoDto = new AsientoDTO();
        asientoDto.setEtiqueta("A1");
        asientoDto.setFila(1);
        asientoDto.setColumna(1);
        asientoDto.setX(100);
        asientoDto.setY(200);
        asientoDto.setForma("circle");
        asientoDto.setTipoEntradaId(999L);
        
        zonaDto.setAsientos(Arrays.asList(asientoDto));
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);
        when(tipoEntradaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> seatMapService.guardarDiseño(evento, layout));
    }

    @Test
    void guardarDiseño_WithAsientoWithoutTipoEntrada_ShouldSaveWithoutTipo() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("General");
        
        AsientoDTO asientoDto = new AsientoDTO();
        asientoDto.setEtiqueta("A1");
        asientoDto.setFila(1);
        asientoDto.setColumna(1);
        asientoDto.setX(100);
        asientoDto.setY(200);
        asientoDto.setForma("circle");
        asientoDto.setTipoEntradaId(null);
        
        zonaDto.setAsientos(Arrays.asList(asientoDto));
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        verify(tipoEntradaRepository, never()).findById(any());
        
        ArgumentCaptor<List<Asiento>> asientosCaptor = ArgumentCaptor.forClass(List.class);
        verify(asientoRepository, times(1)).saveAll(asientosCaptor.capture());
        
        List<Asiento> capturedAsientos = asientosCaptor.getValue();
        assertEquals(1, capturedAsientos.size());
        assertNull(capturedAsientos.get(0).getTipoEntrada());
    }

    @Test
    void guardarDiseño_WithObjetosDecorativos_ShouldSaveAsJson() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("Sala Principal");
        
        Map<String, Object> decorativo1 = new HashMap<>();
        decorativo1.put("tipo", "escenario");
        decorativo1.put("x", 500);
        decorativo1.put("y", 100);
        
        Map<String, Object> decorativo2 = new HashMap<>();
        decorativo2.put("tipo", "barra");
        decorativo2.put("x", 50);
        decorativo2.put("y", 50);
        
        List<Object> decorativos = Arrays.asList(decorativo1, decorativo2);
        zonaDto.setObjetosDecorativos(decorativos);
        zonaDto.setAsientos(new ArrayList<>());
        
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);

        String jsonDecorativos = "{\"escenario\":{\"x\":500,\"y\":100},\"barra\":{\"x\":50,\"y\":50}}";
        when(objectMapper.writeValueAsString(decorativos)).thenReturn(jsonDecorativos);
        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        ArgumentCaptor<ZonaRecinto> zonaCaptor = ArgumentCaptor.forClass(ZonaRecinto.class);
        verify(zonaRepository, times(1)).save(zonaCaptor.capture());
        verify(objectMapper, times(1)).writeValueAsString(decorativos);
    }

    @Test
    void guardarDiseño_WhenJsonProcessingExceptionOnDecorativos_ShouldThrowRuntimeException() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("Sala");
        zonaDto.setObjetosDecorativos(Arrays.asList(Map.of("test", "value")));
        
        layout.setZonas(Arrays.asList(zonaDto));

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Error") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> seatMapService.guardarDiseño(evento, layout));
        assertEquals("Error al procesar objetos decorativos", exception.getMessage());
    }

    @Test
    void guardarDiseño_WhenJsonProcessingExceptionOnMetadata_ShouldThrowRuntimeException() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zonaDto = new ZonaDTO();
        zonaDto.setNombre("General");
        
        AsientoDTO asientoDto = new AsientoDTO();
        asientoDto.setEtiqueta("A1");
        asientoDto.setFila(1);
        asientoDto.setColumna(1);
        asientoDto.setX(100);
        asientoDto.setY(200);
        asientoDto.setForma("circle");
        
        zonaDto.setAsientos(Arrays.asList(asientoDto));
        layout.setZonas(Arrays.asList(zonaDto));

        ZonaRecinto savedZona = new ZonaRecinto();
        savedZona.setId(1L);

        when(zonaRepository.save(any(ZonaRecinto.class))).thenReturn(savedZona);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Error") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> seatMapService.guardarDiseño(evento, layout));
        assertEquals("Error al procesar metadatos visuales del asiento", exception.getMessage());
    }

    @Test
    void guardarDiseño_WithMultipleZonas_ShouldSaveAll() throws JsonProcessingException {
        // Arrange
        layout = new SeatMapLayoutDTO();
        
        ZonaDTO zona1 = new ZonaDTO();
        zona1.setNombre("VIP");
        zona1.setAsientos(new ArrayList<>());
        
        ZonaDTO zona2 = new ZonaDTO();
        zona2.setNombre("General");
        zona2.setAsientos(new ArrayList<>());
        
        layout.setZonas(Arrays.asList(zona1, zona2));

        ZonaRecinto savedZona1 = new ZonaRecinto();
        savedZona1.setId(1L);
        
        ZonaRecinto savedZona2 = new ZonaRecinto();
        savedZona2.setId(2L);

        when(zonaRepository.save(any(ZonaRecinto.class)))
            .thenReturn(savedZona1)
            .thenReturn(savedZona2);

        // Act
        seatMapService.guardarDiseño(evento, layout);

        // Assert
        verify(zonaRepository, times(2)).save(any(ZonaRecinto.class));
    }
}
