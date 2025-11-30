package com.cudeca.controller;

import com.cudeca.dto.usuario.AuthResponse;
import com.cudeca.dto.usuario.LoginRequest;
import com.cudeca.dto.usuario.RegisterRequest;
import com.cudeca.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de la autenticación y cuentas de usuario.
 * Rutas: /api/auth/*
 *
 * NOTA: Estos endpoints deben ser accesibles públicamente (permitAll()) en SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth") // Ruta base para todos los endpoints de autenticación
@RequiredArgsConstructor
public class AuthController {

    // Inyección de la dependencia del servicio de lógica de negocio
    private final AuthServiceImpl authService;

    /**
     * Endpoint de Registro de Usuario.
     * Gestiona el alta de un nuevo usuario, el cifrado de la contraseña y la generación del token inicial.
     * * @param request Datos del nuevo usuario. Se recomienda usar @Valid para la validación.
     * @return 200 OK con el token JWT de la sesión iniciada.
     */
    @PostMapping("/register")
    // ⚠️ CORRECCIÓN CLAVE: Añadir @Valid. Sin esto, las reglas del DTO se ignoran.
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint de Inicio de Sesión (Login).
     * Procesa las credenciales, verifica la contraseña y emite un token JWT si son correctas.
     *
     * @param request Datos de login (email y password).
     * @return 200 OK con el token JWT para la sesión.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Delega la verificación de credenciales al AuthService.
        return ResponseEntity.ok(authService.login(request));
    }
}