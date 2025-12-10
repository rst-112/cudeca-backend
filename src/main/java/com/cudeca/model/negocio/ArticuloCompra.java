package com.cudeca.model.negocio;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ITEMS_COMPRA")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_item", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class ArticuloCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    @OneToMany(mappedBy = "articuloCompra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<AjustePrecio> ajustes = new ArrayList<>();

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "solicita_certificado", nullable = false)
    @Builder.Default
    private boolean solicitaCertificado = false;

    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }

    public void addAjuste(AjustePrecio ajuste) {
        this.ajustes.add(ajuste);
        ajuste.setArticuloCompra(this);
    }

    /**
     * Método abstracto para validar el artículo específico.
     * Cada tipo de artículo puede tener sus propias reglas de validación.
     *
     * @return true si el artículo es válido, false en caso contrario
     */
    public abstract boolean validar();
}
