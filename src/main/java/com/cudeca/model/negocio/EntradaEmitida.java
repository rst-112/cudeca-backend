package com.cudeca.model.negocio;

import com.cudeca.model.enums.EstadoEntrada;
import com.cudeca.model.negocio.ArticuloEntrada;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntradaEmitida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigoQR;
    private EstadoEntrada estado;

    @ManyToOne
    private ArticuloEntrada articuloEntrada;

    @ManyToMany(mappedBy = "entradasEmitidas")
    private List<ValidacionEntrada> validaciones;


}
