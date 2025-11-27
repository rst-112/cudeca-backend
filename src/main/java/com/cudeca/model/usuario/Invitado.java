package com.cudeca.model.usuario;

import com.cudeca.model.negocio.Compra;
import com.cudeca.model.usuario.VerificacionCuenta; // <-- DESCOMENTAR CUANDO TENGAS LA CLASE VERIFICACION

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "INVITADOS") // Nombre exacto del SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DATOS DEL INVITADO ---
    // SQL: email VARCHAR(150) NOT NULL UNIQUE
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // --- CONEXIONES: HISTORIAL DE COMPRAS ---
    // Un invitado puede hacer muchas compras con el mismo email antes de registrarse.
    // Relación bidireccional con Compra.

    @OneToMany(mappedBy = "invitado", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Compra> compras = new ArrayList<>();

    // --- CONEXIONES: SEGURIDAD (Reclamar Cuenta) ---
    // SQL: Tabla VERIFICACIONES_CUENTA tiene invitado_id
    // Esta relación es vital para el CU1.2 "Reclamar cuenta".
    // Permite enviar un token al invitado para que fusione sus datos con un nuevo usuario.

    @OneToMany(mappedBy = "invitado", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<VerificacionCuenta> verificaciones = new ArrayList<>();


    // --- MÉTODOS HELPER ---

    public void addCompra(Compra compra) {
        this.compras.add(compra);
        compra.setInvitado(this);
    }
}