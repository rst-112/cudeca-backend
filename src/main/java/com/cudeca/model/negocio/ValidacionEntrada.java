package com.cudeca.model.negocio;

import com.cudeca.model.usuario.PersonalEvento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionEntrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate fechaHora;
    private String dispositivoId;
    private boolean revertida;

    // Relaciones
    @ManyToMany
    private List<EntradaEmitida> entradasEmitidas;

    @ManyToOne
    private PersonalEvento personalValidador;
}
