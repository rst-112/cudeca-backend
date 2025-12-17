package com.cudeca.dto.evento;

import com.cudeca.model.evento.Asiento;
import com.cudeca.model.enums.EstadoAsiento;
import com.cudeca.model.evento.ZonaRecinto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para representar el mapa completo de asientos de un evento
 */
public record MapaAsientosDTO(
        Long eventoId,
        Integer ancho,
        Integer alto,
        List<ZonaMapaDTO> zonas) {

    /**
     * DTO para una zona del mapa
     */
    public record ZonaMapaDTO(
            Long id,
            String nombre,
            Integer aforoTotal,
            List<AsientoDTO> asientos) {
    }

    /**
     * DTO para un asiento individual
     */
    public record AsientoDTO(
            String id, // Usamos String para compatibilidad con frontend
            Double x, // Coordenada X (se calcula automáticamente)
            Double y, // Coordenada Y (se calcula automáticamente)
            EstadoAsiento estado,
            String etiqueta,
            Integer fila,
            Integer columna,
            Long tipoEntradaId,
            BigDecimal precio) {
    }

    /**
     * Crea un MapaAsientosDTO a partir de las zonas de un evento
     */
    public static MapaAsientosDTO fromZonas(Long eventoId, List<ZonaRecinto> zonas) {
        List<ZonaMapaDTO> zonasDTO = zonas.stream()
                .map(zona -> {
                    List<AsientoDTO> asientosDTO = zona.getAsientos().stream()
                            .map(asiento -> new AsientoDTO(
                                    asiento.getId().toString(),
                                    calcularX(asiento),
                                    calcularY(asiento),
                                    asiento.getEstado(),
                                    asiento.getCodigoEtiqueta(),
                                    asiento.getFila(),
                                    asiento.getColumna(),
                                    asiento.getTipoEntrada() != null ? asiento.getTipoEntrada().getId() : null,
                                    asiento.getTipoEntrada() != null ? asiento.getTipoEntrada().getCosteBase()
                                            .add(asiento.getTipoEntrada().getDonacionImplicita()) : BigDecimal.ZERO))
                            .toList();

                    return new ZonaMapaDTO(
                            zona.getId(),
                            zona.getNombre(),
                            zona.getAforoTotal(),
                            asientosDTO);
                })
                .toList();

        return new MapaAsientosDTO(eventoId, 800, 600, zonasDTO);
    }

    /**
     * Calcula la coordenada X basándose en fila y columna
     */
    private static double calcularX(Asiento asiento) {
        if (asiento.getColumna() == null)
            return 100;
        // Espaciado horizontal: 60px entre asientos, empezando en x=100
        return 100 + (asiento.getColumna() - 1) * 60;
    }

    /**
     * Calcula la coordenada Y basándose en fila y columna
     */
    private static double calcularY(Asiento asiento) {
        if (asiento.getFila() == null)
            return 200;
        // Espaciado vertical: 50px entre filas, empezando en y=200
        return 200 + (asiento.getFila() - 1) * 50;
    }
}
