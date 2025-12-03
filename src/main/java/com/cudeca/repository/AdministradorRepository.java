package com.cudeca.repository;

import com.cudeca.model.usuario.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar la entidad Administrador.
 * Extiende de JpaRepository para obtener operaciones CRUD básicas automáticamente.
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

}
