package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "AUDITORIAS") // Nombre en plural y mayúsculas
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON EL ACTOR (Usuario) ---
    // Eliminamos el campo 'Integer actorId' porque ya tenemos el objeto 'Usuario'.
    // JPA se encarga de guardar el ID en la columna 'usuario_id'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) // Asumimos que siempre hay un responsable
    @ToString.Exclude
    private Usuario usuario;

    // --- DATOS DEL EVENTO ---

    // Sobre qué actuó (Ej: "COMPRA", "EVENTO")
    @Column(nullable = false, length = 50)
    private String entidad;

    // El ID del objeto afectado (Ej: "105")
    // Lo guardamos como String por si auditamos cosas con IDs no numéricos
    @Column(name = "entidad_id", nullable = false, length = 50)
    private String entidadId;

    @Column(nullable = false, length = 50)
    private String accion;

    // CAMBIO AQUÍ: Usamos @Transient para que Hibernate NO busque la columna
    // Así la aplicación arrancará sí o sí.
    @Transient
    private String detalles;
    // --- AUDITORÍA DE TIEMPO ---

    // Usamos Instant (UTC) en lugar de LocalDate para tener precisión de milisegundos
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