package com.cudeca.service;

import com.cudeca.dto.evento.SeatMapLayoutDTO;
import com.cudeca.model.evento.Evento;

public interface SeatMapService {
    void guardarDiseño(Evento evento, SeatMapLayoutDTO layout);
}