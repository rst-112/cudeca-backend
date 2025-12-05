package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.enums.EstadoSuscripcion;
import com.cudeca.model.evento.ReglaPrecio;
import com.cudeca.model.usuario.Invitado;
import com.cudeca.model.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMPRAS", indexes = {
        @Index(name = "ix_compras_email_contacto", columnList = "email_contacto")
})
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
    private OffsetDateTime fecha;

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

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<AjustePrecio> ajustes = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Consentimiento> consentimientos = new ArrayList<>();

    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private Recibo recibo;

    @OneToOne(mappedBy = "compra")
    private CertificadoFiscal certificadoFiscal;

    @OneToMany(mappedBy = "compra")
    @Builder.Default
    private List<Notificacion> notificaciones = new ArrayList<>();

    // --- MÉTODOS DEL CICLO DE VIDA ---

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) this.fecha = OffsetDateTime.now();
        if (this.estado == null) this.estado = EstadoCompra.PENDIENTE;

        if (this.usuario == null && this.invitado == null) {
            throw new IllegalStateException("Una compra debe pertenecer a un Usuario o a un Invitado.");
        }
        if (this.usuario != null && this.invitado != null) {
            throw new IllegalStateException("Una compra no puede ser de Usuario e Invitado a la vez.");
        }
    }

    // --- MÉTODOS DE NEGOCIO ---

    public BigDecimal calcularTotal() {
        if (articulos == null || articulos.isEmpty()) return BigDecimal.ZERO;
        return articulos.stream()
                .map(ArticuloCompra::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Aplica una lista de reglas de precio a la compra actual.
     * Genera registros de AjustePrecio y los añade a la lista 'ajustes'.
     *
     * @param reglas Lista de reglas activas para el evento (proporcionadas por el servicio).
     */
    public void aplicarReglasPrecio(List<ReglaPrecio> reglas) {
        if (reglas == null || reglas.isEmpty()) {
            return;
        }

        BigDecimal subtotal = this.calcularTotal();

        // --- CORRECCIÓN LÓGICA DE SOCIO ---
        // Buscamos si el usuario tiene alguna suscripción ACTIVA
        boolean esSocioActivo = false;

        if (this.usuario != null && this.usuario.getSuscripciones() != null) {
            esSocioActivo = this.usuario.getSuscripciones().stream()
                    .anyMatch(s -> s.getEstado() == EstadoSuscripcion.ACTIVA);
        }
        // ----------------------------------

        for (ReglaPrecio regla : reglas) {
            // Filtro: Si la regla requiere suscripción y el usuario NO es socio, la saltamos.
            if (regla.getRequiereSuscripcion() && !esSocioActivo) {
                continue;
            }

            // Calcular el descuento (usa el método de negocio de ReglaPrecio)
            // ReglaPrecio.aplicarAjuste devuelve el precio FINAL tras el descuento.
            BigDecimal precioConDescuento = regla.aplicarAjuste(subtotal);
            BigDecimal montoDescontado = subtotal.subtract(precioConDescuento);

            // Si el descuento es 0 o negativo (error), no hacemos nada
            if (montoDescontado.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Crear el objeto AjustePrecio (Auditoría)
            AjustePrecio ajuste = AjustePrecio.builder()
                    .compra(this)
                    .articuloCompra(null) // null = afecta a toda la compra
                    .tipo("REGLA_EVENTO")
                    .motivo("Regla aplicada: " + regla.getNombre())
                    .base(subtotal)
                    .valor(montoDescontado.negate()) // Guardamos en negativo
                    .build();

            // Añadir a la lista de ajustes de la compra
            if (this.ajustes == null) {
                this.ajustes = new ArrayList<>();
            }
            this.ajustes.add(ajuste);
        }
    }

    // --- MÉTODOS HELPER ---

    public void addArticulo(ArticuloCompra articulo) {
        this.articulos.add(articulo);
        articulo.setCompra(this);
    }

    public void addPago(Pago pago) {
        this.pagos.add(pago);
        pago.setCompra(this);
    }
}
