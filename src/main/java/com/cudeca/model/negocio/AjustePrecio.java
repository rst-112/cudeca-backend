package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "AJUSTES_PRECIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjustePrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = true)
    @ToString.Exclude
    private ArticuloCompra articuloCompra;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal base = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String motivo;
}
