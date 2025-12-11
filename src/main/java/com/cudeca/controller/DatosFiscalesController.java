package com.cudeca.controller;

import com.cudeca.model.usuario.DatosFiscales;
import com.cudeca.service.DatosFiscalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar datos fiscales (libreta de direcciones de facturación).
 * Permite a los usuarios crear, actualizar, consultar y eliminar perfiles fiscales.
 */
@RestController
@RequestMapping("/api/datos-fiscales")
@CrossOrigin(origins = "*")
public class DatosFiscalesController {

    private static final Logger log = LoggerFactory.getLogger(DatosFiscalesController.class);

    private final DatosFiscalesService datosFiscalesService;

    public DatosFiscalesController(DatosFiscalesService datosFiscalesService) {
        this.datosFiscalesService = datosFiscalesService;
    }

    /**
     * Obtiene todos los perfiles fiscales de un usuario.
     *
     * @param usuarioId ID del usuario
     * @return Lista de perfiles fiscales
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<DatosFiscales>> obtenerDatosFiscalesPorUsuario(@PathVariable Long usuarioId) {
        try {
            log.info("GET /api/datos-fiscales/usuario/{} - Obteniendo datos fiscales", usuarioId);
            List<DatosFiscales> datos = datosFiscalesService.obtenerDatosFiscalesPorUsuario(usuarioId);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            log.error("Error obteniendo datos fiscales para usuario: {}", usuarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un perfil fiscal específico por ID.
     *
     * @param id ID del perfil fiscal
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return Perfil fiscal si existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatosFiscales> obtenerDatosFiscalesPorId(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        try {
            log.info("GET /api/datos-fiscales/{} - Usuario: {}", id, usuarioId);
            return datosFiscalesService.obtenerDatosFiscalesPorId(id, usuarioId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error obteniendo datos fiscales: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo perfil fiscal para un usuario.
     *
     * @param datosFiscales Datos fiscales a crear
     * @param usuarioId ID del usuario propietario
     * @return Perfil fiscal creado
     */
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> crearDatosFiscales(
            @RequestBody DatosFiscales datosFiscales,
            @PathVariable Long usuarioId) {
        try {
            if (log.isInfoEnabled()) {
                log.info("POST /api/datos-fiscales/usuario/{} - Creando datos fiscales", usuarioId);
            }
            DatosFiscales creado = datosFiscalesService.crearDatosFiscales(datosFiscales, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error de validación creando datos fiscales: {}", e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creando datos fiscales", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno creando datos fiscales"));
        }
    }

    /**
     * Actualiza un perfil fiscal existente.
     *
     * @param id ID del perfil fiscal a actualizar
     * @param datosFiscales Nuevos datos fiscales
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return Perfil fiscal actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDatosFiscales(
            @PathVariable Long id,
            @RequestBody DatosFiscales datosFiscales,
            @RequestParam Long usuarioId) {
        try {
            if (log.isInfoEnabled()) {
                log.info("PUT /api/datos-fiscales/{} - Actualizando datos fiscales. Usuario: {}", id, usuarioId);
            }
            DatosFiscales actualizado = datosFiscalesService.actualizarDatosFiscales(id, datosFiscales, usuarioId);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error actualizando datos fiscales: {}", e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error actualizando datos fiscales: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno actualizando datos fiscales"));
        }
    }

    /**
     * Elimina un perfil fiscal.
     *
     * @param id ID del perfil fiscal
     * @param usuarioId ID del usuario (para validar propiedad)
     * @return Estado de la eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDatosFiscales(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        try {
            log.info("DELETE /api/datos-fiscales/{} - Eliminando datos fiscales. Usuario: {}", id, usuarioId);
            boolean eliminado = datosFiscalesService.eliminarDatosFiscales(id, usuarioId);

            if (eliminado) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Datos fiscales eliminados exitosamente"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Datos fiscales no encontrados o no pertenecen al usuario"
                ));
            }
        } catch (Exception e) {
            log.error("Error eliminando datos fiscales: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error interno eliminando datos fiscales"
            ));
        }
    }

    /**
     * Valida un NIF.
     *
     * @param payload Objeto con el campo "nif"
     * @return Resultado de la validación
     */
    @PostMapping("/validar-nif")
    public ResponseEntity<Map<String, Object>> validarNIF(@RequestBody Map<String, String> payload) {
        try {
            String nif = payload.get("nif");
            log.info("POST /api/datos-fiscales/validar-nif - Validando NIF");

            if (nif == null || nif.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valido", false,
                        "mensaje", "NIF no proporcionado"
                ));
            }

            boolean valido = datosFiscalesService.validarNIF(nif);

            return ResponseEntity.ok(Map.of(
                    "valido", valido,
                    "mensaje", valido ? "NIF válido" : "NIF inválido"
            ));
        } catch (Exception e) {
            log.error("Error validando NIF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valido", false,
                    "mensaje", "Error validando NIF"
            ));
        }
    }
}

