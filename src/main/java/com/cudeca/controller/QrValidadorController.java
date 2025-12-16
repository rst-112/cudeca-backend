package com.cudeca.controller;

import com.cudeca.dto.QrValidacionDTO;
import com.cudeca.dto.QrValidacionResponseDTO;
import com.cudeca.service.QrValidadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para validación de códigos QR de entradas.
 * <p>
 * Endpoints:
 * 1. POST /api/validador-qr/validar - Valida un QR y cambia estado a USADA
 * 2. GET /api/validador-qr/consultar/{codigoQR} - Consulta estado sin cambiar
 * <p>
 * Casos de respuesta:
 * - 200 OK: Validación exitosa
 * - 400 Bad Request: Entrada no encontrada o ya usada
 * - 500 Internal Server Error: Error inesperado
 */
@RestController
@RequestMapping("/api/validador-qr")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Validador QR", description = "API para validación de códigos QR de entradas")
public class QrValidadorController {

    private final QrValidadorService qrValidadorService;

    /**
     * Valida un código QR y cambia el estado de la entrada de VALIDA a USADA.
     * <p>
     * POST /api/validador-qr/validar
     * <p>
     * Ejemplo de solicitud:
     * {
     * "codigoQR": "TICKET-2024-001-A",
     * "dispositivoId": "DEVICE-SCANNER-001"
     * }
     * <p>
     * Respuesta exitosa (200):
     * {
     * "estado": "OK",
     * "mensaje": "Entrada validada exitosamente. Estado cambiado a USADA.",
     * "entradaId": 123,
     * "codigoQR": "TICKET-2024-001-A",
     * "estadoAnterior": "VALIDA",
     * "estadoActual": "USADA",
     * "timestamp": 1702000000000
     * }
     * <p>
     * Respuesta con error (400):
     * {
     * "estado": "ERROR_NO_ENCONTRADO",
     * "mensaje": "Código QR no válido. La entrada no existe en el sistema.",
     * "codigoQR": "INVALID-CODE",
     * "timestamp": 1702000000000
     * }
     *
     * @param qrValidacionDTO DTO con el código QR a validar
     * @return ResponseEntity con el resultado de la validación
     */
    @PostMapping("/validar")
    @Operation(
            summary = "Validar código QR de entrada",
            description = "Valida una entrada por su código QR. Si el estado es VALIDA, lo cambia a USADA. " +
                    "Si ya fue usada o anulada, devuelve un error."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Validación exitosa",
            content = @Content(schema = @Schema(implementation = QrValidacionResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Error en la validación (no encontrado, ya usada, anulada)"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
    )
    public ResponseEntity<QrValidacionResponseDTO> validarCodigoQR(
            @RequestBody QrValidacionDTO qrValidacionDTO) {

        try {
            log.info("Solicitud de validación QR recibida: {}", qrValidacionDTO.getCodigoQR());

            QrValidacionResponseDTO response = qrValidadorService.validarCodigoQR(qrValidacionDTO);

            // Determinar el código HTTP según el estado de la respuesta
            if ("OK".equals(response.getEstado())) {
                log.info("Validación exitosa para QR: {}", qrValidacionDTO.getCodigoQR());
                return ResponseEntity.ok(response);
            } else if ("ERROR_NO_ENCONTRADO".equals(response.getEstado())) {
                log.warn("QR no encontrado: {}", qrValidacionDTO.getCodigoQR());
                return ResponseEntity.badRequest().body(response);
            } else if ("ERROR_YA_USADA".equals(response.getEstado())) {
                log.warn("QR ya utilizado: {}", qrValidacionDTO.getCodigoQR());
                return ResponseEntity.badRequest().body(response);
            } else if ("ERROR_ANULADA".equals(response.getEstado())) {
                log.warn("QR anulado: {}", qrValidacionDTO.getCodigoQR());
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Error inesperado durante la validación del QR: {}", qrValidacionDTO.getCodigoQR(), e);
            QrValidacionResponseDTO errorResponse = QrValidacionResponseDTO.builder()
                    .estado("ERROR_SERVIDOR")
                    .mensaje("Error inesperado durante la validación: " + e.getMessage())
                    .codigoQR(qrValidacionDTO.getCodigoQR())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Consulta el estado de una entrada por su código QR sin cambiar el estado.
     * <p>
     * GET /api/validador-qr/consultar/{codigoQR}
     * <p>
     * Ejemplo de respuesta (200):
     * {
     * "estado": "ENCONTRADA",
     * "mensaje": "Entrada encontrada en el sistema.",
     * "entradaId": 123,
     * "codigoQR": "TICKET-2024-001-A",
     * "estadoActual": "VALIDA",
     * "timestamp": 1702000000000
     * }
     *
     * @param codigoQR Código QR a consultar
     * @return ResponseEntity con la información de la entrada
     */
    @GetMapping("/consultar/{codigoQR}")
    @Operation(
            summary = "Consultar entrada por QR",
            description = "Busca una entrada por su código QR sin cambiar su estado. " +
                    "Útil para validar que el código existe antes de procesarlo."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Consulta exitosa",
            content = @Content(schema = @Schema(implementation = QrValidacionResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Entrada no encontrada"
    )
    public ResponseEntity<QrValidacionResponseDTO> consultarEntrada(
            @PathVariable String codigoQR) {

        try {
            log.info("Consulta de entrada solicitada: {}", codigoQR);

            QrValidacionResponseDTO response = qrValidadorService.consultarEntrada(codigoQR);

            if ("ENCONTRADA".equals(response.getEstado())) {
                log.info("Entrada encontrada: {}", codigoQR);
                return ResponseEntity.ok(response);
            } else {
                log.warn("Entrada no encontrada: {}", codigoQR);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error inesperado durante la consulta del QR: {}", codigoQR, e);
            QrValidacionResponseDTO errorResponse = QrValidacionResponseDTO.builder()
                    .estado("ERROR_SERVIDOR")
                    .mensaje("Error inesperado durante la consulta: " + e.getMessage())
                    .codigoQR(codigoQR)
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

