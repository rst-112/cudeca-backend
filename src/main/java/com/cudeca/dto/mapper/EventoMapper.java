package com.cudeca.dto.mapper;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.model.enums.EstadoEvento;
import com.cudeca.model.evento.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public EventoDTO toEventoDTO(Evento evento) {
        if (evento == null) {
            return null;
        }
        return new EventoDTO(
                evento.getId(),
                evento.getNombre(),
                evento.getFechaInicio(),
                evento.getLugar(),
                evento.getEstado(),
                evento.getImagenUrl()
        );
    }

    public Evento toEvento(EventCreationRequest request) {
        if (request == null) {
            return null;
        }
        return Evento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .lugar(request.getLugar())
                .objetivoRecaudacion(request.getObjetivoRecaudacion())
                .imagenUrl(request.getImagenUrl())
                .estado(EstadoEvento.BORRADOR) // Estado por defecto al crear
                .build();
    }

    public void updateEventoFromRequest(EventCreationRequest request, Evento evento) {
        if (request == null || evento == null) {
            return;
        }
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setFechaInicio(request.getFechaInicio());
        evento.setFechaFin(request.getFechaFin());
        evento.setLugar(request.getLugar());
        evento.setObjetivoRecaudacion(request.getObjetivoRecaudacion());
        evento.setImagenUrl(request.getImagenUrl());
    }
}
