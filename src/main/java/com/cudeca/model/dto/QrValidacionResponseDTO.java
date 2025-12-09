package com.cudeca.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de validación de QR.
 * Incluye el estado de la validación y detalles de la entrada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrValidacionResponseDTO {

    /**
     * Estado de la validación: "OK", "ERROR_NO_ENCONTRADO", "ERROR_YA_USADA", "ERROR_ANULADA"
     */
    private String estado;

    /**
     * Mensaje descriptivo del resultado de la validación.
     */
    private String mensaje;

    /**
     * ID de la entrada emitida (si se encuentra).
     */
    private Long entradaId;

    /**
     * Código QR validado.
     */
    private String codigoQR;

    /**
     * Estado anterior de la entrada (VALIDA, USADA, ANULADA).
     */
    private String estadoAnterior;

    /**
     * Estado actual de la entrada después de la validación.
     */
    private String estadoActual;

    /**
     * Timestamp de la validación en milisegundos.
     */
    private long timestamp;
}

