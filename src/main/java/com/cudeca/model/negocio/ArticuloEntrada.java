package com.cudeca.model.negocio;

import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.TipoEntrada;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ENTRADA")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArticuloEntrada extends ArticuloCompra {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id")
    @ToString.Exclude
    private TipoEntrada tipoEntrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id")
    @ToString.Exclude
    private Asiento asiento;

    @OneToMany(mappedBy = "articuloEntrada", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<EntradaEmitida> entradasEmitidas = new ArrayList<>();

    @Override
    public boolean validar() {
        return getCantidad() != null && getCantidad() > 0
                && getPrecioUnitario() != null && getPrecioUnitario().compareTo(BigDecimal.ZERO) > 0
                && tipoEntrada != null;
    }
}
