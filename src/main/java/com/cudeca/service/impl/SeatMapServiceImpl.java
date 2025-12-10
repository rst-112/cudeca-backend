package com.cudeca.service.impl;

import com.cudeca.dto.evento.AsientoDTO;
import com.cudeca.dto.evento.SeatMapLayoutDTO;
import com.cudeca.dto.evento.ZonaDTO;
import com.cudeca.exception.ResourceNotFoundException;
import com.cudeca.model.enums.EstadoAsiento;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.Evento;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.model.evento.ZonaRecinto;
import com.cudeca.repository.AsientoRepository;
import com.cudeca.repository.TipoEntradaRepository;
import com.cudeca.repository.ZonaRecintoRepository;
import com.cudeca.service.SeatMapService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Dependencies are Spring-managed beans")
public class SeatMapServiceImpl implements SeatMapService {

    private final ZonaRecintoRepository zonaRepository;
    private final AsientoRepository asientoRepository;
    private final TipoEntradaRepository tipoEntradaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void guardarDiseño(Evento evento, SeatMapLayoutDTO layout) {
        if (layout == null || layout.getZonas() == null) return;

        // 1. Iterar sobre las zonas recibidas
        for (ZonaDTO zonaDto : layout.getZonas()) {
            ZonaRecinto zona = new ZonaRecinto();
            zona.setNombre(zonaDto.getNombre());
            zona.setEvento(evento);
            zona.setAforoTotal(zonaDto.getAsientos() != null ? zonaDto.getAsientos().size() : 0);

            // Guardar objetos decorativos (Escenario, barras) como JSON
            try {
                if (zonaDto.getObjetosDecorativos() != null) {
                    zona.setObjetosDecorativos(objectMapper.writeValueAsString(zonaDto.getObjetosDecorativos()));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error al procesar objetos decorativos", e);
            }

            zona = zonaRepository.save(zona);

            // 2. Procesar asientos de la zona
            if (zonaDto.getAsientos() != null) {
                List<Asiento> listaAsientos = new ArrayList<>();

                for (AsientoDTO asientoDto : zonaDto.getAsientos()) {
                    Asiento asiento = new Asiento();
                    asiento.setCodigoEtiqueta(asientoDto.getEtiqueta());
                    asiento.setFila(asientoDto.getFila());
                    asiento.setColumna(asientoDto.getColumna());
                    asiento.setEstado(EstadoAsiento.LIBRE);
                    asiento.setZona(zona);

                    // Vincular Tipo de Entrada
                    if (asientoDto.getTipoEntradaId() != null) {
                        TipoEntrada tipo = tipoEntradaRepository.findById(asientoDto.getTipoEntradaId())
                                .orElseThrow(() -> new ResourceNotFoundException("TipoEntrada no encontrado: " + asientoDto.getTipoEntradaId()));
                        asiento.setTipoEntrada(tipo);
                    }

                    // 3. Guardar Metadatos Visuales (X, Y, Forma) en el JSONB
                    try {
                        Map<String, Object> visualData = new HashMap<>();
                        visualData.put("x", asientoDto.getX());
                        visualData.put("y", asientoDto.getY());
                        visualData.put("forma", asientoDto.getForma());
                        asiento.setMetadataVisual(objectMapper.writeValueAsString(visualData));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error al procesar metadatos visuales del asiento", e);
                    }

                    listaAsientos.add(asiento);
                }
                // Guardado masivo por rendimiento
                asientoRepository.saveAll(listaAsientos);
            }
        }
    }
}