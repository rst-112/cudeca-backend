package com.cudeca.repository;

import com.cudeca.model.evento.ReglaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar reglas de precios de eventos.
 */
@Repository
public interface ReglaPrecioRepository extends JpaRepository<ReglaPrecio, Long> {

    /**
     * Encuentra todas las reglas de precio de un evento.
     */
    List<ReglaPrecio> findByEvento_Id(Long eventoId);

    /**
     * Encuentra reglas que requieren suscripción.
     */
    List<ReglaPrecio> findByEvento_IdAndRequiereSuscripcion(Long eventoId, Boolean requiereSuscripcion);
}
