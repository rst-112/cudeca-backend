package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class TipoEntrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private BigDecimal  costeBase;
    private BigDecimal donacionImplicita;
    private Integer cantidadTotal; //ver si dejar int o Integer
    private Integer cantidadVendida;
    private Integer limitePorCompra;

    //relaciones
    @ManyToOne
    private Evento eventoAsociado;

    //crear metodos

    public BigDecimal getPrecioTotal() {
        return costeBase.add(donacionImplicita);
    }

    public Integer consultarDisponibilidad() {
        return cantidadTotal - cantidadVendida;
    }
}
