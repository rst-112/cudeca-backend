package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "USUARIOS")
public class Usuario {

    // Getters y Setters
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 100)
    private String nombre;

    @Setter
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Setter
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Setter
    private String direccion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- AQUÍ ESTÁ LA RELACIÓN QUE PIDE EL CHECKLIST ---
    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USUARIOS_ROLES",                  // Nombre de la tabla intermedia en BBDD
            joinColumns = @JoinColumn(name = "usuario_id"), // FK hacia Usuario
            inverseJoinColumns = @JoinColumn(name = "rol_id") // FK hacia Rol
    )
    private Set<Rol> roles = new HashSet<>();
    // ---------------------------------------------------

    // Constructor vacío obligatorio para JPA
    public Usuario() {}

    // Constructor útil
    public Usuario(String nombre, String email, String passwordHash) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Métodos del ciclo de vida para las fechas
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos helper para gestionar la relación ManyToMany fácilmente
    public void addRol(Rol rol) {
        this.roles.add(rol);
    }

    public void removeRol(Rol rol) {
        this.roles.remove(rol);
    }

}