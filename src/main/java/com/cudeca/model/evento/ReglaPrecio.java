package com.cudeca.model.evento;

import com.cudeca.model.enums.TipoAjusteRegla;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa una regla de ajuste de precios (descuento/recargo) para un evento.
 */
@Entity
@Table(name = "REGLAS_PRECIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotNull(message = "El nombre de la regla es obligatorio")
    @Size(min = 1, max = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ajuste", nullable = false)
    @NotNull(message = "El tipo de ajuste es obligatorio")
    private TipoAjusteRegla tipoAjuste;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El valor es obligatorio")
    @Positive(message = "El valor debe ser positivo")
    private BigDecimal valor;

    @Column(name = "requiere_suscripcion", nullable = false)
    @Builder.Default
    private Boolean requiereSuscripcion = false;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    @ToString.Exclude
    private Evento evento;

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Aplica la regla de ajuste de precio a un precio base.
     */
    public BigDecimal aplicarAjuste(BigDecimal precioBase) {
        if (precioBase == null || precioBase.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        if (tipoAjuste == TipoAjusteRegla.PORCENTAJE) {
            BigDecimal ajuste = precioBase.multiply(valor).divide(new BigDecimal(100));
            return precioBase.subtract(ajuste);
        } else if (tipoAjuste == TipoAjusteRegla.FIJO) {
            return precioBase.subtract(valor);
        }
        return precioBase;
    }

}
