package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import com.cudeca.enums.EstadoRetiro;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "SOLICITUDES_RETIRO") // [cite: 4821]
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON EL USUARIO/COMPRADOR ---
    // SQL: usuario_id BIGINT NOT NULL REFERENCES USUARIOS
    // Java: Usamos FetchType.LAZY por rendimiento (no traer al usuario si no hace falta).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude // Evita bucles al imprimir logs
    private Usuario usuario;

    // --- DATOS ECONÓMICOS ---
    // SQL: NUMERIC(12,2) NOT NULL CHECK (importe > 0) [cite: 4838]
    // Java: BigDecimal es OBLIGATORIO para dinero (double pierde precisión).
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    // SQL: VARCHAR(34) NOT NULL [cite: 4839]
    @Column(name = "iban_destino", nullable = false, length = 34)
    private String ibanDestino;

    // --- ESTADO ---
    // SQL: estado_retiro NOT NULL DEFAULT 'PENDIENTE' [cite: 4840]
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default // Asegura que el Builder use el valor por defecto si no se especifica
    private EstadoRetiro estado = EstadoRetiro.PENDIENTE;

    // --- FECHAS (Auditoría) ---
    // SQL: TIMESTAMPTZ NOT NULL DEFAULT now()
    // Java: Usamos Instant para guardar el momento exacto en UTC.
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private Instant fechaSolicitud;

    // --- MÉTODOS DEL CICLO DE VIDA ---

    @PrePersist
    protected void onCreate() {
        // Asigna la fecha automáticamente antes de guardar en BD
        this.fechaSolicitud = Instant.now();
        // Garantiza el estado inicial si venía nulo
        if (this.estado == null) {
            this.estado = EstadoRetiro.PENDIENTE;
        }
    }

    // --- MÉTODOS DE NEGOCIO (Domain Logic) ---

    /**
     * Diagrama: procesar(): void [cite: 1841]
     * Cambia el estado de la solicitud a PROCESADA.
     * Debería ser llamado por el Servicio cuando el admin confirme la transferencia.
     */
    public void procesar() {
        if (this.estado != EstadoRetiro.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden procesar solicitudes pendientes.");
        }
        this.estado = EstadoRetiro.PROCESADA;
    }
}