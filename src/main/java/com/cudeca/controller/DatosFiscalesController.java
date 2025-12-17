package com.cudeca.controller;

import com.cudeca.dto.DatosFiscalesDTO;
import com.cudeca.service.DatosFiscalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil/{usuarioId}/datos-fiscales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DatosFiscalesController {

    private final DatosFiscalesService service;

    @GetMapping
    public ResponseEntity<List<DatosFiscalesDTO>> listar(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerDatosFiscalesPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long usuarioId,
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.obtenerPorId(id, usuarioId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @PathVariable Long usuarioId,
            @Valid @RequestBody DatosFiscalesDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.crearDatosFiscales(usuarioId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long usuarioId,
            @PathVariable Long id,
            @Valid @RequestBody DatosFiscalesDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(service.actualizarDatosFiscales(id, usuarioId, dto));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long usuarioId,
            @PathVariable Long id) {
        try {
            service.eliminarDatosFiscales(id, usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Endpoint auxiliar para validar NIF desde el front si lo necesitas
    @PostMapping("/validar-nif")
    public ResponseEntity<?> validarNif(@RequestBody Map<String, String> payload) {
        boolean esValido = service.validarNIF(payload.get("nif"));
        return ResponseEntity.ok(Map.of("valido", esValido));
    }
}