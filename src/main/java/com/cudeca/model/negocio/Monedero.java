package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MONEDEROS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Monedero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN 1:1 CON COMPRADOR ---
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario;

    // --- RELACIÓN 1:N CON MOVIMIENTOS ---
    @OneToMany(mappedBy = "monedero", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @JsonIgnore
    private List<MovimientoMonedero> movimientos = new ArrayList<>();

    // --- SALDO ---
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    // --- MÉTODOS DE NEGOCIO (Del Diagrama) ---

    public BigDecimal consultarSaldo() {
        return this.saldo;
    }

    public void ingresar(BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe a ingresar debe ser positivo.");
        }
        this.saldo = this.saldo.add(importe);
    }

    public void retirar(BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe a retirar debe ser positivo.");
        }
        if (this.saldo.compareTo(importe) < 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }
        this.saldo = this.saldo.subtract(importe);
    }

    // Helper para mantener la coherencia bidireccional
    public void addMovimiento(MovimientoMonedero movimiento) {
        movimientos.add(movimiento);
        movimiento.setMonedero(this);
    }
}