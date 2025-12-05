package com.cudeca.repository;

import com.cudeca.model.usuario.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar permisos del sistema.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /**
     * Encuentra un permiso por su código.
     */
    Optional<Permiso> findByCodigoIgnoreCase(String codigo);
}
