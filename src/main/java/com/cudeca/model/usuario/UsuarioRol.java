package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "USUARIOS_ROLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "asignado_en", nullable = false, updatable = false)
    private OffsetDateTime asignadoEn;

    @PrePersist
    protected void onCreate() {
        if (this.asignadoEn == null) {
            this.asignadoEn = OffsetDateTime.now();
        }
    }
}
