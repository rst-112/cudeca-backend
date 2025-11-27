package com.cudeca.repository;

import com.cudeca.model.usuario.Invitado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar la entidad Invitado.
 * Permite la persistencia y recuperación de usuarios no registrados.
 */
@Repository // (1)
public interface InvitadoRepository extends JpaRepository<Invitado, Long> { // (2)

    /**
     * Busca un invitado por su correo electrónico.
     * Fundamental para la lógica de negocio: Antes de crear un nuevo Invitado,
     * verificamos si este email ya ha comprado antes para reutilizar el ID.
     *
     * @param email El correo del invitado.
     * @return Un Optional con el invitado si ya existe en BD.
     */
    Optional<Invitado> findByEmail(String email); // (3)
}