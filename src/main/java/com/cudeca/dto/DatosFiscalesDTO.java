package com.cudeca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DatosFiscalesDTO {
    private Long id;

    @NotBlank(message = "El nombre o razón social es obligatorio")
    @Size(max = 150, message = "El nombre es demasiado largo")
    private String nombreCompleto;

    @NotBlank(message = "El NIF/CIF es obligatorio")
    @Size(min = 8, max = 20, message = "Formato de NIF incorrecto")
    private String nif;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe tener 5 dígitos")
    private String codigoPostal;

    @NotBlank(message = "El país es obligatorio")
    private String pais;

    private String alias;
}