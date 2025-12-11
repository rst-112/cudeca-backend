package com.cudeca.controller;

import com.cudeca.config.JwtAuthFilter;
import com.cudeca.config.SecurityConfig;
import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.service.EventoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = EventoController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventoService eventoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEventos_ShouldReturnOk() throws Exception {
        // Arrange
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setId(1L);
        when(eventoService.getAllEventos()).thenReturn(Arrays.asList(eventoDTO));

        // Act & Assert
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getEventoById_ShouldReturnOk() throws Exception {
        // Arrange
        Long id = 1L;
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setId(id);
        when(eventoService.getEventoById(id)).thenReturn(eventoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/eventos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void createEvento_ShouldReturnCreated() throws Exception {
        // Arrange
        EventCreationRequest request = new EventCreationRequest();
        request.setNombre("New Event");
        request.setFechaInicio(OffsetDateTime.now().plusDays(10));
        request.setLugar("Test Location");
        request.setObjetivoRecaudacion(BigDecimal.valueOf(1000));

        EventoDTO createdDto = new EventoDTO();
        createdDto.setId(1L);
        createdDto.setNombre("New Event");

        when(eventoService.createEvento(any(EventCreationRequest.class))).thenReturn(createdDto);

        // Act & Assert
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateEvento_ShouldReturnOk() throws Exception {
        // Arrange
        Long id = 1L;
        EventCreationRequest request = new EventCreationRequest();
        request.setNombre("Updated Event");
        request.setFechaInicio(OffsetDateTime.now().plusDays(15));
        request.setLugar("Updated Location");
        request.setObjetivoRecaudacion(BigDecimal.valueOf(2000));

        EventoDTO updatedDto = new EventoDTO();
        updatedDto.setId(id);
        updatedDto.setNombre("Updated Event");

        when(eventoService.updateEvento(eq(id), any(EventCreationRequest.class))).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(put("/api/eventos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Updated Event"));
    }

    @Test
    void deleteEvento_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/eventos/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicarEvento_ShouldReturnOk() throws Exception {
        // Arrange
        Long id = 1L;
        EventoDTO publishedDto = new EventoDTO();
        publishedDto.setId(id);

        when(eventoService.publicarEvento(id)).thenReturn(publishedDto);

        // Act & Assert
        mockMvc.perform(patch("/api/eventos/{id}/publicar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void cancelarEvento_ShouldReturnOk() throws Exception {
        // Arrange
        Long id = 1L;
        EventoDTO canceledDto = new EventoDTO();
        canceledDto.setId(id);

        when(eventoService.cancelarEvento(id)).thenReturn(canceledDto);

        // Act & Assert
        mockMvc.perform(patch("/api/eventos/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void finalizarEvento_ShouldReturnOk() throws Exception {
        // Arrange
        Long id = 1L;
        EventoDTO finalizedDto = new EventoDTO();
        finalizedDto.setId(id);

        when(eventoService.finalizarEvento(id)).thenReturn(finalizedDto);

        // Act & Assert
        mockMvc.perform(patch("/api/eventos/{id}/finalizar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }
}
