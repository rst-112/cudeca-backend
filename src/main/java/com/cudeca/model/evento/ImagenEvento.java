package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private boolean esResumen;
    private boolean esPrincipal;
    private Integer orden;


    //relaciones
    @ManyToOne
    private Evento eventoAsociadoAImagenes;
}
