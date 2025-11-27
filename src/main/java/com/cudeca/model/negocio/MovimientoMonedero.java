package com.cudeca.model.negocio;

import com.cudeca.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "MOVIMIENTOS_MONEDERO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoMonedero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación N:1 con Monedero (FK: monedero_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monedero_id", nullable = false)
    @ToString.Exclude
    private Monedero monedero;

    // --- NUEVA CONEXIÓN AÑADIDA ---
    // Relación inversa con Devolucion.
    // Usamos "mappedBy" porque la FK (mov_monedero_id) está en la tabla DEVOLUCIONES.
    @OneToMany(mappedBy = "movimientoMonedero", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Devolucion devolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false)
    private Instant fecha;

    @Column(length = 255)
    private String referencia; // Ej: "Devolución Compra #123"

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) this.fecha = Instant.now();
    }
}