package com.cudeca.model.negocio;

import com.cudeca.model.enums.TipoMovimiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "MOVIMIENTOS_MONEDERO", indexes = {
        @Index(name = "ix_movmon_monedero_fecha", columnList = "monedero_id,fecha")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoMonedero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monedero_id", nullable = false)
    @NotNull(message = "El monedero es obligatorio")
    @ToString.Exclude
    private Monedero monedero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser positivo")
    private BigDecimal importe;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime fecha;

    @Column(length = 255)
    @Size(max = 255)
    private String referencia;

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = OffsetDateTime.now();
        }
    }
}
