package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoNotificacion;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "NOTIFICACIONES", indexes = {
        @Index(name = "ix_notif_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = true)
    @ToString.Exclude
    private Compra compra;

    @Column(nullable = false, length = 120)
    private String tipo;

    @Column(nullable = false, length = 255)
    private String destino;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    // --- ESTADO Y CONTROL ---

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    // --- MÉTODOS DEL CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.estado == null) this.estado = EstadoNotificacion.PENDIENTE;
        if (this.intentos == null) this.intentos = 0;
    }

    // --- MÉTODOS DE NEGOCIO ---

    public void marcarComoEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.fechaEnvio = OffsetDateTime.from(Instant.now());
    }

    public void registrarFallo() {
        this.estado = EstadoNotificacion.ERROR;
        this.intentos++;
        this.fechaEnvio = OffsetDateTime.from(Instant.now());
    }
}
