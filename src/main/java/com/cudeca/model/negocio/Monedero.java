package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "MONEDEROS")
public class Monedero {

    // --- GETTERS Y SETTERS ESTÁNDAR (Necesarios para JPA/Hibernate) ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1:1 con Usuario
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    // Getter de saldo estándar (por si acaso frameworks lo necesitan)
    // "decimal" en el diagrama -> BigDecimal en Java
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    public Monedero() {}

    // --- MÉTODOS DEL DIAGRAMA ---

    // En el diagrama: consultarSaldo(): decimal
    public BigDecimal consultarSaldo() {
        return this.saldo;
    }

    // En el diagrama: ingresar(importe: decimal): void
    public void ingresar(BigDecimal importe) {
        if (importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo");
        }
        this.saldo = this.saldo.add(importe);
    }

    // En el diagrama: retirar(importe: decimal): void
    public void retirar(BigDecimal importe) {
        if (importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo");
        }
        if (this.saldo.compareTo(importe) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        this.saldo = this.saldo.subtract(importe);
    }

}