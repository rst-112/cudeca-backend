package com.cudeca.model.evento;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una zona o sector dentro del recinto de un evento.
 */
@Entity
@Table(name = "ZONAS_RECINTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZonaRecinto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotNull(message = "El nombre de la zona es obligatorio")
    @Size(min = 1, max = 100)
    private String nombre;

    @Column(name = "aforo_total", nullable = false)
    @NotNull(message = "El aforo total es obligatorio")
    @Positive(message = "El aforo total debe ser positivo")
    private Integer aforoTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "objetos_decorativos", columnDefinition = "jsonb")
    private String objetosDecorativos;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    @ToString.Exclude
    private Evento evento;

    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Asiento> asientos = new ArrayList<>();

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Calcula el número de asientos aún disponibles en la zona.
     */
    public Integer calcularAsientosDisponibles() {
        return asientos.stream()
                .filter(Asiento::estaDisponible)
                .mapToInt(asiento -> 1)
                .sum();
    }
}
