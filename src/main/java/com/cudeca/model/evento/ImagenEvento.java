package com.cudeca.model.evento;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "IMAGENES_EVENTO") // <--- NOMBRE CORRECTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private boolean esResumen;
    private boolean esPrincipal;
    private Integer orden;


    //relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id") // <--- ¡AQUÍ ESTÁ LA CLAVE!
    @ToString.Exclude
    private Evento eventoAsociadoAImagenes;
}
