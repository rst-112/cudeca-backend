package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.enums.EstadoCompra;

// Importamos las clases que ya hemos hecho en pasos anteriores
import com.cudeca.model.negocio.ItemCompra;
import com.cudeca.model.negocio.Pago;
import com.cudeca.model.negocio.Devolucion;

// Clases futuras (Descomentar cuando se creen)
import com.cudeca.model.negocio.Recibo;
import com.cudeca.model.negocio.AjustePrecio;
import com.cudeca.model.negocio.Consentimiento;
import com.cudeca.model.negocio.Notificacion;
import com.cudeca.model.negocio.CertificadoFiscal;
import com.cudeca.model.evento.ReglaPrecio;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMPRAS") // [cite: 2668]
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIONES: QUIÉN COMPRA (Regla XOR) ---
    // [cite: 2685] Regla de negocio: O es Usuario O es Invitado, no ambos.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = true)
    @ToString.Exclude
    private Invitado invitado;

    // --- DATOS BÁSICOS ---

    @Column(nullable = false)
    @Builder.Default
    private Instant fecha = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    // --- CONEXIONES: QUÉ COMPRA (Items) ---

    // Relación con Articulos (Antes Items)
    // mappedBy = "compra" coincide con el campo 'private Compra compra' en ArticuloCompra
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ArticuloCompra> articulos = new ArrayList<>();
    // --- CONEXIONES: DINERO (Pagos y Devoluciones) ---

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Devolucion> devoluciones = new ArrayList<>();

    // --- CONEXIONES PENDIENTES (Futuras Clases) ---

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<AjustePrecio> ajustes = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Consentimiento> consentimientos = new ArrayList<>();

    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private Recibo recibo;

    @OneToOne(mappedBy = "compra")
    private CertificadoFiscal certificadoFiscal;

    @OneToMany(mappedBy = "compra")
    private List<Notificacion> notificaciones = new ArrayList<>();


    // --- MÉTODOS DEL CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) this.fecha = Instant.now();
        if (this.estado == null) this.estado = EstadoCompra.PENDIENTE;

        // Validación de Integridad (Regla XOR)
        if (this.usuario == null && this.invitado == null) {
            throw new IllegalStateException("Una compra debe pertenecer a un Usuario o a un Invitado.");
        }
        if (this.usuario != null && this.invitado != null) {
            throw new IllegalStateException("Una compra no puede ser de Usuario e Invitado a la vez.");
        }
    }

    // --- MÉTODOS DE NEGOCIO (Domain Logic) ---

    /**
     * Diagrama: calcularTotal(): decimal
     * Recorre los items, suma (precio * cantidad) y devuelve el total.
     * Es vital usar BigDecimal para evitar errores de céntimos.
     */
    public BigDecimal calcularTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> {
                    BigDecimal precio = item.getPrecioUnitario();
                    BigDecimal cantidad = new BigDecimal(item.getCantidad());
                    return precio.multiply(cantidad);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Diagrama: aplicarReglasPrecio(reglas: List<ReglaPrecio>): void
     * Aplica descuentos dinámicos.
     * (El tipo List<?> es temporal hasta que B2 cree la clase ReglaPrecio).
     */
    public void aplicarReglasPrecio(List<?> reglas) {
        // TODO: Implementar lógica compleja de descuentos.
        // 1. Calcular base
        // 2. Iterar reglas
        // 3. Crear objetos AjustePrecio y añadirlos a la lista 'ajustes'
        System.out.println("Aplicando reglas... (Pendiente de implementación)");
    }

    // --- MÉTODOS HELPER (Para gestión limpia de colecciones) ---

    public void addItem(ArticuloCompra item) {
        this.items.add(item);
        item.setCompra(this);
    }

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setCompra(this);
    }
}