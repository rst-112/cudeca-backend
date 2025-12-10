package com.cudeca.exception;

import com.cudeca.service.impl.ServiceExceptions.EmailAlreadyExistsException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura y procesa las excepciones lanzadas por los controladores y servicios.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de datos (@Valid en RequestBody).
     * Devuelve un 400 Bad Request con los errores de campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());

        // Extraer los errores de campo y sus mensajes
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage
                ));

        body.put("errors", fieldErrors);
        body.put("message", "La validación de los datos de entrada ha fallado.");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja la excepción de email duplicado durante el registro.
     * Devuelve un 409 Conflict.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Maneja credenciales inválidas durante el login.
     * Devuelve un 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Credenciales inválidas. Por favor, verifica tu email y contraseña.");
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja usuario no encontrado.
     * Devuelve un 401 Unauthorized.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Credenciales inválidas o usuario no encontrado.");
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja cualquier otra excepción no controlada.
     * Devuelve un 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Ha ocurrido un error inesperado en el servidor.");
        body.put("error", ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
