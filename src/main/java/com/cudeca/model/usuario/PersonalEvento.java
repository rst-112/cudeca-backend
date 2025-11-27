package com.cudeca.model.usuario;

import jakarta.persistence.OneToMany;
import com.cudeca.model.negocio.ValidacionEntrada;

import java.util.List;

public class PersonalEvento extends Usuario {

    //relaciones
    @OneToMany(mappedBy = "personalValidador")
    private List<ValidacionEntrada> validacionesReal;

}
