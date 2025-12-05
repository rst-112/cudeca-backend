package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- DATOS DEL CONSENTIMIENTO ---

    // SQL: tipo VARCHAR(60) NOT NULL
    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, length = 40)
    private String version;

    @Column(nullable = false)
    private boolean otorgado;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = OffsetDateTime.now();
        }
    }
}
