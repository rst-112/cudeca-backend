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

    @Column(name = "codigo_qr", nullable = false, unique = true)
    private String codigoQR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEntrada estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_compra_id")
    @ToString.Exclude
    private ArticuloEntrada articuloEntrada;

    @OneToMany(mappedBy = "entradaEmitida", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<ValidacionEntrada> validaciones = new ArrayList<>();
}
