package com.cudeca.dto.mapper;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.evento.TipoEntradaDTO;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.model.enums.EstadoEvento;
import com.cudeca.model.evento.Evento;
import com.cudeca.model.evento.TipoEntrada;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventoMapper {

    public EventoDTO toEventoDTO(Evento evento) {
        if (evento == null) {
            return null;
        }

        List<TipoEntradaDTO> tiposEntrada = evento.getTiposEntrada() != null
                ? evento.getTiposEntrada().stream()
                .map(this::toTipoEntradaDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new EventoDTO(
                evento.getId(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getFechaInicio(),
                evento.getLugar(),
                evento.getEstado(),
                evento.getImagenUrl(),
                evento.getObjetivoRecaudacion(),
                tiposEntrada);
    }

    private TipoEntradaDTO toTipoEntradaDTO(TipoEntrada tipo) {
        return TipoEntradaDTO.builder()
                .id(tipo.getId())
                .nombre(tipo.getNombre())
                .costeBase(tipo.getCosteBase())
                .donacionImplicita(tipo.getDonacionImplicita())
                .cantidadTotal(tipo.getCantidadTotal())
                .cantidadVendida(tipo.getCantidadVendida())
                .limitePorCompra(tipo.getLimitePorCompra())
                .build();
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
                .estado(EstadoEvento.BORRADOR)
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
