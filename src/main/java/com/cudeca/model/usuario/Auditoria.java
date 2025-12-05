package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "AUDITORIAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String entidad;

    @Column(name = "entidad_id", nullable = false, length = 120)
    private String entidadId;

    @Column(nullable = false, length = 120)
    private String accion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = OffsetDateTime.now();
        }
    }
}
