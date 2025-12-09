package com.cudeca.repository;

import com.cudeca.model.negocio.ValidacionEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gestionar validaciones de entradas.
 */
@Repository
public interface ValidacionEntradaRepository extends JpaRepository<ValidacionEntrada, Long> {
}

