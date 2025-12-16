package com.cudeca.dto.evento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEntradaDTO {
    private Long id;
    private String nombre;
    private BigDecimal costeBase;
    private BigDecimal donacionImplicita;
    private Integer cantidadTotal;
    private Integer cantidadVendida;
    private Integer limitePorCompra;

    public BigDecimal getPrecioTotal() {
        if (costeBase == null)
            return BigDecimal.ZERO;
        BigDecimal donacion = (donacionImplicita != null) ? donacionImplicita : BigDecimal.ZERO;
        return costeBase.add(donacion);
    }

    public boolean isAgotado() {
        if (cantidadTotal == null || cantidadVendida == null)
            return false;
        return cantidadVendida >= cantidadTotal;
    }
}
