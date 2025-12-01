package com.cudeca.model.negocio;

import com.cudeca.model.usuario.PersonalEvento;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "VALIDACIONES_ENTRADA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Entrada (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_emitida_id", nullable = false)
    @ToString.Exclude
    private EntradaEmitida entradaEmitida;

    // --- AQUÍ ESTÁ EL PROBLEMA ---
    // Asegúrate de que tienes esta línea @JoinColumn(name = "usuario_id")
    // Si no está, Hibernate busca 'personal_validador_id' y falla.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id") // <--- ¡VERIFICA QUE ESTO ESTÁ ESCRITO!
    @ToString.Exclude
    private PersonalEvento personalValidador;

    // --- DATOS ---
    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "dispositivo_id", length = 50)
    private String dispositivoId;

    @Column(nullable = false)
    @Builder.Default
    private boolean revertida = false;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = Instant.now();
        }
    }
}