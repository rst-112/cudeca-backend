package com.cudeca.model.evento;

import com.cudeca.enums.TipoAjusteRegla;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "REGLAS_PRECIOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoAjusteRegla tipoAjuste;
    private BigDecimal valor;
    @Column(name = "requiere_suscripcion")
    private boolean requiereSuscripcion;

    @ManyToOne
    @JoinColumn(name = "evento_id") // Añadido por si acaso faltaba
    @ToString.Exclude
    private Evento evento;

    //crear metodos

    public BigDecimal aplicarDescuento(BigDecimal precioBase) {
        if (tipoAjuste == TipoAjusteRegla.PORCENTAJE) {
            BigDecimal descuento = precioBase.multiply(valor).divide(new BigDecimal(100));
            return precioBase.subtract(descuento);
        } else if (tipoAjuste == TipoAjusteRegla.FIJO) {
            return precioBase.subtract(valor);
        } else {
            return precioBase; // Sin ajuste
        }
    }

}
