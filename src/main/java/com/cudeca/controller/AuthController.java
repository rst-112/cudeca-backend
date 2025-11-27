package com.cudeca.controller;

import com.cudeca.dto.AuthResponse;
import com.cudeca.dto.LoginRequest;
import com.cudeca.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // Esta es la ruta base para todos los endpoints de este controlador
public class AuthController {

    // Aquí inyectarás el AuthService, una vez que lo creemos
    // private final AuthService authService;

    // Constructor para inyección de dependencias (opcional en versiones modernas de Spring, pero buena práctica)
    /* public AuthController(AuthService authService) {
        this.authService = authService;
    }
    */

    //Endpoint de registro
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // La lógica real se delega a un servicio (AuthService) que tú crearás más tarde
        // return ResponseEntity.ok(authService.register(request));

        // Por ahora, solo devuelve un mensaje de prueba
        return ResponseEntity.ok(new AuthResponse("Token de prueba para el registro"));
    }
    // Endpoint para el Login de un usuario existente
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // La lógica real se delega a un servicio
        // return ResponseEntity.ok(authService.login(request));

        // Por ahora, solo devuelve un mensaje de prueba
        return ResponseEntity.ok(new AuthResponse("Token de prueba para el login"));
    }

}