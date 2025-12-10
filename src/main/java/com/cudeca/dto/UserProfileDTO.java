package com.cudeca.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa el perfil de un usuario.
 * Contiene la información personal y saldo del monedero.
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String nombre;
    private String email;
    private String direccion;
    private String rol;            // "COMPRADOR", "ADMINISTRADOR", etc.
    private BigDecimal saldoMonedero;
}
