package com.cudeca.model.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "INVITADOS")
public class Invitado {

    // Getters y Setters básicos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    public Invitado() {}

}
//No se si quitar INVITADOS