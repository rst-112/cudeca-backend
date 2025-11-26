package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "ROLES")
public class Rol {

    // Getters y Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre; // Ej: "ADMIN", "DONANTE"

    // --- RELACIÓN CON PERMISOS (El equivalente a la clase 'RolPermiso' del diagrama) ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ROLES_PERMISOS",                  // Nombre de la tabla SQL
            joinColumns = @JoinColumn(name = "rol_id"), // Tu ID en la tabla intermedia
            inverseJoinColumns = @JoinColumn(name = "permiso_id") // El ID del otro lado
    )
    private Set<Permiso> permisos = new HashSet<>();
    // -----------------------------------------------------------------------------------

    // Constructor vacío
    public Rol() {}

    public Rol(String nombre) {
        this.nombre = nombre;
    }

    // Métodos Helper (Buenas prácticas para añadir/quitar)
    public void addPermiso(Permiso permiso) {
        this.permisos.add(permiso);
    }

    public void removePermiso(Permiso permiso) {
        this.permisos.remove(permiso);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(nombre, rol.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}