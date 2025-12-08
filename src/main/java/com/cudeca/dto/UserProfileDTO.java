package com.cudeca.dto;

import java.math.BigDecimal;

public class UserProfileDTO {//Datos del usuario
    private Long id;
    private String nombre;
    private String email;
    private String direccion;
    // Aquí podrías añadir saldo del monedero si hiciera falta
    private String rol;            // "COMPRADOR"
    private BigDecimal saldoMonedero;
}
