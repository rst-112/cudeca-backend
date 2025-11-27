package com.cudeca.model.negocio;


// IMPORTANTE: Esta clase la crea B2. Cuando él la tenga, descomenta la siguiente línea:
// import com.cudeca.model.evento.Asiento;
// import com.cudeca.model.evento.TipoEntrada;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "ITEMS_COMPRA")
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la Compra (Padre)
    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    // --- REQUISITO DEL CHECKLIST (Relación con Asiento) ---
    // Mapeado a la entidad Asiento (FK: asiento_id)
    // B2 debe crear la clase 'Asiento' en el paquete 'com.cudeca.model.evento'
    // Descomenta estas líneas cuando B2 termine su parte:

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id")
    private Asiento asiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id")
    private TipoEntrada tipoEntrada;
    */

    // --- CAMPOS DEL DIAGRAMA ---

    // Diagrama: cantidad: int
    @Column(nullable = false)
    private Integer cantidad;

    // Diagrama: precioUnitario: decimal -> BigDecimal en Java
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Diagrama: solicitaCertificado: boolean
    @Column(name = "solicita_certificado", nullable = false)
    private boolean solicitaCertificado;


    public ItemCompra() {}

    // --- GETTERS Y SETTERS ---

    /* Descomentar cuando B2 termine Asiento
    public Asiento getAsiento() { return asiento; }
    public void setAsiento(Asiento asiento) { this.asiento = asiento; }

    public TipoEntrada getTipoEntrada() { return tipoEntrada; }
    public void setTipoEntrada(TipoEntrada tipoEntrada) { this.tipoEntrada = tipoEntrada; }
    */

}