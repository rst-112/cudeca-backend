package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Comprador; // <--- Importante: Comprador, no Usuario
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
    // En BD la FK es 'usuario_id' (porque Comprador es un Usuario),
    // pero en Java lo tratamos como 'Comprador' para cumplir el diagrama.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    @ToString.Exclude
    private Comprador comprador;

    // --- RELACIÓN 1:N CON MOVIMIENTOS ---
    // Diagrama: Monedero "1" -- "0..*" MovimientoMonedero
    @OneToMany(mappedBy = "monedero", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
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
        // Nota: En un servicio real, aquí crearías también el MovimientoMonedero de tipo ABONO
    }

    public void retirar(BigDecimal importe) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe a retirar debe ser positivo.");
        }
        if (this.saldo.compareTo(importe) < 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }
        this.saldo = this.saldo.subtract(importe);
        // Nota: Aquí se crearía el MovimientoMonedero de tipo CARGO/RETIRO
    }

    // Helper para mantener la coherencia bidireccional
    public void addMovimiento(MovimientoMonedero movimiento) {
        movimientos.add(movimiento);
        movimiento.setMonedero(this);
    }
}