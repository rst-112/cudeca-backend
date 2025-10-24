package com.cudeca.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint público de prueba para verificar conexión backend–frontend.
 */
@RestController
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    @GetMapping("/api/public/test")
    public Map<String, String> testConnection() {
        log.debug("Recibida petición GET /api/public/test");
        Map<String, String> response = Map.of("message", "Conexión correcta con el backend CUDECA");
        log.debug("Respuesta enviada: {}", response);
        return response;
    }
}
