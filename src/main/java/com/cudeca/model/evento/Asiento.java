package com.cudeca.model.evento;

import com.cudeca.enums.EstadoAsiento;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigoEtiqueta;
    private Integer fila;
    private Integer columna;
    @Enumerated(EnumType.STRING)
    private EstadoAsiento estado;

    //relaciones

    @ManyToOne
    private ZonaRecinto zonaRecinto;

    //hacer metodos

    public void reservar(){
        this.estado = EstadoAsiento.RESERVADO;
    }

    public void liberar(){
        this.estado = EstadoAsiento.DISPONIBLE;
    }
}
