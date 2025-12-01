package com.cudeca.model.negocio;

import com.cudeca.model.negocio.AjustePrecio; // <-- DESCOMENTAR CUANDO TENGAS LA CLASE AJUSTEPRECIO

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ITEMS_COMPRA") // Mapea a la tabla única del SQL
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Estrategia de herencia: Todo en una tabla
@DiscriminatorColumn(name = "tipo_item", discriminatorType = DiscriminatorType.STRING) // La columna que distingue hijos
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // ¡Vital para que los hijos hereden el constructor!
public abstract class ArticuloCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN CON COMPRA (Padre) ---
    // SQL: compra_id BIGINT NOT NULL
    // Diagrama: Compra "1" -- "1..*" ArticuloCompra
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    private Compra compra;

    // --- RELACIÓN CON AJUSTES DE PRECIO ---
    // Diagrama: ArticuloCompra "1" -- "0..*" AjustePrecio
    // Un artículo puede tener descuentos específicos aplicados.
    // (Dejo esto comentado hasta que creemos la clase AjustePrecio)

    @OneToMany(mappedBy = "articuloCompra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<AjustePrecio> ajustes = new ArrayList<>();


    // --- DATOS COMUNES (SQL y Diagrama) ---

    // SQL: INT NOT NULL CHECK (cantidad > 0)
    @Column(nullable = false)
    private Integer cantidad;

    // SQL: NUMERIC(10,2) NOT NULL
    // Diagrama: precioUnitario: decimal
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // SQL: BOOLEAN NOT NULL DEFAULT FALSE
    // Diagrama: solicitaCertificado: boolean
    @Column(name = "solicita_certificado", nullable = false)
    @Builder.Default
    private boolean solicitaCertificado = false;

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Calcula el subtotal de esta línea (Precio x Cantidad).
     */
    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }

    // Método helper para añadir ajustes (cuando exista la clase)

    public void addAjuste(AjustePrecio ajuste) {
        this.ajustes.add(ajuste);
        ajuste.setArticuloCompra(this);
    }

}