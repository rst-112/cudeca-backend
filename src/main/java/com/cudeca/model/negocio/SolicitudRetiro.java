package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoRetiro;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "SOLICITUDES_RETIRO")
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación N:1 con Usuario (Muchas solicitudes pertenecen a un Usuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "iban_destino", nullable = false, length = 34)
    private String ibanDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRetiro estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    // Constructor vacío obligatorio
    public SolicitudRetiro() {}

    // Se ejecuta antes de guardar en la base de datos automáticamente
    @PrePersist
    public void prePersist() {
        this.fechaSolicitud = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoRetiro.PENDIENTE;
        }
    }

    // --- MÉTODO DEL DIAGRAMA ---
    // En el diagrama: procesar(): void
    public void procesar() {
        // Lógica de negocio: cambiar el estado a PROCESADA
        this.estado = EstadoRetiro.PROCESADA;
    }

    // --- GETTERS Y SETTERS ---

}