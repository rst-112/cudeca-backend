package com.cudeca.model.evento;

// IMPORTANTE: Necesitamos importar tu clase de negocio
import com.cudeca.model.negocio.ArticuloEntrada;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TIPOS_ENTRADA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Añadido para ser consistente con el resto del proyecto
public class TipoEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private BigDecimal costeBase;
    private BigDecimal donacionImplicita;
    private Integer cantidadTotal;
    private Integer cantidadVendida;
    private Integer limitePorCompra;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY) // Añade el LAZY ya que estás
    @JoinColumn(name = "evento_id")    // <--- ¡ESTO ES LA SOLUCIÓN!
    @ToString.Exclude
    private Evento eventoAsociado;
    // Relación con Asientos (Asumiendo que Asiento tiene el campo 'tipoEntradaAsignada')
    @OneToMany(mappedBy = "tipoEntradaAsignada")
    @Builder.Default // Evita el warning amarillo de Lombok
    @ToString.Exclude
    private List<Asiento> asientosAsignados = new ArrayList<>();

    // --- CORRECCIÓN CRÍTICA AQUÍ ---
    // Antes: List<TipoEntrada> -> MAL (se apunta a sí mismo)
    // Ahora: List<ArticuloEntrada> -> BIEN (apunta a las ventas)

    @OneToMany(mappedBy = "tipoEntrada", cascade = CascadeType.ALL)
    @Builder.Default // Evita el warning amarillo
    @ToString.Exclude
    private List<ArticuloEntrada> articulosEntradas = new ArrayList<>(); // Cambiado nombre a minúscula (camelCase) por convención

    // --- MÉTODOS DE NEGOCIO ---

    public BigDecimal getPrecioTotal() {
        if (costeBase == null) return BigDecimal.ZERO;
        BigDecimal donacion = (donacionImplicita != null) ? donacionImplicita : BigDecimal.ZERO;
        return costeBase.add(donacion);
    }

    public Integer consultarDisponibilidad() {
        int total = (cantidadTotal != null) ? cantidadTotal : 0;
        int vendida = (cantidadVendida != null) ? cantidadVendida : 0;
        return total - vendida;
    }

    // Helper para añadir ventas bidireccionalmente
    public void addArticuloEntrada(ArticuloEntrada articulo) {
        this.articulosEntradas.add(articulo);
        articulo.setTipoEntrada(this);
    }
}