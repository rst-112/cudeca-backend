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
public class Asiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigoEtiqueta;
    private Integer fila;
    private Integer columna;
    //@Enumerated(EnumType.STRING)
    //private EstadoAsiento estado;

    //hacer metodos
}
