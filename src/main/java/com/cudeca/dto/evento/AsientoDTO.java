package com.cudeca.dto.evento;

import lombok.Data;

@Data
public class AsientoDTO {
    private String etiqueta;
    private Integer fila;
    private Integer columna;

    private Integer x;
    private Integer y;
    private String forma;

    private Long tipoEntradaId;
}