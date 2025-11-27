package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("DONACION") // <--- Pone "DONACION" en la BD
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ArticuloDonacion extends ArticuloCompra {

    // Diagrama: destino: String
    // Nota: Asegúrate de añadir la columna 'destino' a la tabla ITEMS_COMPRA en la BD
    // o esto fallará al arrancar.
    @Column(name = "destino")
    private String destino; // Ej: "Hospice", "Investigación"
}