package com.cudeca.service.impl;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.evento.MapaAsientosDTO;
import com.cudeca.dto.mapper.EventoMapper;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.evento.Evento;
import com.cudeca.model.evento.ZonaRecinto;
import com.cudeca.repository.EventoRepository;
import com.cudeca.repository.ZonaRecintoRepository;
import com.cudeca.service.EventoService;
import com.cudeca.service.SeatMapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final SeatMapService seatMapService;
    private final ZonaRecintoRepository zonaRecintoRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventoServiceImpl(EventoRepository eventoRepository,
                             EventoMapper eventoMapper,
                             SeatMapService seatMapService,
                             ZonaRecintoRepository zonaRecintoRepository,
                             ObjectMapper objectMapper) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
        this.seatMapService = seatMapService;
        this.zonaRecintoRepository = zonaRecintoRepository;
        // Guardamos la referencia original, la copia defensiva se hace en el getter si es necesario
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoDTO> getAllEventos() {
        return eventoRepository.findAll().stream()
                .map(eventoMapper::toEventoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventoDTO getEventoById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return eventoMapper.toEventoDTO(evento);
    }

    @Override
    @Transactional
    public EventoDTO createEvento(EventCreationRequest request) {
        Evento evento = eventoMapper.toEvento(request);

        Evento savedEvento = eventoRepository.save(evento);

        if (request.getLayout() != null) {
            seatMapService.guardarDiseño(savedEvento, request.getLayout());
        }

        return eventoMapper.toEventoDTO(savedEvento);
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

    @Override
    @Transactional(readOnly = true)
    public MapaAsientosDTO getMapaAsientos(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento no encontrado con id: " + eventoId);
        }

        List<ZonaRecinto> zonas = zonaRecintoRepository.findByEvento_Id(eventoId);
        if (zonas.isEmpty()) return null;

        // MAPEO MANUAL LEYENDO METADATOS REALES
        List<MapaAsientosDTO.ZonaMapaDTO> zonasDTO = zonas.stream().map(zona -> {

            List<MapaAsientosDTO.AsientoDTO> asientosDTO = zona.getAsientos().stream().map(asiento -> {
                double x = 0;
                double y = 0;
                String forma = "circulo";

                // Intentar leer posición real del JSON guardado por el frontend
                try {
                    if (asiento.getMetadataVisual() != null) {
                        JsonNode node = objectMapper.readTree(asiento.getMetadataVisual());
                        if (node.has("x")) x = node.get("x").asDouble();
                        if (node.has("y")) y = node.get("y").asDouble();
                        if (node.has("forma")) forma = node.get("forma").asText();
                    } else {
                        // Fallback a grid si no hay metadatos (para datos antiguos/mock)
                        x = (asiento.getColumna() != null ? asiento.getColumna() : 1) * 50.0;
                        y = (asiento.getFila() != null ? asiento.getFila() : 1) * 50.0;
                    }
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    // Ignorar error de parseo JSON y usar coordenadas por defecto
                }

                // Calcular precio total
                java.math.BigDecimal precio = asiento.getTipoEntrada() != null
                        ? asiento.getTipoEntrada().getCosteBase().add(asiento.getTipoEntrada().getDonacionImplicita())
                        : java.math.BigDecimal.ZERO;

                return new MapaAsientosDTO.AsientoDTO(
                        asiento.getId().toString(),
                        x, y,
                        asiento.getEstado(),
                        asiento.getCodigoEtiqueta(),
                        asiento.getFila(),
                        asiento.getColumna(),
                        asiento.getTipoEntrada() != null ? asiento.getTipoEntrada().getId() : null,
                        precio,
                        forma
                );
            }).toList();

            return new MapaAsientosDTO.ZonaMapaDTO(
                    zona.getId(),
                    zona.getNombre(),
                    zona.getAforoTotal(),
                    asientosDTO,
                    zona.getObjetosDecorativos()
            );
        }).toList();

        // 800x600 como base, idealmente esto debería guardarse en la entidad Evento o Zona
        return new MapaAsientosDTO(eventoId, 800, 600, zonasDTO);
    }
}
