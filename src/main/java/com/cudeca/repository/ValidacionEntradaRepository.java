package com.cudeca.repository;

import com.cudeca.model.negocio.ValidacionEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio para gestionar validaciones de entradas (registros de escaneo).
 */
@Repository
public interface ValidacionEntradaRepository extends JpaRepository<ValidacionEntrada, Long> {

    /**
     * Encuentra todas las validaciones de una entrada emitida.
     */
    List<ValidacionEntrada> findByEntradaEmitida_Id(Long entradaEmitidaId);

    /**
     * Encuentra validaciones activas (no revertidas) de una entrada.
     */
    List<ValidacionEntrada> findByEntradaEmitida_IdAndRevertidaFalse(Long entradaEmitidaId);

    /**
     * Encuentra validaciones por personal validador y rango de fechas.
     */
    List<ValidacionEntrada> findByPersonalValidador_IdAndFechaHoraBetween(Long personalValidadorId, Instant inicio, Instant fin);
}

