package com.cudeca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalDataDTO {

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