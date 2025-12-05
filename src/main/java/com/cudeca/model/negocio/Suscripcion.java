package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoSuscripcion;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SUSCRIPCIONES", indexes = {
        @Index(name = "ix_susc_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<BeneficioSocio> beneficios = new ArrayList<>();

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoSuscripcion estado = EstadoSuscripcion.PENDIENTE;

    @Column(name = "renovacion_automatica", nullable = false)
    @Builder.Default
    private boolean renovacionAutomatica = false;

    @PrePersist
    protected void onCreate() {
        if (this.fechaInicio == null) {
            this.fechaInicio = OffsetDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoSuscripcion.PENDIENTE;
        }
    }

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setSuscripcion(this);
    }

    public void addBeneficio(BeneficioSocio beneficio) {
        this.beneficios.add(beneficio);
        beneficio.setSuscripcion(this);
    }
}
