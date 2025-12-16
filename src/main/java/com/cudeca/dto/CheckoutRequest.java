package com.cudeca.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa la solicitud de checkout.
 * Actualizado con método de pago y datos fiscales.
 */
@Data
public class CheckoutRequest {

    private Long usuarioId;

    @Email(message = "El email de contacto debe ser válido")
    @NotBlank(message = "El email de contacto es obligatorio")
    private String emailContacto;

    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<ItemDTO> items;

    private List<Long> asientoIds;

    @PositiveOrZero(message = "La donación extra no puede ser negativa")
    private Double donacionExtra;

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "TARJETA|PAYPAL|BIZUM|MONEDERO", message = "Método de pago inválido")
    private String metodoPago;

    @Valid
    private FiscalDataDTO datosFiscales;

    @Data
    public static class ItemDTO {
        @NotBlank(message = "El tipo de ítem es obligatorio")
        private String tipo; // "ENTRADA", "DONACION"

        private Long referenciaId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        private Double precio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiscalDataDTO {
        @NotBlank(message = "El nombre o razón social es obligatorio para la factura")
        private String nombreCompleto;

        @NotBlank(message = "El NIF/CIF es obligatorio")
        @Pattern(regexp = "^[0-9A-Z]{1,20}$", message = "El NIF contiene caracteres inválidos")
        private String nif;

        @NotBlank(message = "La dirección fiscal es obligatoria")
        private String direccion;

        @NotBlank(message = "El país es obligatorio")
        private String pais;
    }
}