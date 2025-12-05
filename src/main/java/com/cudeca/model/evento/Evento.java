package com.cudeca.model.evento;

import com.cudeca.model.enums.EstadoEvento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un evento organizado por Cudeca.
 * Hereda características de gestión de fechas, estado y recaudación.
 */
@Entity
@Table(name = "EVENTOS", indexes = {
        @Index(name = "ix_eventos_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotNull(message = "El nombre del evento es obligatorio")
    @Size(min = 1, max = 150, message = "El nombre debe tener entre 1 y 150 caracteres")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    @NotNull(message = "La fecha de inicio es obligatoria")
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @Column(length = 255)
    @Size(max = 255)
    private String lugar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @Column(name = "objetivo_recaudacion", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal objetivoRecaudacion = BigDecimal.ZERO;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;


    // --- RELACIONES ---

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<TipoEntrada> tiposEntrada = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ReglaPrecio> reglasPrecios = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ZonaRecinto> zonasRecinto = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ImagenEvento> imagenesEvento = new ArrayList<>();

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Publica el evento cambiando su estado a PUBLICADO.
     */
    public void publicar() {
        this.estado = EstadoEvento.PUBLICADO;
    }

    /**
     * Cancela el evento cambiando su estado a CANCELADO.
     */
    public void cancelar() {
        this.estado = EstadoEvento.CANCELADO;
    }

    /**
     * Finaliza el evento cambiando su estado a FINALIZADO.
     */
    public void finalizar() {
        this.estado = EstadoEvento.FINALIZADO;
    }
}
