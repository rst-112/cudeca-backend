package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = true)
    @ToString.Exclude
    private Invitado invitado;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 180)
    private String token;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(nullable = false)
    @Builder.Default
    private boolean usado = false;

    @PrePersist
    @PreUpdate
    public void validarXor() {
        boolean tieneUsuario = (this.usuario != null);
        boolean tieneInvitado = (this.invitado != null);

        if (tieneUsuario == tieneInvitado) {
            throw new IllegalStateException("La verificación debe pertenecer EXCLUSIVAMENTE a un Usuario o a un Invitado.");
        }
    }

    public boolean isValid() {
        return !this.usado && Instant.now().isBefore(this.expiraEn.toInstant());
    }
}
