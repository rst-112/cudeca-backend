package com.cudeca.model.evento;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un tipo de entrada disponible para un evento.
 * Define precios, límites de compra y cantidad disponible.
 */
@Entity
@Table(name = "TIPOS_ENTRADA", indexes = {
        @Index(name = "ix_tipos_evento", columnList = "evento_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotNull(message = "El nombre del tipo de entrada es obligatorio")
    @Size(min = 1, max = 100)
    private String nombre;

    @Column(name = "coste_base", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El coste base es obligatorio")
    @Positive(message = "El coste base debe ser positivo")
    private BigDecimal costeBase;

    @Column(name = "donacion_implicita", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal donacionImplicita = BigDecimal.ZERO;

    @Column(name = "cantidad_total", nullable = false)
    @NotNull(message = "La cantidad total es obligatoria")
    @Positive(message = "La cantidad total debe ser positiva")
    private Integer cantidadTotal;

    @Column(name = "cantidad_vendida", nullable = false)
    @Builder.Default
    private Integer cantidadVendida = 0;

    @Column(name = "limite_por_compra", nullable = false)
    @Builder.Default
    private Integer limitePorCompra = 10;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    @ToString.Exclude
    private Evento evento;

    @OneToMany(mappedBy = "tipoEntrada", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Asiento> asientos = new ArrayList<>();

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Calcula el precio total (coste base + donación implícita).
     */
    public BigDecimal getPrecioTotal() {
        if (costeBase == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal donacion = (donacionImplicita != null) ? donacionImplicita : BigDecimal.ZERO;
        return costeBase.add(donacion);
    }

    /**
     * Consulta la cantidad de entradas disponibles.
     */
    public Integer consultarDisponibilidad() {
        int total = (cantidadTotal != null) ? cantidadTotal : 0;
        int vendida = (cantidadVendida != null) ? cantidadVendida : 0;
        return total - vendida;
    }

    /**
     * Verifica si hay disponibilidad para una cantidad específica.
     */
    public boolean tieneDisponibilidad(Integer cantidad) {
        return consultarDisponibilidad() >= cantidad;
    }
}
