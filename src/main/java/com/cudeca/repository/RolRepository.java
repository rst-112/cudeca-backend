package com.cudeca.repository;

import com.cudeca.model.usuario.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar roles del sistema.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Encuentra un rol por su nombre.
     */
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}
