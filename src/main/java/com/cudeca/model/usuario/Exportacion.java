package com.cudeca.model.usuario;

import com.cudeca.model.enums.FormatoExportacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "EXPORTACIONES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exportacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoExportacion formato;

    @Column(name = "generado_en", nullable = false)
    private Instant generadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    @ToString.Exclude
    private Administrador administrador;

    @PrePersist
    public void prePersist() {
        if (this.generadoEn == null) {
            this.generadoEn = Instant.now();
        }
    }
}