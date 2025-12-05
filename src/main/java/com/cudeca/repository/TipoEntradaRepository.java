package com.cudeca.repository;

import com.cudeca.model.evento.TipoEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar tipos de entradas de eventos.
 */
@Repository
public interface TipoEntradaRepository extends JpaRepository<TipoEntrada, Long> {

    /**
     * Encuentra todos los tipos de entrada de un evento.
     */
    List<TipoEntrada> findByEvento_Id(Long eventoId);

    /**
     * Encuentra un tipo de entrada específico de un evento por nombre.
     */
    List<TipoEntrada> findByEvento_IdAndNombreContainingIgnoreCase(Long eventoId, String nombre);
}
