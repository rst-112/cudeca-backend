package com.cudeca.controller;

import com.cudeca.dto.usuario.*;
import com.cudeca.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para la gestión de la autenticación y cuentas de usuario.
 * Rutas: /api/auth/*
 * <p>
 * NOTA: Estos endpoints deben ser accesibles públicamente (permitAll()) en SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de cuentas de usuario")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de Registro de Usuario.
     * Gestiona el alta de un nuevo usuario, el cifrado de la contraseña y la generación del token inicial.
     *
     * @param request Datos del nuevo usuario validados con @Valid.
     * @return 200 OK con el token JWT de la sesión iniciada.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario con email y contraseña. Devuelve un token JWT para acceso inmediato."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint de Inicio de Sesión (Login).
     * Procesa las credenciales, verifica la contraseña y emite un token JWT si son correctas.
     *
     * @param request Datos de login (email y password) validados con @Valid.
     * @return 200 OK con el token JWT para la sesión.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica a un usuario existente con email y contraseña. Devuelve un token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint de Validación de Token.
     * Verifica si un token JWT es válido y está activo.
     * Este endpoint requiere autenticación (el filtro JWT lo valida automáticamente).
     *
     * @param authentication Objeto de autenticación inyectado por Spring Security tras validar el token.
     * @return 200 OK con {valid: true} si el token es válido.
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validar token JWT",
            description = "Verifica si el token JWT proporcionado en el header Authorization es válido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    public ResponseEntity<Map<String, Boolean>> validateToken(Authentication authentication) {
        // Si llegamos aquí, el token es válido (el filtro JWT ya lo validó)
        boolean isValid = authentication != null && authentication.isAuthenticated();
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación", description = "Envía un email si el usuario existe.")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.solicitarRecuperacionPassword(request.getEmail());
        // Respuesta genérica por seguridad
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás instrucciones en breve."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña usando un token válido.")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.restablecerPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }
}
