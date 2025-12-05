package com.cudeca.repository;


import com.cudeca.model.enums.EstadoEvento;
import com.cudeca.model.evento.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar eventos.
 * Proporciona búsquedas por estado, fechas y otros criterios.
 */
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Encuentra todos los eventos por su estado.
     */
    List<Evento> findByEstado(EstadoEvento estado);

    /**
     * Encuentra eventos publicados paginados.
     */
    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);

    /**
     * Encuentra eventos por nombre (búsqueda parcial).
     */
    List<Evento> findByNombreContainingIgnoreCase(String nombre);
}
