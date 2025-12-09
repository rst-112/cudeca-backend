package com.cudeca.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO que representa la solicitud de checkout (finalización de compra).
 * Contiene la información del carrito y del comprador.
 */
@Data
public class CheckoutRequest {
    private Long usuarioId;
    private String emailContacto; // Obligatorio si es invitado

    private List<ItemDTO> items; // El carrito
    private List<Long> asientoIds; // IDs de asientos a reservar (para bloqueo transaccional)
    private Double donacionExtra; // La casilla de "Donación Extra"

    @Data
    public static class ItemDTO {
        private String tipo; // "ENTRADA", "DONACION"
        private Long referenciaId; // ID del TipoEntrada
        private Integer cantidad;
        private Double precio;
    }
}
