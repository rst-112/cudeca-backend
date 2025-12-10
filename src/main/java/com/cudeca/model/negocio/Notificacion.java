package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoNotificacion;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "NOTIFICACIONES") //
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- CONEXIONES (Origen del evento) ---
    // Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario; // <--- ¡TIENE QUE LLAMARSE 'usuario'!
    // Relación con Compra (Opcional: ej. Email de Entradas)
    // SQL: compra_id BIGINT REFERENCES COMPRAS (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = true)
    @ToString.Exclude
    private Compra compra;

    // --- DATOS DEL ENVÍO ---

    // SQL: tipo VARCHAR(120) NOT NULL
    // Ej: "CONFIRMACION_COMPRA", "RECUPERAR_PASS", "ALTA_SOCIO"
    @Column(nullable = false, length = 120)
    private String tipo;

    // SQL: destino VARCHAR(255) NOT NULL
    // El email al que se envía (ej: cliente@gmail.com)
    @Column(nullable = false, length = 255)
    private String destino;

    // SQL: payload_json JSONB (en PostgreSQL), TEXT (en H2)
    // Guardamos el JSON como String para simplificar el mapeo en Java
    @Column(name = "payload_json", columnDefinition = "JSONB")
    private String payloadJson;

    // --- ESTADO Y CONTROL ---

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    // SQL: intentos INT NOT NULL DEFAULT 0
    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    // SQL: fecha_envio TIMESTAMPTZ
    // Puede ser nulo si aún no se ha enviado
    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    // --- MÉTODOS DEL CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.estado == null) this.estado = EstadoNotificacion.PENDIENTE;
        if (this.intentos == null) this.intentos = 0;
    }

    // --- MÉTODOS DE NEGOCIO ---

    public void marcarComoEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.fechaEnvio = Instant.now();
    }

    public void registrarFallo() {
        this.estado = EstadoNotificacion.ERROR;
        this.intentos++;
        this.fechaEnvio = Instant.now(); // Registramos cuándo falló
    }
}