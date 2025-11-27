package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "VERIFICACIONES_CUENTA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIONES (Regla XOR: Solo uno de los dos debe estar relleno) ---

    // Relación N:1 con Usuario (Usuario puede tener varios tokens de recuperación)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    // Relación N:1 con Invitado (Invitado puede tener varios tokens de reclamación)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = true)
    @ToString.Exclude
    private Invitado invitado;

    // --- DATOS DEL TOKEN ---

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 180)
    private String token;

    // SQL: expira_en TIMESTAMPTZ NOT NULL
    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    // SQL: usado BOOLEAN NOT NULL DEFAULT FALSE
    @Column(nullable = false)
    @Builder.Default
    private boolean usado = false;

    // --- VALIDACIONES DE NEGOCIO (Regla XOR) ---
    // SQL: CHECK (num_nonnulls(usuario_id, invitado_id) = 1)

    @PrePersist
    @PreUpdate
    public void validarXor() {
        boolean tieneUsuario = (this.usuario != null);
        boolean tieneInvitado = (this.invitado != null);

        // O uno u otro, pero no ambos y no ninguno. (Operación XOR)
        if (tieneUsuario == tieneInvitado) { // Si son iguales (ambos true o ambos false) -> Error
            throw new IllegalStateException("La verificación debe pertenecer EXCLUSIVAMENTE a un Usuario o a un Invitado.");
        }
    }

    // --- MÉTODO DE NEGOCIO (Del Diagrama) ---

    // Diagrama: isValid(): boolean
    public boolean isValid() {
        return !this.usado && Instant.now().isBefore(this.expiraEn);
    }
}