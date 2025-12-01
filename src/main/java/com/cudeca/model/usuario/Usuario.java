package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DiscriminatorFormula;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USUARIOS")
// --- CAMBIO IMPORTANTE AQUÍ ---
// Usamos SINGLE_TABLE para que NO busque la tabla 'comprador'
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Define el nombre de la columna que distingue tipos (revisa si en tu SQL es 'tipo_usuario' o 'dtype')
@DiscriminatorFormula("'COMPRADOR'")// ------------------------------
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String direccion;

    // Uso de Instant (UTC) como vimos en tu tabla de fechas
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relación con Roles (Tabla USUARIOS_ROLES del PDF pág. 48)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USUARIOS_ROLES",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default // Asegura que el Set no sea null al usar el Builder
    @ToString.Exclude // Evita bucles infinitos en los logs
    @EqualsAndHashCode.Exclude
    private Set<Rol> roles = new HashSet<>();

    // --- MÉTODOS DEL CICLO DE VIDA (PrePersist/PreUpdate) ---

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Añadir si quieres ver el historial de tokens de un usuario:
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<VerificacionCuenta> verificaciones = new HashSet<>();

    @OneToMany(mappedBy = "usuario")
    @Builder.Default // <--- AÑADIR
    private Set<Auditoria> auditorias = new HashSet<>(); //Por que es un Set?
    // --- MÉTODOS DE NEGOCIO (Del Diagrama UML) ---

    /**
     * Diagrama: actualizarPerfil(): void
     * Implementación: Actualiza los datos permitidos y refresca la fecha.
     */
    public void actualizarPerfil(String nuevoNombre, String nuevaDireccion) {
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            this.nombre = nuevoNombre;
        }
        if (nuevaDireccion != null) {
            this.direccion = nuevaDireccion;
        }
        // El @PreUpdate actualizará el 'updatedAt' automáticamente al guardar
    }

    /*
     * NOTA ARQUITECTÓNICA SOBRE: registrarse() e iniciarSesion()
     * * En Spring Boot, estos métodos NO se implementan aquí dentro.
     * La entidad Usuario solo representa DATOS.
     * * - registrarse() -> Se implementa en AuthService.register(DTO)
     * - iniciarSesion() -> Se implementa en AuthService.login(DTO)
     * * Poner lógica de autenticación aquí rompería el principio de
     * responsabilidad única y haría imposible inyectar Repositorios.
     */
}