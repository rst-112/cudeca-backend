package com.cudeca.model.negocio;

import com.cudeca.enums.TipoDevolucion;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "DEVOLUCIONES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON COMPRA (Obligatoria) ---
    // SQL: compra_id BIGINT NOT NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- RELACIÓN CON PAGO (Si es tipo PASARELA) ---
    // SQL: pago_id BIGINT REFERENCES PAGOS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    @ToString.Exclude
    private Pago pago;

    // --- RELACIÓN CON MOVIMIENTO (Si es tipo MONEDERO) ---
    // SQL: mov_monedero_id BIGINT REFERENCES MOVIMIENTOS_MONEDERO
    // Es OneToOne porque un movimiento de abono corresponde a una devolución específica.
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "mov_monedero_id")
    @ToString.Exclude
    private MovimientoMonedero movimientoMonedero;

    // --- DATOS ECONÓMICOS ---

    // SQL: NUMERIC(12,2) NOT NULL CHECK (importe > 0)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(length = 255)
    private String motivo; // Ej: "Cancelación de evento", "Error usuario"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
    }

    // --- VALIDACIÓN DE NEGOCIO (Regla XOR del SQL) ---
    // SQL: CONSTRAINT chk_devol_tipo CHECK (...)
    // Validamos que si es MONEDERO tenga movimiento, y si es PASARELA tenga pago.
    @PrePersist
    @PreUpdate
    public void validarConsistencia() {
        if (tipo == TipoDevolucion.MONEDERO && movimientoMonedero == null) {
            throw new IllegalStateException("Una devolución a MONEDERO debe tener un Movimiento asociado.");
        }
        if (tipo == TipoDevolucion.PASARELA && pago == null) {
            throw new IllegalStateException("Una devolución a PASARELA debe tener un Pago asociado.");
        }
    }
}