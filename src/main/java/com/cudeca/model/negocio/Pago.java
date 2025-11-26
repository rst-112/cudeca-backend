package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoPago;
import com.cudeca.model.enums.MetodoPago;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAGOS")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Compra (Obligatoria según SQL para pagos de carritos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    // Relación con Suscripción (Opcional/Nullable en SQL)
    // DESCOMENTAR CUANDO TENGÁIS LA CLASE SUSCRIPCION
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "suscripcion_id")
    // private Suscripcion suscripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado;

    // Para guardar el ID que nos devuelve PayPal/Stripe/Redsys
    @Column(name = "id_transaccion_externa", unique = true)
    private String idTransaccionExterna;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Pago() {}

    // Ciclo de vida
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
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
        this.estado = EstadoPago.ANULADO;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public MetodoPago getMetodo() { return metodo; }
    public void setMetodo(MetodoPago metodo) { this.metodo = metodo; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public String getIdTransaccionExterna() { return idTransaccionExterna; }
    public void setIdTransaccionExterna(String idTransaccionExterna) { this.idTransaccionExterna = idTransaccionExterna; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}