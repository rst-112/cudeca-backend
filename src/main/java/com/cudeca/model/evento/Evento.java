package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    @Temporal(TemporalType.DATE) //cambiar a el que se eliga al final
    private Date fechaInicio;
    @Temporal(TemporalType.DATE)
    private Date fechaFin;
    private String lugar;
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;
    private Double objetivoRecaudacion;
    private String imagenUrl;

    //crear metodos



}
