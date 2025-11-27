package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("SORTEO") // <--- Pone "SORTEO" en la BD
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ArticuloSorteo extends ArticuloCompra {
    // Sin campos extra por ahora según diagrama
}