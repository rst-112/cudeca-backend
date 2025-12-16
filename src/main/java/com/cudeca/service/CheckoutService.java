package com.cudeca.service;

import com.cudeca.dto.CheckoutRequest;
import com.cudeca.dto.CheckoutResponse;

/**
 * Servicio para gestionar el proceso de checkout (finalización de compra).
 * Maneja la creación de compras, validación de stock, cálculo de totales,
 * y coordinación con sistemas de pago.
 */
public interface CheckoutService {

    /**
     * Procesa una solicitud de checkout y crea una compra.
     *
     * @param request Datos del checkout (carrito, usuario, donación extra, etc.)
     * @return Respuesta con información de la compra creada y URL de pago si aplica
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si no hay stock disponible
     */
    CheckoutResponse procesarCheckout(CheckoutRequest request);

    /**
     * Confirma el pago de una compra (llamado por webhook de pasarela).
     *
     * @param compraId ID de la compra a confirmar
     * @return true si se confirmó correctamente
     * @throws IllegalArgumentException si la compra no existe
     */
    boolean confirmarPago(Long compraId);

    /**
     * Cancela una compra pendiente.
     *
     * @param compraId ID de la compra a cancelar
     * @param motivo Motivo de cancelación
     * @return true si se canceló correctamente
     */
    boolean cancelarCompra(Long compraId, String motivo);

    /**
     * Obtiene los detalles de una compra.
     *
     * @param compraId ID de la compra
     * @return Respuesta con los detalles de la compra
     */
    CheckoutResponse obtenerDetallesCompra(Long compraId);
}

