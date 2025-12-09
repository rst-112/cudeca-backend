package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

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

    // --- RELACIÓN CON COMPRA (1:1) ---
    // SQL: compra_id BIGINT NOT NULL UNIQUE REFERENCES COMPRAS
    // Es "optional = false" porque un recibo no puede existir sin una compra asociada.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", unique = true, nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- DATOS DEL RECIBO ---

    // SQL: fecha_emision TIMESTAMPTZ NOT NULL DEFAULT now()
    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private Instant fechaEmision;

    // SQL: total NUMERIC(12,2) NOT NULL CHECK (total >= 0)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // SQL: resumen TEXT
    // Usamos @Lob para textos largos compatibles tanto con PostgreSQL como con H2
    @Lob
    @Column(name = "resumen")
    private String resumen;

    // --- CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) {
            this.fechaEmision = Instant.now();
        }

        // Validación de Integridad (CHECK >= 0)
        if (this.total != null && this.total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total del recibo no puede ser negativo.");
        }
    }
}