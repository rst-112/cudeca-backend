package com.cudeca.repository;

import com.cudeca.model.enums.EstadoAsiento;
import com.cudeca.model.evento.Asiento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar asientos de una zona de recinto.
 */
@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    /**
     * Encuentra todos los asientos de una zona.
     */
    List<Asiento> findByZona_Id(Long zonaId);

    /**
     * Encuentra un asiento específico por su código de etiqueta en una zona.
     */
    Optional<Asiento> findByZona_IdAndCodigoEtiqueta(Long zonaId, String codigoEtiqueta);

    /**
     * Encuentra asientos libres en una zona.
     */
    List<Asiento> findByZona_IdAndEstado(Long zonaId, EstadoAsiento estado);

    /**
     * Cuenta asientos libres en una zona.
     */
    long countByZona_IdAndEstado(Long zonaId, EstadoAsiento estado);

    /**
     * Encuentra y bloquea asientos por sus IDs para evitar condiciones de carrera.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Asiento a where a.id in :ids")
    List<Asiento> findAllByIdWithLock(@Param("ids") List<Long> ids);
}
