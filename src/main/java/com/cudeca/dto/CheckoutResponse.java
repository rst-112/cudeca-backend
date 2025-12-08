package com.cudeca.dto;

import java.math.BigDecimal;
//Lo que respondes
public class CheckoutResponse {
    private Long compraId;
    private String estado; // "PENDIENTE", "COMPLETADA"
    private BigDecimal total;
    private String mensaje; // "Redirigiendo a pasarela..."
    private String urlPasarela; // Si hubiera que redirigir a PayPal
}
