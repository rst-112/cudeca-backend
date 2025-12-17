package com.cudeca.controller;

import com.cudeca.dto.evento.EventoDTO;
import com.cudeca.dto.evento.MapaAsientosDTO;
import com.cudeca.dto.usuario.EventCreationRequest;
import com.cudeca.service.EventoService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    @Autowired
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EventoService is a Spring-managed bean")
    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<List<EventoDTO>> getAllEventos() {
        List<EventoDTO> eventos = eventoService.getAllEventos();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> getEventoById(@PathVariable Long id) {
        EventoDTO evento = eventoService.getEventoById(id);
        return ResponseEntity.ok(evento);
    }

    @PostMapping
    public ResponseEntity<EventoDTO> createEvento(@Valid @RequestBody EventCreationRequest request) {
        EventoDTO createdEvento = eventoService.createEvento(request);
        return new ResponseEntity<>(createdEvento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id,
                                                  @Valid @RequestBody EventCreationRequest request) {
        EventoDTO updatedEvento = eventoService.updateEvento(id, request);
        return ResponseEntity.ok(updatedEvento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        eventoService.deleteEvento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publicar")
    public ResponseEntity<EventoDTO> publicarEvento(@PathVariable Long id) {
        EventoDTO updatedEvento = eventoService.publicarEvento(id);
        return ResponseEntity.ok(updatedEvento);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EventoDTO> cancelarEvento(@PathVariable Long id) {
        EventoDTO updatedEvento = eventoService.cancelarEvento(id);
        return ResponseEntity.ok(updatedEvento);
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<EventoDTO> finalizarEvento(@PathVariable Long id) {
        EventoDTO updatedEvento = eventoService.finalizarEvento(id);
        return ResponseEntity.ok(updatedEvento);
    }

    @GetMapping("/{id}/mapa-asientos")
    public ResponseEntity<MapaAsientosDTO> getMapaAsientos(@PathVariable Long id) {
        // Eliminamos el try-catch. Si hay un error, que salte la excepción y el
        // GlobalExceptionHandler se encargue.
        MapaAsientosDTO mapaAsientos = eventoService.getMapaAsientos(id);

        if (mapaAsientos == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapaAsientos);
    }
}
