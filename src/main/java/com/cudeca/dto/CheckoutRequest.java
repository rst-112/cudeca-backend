package com.cudeca.dto;

import lombok.Data;

import java.util.List;
//Lo que envía el Frontend al comprar
public class CheckoutRequest {
   private Long usuarioId;

    private String emailContacto; // Obligatorio si es invitado

    private List<ItemDTO> items; // El carrito
    private Double donacionExtra; // La casilla de "Donación Extra"

    @Data
    public static class ItemDTO {
        private String tipo; // "ENTRADA", "DONACION"
        private Long referenciaId; // ID del TipoEntrada
        private Integer cantidad;
        private Double precio;
    }
}
