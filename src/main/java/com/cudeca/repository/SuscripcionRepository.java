package com.cudeca.repository;

import com.cudeca.model.negocio.Suscripcion;
import com.cudeca.model.enums.EstadoSuscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para gestionar suscripciones de usuarios.
 */
@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    /**
     * Encuentra suscripciones activas de un comprador.
     */
    List<Suscripcion> findByComprador_IdAndEstado(Long compradorId, EstadoSuscripcion estado);

    /**
     * Encuentra suscripciones próximas a vencer.
     */
    List<Suscripcion> findByFechaFinBetweenAndEstado(Instant fechaInicio, Instant fechaFin, EstadoSuscripcion estado);

    /**
     * Cuenta suscripciones activas de un comprador.
     */
    long countByComprador_IdAndEstado(Long compradorId, EstadoSuscripcion estado);

    /**
     * Encuentra suscripciones con renovación automática.
     */
    Page<Suscripcion> findByRenovacionAutomaticaTrue(Pageable pageable);
}

