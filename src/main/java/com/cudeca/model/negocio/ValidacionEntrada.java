package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "VALIDACIONES_ENTRADA", indexes = {
        @Index(name = "ix_validaciones_entrada", columnList = "entrada_emitida_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_emitida_id", nullable = false)
    @ToString.Exclude
    private EntradaEmitida entradaEmitida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario personalValidador;

    // --- DATOS ---
    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Column(name = "dispositivo_id", length = 120)
    private String dispositivoId;

    @Column(nullable = false)
    @Builder.Default
    private boolean revertida = false;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = OffsetDateTime.from(Instant.now());
        }
    }
}
