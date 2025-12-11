package com.cudeca.dto.evento;

import com.cudeca.model.enums.EstadoEvento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    private Long id;
    private String nombre;
    private OffsetDateTime fechaInicio;
    private String lugar;
    private EstadoEvento estado;
    private String imagenUrl;
}
