package com.cudeca.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreationRequest {

    private String nombre;
    private String descripcion;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFin;
    private String lugar;
    private BigDecimal objetivoRecaudacion;
    private String imagenUrl;

}
