package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "tipoEntradaAsignada")
    private List<Asiento> asientosAsignados;

    @OneToMany(mappedBy = "tipoEntrada")
    private List<TipoEntrada> ArticulosEntradas;




    //crear metodos

    public BigDecimal getPrecioTotal() {
        return costeBase.add(donacionImplicita);
    }

    public Integer consultarDisponibilidad() {
        return cantidadTotal - cantidadVendida;
    }
}
