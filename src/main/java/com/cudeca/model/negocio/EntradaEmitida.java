package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoEntrada;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ENTRADAS_EMITIDAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaEmitida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DATOS ---

    // Corrección QR: Mapeado a 'codigo_qr'
    @Column(name = "codigo_qr", nullable = false, unique = true)
    private String codigoQR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEntrada estado;

    // --- RELACIONES ---

    // CORRECCIÓN DEL ERROR ACTUAL:
    // Antes probamos con 'articulo_id' y 'item_id' y fallaron.
    // La siguiente opción más lógica según el estándar es 'item_compra_id'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_compra_id")
    @ToString.Exclude
    private ArticuloEntrada articuloEntrada;

    // Relación con Validaciones (Historial de escaneos)
    @OneToMany(mappedBy = "entradaEmitida", cascade = CascadeType.ALL)
    @Builder.Default // Evita warnings de Lombok
    @ToString.Exclude
    private List<ValidacionEntrada> validaciones = new ArrayList<>();

    // NOTA: Si vuelve a fallar con "missing column", tendrás que abrir el archivo
    // V1Initial_Schema.sql (o el script SQL que tengas) y buscar "CREATE TABLE ENTRADAS_EMITIDAS".
    // Ahí verás el nombre exacto de la columna FK.
    // Posibles nombres: 'item_compra_id', 'articulo_id', 'articulo_entrada_id'.
}