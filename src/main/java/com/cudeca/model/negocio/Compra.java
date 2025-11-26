package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoCompra;
import com.cudeca.model.usuario.Usuario;
import com.cudeca.model.usuario.Invitado;

// import com.cudeca.model.evento.ReglaPrecio; // <-- ESTO LO HACE B2, LO DESCOMENTARÁS LUEGO
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "COMPRAS")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    // --- RELACIONES (Requisito: Usuario e Invitado nullable) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true) // Puede ser nulo si compra un invitado
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = true) // Puede ser nulo si compra un usuario registrado
    private Invitado invitado;

    // Relación con Items (fundamental para calcular el total)
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCompra> items = new ArrayList<>();

    public Compra() {}

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoCompra.PENDIENTE;
        }
    }

    // --- MÉTODOS DEL DIAGRAMA ---

    // Diagrama: calcularTotal(): decimal
    public BigDecimal calcularTotal() {
        // Suma el precio de todos los items
        return items.stream()
                .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Diagrama: aplicarReglasPrecio(reglas: List<ReglaPrecio>): void
    // NOTA: Usa Object o comenta el tipo hasta que B2 cree la clase ReglaPrecio
    public void aplicarReglasPrecio(List<?> reglas) {
        // TODO: Implementar lógica cuando B2 termine el módulo de Eventos/Reglas
        // Aquí iría la lógica de la nota del diagrama: (costeBase - descuentos) + donacion...
        System.out.println("Aplicando reglas de precio (Pendiente integración con B2)");
    }

    // --- GETTERS Y SETTERS ---

    // Helper para añadir items bidireccionalmente
    public void addItem(ItemCompra item) {
        items.add(item);
        item.setCompra(this);
    }
}