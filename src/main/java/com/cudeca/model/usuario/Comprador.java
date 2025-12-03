package com.cudeca.model.usuario;

import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.SolicitudRetiro;
import com.cudeca.model.enums.EstadoRetiro; // Necesario para el método helper
// import com.cudeca.model.negocio.Suscripcion; // <-- B3: Descomentar cuando exista Suscripción

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("COMPRADOR")
// ¡OJO! AQUÍ NO DEBE HABER @Table(name="...")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Comprador extends Usuario {

    // --- RELACIÓN CORREGIDA ---
    // mappedBy dice: "El dueño es el campo 'comprador' de la clase Monedero".
    // NO pongas @JoinColumn aquí.
    @OneToOne(mappedBy = "comprador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Monedero monedero;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<SolicitudRetiro> solicitudesRetiro = new HashSet<>();
    /* * PENDIENTE: Relación con Suscripción
     * @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
     * private Suscripcion suscripcion;
     */

    // --- MÉTODOS DEL DIAGRAMA (Implementación de Dominio) ---

    /**
     * Diagrama: verHistorialCompras(): void
     * NOTA: En arquitectura Spring, las entidades NO hacen consultas a la BD.
     * Este método se deja vacío o se mueve al Service (CompradorService).
     * La entidad solo guarda datos, no "ve" cosas.
     */
    public void verHistorialCompras() {
        // Lógica a implementar en la capa de Servicio usando CompraRepository
    }

    /**
     * Diagrama: solicitarRetiro(importe: decimal, iban: String): void
     * Este método SÍ puede tener lógica interna para crear el objeto y añadirlo a la lista,
     * actuando como un "Factory method" interno.
     */
    public void solicitarRetiro(BigDecimal importe, String iban) {
        if (monedero == null || monedero.getSaldo().compareTo(importe) < 0) {
            throw new IllegalStateException("Saldo insuficiente o monedero no activo");
        }

        // Creamos la solicitud
        SolicitudRetiro solicitud = new SolicitudRetiro();
        solicitud.setImporte(importe);
        solicitud.setIbanDestino(iban);
        solicitud.setUsuario(this); // Vinculamos con 'this' (el comprador actual)
        solicitud.setEstado(EstadoRetiro.PENDIENTE);

        // Añadimos a la colección (CascadeType.ALL lo guardará en BD al guardar el usuario)
        this.solicitudesRetiro.add(solicitud);
    }
}