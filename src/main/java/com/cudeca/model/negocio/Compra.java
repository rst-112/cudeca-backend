package com.cudeca.model.negocio;

import com.cudeca.model.usuario.Usuario;
import com.cudeca.model.usuario.Invitado;
// CORRECCIÓN: El paquete correcto de los Enums suele ser model.enums
import com.cudeca.model.enums.EstadoCompra;

// CORRECCIÓN: Importamos la nueva clase abstracta, no la antigua ItemCompra
import com.cudeca.model.negocio.ArticuloCompra;
import com.cudeca.model.negocio.Pago;
import com.cudeca.model.negocio.Devolucion;

// Clases futuras (Descomentar cuando existan)
 import com.cudeca.model.negocio.Recibo;
 import com.cudeca.model.negocio.AjustePrecio;
 import com.cudeca.model.negocio.Consentimiento;
 import com.cudeca.model.negocio.Notificacion;
 import com.cudeca.model.negocio.CertificadoFiscal;
 import com.cudeca.model.evento.ReglaPrecio;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMPRAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIONES: QUIÉN COMPRA (Regla XOR) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = true)
    @ToString.Exclude
    private Invitado invitado;

    // --- DATOS BÁSICOS ---

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(name = "email_contacto", length = 150)
    @Email(message = "El email debe ser válido")
    @Size(max = 150)
    private String emailContacto;

    // --- CONEXIONES: QUÉ COMPRA (Artículos) ---

    // CORRECCIÓN: Usamos 'ArticuloCompra' y la lista se llama 'articulos'
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ArticuloCompra> articulos = new ArrayList<>();

    // --- CONEXIONES: DINERO ---

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Devolucion> devoluciones = new ArrayList<>();

    // --- CONEXIONES PENDIENTES ---
    // (Mantengo esto comentado como pediste hasta que tengas las clases)

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default // <--- AÑADIR
    @ToString.Exclude
    private List<AjustePrecio> ajustes = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default // <--- AÑADIR
    @ToString.Exclude
    private List<Consentimiento> consentimientos = new ArrayList<>();

    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private Recibo recibo;

    @OneToOne(mappedBy = "compra")
    private CertificadoFiscal certificadoFiscal;

    @OneToMany(mappedBy = "compra")
    @Builder.Default // <--- AÑADIR
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

    // --- MÉTODOS DE NEGOCIO (CORREGIDOS) ---

    /**
     * CORRECCIÓN: Ahora usa la lista 'articulos' y el método 'calcularSubtotal()'
     * de la clase ArticuloCompra.
     */
    public BigDecimal calcularTotal() {
        if (articulos == null || articulos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // Usamos programación funcional (Streams) para sumar
        return articulos.stream()
                .map(ArticuloCompra::calcularSubtotal) // Delegamos el cálculo al artículo (Mejor práctica)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sumamos todos los subtotales
    }

    /**
     * Aplica una lista de reglas de precio a la compra actual.
     * Genera registros de AjustePrecio y los añade a la lista 'ajustes'.
     * * @param reglas Lista de r   eglas activas para el evento (proporcionadas por el servicio).
     */
    public void aplicarReglasPrecio(List<ReglaPrecio> reglas) {
        if (reglas == null || reglas.isEmpty()) {
            return;
        }

        // Calculamos el subtotal actual de la compra para aplicar porcentajes
        BigDecimal subtotal = this.calcularTotal();

        // Verificamos si el comprador tiene suscripción ACTIVA (para reglas VIP)
        boolean esSocioActivo = false;
        if (this.usuario != null && this.usuario instanceof com.cudeca.model.usuario.Comprador) {
            com.cudeca.model.usuario.Comprador comprador = (com.cudeca.model.usuario.Comprador) this.usuario;
            // Accedemos de forma segura a la suscripción (puede ser null)
            if (comprador.getMonedero() != null) { // Asumimos acceso a través del comprador, ajustar según tu modelo exacto de suscripción
                // Nota: Si implementaste la relación Suscripción en Comprador, úsala aquí.
                // Si no, asumimos false por defecto o verificamos lógica de negocio.
                // Ejemplo ideal: esSocioActivo = comprador.getSuscripcion() != null && comprador.getSuscripcion().getEstado() == EstadoSuscripcion.ACTIVA;
            }
        }

        for (ReglaPrecio regla : reglas) {
            // 1. Filtro: Si la regla requiere suscripción y el usuario NO es socio, la saltamos.
            if (regla.getRequiereSuscripcion() && !esSocioActivo) {
                continue;
            }

            // 2. Calcular el descuento usando el método que ya trae la clase ReglaPrecio
            // El método aplicarDescuento devuelve el precio FINAL, así que calculamos la diferencia.
            BigDecimal precioConDescuento = regla.aplicarAjuste(subtotal);
            BigDecimal montoDescontado = subtotal.subtract(precioConDescuento);

            // Si el descuento es 0 o negativo (error), no hacemos nada
            if (montoDescontado.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 3. Crear el objeto AjustePrecio (Auditoría)
            AjustePrecio ajuste = AjustePrecio.builder()
                    .compra(this)
                    .articuloCompra(null) // null porque afecta al total de la compra (regla global)
                    .tipo("REGLA_EVENTO")
                    .motivo("Regla aplicada: " + regla.getNombre())
                    .base(subtotal)
                    .valor(montoDescontado.negate()) // Guardamos en negativo porque es un descuento
                    .build();

            // 4. Añadir a la lista de ajustes de la compra
            if (this.ajustes == null) {
                this.ajustes = new ArrayList<>();
            }
            this.ajustes.add(ajuste);

            // Actualizamos el subtotal para que la siguiente regla aplique sobre el nuevo precio
            // (Descuento acumulativo) o mantenemos el original según política.
            // Aquí asumimos descuento sobre el total original para simplificar,
            // o actualizamos 'subtotal' si queremos descuento en cascada.
            // subtotal = precioConDescuento;
        }
    }

    // --- MÉTODOS HELPER (CORREGIDOS) ---

    /**
     * CORRECCIÓN: Renombrado a addArticulo para coherencia.
     * Usa la lista correcta 'articulos'.
     */
    public void addArticulo(ArticuloCompra articulo) {
        this.articulos.add(articulo);
        articulo.setCompra(this); // Vinculación bidireccional
    }

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setCompra(this);
    }
}