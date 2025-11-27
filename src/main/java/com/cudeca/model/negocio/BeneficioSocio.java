package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "BENEFICIOS_SOCIO") // Nombre exacto del SQL (Línea 3904 de la guía)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficioSocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON LA SUSCRIPCIÓN (FK) ---
    // SQL: suscripcion_id BIGINT NOT NULL REFERENCES SUSCRIPCIONES
    // Diagrama: Suscripcion "1" -- "0..*" BeneficioSocio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    @ToString.Exclude // Evita bucles infinitos al imprimir
    private Suscripcion suscripcion;

    // --- ATRIBUTOS DE NEGOCIO ---

    // SQL: tipo VARCHAR(60) NOT NULL
    @Column(nullable = false, length = 60)
    private String tipo; // Ej: "DESCUENTO_TIENDA", "PRIORIDAD_EVENTOS"

    // SQL: valor NUMERIC(12,2) NOT NULL
    // Java: BigDecimal es obligatorio para valores numéricos precisos
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor; // Ej: 10.00 (para un 10%)

    // SQL: descripcion VARCHAR(255)
    @Column(length = 255)
    private String descripcion; // Ej: "Descuento aplicable en todas las compras"
}