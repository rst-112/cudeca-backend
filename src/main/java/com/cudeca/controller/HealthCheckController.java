package com.cudeca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint público de prueba para verificar conexión backend–frontend.
 */
@RestController
public class HealthCheckController {

    @GetMapping("/api/public/test")
    public Map<String, String> testConnection() {
        return Map.of("message", "Conexión correcta con el backend CUDECA");
    }
}
