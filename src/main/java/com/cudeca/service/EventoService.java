package com.cudeca.service;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.usuario.EventCreationRequest;

import java.util.List;

public interface EventoService {
    List<EventoDTO> getAllEventos();
    EventoDTO getEventoById(Long id);
    EventoDTO createEvento(EventCreationRequest request);
    EventoDTO updateEvento(Long eventoId, EventCreationRequest request);
    void deleteEvento(Long eventoId);
    EventoDTO publicarEvento(Long eventoId);
    EventoDTO cancelarEvento(Long eventoId);
    EventoDTO finalizarEvento(Long eventoId);
}
