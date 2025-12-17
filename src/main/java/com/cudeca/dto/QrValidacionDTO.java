package com.cudeca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar el hash/código QR a validar.
 * Utilizado en la validación de entradas mediante lectura de QR.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrValidacionDTO {

    /**
     * Hash o código QR leído desde el lector de cámara/escáner.
     * Corresponde al campo codigoQR de la EntradaEmitida.
     */
    private String codigoQR;

    /**
     * ID del dispositivo que realiza la validación (opcional).
     * Útil para registrar desde qué dispositivo se validó.
     */
    private String dispositivoId;

    /**
     * ID del usuario que realiza la validación.
     * Es obligatorio para registrar quién validó la entrada.
     */
    private Long usuarioValidadorId;
}
