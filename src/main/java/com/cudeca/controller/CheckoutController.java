package com.cudeca.controller;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/resumen")
    public ResponseEntity<?> iniciarResumen(@RequestBody List<Long> asientoIds) {
        // TODO: Falta el método 'bloquearAsientos(List<Long> ids) presuntamente'
        
        // Código temporal que hace como que todo ha ido bien
        System.out.println("SIMULACIÓN: Bloqueando asientos " + asientoIds);
        
        // Devuelve un OK falso para que el Frontend no se queje
        return ResponseEntity.ok("Asientos bloqueados correctamente (Simulado).");
    }

    @PostMapping("/procesar")
    public ResponseEntity<?> finalizarCompra(@RequestBody CheckoutRequest request) {
        // TODO: Falta añadir campos (NIF, Toggle) al DTO presuntamente, se validarían aquí.
        
        try {
            CheckoutResponse response = checkoutService.procesarCheckout(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
