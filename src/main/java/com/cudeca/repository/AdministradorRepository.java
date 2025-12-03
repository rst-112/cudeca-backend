package com.cudeca.repository;

import com.cudeca.model.usuario.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar administradores.
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    /**
     * Encuentra un administrador por su email.
     */
    Optional<Administrador> findByEmail(String email);

    /**
     * Verifica si existe un administrador con el email especificado.
     */
    boolean existsByEmail(String email);
}

