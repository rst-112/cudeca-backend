package com.cudeca.model.usuario;

import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.SolicitudRetiro;
import com.cudeca.model.negocio.Suscripcion;
import com.cudeca.model.negocio.ValidacionEntrada;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "USUARIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USUARIOS_ROLES",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Rol> roles = new HashSet<>();

    // --- CAMPOS DE "COMPRADOR" ---

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Monedero monedero;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<SolicitudRetiro> solicitudesRetiro = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Suscripcion> suscripciones = new ArrayList<>();

    // --- CAMPOS DE "PERSONAL" ---
    @OneToMany(mappedBy = "personalValidador", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<ValidacionEntrada> validacionesRealizadas = new ArrayList<>();

    // --- CAMPOS DE "ADMIN" ---
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Exportacion> exportaciones = new ArrayList<>();

    // --- AUDITORÍA Y OTROS ---
    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private Set<Auditoria> auditorias = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<VerificacionCuenta> verificaciones = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<DatosFiscales> datosFiscales = new ArrayList<>();

    // --- CICLO DE VIDA ---
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // --- MÉTODOS ---
    public void actualizarPerfil(String nuevoNombre, String nuevaDireccion) {
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            this.nombre = nuevoNombre;
        }
        if (nuevaDireccion != null) {
            this.direccion = nuevaDireccion;
        }
    }

    public boolean esAdmin() {
        return roles.stream().anyMatch(r -> "ADMINISTRADOR".equalsIgnoreCase(r.getNombre()));
    }

    // --- IMPLEMENTACIÓN DE USERDETAILS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
