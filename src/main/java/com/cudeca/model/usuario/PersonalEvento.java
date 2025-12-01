package com.cudeca.model.usuario;

import com.cudeca.model.negocio.ValidacionEntrada;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PERSONAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PersonalEvento extends Usuario {

    // CORRECCIÓN: Solo mappedBy. Sin JoinColumn.
    @OneToMany(mappedBy = "personalValidador", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<ValidacionEntrada> validacionesRealizadas = new ArrayList<>();

}