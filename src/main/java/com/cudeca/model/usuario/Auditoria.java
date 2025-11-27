package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer actorId;
    private String entidad;
    private String entidadId;
    private String accion;
    private LocalDate fecha;
    private String detalles;

    // Relaciones
    @ManyToOne
    private Usuario usuario;
}
