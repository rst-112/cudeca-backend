package com.cudeca.model.negocio;

import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.TipoEntrada;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticuloEntrada {
    @ManyToMany
    private List<Asiento> asientos;
    @ManyToOne
    private TipoEntrada tipoEntrada;
    @OneToMany(mappedBy = "articuloEntrada")
    private List<EntradaEmitida> entradasEmitidas;



}
