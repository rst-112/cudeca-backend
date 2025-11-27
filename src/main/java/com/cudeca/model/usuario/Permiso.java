package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PERMISOS") // Nombre exacto según el DDL (Línea 92 del PDF)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Según DDL: codigo VARCHAR(120) NOT NULL UNIQUE
    @Column(nullable = false, unique = true, length = 120)
    private String codigo; // Ej: "EVENTO_READ", "USUARIO_WRITE"

    /*
     * NOTA TÉCNICA SOBRE RELACIÓN INVERSA:
     * No es estrictamente necesario mapear la lista de Roles aquí ("mappedBy")
     * a menos que necesites preguntar: "¿Qué roles tienen el permiso X?".
     * * Si lo necesitaras en el futuro, descomenta esto:
     * * @ManyToMany(mappedBy = "permisos")
     * @ToString.Exclude
     * @EqualsAndHashCode.Exclude
     * private Set<Rol> roles = new HashSet<>();
     * * Por ahora, lo mantenemos simple (Unidireccional) para evitar complejidad.
     */
    /*
    UsuarioRol.java: EXISTE (porque tiene fecha).

RolPermiso.java: NO EXISTE (es una relación @ManyToMany pura gestionada dentro de Rol).
     */
}