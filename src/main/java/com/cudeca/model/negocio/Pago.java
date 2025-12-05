package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoPago;
import com.cudeca.model.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    @ToString.Exclude
    private Suscripcion suscripcion;

    // --- CONEXIONES SALIENTES (Consecuencias) ---

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Devolucion> devoluciones = new ArrayList<>();

    // --- DATOS ECONÓMICOS ---

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

    @Column(name = "id_transaccion_externa", unique = true, length = 255)
    private String idTransaccionExterna;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPago.PENDIENTE;
        }
    }

    // --- MÉTODOS DE NEGOCIO (Del Diagrama UML) ---

    public void aprobar() {
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
