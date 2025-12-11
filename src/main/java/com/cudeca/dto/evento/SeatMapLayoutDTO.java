package com.cudeca.dto.evento;

import lombok.Data;

import java.util.List;

@Data
public class SeatMapLayoutDTO {
    private Integer ancho;
    private Integer alto;
    private List<ZonaDTO> zonas;
}