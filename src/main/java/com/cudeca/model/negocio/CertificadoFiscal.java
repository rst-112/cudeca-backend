package com.cudeca.model.negocio;

import com.cudeca.model.usuario.DatosFiscales;
// import com.cudeca.model.negocio.ArticuloDonacion; // <-- Descomentar si la tienes creada

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Entity
@Table(name = "CERTIFICADOS_FISCALES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIONES ESTRUCTURALES ---

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compra_id", unique = true, nullable = false)
    @ToString.Exclude
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "datos_fiscales_id", nullable = false)
    @ToString.Exclude
    private DatosFiscales datosFiscales;

    // --- DATOS LEGALES ---

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private Instant fechaEmision;

    @Column(name = "importe_donado", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeDonado;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 80)
    private String numeroSerie;

    @Column(name = "hash_documento", length = 120)
    private String hashDocumento;

    @Column(name = "datos_snapshot_json", columnDefinition = "JSONB")
    private String datosSnapshotJson;

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) this.fechaEmision = Instant.now();
    }

    // --- RELACIÓN CON ARTÍCULOS DE DONACIÓN (Derivada) ---

    /**
     * Diagrama: Relación "justifica" con ArticuloDonacion.
     * Como no hay FK directa en SQL, la obtenemos a través de la Compra.
     * Devuelve solo los artículos que son Donaciones.
     */
    /* DESCOMENTAR ESTE BLOQUE CUANDO TENGAS 'ArticuloDonacion' FUNCIONANDO
     */
    public List<ArticuloDonacion> getArticulosJustificantes() {

        if (this.compra == null || this.compra.getArticulos() == null) {
            return Collections.emptyList();
        }
        // Filtramos de la compra solo aquello que sea Donación
        return this.compra.getArticulos().stream()
                .filter(art -> art instanceof ArticuloDonacion)
                .map(art -> (ArticuloDonacion) art)
                .collect(Collectors.toList());
    }

}