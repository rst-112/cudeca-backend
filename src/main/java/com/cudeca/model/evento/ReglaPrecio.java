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
public class ReglaPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    //@Enumerated(EnumType.STRING)
    //private TipoAjusteRegla tipo;
    private Double valor;
    private boolean requiereSuscricion;

    //crear metodos

}
