package com.cudeca.model.evento;

import com.cudeca.model.evento.enums.TipoAjusteRegla;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class ReglaPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoAjusteRegla tipoAjuste;
    private BigDecimal valor;
    private boolean requiereSuscricion;

    //relaciones

    //crear metodos

    public BigDecimal aplicarDescuento(BigDecimal precioBase) {
        if (tipoAjuste == TipoAjusteRegla.PORCENTAJE) {
            BigDecimal descuento = precioBase.multiply(valor).divide(new BigDecimal(100));
            return precioBase.subtract(descuento);
        } else if (tipoAjuste == TipoAjusteRegla.FIJO) {
            return precioBase.subtract(valor);
        } else {
            return precioBase; // Sin ajuste
        }
    }

}
