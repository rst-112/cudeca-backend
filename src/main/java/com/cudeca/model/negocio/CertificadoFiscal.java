package com.cudeca.model.negocio;

import com.cudeca.model.usuario.DatosFiscales;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "CERTIFICADOS_FISCALES", indexes = {
        @Index(name = "ix_certs_datos", columnList = "datos_fiscales_id")
})
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
    private OffsetDateTime fechaEmision;

    @Column(name = "importe_donado", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeDonado;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 80)
    private String numeroSerie;

    @Column(name = "hash_documento", length = 120)
    private String hashDocumento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_snapshot_json", columnDefinition = "TEXT")
    private String datosSnapshotJson;

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) this.fechaEmision = OffsetDateTime.from(Instant.now());
    }

    /**
     * Obtiene los artículos de donación asociados a esta certificación.
     * Filtra de la compra solo los artículos que son donaciones.
     */
    public List<ArticuloDonacion> getArticulosJustificantes() {
        if (this.compra == null || this.compra.getArticulos() == null) {
            return Collections.emptyList();
        }
        return this.compra.getArticulos().stream()
                .filter(art -> art instanceof ArticuloDonacion)
                .map(art -> (ArticuloDonacion) art)
                .collect(Collectors.toList());
    }
}
