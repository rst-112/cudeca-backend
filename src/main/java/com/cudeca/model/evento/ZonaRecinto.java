package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ZonaRecinto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer aforoTotal;

    //relaciones

    @OneToMany(mappedBy = "zonaRecinto")
    private List<Asiento> asientos;
}
