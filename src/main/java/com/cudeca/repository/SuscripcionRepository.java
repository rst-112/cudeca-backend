package com.cudeca.repository;

import com.cudeca.model.enums.EstadoSuscripcion;
import com.cudeca.model.negocio.Suscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List<Suscripcion> findByUsuario_IdAndEstado(Long usuarioId, EstadoSuscripcion estado);

    List<Suscripcion> findByFechaFinBetweenAndEstado(Instant fechaInicio, Instant fechaFin, EstadoSuscripcion estado);

    long countByUsuario_IdAndEstado(Long usuarioId, EstadoSuscripcion estado);

    Page<Suscripcion> findByRenovacionAutomaticaTrue(Pageable pageable);
}
