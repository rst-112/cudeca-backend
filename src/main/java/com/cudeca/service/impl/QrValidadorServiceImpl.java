package com.cudeca.service.impl;

import com.cudeca.model.dto.QrValidacionDTO;
import com.cudeca.model.dto.QrValidacionResponseDTO;
import com.cudeca.model.enums.EstadoEntrada;
import com.cudeca.model.negocio.EntradaEmitida;
import com.cudeca.model.negocio.ValidacionEntrada;
import com.cudeca.repository.EntradaEmitidaRepository;
import com.cudeca.repository.ValidacionEntradaRepository;
import com.cudeca.service.QrValidadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación del servicio de validación de códigos QR.
 *
 * Gestiona:
 * 1. Búsqueda de entradas por código QR
 * 2. Validación del estado de la entrada
 * 3. Cambio de estado de VALIDA a USADA
 * 4. Registro de la validación en la base de datos
 * 5. Generación de respuestas con el resultado
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QrValidadorServiceImpl implements QrValidadorService {

    private final EntradaEmitidaRepository entradaEmitidaRepository;
    private final ValidacionEntradaRepository validacionEntradaRepository;

    /**
     * Valida una entrada por su código QR y cambia su estado de VALIDA a USADA.
     *
     * Casos de respuesta:
     * - "OK": Entrada validada exitosamente (cambió de VALIDA a USADA)
     * - "ERROR_NO_ENCONTRADO": No existe entrada con ese código QR
     * - "ERROR_YA_USADA": La entrada ya fue usada anteriormente
     * - "ERROR_ANULADA": La entrada fue anulada y no puede validarse
     *
     * @param qrValidacionDTO DTO con el código QR a validar
     * @return QrValidacionResponseDTO con el resultado de la validación
     */
    @Override
    @Transactional
    public QrValidacionResponseDTO validarCodigoQR(QrValidacionDTO qrValidacionDTO) {
        long timestamp = System.currentTimeMillis();
        String codigoQR = qrValidacionDTO.getCodigoQR();

        log.info("Iniciando validación del código QR: {}", codigoQR);

        // Buscar entrada por código QR
        Optional<EntradaEmitida> entradaOptional = entradaEmitidaRepository.findByCodigoQR(codigoQR);

        if (entradaOptional.isEmpty()) {
            log.warn("Entrada no encontrada para código QR: {}", codigoQR);
            return QrValidacionResponseDTO.builder()
                    .estado("ERROR_NO_ENCONTRADO")
                    .mensaje("Código QR no válido. La entrada no existe en el sistema.")
                    .codigoQR(codigoQR)
                    .timestamp(timestamp)
                    .build();
        }

        EntradaEmitida entrada = entradaOptional.get();
        EstadoEntrada estadoAnterior = entrada.getEstado();

        // Validar estado de la entrada
        if (estadoAnterior == EstadoEntrada.USADA) {
            log.warn("Intento de validar entrada ya usada. Código QR: {}", codigoQR);
            return QrValidacionResponseDTO.builder()
                    .estado("ERROR_YA_USADA")
                    .mensaje("Esta entrada ya ha sido utilizada. No puede ser validada de nuevo.")
                    .entradaId(entrada.getId())
                    .codigoQR(codigoQR)
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoActual(estadoAnterior.toString())
                    .timestamp(timestamp)
                    .build();
        }

        if (estadoAnterior == EstadoEntrada.ANULADA) {
            log.warn("Intento de validar entrada anulada. Código QR: {}", codigoQR);
            return QrValidacionResponseDTO.builder()
                    .estado("ERROR_ANULADA")
                    .mensaje("Esta entrada ha sido anulada y no puede ser validada.")
                    .entradaId(entrada.getId())
                    .codigoQR(codigoQR)
                    .estadoAnterior(estadoAnterior.toString())
                    .estadoActual(estadoAnterior.toString())
                    .timestamp(timestamp)
                    .build();
        }

        // Si llegamos aquí, el estado es VALIDA
        // Cambiar estado a USADA
        entrada.setEstado(EstadoEntrada.USADA);
        EntradaEmitida entradaActualizada = entradaEmitidaRepository.save(entrada);

        // Registrar la validación (opcional pero recomendado para auditoría)
        ValidacionEntrada validacionEntrada = ValidacionEntrada.builder()
                .entradaEmitida(entradaActualizada)
                .dispositivoId(qrValidacionDTO.getDispositivoId())
                .build();
        validacionEntradaRepository.save(validacionEntrada);

        log.info("Entrada validada exitosamente. Código QR: {}, ID: {}", codigoQR, entrada.getId());

        return QrValidacionResponseDTO.builder()
                .estado("OK")
                .mensaje("Entrada validada exitosamente. Estado cambiado a USADA.")
                .entradaId(entrada.getId())
                .codigoQR(codigoQR)
                .estadoAnterior(EstadoEntrada.VALIDA.toString())
                .estadoActual(EstadoEntrada.USADA.toString())
                .timestamp(timestamp)
                .build();
    }

    /**
     * Consulta una entrada por su código QR sin cambiar su estado.
     * Útil para obtener información de la entrada antes de validarla.
     *
     * @param codigoQR Código QR a buscar
     * @return QrValidacionResponseDTO con la información de la entrada
     */
    @Override
    @Transactional(readOnly = true)
    public QrValidacionResponseDTO consultarEntrada(String codigoQR) {
        long timestamp = System.currentTimeMillis();

        log.info("Consultando entrada con código QR: {}", codigoQR);

        Optional<EntradaEmitida> entradaOptional = entradaEmitidaRepository.findByCodigoQR(codigoQR);

        if (entradaOptional.isEmpty()) {
            log.warn("Entrada no encontrada para consulta. Código QR: {}", codigoQR);
            return QrValidacionResponseDTO.builder()
                    .estado("ERROR_NO_ENCONTRADO")
                    .mensaje("Código QR no válido. La entrada no existe en el sistema.")
                    .codigoQR(codigoQR)
                    .timestamp(timestamp)
                    .build();
        }

        EntradaEmitida entrada = entradaOptional.get();

        return QrValidacionResponseDTO.builder()
                .estado("ENCONTRADA")
                .mensaje("Entrada encontrada en el sistema.")
                .entradaId(entrada.getId())
                .codigoQR(codigoQR)
                .estadoActual(entrada.getEstado().toString())
                .timestamp(timestamp)
                .build();
    }
}

