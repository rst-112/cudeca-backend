package com.cudeca.model.usuario;

import com.cudeca.model.negocio.Monedero;
import com.cudeca.model.negocio.SolicitudRetiro;
import com.cudeca.enums.EstadoRetiro; // Necesario para el método helper
// import com.cudeca.model.negocio.Suscripcion; // <-- B3: Descomentar cuando exista Suscripción

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // Necesario para heredar los campos de Usuario (id, email...) en el builder
@EqualsAndHashCode(callSuper = true) // Incluye los campos del padre en la comparación
@DiscriminatorValue("COMPRADOR") // Valor que se guardará en la columna 'tipo_usuario'
public class Comprador extends Usuario {

    // --- RELACIONES ESPECÍFICAS DEL COMPRADOR ---

    // Diagrama: monedero: Monedero (Relación 1 a 1)
    // "mappedBy" significa que la clave foránea (usuario_id) está en la tabla MONEDEROS
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Monedero monedero;

    // Diagrama: posee 0..* SolicitudRetiro
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