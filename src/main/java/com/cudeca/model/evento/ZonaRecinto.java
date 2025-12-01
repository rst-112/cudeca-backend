package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ZONAS_RECINTO")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    @ToString.Exclude
    private Evento eventoAsociadoAZonaRecinto; // (O como se llame la variable)

    // Relación con Asientos (si la tienes)
    @OneToMany(mappedBy = "zonaRecinto", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Asiento> asientos;
}
