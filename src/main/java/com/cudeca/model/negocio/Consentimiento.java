package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "CONSENTIMIENTOS", uniqueConstraints = {
        @UniqueConstraint(name = "ux_compra_tipo", columnNames = {"compra_id", "tipo"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consentimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON COMPRA ---
    // SQL: compra_id BIGINT NOT NULL REFERENCES COMPRAS
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- DATOS DEL CONSENTIMIENTO ---

    // SQL: tipo VARCHAR(60) NOT NULL
    // Ej: "TERMINOS_Y_CONDICIONES", "MARKETING", "CESION_DATOS"
    @Column(nullable = false, length = 60)
    private String tipo;

    // SQL: version VARCHAR(40) NOT NULL
    // Vital para saber QUÉ texto exacto firmó (ej: "v1.0", "2025-NOV")
    @Column(nullable = false, length = 40)
    private String version;

    // SQL: otorgado BOOLEAN NOT NULL
    @Column(nullable = false)
    private boolean otorgado;

    // --- AUDITORÍA ---

    // SQL: fecha TIMESTAMPTZ NOT NULL DEFAULT now()
    @Column(nullable = false, updatable = false)
    private Instant fecha;

    // --- CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = Instant.now();
        }
    }
}