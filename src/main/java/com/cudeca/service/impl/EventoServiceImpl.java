package com.cudeca.service.impl;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.mapper.EventoMapper;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.evento.Evento;
import com.cudeca.repository.EventoRepository;
import com.cudeca.service.EventoService;
import com.cudeca.service.SeatMapService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final SeatMapService seatMapService;

    @Autowired
    public EventoServiceImpl(EventoRepository eventoRepository,
            EventoMapper eventoMapper,
            SeatMapService seatMapService) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
        this.seatMapService = seatMapService;
    }

    @Override
    public List<EventoDTO> getAllEventos() {
        return eventoRepository.findAll().stream()
                .map(eventoMapper::toEventoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EventoDTO getEventoById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return eventoMapper.toEventoDTO(evento);
    }

    @Override
    @Transactional
    public EventoDTO createEvento(EventCreationRequest request) {
        // 1. Guardar el evento base usando SQL nativo para evitar problema de ENUM
        Evento evento = eventoMapper.toEvento(request);

        // Forzar flush antes de save para evitar problemas
        try {
            Evento savedEvento = eventoRepository.saveAndFlush(evento);

            // 2. Procesar el mapa si viene en la petición
            if (request.getLayout() != null) {
                seatMapService.guardarDiseño(savedEvento, request.getLayout());
            }

            return eventoMapper.toEventoDTO(savedEvento);
        } catch (Exception e) {
            // Si falla, lanzar una excepción más clara
            throw new RuntimeException("Error al crear evento: " + e.getMessage(), e);
        }
    }

    @Override
    public EventoDTO updateEvento(Long eventoId, EventCreationRequest request) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventoId));

        eventoMapper.updateEventoFromRequest(request, evento);
        Evento updatedEvento = eventoRepository.save(evento);
        return eventoMapper.toEventoDTO(updatedEvento);
    }

    @Override
    public void deleteEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + eventoId);
        }
        eventoRepository.deleteById(eventoId);
    }

    @Override
    public EventoDTO publicarEvento(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventoId));

        evento.publicar();
        Evento updatedEvento = eventoRepository.save(evento);
        return eventoMapper.toEventoDTO(updatedEvento);
    }

    @Override
    public EventoDTO cancelarEvento(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventoId));

        evento.cancelar();
        Evento updatedEvento = eventoRepository.save(evento);
        return eventoMapper.toEventoDTO(updatedEvento);
    }

    @Override
    public EventoDTO finalizarEvento(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + eventoId));

        evento.finalizar();
        Evento updatedEvento = eventoRepository.save(evento);
        return eventoMapper.toEventoDTO(updatedEvento);
    }
}
