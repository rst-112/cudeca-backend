package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "BENEFICIOS_SOCIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficioSocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    @ToString.Exclude
    private Suscripcion suscripcion;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descripcion;
}
