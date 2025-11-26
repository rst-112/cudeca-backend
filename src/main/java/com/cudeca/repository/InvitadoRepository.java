package com.cudeca.repository;

import com.cudeca.model.usuario.Invitado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvitadoRepository extends JpaRepository<Invitado, Long> {
    // Para comprobar si este correo ya ha comprado antes como invitado
    Optional<Invitado> findByEmail(String email);
}