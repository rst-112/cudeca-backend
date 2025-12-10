package com.cudeca.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa la respuesta de checkout.
 * Contiene la información de la compra creada.
 */
@Data
public class CheckoutResponse {
    private Long compraId;
    private String estado; // "PENDIENTE", "COMPLETADA"
    private BigDecimal total;
    private String mensaje; // "Redirigiendo a pasarela..."
    private String urlPasarela; // Si hubiera que redirigir a PayPal
}
