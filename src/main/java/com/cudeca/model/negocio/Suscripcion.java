package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Comprador;
import com.cudeca.enums.EstadoSuscripcion;
// import com.cudeca.model.negocio.BeneficioSocio; // <-- DESCOMENTAR CUANDO TENGAS LA CLASE BENEFICIO

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SUSCRIPCIONES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON EL COMPRADOR (Cliente) ---
    // SQL: usuario_id BIGINT NOT NULL REFERENCES USUARIOS
    // Diagrama: Comprador "1" -- "0..1" Suscripcion
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Comprador comprador;

    // --- RELACIÓN CON PAGOS (Cuotas) ---
    // Diagrama: Suscripcion "1" -- "0..*" Pago (cobra >)
    // Nota: Debes ir a la clase Pago y descomentar el campo 'private Suscripcion suscripcion;'
    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Pago> pagos = new ArrayList<>();


    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<BeneficioSocio> beneficios = new ArrayList<>();


    // --- DATOS DE LA SUSCRIPCIÓN ---

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoSuscripcion estado = EstadoSuscripcion.PENDIENTE;

    @Column(name = "renovacion_automatica", nullable = false)
    @Builder.Default
    private boolean renovacionAutomatica = false;

    // --- CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fechaInicio == null) {
            this.fechaInicio = Instant.now();
        }
        if (this.estado == null) {
            this.estado = EstadoSuscripcion.PENDIENTE;
        }
    }

    // --- MÉTODOS HELPER (Para gestión limpia) ---

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setSuscripcion(this); // Asegúrate de añadir este setter en Pago
    }

     public void addBeneficio(BeneficioSocio beneficio) {
        this.beneficios.add(beneficio);
        beneficio.setSuscripcion(this);
    }

}