package com.cudeca.controller;

import com.cudeca.dto.UserProfileDTO;
import com.cudeca.model.negocio.EntradaEmitida;
import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.MovimientoMonedero;
import com.cudeca.service.PerfilUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar el perfil de usuario.
 * Expone endpoints para consultar y actualizar información personal,
 * gestionar entradas, consultar monedero y descargar PDFs.
 */
@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "*")
public class PerfilUsuarioController {

    private static final Logger log = LoggerFactory.getLogger(PerfilUsuarioController.class);

    private final PerfilUsuarioService perfilUsuarioService;

    public PerfilUsuarioController(PerfilUsuarioService perfilUsuarioService) {
        this.perfilUsuarioService = perfilUsuarioService;
    }

    /**
     * Obtiene el perfil de un usuario por ID.
     *
     * @param usuarioId ID del usuario
     * @return Información del perfil
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long usuarioId) {
        try {
            log.info("GET /api/perfil/{} - Obteniendo perfil de usuario", usuarioId);
            UserProfileDTO perfil = perfilUsuarioService.obtenerPerfilPorId(usuarioId);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            log.error("Usuario no encontrado: {}", usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error obteniendo perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno obteniendo perfil"));
        }
    }

    /**
     * Obtiene el perfil de un usuario por email.
     *
     * @param email Email del usuario
     * @return Información del perfil si existe
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> obtenerPerfilPorEmail(@PathVariable String email) {
        try {
            log.info("GET /api/perfil/email/{} - Obteniendo perfil por email", email);
            return perfilUsuarioService.obtenerPerfilPorEmail(email)
                    .map(perfil -> ResponseEntity.ok((Object) perfil))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error obteniendo perfil por email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno obteniendo perfil"));
        }
    }

    /**
     * Actualiza el perfil de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param payload Datos a actualizar (nombre, direccion)
     * @return Perfil actualizado
     */
    @PutMapping("/{usuarioId}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, String> payload) {
        try {
            if (log.isInfoEnabled()) {
                log.info("PUT /api/perfil/{} - Actualizando perfil", usuarioId);
            }
            String nombre = payload.get("nombre");
            String direccion = payload.get("direccion");

            UserProfileDTO perfilActualizado = perfilUsuarioService.actualizarPerfil(usuarioId, nombre, direccion);
            return ResponseEntity.ok(perfilActualizado);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error actualizando perfil: {}", e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error actualizando perfil", e);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno actualizando perfil"));
        }
    }

    /**
     * Obtiene todas las entradas (tickets) de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de entradas emitidas
     */
    @GetMapping("/{usuarioId}/entradas")
    public ResponseEntity<?> obtenerEntradas(@PathVariable Long usuarioId) {
        try {
            log.info("GET /api/perfil/{}/entradas - Obteniendo entradas del usuario", usuarioId);
            List<EntradaEmitida> entradas = perfilUsuarioService.obtenerEntradasUsuario(usuarioId);
            return ResponseEntity.ok(entradas);
        } catch (IllegalArgumentException e) {
            log.error("Usuario no encontrado: {}", usuarioId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error obteniendo entradas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno obteniendo entradas"));
        }
    }

    /**
     * Descarga el PDF de una entrada específica.
     *
     * @param usuarioId ID del usuario
     * @param entradaId ID de la entrada
     * @return PDF como bytes
     */
    @GetMapping("/{usuarioId}/entradas/{entradaId}/pdf")
    public ResponseEntity<?> descargarPDFEntrada(
            @PathVariable Long usuarioId,
            @PathVariable Long entradaId) {
        try {
            if (log.isInfoEnabled()) {
                log.info("GET /api/perfil/{}/entradas/{}/pdf - Generando PDF de entrada", usuarioId, entradaId);
            }
            byte[] pdfBytes = perfilUsuarioService.generarPDFEntrada(entradaId, usuarioId);

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "entrada_" + entradaId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error generando PDF: {}", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error generando PDF de entrada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno generando PDF"));
        }
    }

    /**
     * Obtiene el monedero de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Información del monedero
     */
    @GetMapping("/{usuarioId}/monedero")
    public ResponseEntity<?> obtenerMonedero(@PathVariable Long usuarioId) {
        try {
            if (log.isInfoEnabled()) {
                log.info("GET /api/perfil/{}/monedero - Obteniendo monedero", usuarioId);
            }
            Monedero monedero = perfilUsuarioService.obtenerMonedero(usuarioId);
            return ResponseEntity.ok(monedero);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error obteniendo monedero: {}", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error obteniendo monedero", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno obteniendo monedero"));
        }
    }

    /**
     * Obtiene el historial de movimientos del monedero.
     *
     * @param usuarioId ID del usuario
     * @return Lista de movimientos del monedero
     */
    @GetMapping("/{usuarioId}/monedero/movimientos")
    public ResponseEntity<?> obtenerMovimientosMonedero(@PathVariable Long usuarioId) {
        try {
            if (log.isInfoEnabled()) {
                log.info("GET /api/perfil/{}/monedero/movimientos - Obteniendo movimientos del monedero", usuarioId);
            }
            List<MovimientoMonedero> movimientos = perfilUsuarioService.obtenerMovimientosMonedero(usuarioId);
            return ResponseEntity.ok(movimientos);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error obteniendo movimientos del monedero: {}", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error obteniendo movimientos del monedero", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno obteniendo movimientos"));
        }
    }

    /**
     * Verifica si un usuario existe.
     *
     * @param usuarioId ID del usuario
     * @return Estado de existencia del usuario
     */
    @GetMapping("/{usuarioId}/existe")
    public ResponseEntity<Map<String, Boolean>> verificarExistencia(@PathVariable Long usuarioId) {
        try {
            log.info("GET /api/perfil/{}/existe - Verificando existencia de usuario", usuarioId);
            boolean existe = perfilUsuarioService.existeUsuario(usuarioId);
            return ResponseEntity.ok(Map.of("existe", existe));
        } catch (Exception e) {
            log.error("Error verificando existencia de usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("existe", false));
        }
    }
}

