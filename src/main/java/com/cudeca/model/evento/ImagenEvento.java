package com.cudeca.model.evento;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entidad que representa una imagen asociada a un evento.
 */
@Entity
@Table(name = "IMAGENES_EVENTO", indexes = {
        @Index(name = "ix_imagenes_evento", columnList = "evento_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotNull(message = "La URL de la imagen es obligatoria")
    @Size(min = 1, max = 255)
    private String url;

    @Column(length = 255)
    @Size(max = 255)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "es_resumen", nullable = false)
    @Builder.Default
    private Boolean esResumen = false;

    @Column(name = "es_principal", nullable = false)
    @Builder.Default
    private Boolean esPrincipal = false;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    @ToString.Exclude
    private Evento evento;
}
