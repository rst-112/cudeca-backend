package com.cudeca.model.negocio;

import com.cudeca.model.enums.TipoDevolucion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad que registra una devolución de dinero asociada a una compra.
 * Puede ser devuelto a través de la pasarela de pago o al monedero del usuario.
 */
@Entity
@Table(name = "DEVOLUCIONES", indexes = {
        @Index(name = "ix_devol_compra", columnList = "compra_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON COMPRA (Obligatoria) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @NotNull(message = "La compra es obligatoria")
    @ToString.Exclude
    private Compra compra;

    // --- RELACIÓN CON PAGO (Si es tipo PASARELA) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    @ToString.Exclude
    private Pago pago;

    // --- RELACIÓN CON MOVIMIENTO (Si es tipo MONEDERO) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mov_monedero_id")
    @ToString.Exclude
    private MovimientoMonedero movimientoMonedero;

    // --- DATOS ECONÓMICOS ---
    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser positivo")
    private BigDecimal importe;

    @Column(length = 255)
    @Size(max = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El tipo de devolución es obligatorio")
    private TipoDevolucion tipo;

    // --- AUDITORÍA ---
    @Column(nullable = false, updatable = false)
    private Instant fecha;

    // --- CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = Instant.now();
        }
        validarConsistencia();
    }

    @PreUpdate
    public void preUpdate() {
        validarConsistencia();
    }

    /**
     * Valida que la devolución sea consistente según su tipo.
     * - Si es MONEDERO: debe tener movimientoMonedero
     * - Si es PASARELA: debe tener pago
     */
    private void validarConsistencia() {
        if (tipo == TipoDevolucion.MONEDERO && movimientoMonedero == null) {
            throw new IllegalStateException("Una devolución a MONEDERO debe tener un Movimiento asociado.");
        }
        if (tipo == TipoDevolucion.PASARELA && pago == null) {
            throw new IllegalStateException("Una devolución a PASARELA debe tener un Pago asociado.");
        }
    }
}