package com.cudeca.dto.evento;

import lombok.Data;

import java.util.List;

@Data
public class ZonaDTO {
    private String nombre;
    private Integer capacidad;
    private List<AsientoDTO> asientos;
    private List<Object> objetosDecorativos;
}