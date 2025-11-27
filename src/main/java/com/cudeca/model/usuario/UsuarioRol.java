package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "USUARIOS_ROLES") // Nombre exacto del DDL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIONES (Many-to-One hacia ambos lados) ---

    // Relación con Usuario (FK: usuario_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude // Evitamos bucles infinitos en los logs
    private Usuario usuario;

    // Relación con Rol (FK: rol_id)
    @ManyToOne(fetch = FetchType.EAGER) // Eager porque al cargar el permiso queremos saber qué rol es
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // --- DATOS PROPIOS DE LA RELACIÓN ---

    // DDL: asignado_en TIMESTAMPTZ NOT NULL DEFAULT now()
    @Column(name = "asignado_en", nullable = false, updatable = false)
    private Instant asignadoEn;

    // --- AUDITORÍA AUTOMÁTICA ---

    @PrePersist
    protected void onCreate() {
        this.asignadoEn = Instant.now();
    }
}