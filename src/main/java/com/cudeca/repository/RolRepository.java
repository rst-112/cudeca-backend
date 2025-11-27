package com.cudeca.repository;

import com.cudeca.model.usuario.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de Roles en la base de datos.
 */
@Repository // (1)
public interface RolRepository extends JpaRepository<Rol, Long> { // (2)

    /**
     * Busca un rol por su nombre único.
     * Es vital para asignar roles sin saber su ID (que puede cambiar entre entornos).
     * Ejemplo: findByNombre("ROLE_ADMIN")
     *
     * @param nombre El nombre del rol a buscar.
     * @return Un Optional con el rol si existe.
     */
    Optional<Rol> findByNombre(String nombre); // (3)
}