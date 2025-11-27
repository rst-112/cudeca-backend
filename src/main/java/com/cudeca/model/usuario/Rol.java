package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ROLES") // Nombre exacto del DDL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DDL: nombre VARCHAR(80) NOT NULL UNIQUE
    @Column(nullable = false, unique = true, length = 80)
    private String nombre; // Ej: "ADMIN", "SOCIO", "STAFF"

    // --- RELACIÓN CON PERMISOS (ManyToMany Puro) ---
    // Al no tener datos extra (fecha, etc), JPA lo maneja solo con esta anotación.

    @ManyToMany(fetch = FetchType.EAGER) // EAGER: Al cargar el Rol, trae sus permisos inmediatamente (vital para Seguridad)
    @JoinTable(
            name = "ROLES_PERMISOS",                  // Tabla intermedia del SQL
            joinColumns = @JoinColumn(name = "rol_id"), // Mi ID
            inverseJoinColumns = @JoinColumn(name = "permiso_id") // El ID del otro
    )
    @Builder.Default // Para que el Builder inicie el HashSet vacío en vez de null
    @ToString.Exclude // Evita bucles infinitos
    @EqualsAndHashCode.Exclude
    private Set<Permiso> permisos = new HashSet<>();

    // --- RELACIÓN INVERSA CON USUARIO_ROL (Opcional pero recomendada) ---
    // Esto permite saber "quiénes tienen este rol" consultando desde el objeto Rol.

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<UsuarioRol> asignaciones = new HashSet<>();

    // --- MÉTODOS HELPER (Para gestión limpia) ---

    public void addPermiso(Permiso permiso) {
        this.permisos.add(permiso);
    }

    public void removePermiso(Permiso permiso) {
        this.permisos.remove(permiso);
    }
}