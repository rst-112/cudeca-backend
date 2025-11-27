package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaRecinto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer aforoTotal;

    //relaciones

    @ManyToOne
    private Evento eventoAsociadoAZonaRecinto;

    @OneToMany(mappedBy = "zonaRecinto")
    private List<Asiento> asientos;
}
