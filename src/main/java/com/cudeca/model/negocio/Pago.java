package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoPago;
import com.cudeca.model.enums.MetodoPago;
// import com.cudeca.model.negocio.Devolucion; // <-- DESCOMENTAR CUANDO TENGAS LA CLASE DEVOLUCION

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PAGOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- CONEXIONES ENTRANTES (Origen del dinero) ---

    // Relación con Compra (SQL: compra_id NOT NULL)
    // Una compra tiene varios intentos de pago (o pagos fraccionados si se permitiera)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // Relación con Suscripción (SQL: suscripcion_id)
    // Como ya creaste la clase Suscripcion, la dejamos activa.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    @ToString.Exclude
    private Suscripcion suscripcion;

    // --- CONEXIONES SALIENTES (Consecuencias) ---

    // Relación con Devoluciones (SQL: Devolucion tiene pago_id)
    // Un pago puede ser reembolsado parcial o totalmente varias veces.
    // ESTA CLASE AÚN NO LA TIENES, LA DEJO PREPARADA:

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Devolucion> devoluciones = new ArrayList<>();

    // --- DATOS ECONÓMICOS ---

    // SQL: NUMERIC(12,2) NOT NULL CHECK (importe > 0)
    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser positivo")
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    // ID de la pasarela (Stripe/PayPal/Redsys) para conciliación
    @Column(name = "id_transaccion_externa", unique = true)
    private String idTransaccionExterna;

    // --- AUDITORÍA ---

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.estado == null) {
            this.estado = EstadoPago.PENDIENTE;
        }
    }

    // --- MÉTODOS DE NEGOCIO (Del Diagrama UML) ---

    public void aprobar() {
        // Aquí podrías añadir lógica extra (ej: generar factura si fuese necesario)
        this.estado = EstadoPago.APROBADO;
    }

    public void rechazar() {
        this.estado = EstadoPago.RECHAZADO;
    }

    public void anular() {
        // Solo se anula si no estaba ya pagado, por ejemplo
        if (this.estado != EstadoPago.APROBADO) {
            this.estado = EstadoPago.ANULADO;
        } else {
            throw new IllegalStateException("No se puede anular un pago ya aprobado. Debe solicitarse devolución.");
        }
    }
}