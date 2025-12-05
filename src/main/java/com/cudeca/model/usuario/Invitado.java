package com.cudeca.model.usuario;

import com.cudeca.model.negocio.Compra;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un usuario invitado (sin registrarse en la plataforma).
 * Puede hacer compras y posteriormente reclamar su cuenta.
 */
@Entity
@Table(name = "INVITADOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DATOS DEL INVITADO ---
    @Column(nullable = false, unique = true, length = 150)
    @NotNull(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150)
    private String email;

    // --- CONEXIONES: HISTORIAL DE COMPRAS ---
    @OneToMany(mappedBy = "invitado", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Compra> compras = new ArrayList<>();

    // --- CONEXIONES: SEGURIDAD (Reclamar Cuenta) ---
    @OneToMany(mappedBy = "invitado", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<VerificacionCuenta> verificaciones = new ArrayList<>();

    // --- MÉTODOS HELPER ---

    /**
     * Añade una compra al historial del invitado.
     */
    public void addCompra(Compra compra) {
        if (this.compras == null) {
            this.compras = new ArrayList<>();
        }
        this.compras.add(compra);
        compra.setInvitado(this);
    }

    /**
     * Añade una verificación de cuenta.
     */
    public void addVerificacion(VerificacionCuenta verificacion) {
        if (this.verificaciones == null) {
            this.verificaciones = new ArrayList<>();
        }
        this.verificaciones.add(verificacion);
        verificacion.setInvitado(this);
    }
}
