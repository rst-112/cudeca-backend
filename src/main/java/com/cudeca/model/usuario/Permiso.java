package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "PERMISOS")
public class Permiso {

    // Getters y Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String codigo; // Ej: "EVENTO_READ", "USER_WRITE"

    // Constructor vacío obligatorio
    public Permiso() {}

    // Constructor útil
    public Permiso(String codigo) {
        this.codigo = codigo;
    }

    // HashCode y Equals son vitales en Sets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(codigo, permiso.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}