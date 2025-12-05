package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoRetiro;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "SOLICITUDES_RETIRO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "iban_destino", nullable = false, length = 34)
    private String ibanDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoRetiro estado = EstadoRetiro.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private OffsetDateTime fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = OffsetDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoRetiro.PENDIENTE;
        }
    }

    public void procesar() {
        if (this.estado != EstadoRetiro.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden procesar solicitudes pendientes");
        }
        this.estado = EstadoRetiro.PROCESADA;
    }
}
