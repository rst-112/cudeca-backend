package com.cudeca.dto.evento;

import com.cudeca.model.enums.EstadoAsiento;

import java.math.BigDecimal;
import java.util.List;

public record MapaAsientosDTO(
        Long eventoId,
        Integer ancho,
        Integer alto,
        List<ZonaMapaDTO> zonas) {

    public record ZonaMapaDTO(
            Long id,
            String nombre,
            Integer aforoTotal,
            List<AsientoDTO> asientos,
            String objetosDecorativosJson) { // Campo para objetos (escenario, barras)
    }

    public record AsientoDTO(
            String id,
            Double x,
            Double y,
            EstadoAsiento estado,
            String etiqueta,
            Integer fila,
            Integer columna,
            Long tipoEntradaId,
            BigDecimal precio,
            String forma) { // Campo para saber si es círculo, cuadrado, etc.
    }
}