package com.cudeca.model.evento;

import com.cudeca.model.enums.EstadoAsiento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entidad que representa un asiento o localidad física en un recinto de evento.
 * Puede estar libre, bloqueado o vendido.
 */
@Entity
@Table(name = "ASIENTOS", uniqueConstraints = {
        @UniqueConstraint(name = "ux_zona_codigo", columnNames = {"zona_id", "codigo_etiqueta"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_etiqueta", nullable = false, length = 20)
    @NotNull(message = "El código de etiqueta es obligatorio")
    @Size(min = 1, max = 20)
    private String codigoEtiqueta;

    @Column
    private Integer fila;

    @Column
    private Integer columna;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoAsiento estado = EstadoAsiento.LIBRE;

    // Aquí guardaremos: {"x": 100, "y": 200, "forma": "circulo"}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_visual", columnDefinition = "jsonb")
    private String metadataVisual;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    @NotNull(message = "La zona es obligatoria")
    @ToString.Exclude
    private ZonaRecinto zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id", nullable = false)
    @NotNull(message = "El tipo de entrada es obligatorio")
    @ToString.Exclude
    private TipoEntrada tipoEntrada;

    // --- MÉTODOS DE NEGOCIO ---

    /**
     * Bloquea el asiento cambiando su estado a BLOQUEADO.
     */
    public void bloquear() {
        this.estado = EstadoAsiento.BLOQUEADO;
    }

    /**
     * Libera el asiento cambiando su estado a LIBRE.
     */
    public void liberar() {
        this.estado = EstadoAsiento.LIBRE;
    }

    /**
     * Marca el asiento como vendido.
     */
    public void vender() {
        this.estado = EstadoAsiento.VENDIDO;
    }

    /**
     * Verifica si el asiento está disponible (LIBRE).
     */
    public boolean estaDisponible() {
        return this.estado == EstadoAsiento.LIBRE;
    }
}
