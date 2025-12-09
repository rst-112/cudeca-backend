package com.cudeca.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
public class UserProfileController {

    @GetMapping("/{usuarioId}/entradas")
    public ResponseEntity<?> getHistorial(@PathVariable Long usuarioId) {
        // Devolvemos una lista falsa de entradas
        return ResponseEntity.ok(List.of(
            Map.of(
                "id", 101, 
                "evento", "Gala Benéfica de Navidad", 
                "fecha", "2025-12-24", 
                "estado", "VALIDA",
                "pdfUrl", "/api/perfil/entradas/101/pdf"
            ),
            Map.of(
                "id", 102, 
                "evento", "Concierto Solidario", 
                "fecha", "2025-08-15", 
                "estado", "USADA",
                "pdfUrl", "/api/perfil/entradas/102/pdf"
            )
        ));
    }

    // Acción: Descargar PDF (Simulado)
    @GetMapping("/entradas/{entradaId}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long entradaId) {
        // Simulamos que devolvemos un enlace de descarga
        return ResponseEntity.ok(Map.of("url", "https://cdn.cudeca.org/simulacion/ticket_" + entradaId + ".pdf"));
    }

    // --- TAB 2: MIS DATOS FISCALES (Simulado) ---
    @GetMapping("/{usuarioId}/fiscal")
    public ResponseEntity<?> getDatosFiscales(@PathVariable Long usuarioId) {
        // Simulamos recuperar datos de la "Libreta de Direcciones"
        return ResponseEntity.ok(Map.of(
            "nombre", "Usuario Pendiente",
            "nif", "", // Vacío para simular que no tiene
            "direccion", "",
            "pais", "España"
        ));
    }

    @PutMapping("/{usuarioId}/fiscal")
    public ResponseEntity<?> updateDatosFiscales(@PathVariable Long usuarioId, @RequestBody Map<String, String> datos) {
        // TODO: Conectar con base de datos real (Tabla DATOS_FISCALES)
        System.out.println("Guardando datos fiscales (Simulado) para usuario " + usuarioId + ": " + datos);
        return ResponseEntity.ok("Datos fiscales actualizados correctamente (Simulado).");
    }

    // --- TAB 3: MONEDERO (Simulado) ---
    @GetMapping("/{usuarioId}/monedero")
    public ResponseEntity<?> getMonedero(@PathVariable Long usuarioId) {
        // Simulamos un saldo
        return ResponseEntity.ok(Map.of(
            "saldo", 0.00, 
            "moneda", "EUR",
            "movimientos", List.of() // Lista vacía
        ));
    }
}
