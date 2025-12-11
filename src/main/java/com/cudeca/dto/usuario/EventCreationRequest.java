package com.cudeca.dto.usuario;

import com.cudeca.dto.evento.SeatMapLayoutDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreationRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;

    @Size(max = 2000)
    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaFin;

    @NotBlank(message = "El lugar es obligatorio")
    private String lugar;

    @PositiveOrZero(message = "El objetivo de recaudación no puede ser negativo")
    private BigDecimal objetivoRecaudacion;

    private String imagenUrl;

    @Valid
    private SeatMapLayoutDTO layout;
}