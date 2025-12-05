package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "RECIBOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", unique = true, nullable = false)
    @ToString.Exclude
    private Compra compra;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private OffsetDateTime fechaEmision;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) {
            this.fechaEmision = OffsetDateTime.now();
        }
        if (this.total != null && this.total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total del recibo no puede ser negativo.");
        }
    }
}
