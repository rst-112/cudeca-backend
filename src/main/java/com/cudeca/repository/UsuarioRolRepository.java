package com.cudeca.repository;

import com.cudeca.model.usuario.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar asignación de roles a usuarios.
 */
@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    /**
     * Encuentra todos los roles asignados a un usuario.
     */
    List<UsuarioRol> findByUsuario_Id(Long usuarioId);

    /**
     * Cuenta cuántos usuarios tienen un rol específico.
     */
    long countByRol_Id(Long rolId);
}

