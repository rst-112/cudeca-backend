package com.cudeca.service;

import com.cudeca.dto.QrValidacionDTO;
import com.cudeca.dto.QrValidacionResponseDTO;

/**
 * Interfaz para la validación de códigos QR de entradas.
 * <p>
 * Responsabilidades:
 * 1. Buscar la entrada por hash del QR
 * 2. Validar que el estado sea VALIDA
 * 3. Cambiar el estado a USADA
 * 4. Registrar la validación
 * 5. Devolver respuesta con estado de la validación
 */
public interface QrValidadorService {

    /**
     * Valida una entrada por su código QR.
     * <p>
     * Proceso:
     * 1. Busca la entrada por codigoQR
     * 2. Si no existe, devuelve error
     * 3. Si estado es ANULADA o USADA, devuelve error
     * 4. Si estado es VALIDA, cambia a USADA y registra la validación
     * 5. Devuelve respuesta con el resultado
     *
     * @param qrValidacionDTO DTO con el código QR a validar
     * @return QrValidacionResponseDTO con el resultado de la validación
     */
    QrValidacionResponseDTO validarCodigoQR(QrValidacionDTO qrValidacionDTO);

    /**
     * Busca una entrada por su código QR sin cambiar su estado.
     * Útil para consultar información de la entrada.
     *
     * @param codigoQR Código QR a buscar
     * @return QrValidacionResponseDTO con la información de la entrada
     */
    QrValidacionResponseDTO consultarEntrada(String codigoQR);
}

