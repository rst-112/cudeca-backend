package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "AJUSTES_PRECIO") // Nombre exacto del SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustePrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN OBLIGATORIA: COMPRA ---
    // SQL: compra_id BIGINT NOT NULL
    // Todo ajuste pertenece a un ticket/compra, aunque afecte a un item concreto.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- RELACIÓN OPCIONAL: ARTÍCULO ---
    // SQL: item_id BIGINT REFERENCES ITEMS_COMPRA (nullable)
    // Java: Usamos la clase 'ArticuloCompra' que creamos antes.
    // Si es NULL, el ajuste afecta al total de la compra.
    // Si tiene valor, afecta solo a esa línea.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
    @ToString.Exclude
    private ArticuloCompra articuloCompra;

    // --- DATOS ECONÓMICOS ---

    // SQL: tipo VARCHAR(60) NOT NULL
    // Ej: "DESCUENTO_SOCIO", "CUPON_NAVIDAD", "GASTOS_GESTION"
    @Column(nullable = false, length = 60)
    private String tipo;

    // SQL: base NUMERIC(12,2) NOT NULL DEFAULT 0
    // El precio sobre el que se calculó el ajuste.
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal base = BigDecimal.ZERO;

    // SQL: valor NUMERIC(12,2) NOT NULL
    // La cantidad que se suma o resta. (Ej: -5.00 o +1.50)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    // SQL: motivo VARCHAR(255)
    // Ej: "Campaña de verano 2025"
    @Column(length = 255)
    private String motivo;
}