package com.cudeca.model.negocio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SORTEO")
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArticuloSorteo extends ArticuloCompra {

    @Override
    public boolean validar() {
        return getCantidad() != null && getCantidad() > 0
                && getPrecioUnitario() != null && getPrecioUnitario().compareTo(BigDecimal.ZERO) > 0;
    }
}