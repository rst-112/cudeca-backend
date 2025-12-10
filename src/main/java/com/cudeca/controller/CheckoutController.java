package com.cudeca.controller;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;
import com.cudeca.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestionar el proceso de checkout.
 * Expone endpoints para crear compras, confirmar pagos y consultar detalles.
 */
@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Procesa una solicitud de checkout y crea una compra.
     *
     * @param request Datos del checkout (carrito, usuario, donación extra, etc.)
     * @return Respuesta con información de la compra creada y URL de pago
     */
    @PostMapping
    public ResponseEntity<CheckoutResponse> procesarCheckout(@RequestBody CheckoutRequest request) {
        try {
            if (log.isInfoEnabled()) {
                log.info("POST /api/checkout - Procesando checkout para usuario: {}", request.getUsuarioId());
            }
            CheckoutResponse response = checkoutService.procesarCheckout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Error de validación en checkout: {}", e.getMessage());
            }
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        } catch (IllegalStateException e) {
            if (log.isErrorEnabled()) {
                log.error("Error de estado en checkout: {}", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado en checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error procesando la compra"));
        }
    }

    /**
     * Obtiene los detalles de una compra.
     *
     * @param compraId ID de la compra
     * @return Detalles de la compra
     */
    @GetMapping("/{compraId}")
    public ResponseEntity<CheckoutResponse> obtenerDetallesCompra(@PathVariable Long compraId) {
        try {
            log.info("GET /api/checkout/{} - Obteniendo detalles de compra", compraId);
            CheckoutResponse response = checkoutService.obtenerDetallesCompra(compraId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Compra no encontrada: {}", compraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error obteniendo detalles de compra", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Confirma el pago de una compra (webhook de pasarela de pago).
     *
     * @param compraId ID de la compra
     * @param payload Datos del webhook (puede incluir detalles del pago)
     * @return Estado de la confirmación
     */
    @PostMapping("/{compraId}/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarPago(
            @PathVariable Long compraId,
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            log.info("POST /api/checkout/{}/confirmar - Confirmando pago", compraId);
            boolean confirmado = checkoutService.confirmarPago(compraId);

            if (confirmado) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Pago confirmado exitosamente",
                        "compraId", compraId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "No se pudo confirmar el pago. La compra no está en estado válido.",
                        "compraId", compraId
                ));
            }
        } catch (IllegalArgumentException e) {
            log.error("Compra no encontrada para confirmar: {}", compraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error confirmando pago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error interno confirmando el pago"
            ));
        }
    }

    /**
     * Cancela una compra pendiente.
     *
     * @param compraId ID de la compra
     * @param payload Datos de cancelación (motivo)
     * @return Estado de la cancelación
     */
    @PostMapping("/{compraId}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarCompra(
            @PathVariable Long compraId,
            @RequestBody Map<String, String> payload) {
        try {
            String motivo = payload.getOrDefault("motivo", "Sin motivo especificado");
            log.info("POST /api/checkout/{}/cancelar - Cancelando compra. Motivo: {}", compraId, motivo);

            boolean cancelado = checkoutService.cancelarCompra(compraId, motivo);

            if (cancelado) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Compra cancelada exitosamente",
                        "compraId", compraId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "message", "No se pudo cancelar la compra. Verifica su estado.",
                        "compraId", compraId
                ));
            }
        } catch (IllegalArgumentException e) {
            log.error("Compra no encontrada para cancelar: {}", compraId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error cancelando compra", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error interno cancelando la compra"
            ));
        }
    }

    // --- MÉTODOS AUXILIARES ---

    /**
     * Crea una respuesta de error en formato CheckoutResponse.
     */
    private CheckoutResponse crearRespuestaError(String mensaje) {
        CheckoutResponse response = new CheckoutResponse();
        response.setMensaje(mensaje);
        return response;
    }
}

