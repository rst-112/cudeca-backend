package com.cudeca.repository;

import com.cudeca.model.evento.ZonaRecinto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar zonas de recintos en eventos.
 */
@Repository
public interface ZonaRecintoRepository extends JpaRepository<ZonaRecinto, Long> {

    /**
     * Encuentra todas las zonas de un evento.
     */
    List<ZonaRecinto> findByEvento_Id(Long eventoId);

    /**
     * Encuentra una zona específica por nombre en un evento.
     */
    List<ZonaRecinto> findByEvento_IdAndNombreContainingIgnoreCase(Long eventoId, String nombre);
}

