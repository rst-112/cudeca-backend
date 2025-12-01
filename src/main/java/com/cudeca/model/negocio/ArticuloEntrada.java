package com.cudeca.model.negocio;

import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.TipoEntrada;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArticuloEntrada extends ArticuloCompra{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_entrada_id")
    @ToString.Exclude
    private TipoEntrada tipoEntrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asiento_id")
    @ToString.Exclude
    private Asiento asiento;

    // --- CONEXIÓN CON ENTRADAS EMITIDAS (QRs) ---
    @OneToMany(mappedBy = "articuloEntrada", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<EntradaEmitida> entradasEmitidas = new ArrayList<>();



}
