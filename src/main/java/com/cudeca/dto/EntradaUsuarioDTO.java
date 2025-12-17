package com.cudeca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaUsuarioDTO {
    private Long id;
    private String eventoNombre;
    private String fechaEvento;
    private String asientoNumero;
    private String estadoEntrada;
    private String codigoQR;
    private String fechaEmision;
}