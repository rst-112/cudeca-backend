package com.cudeca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa la respuesta de checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private Long compraId;
    private String estado; // "PENDIENTE", "COMPLETADA"
    private BigDecimal total;
    private String mensaje; // "Redirigiendo a pasarela..."
    private String urlPasarela; // URL para redirigir al usuario (Stripe/PayPal)
}