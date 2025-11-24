package com.cudeca.model.evento;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TipoEntrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double conteBase;
    private Double donacionImplicita;
    private int cantidadTotal; //ver si dejar int o Integer
    private int cantidadVendida;
    private int limitePorCompra;

    //crear metodos
}
